package com.example.hiddenseek.navigation_button.map;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.hiddenseek.MainActivity;
import com.example.hiddenseek.R;
import com.example.hiddenseek.data.DailyMission;
import com.example.hiddenseek.navigation_button.map.LangPressEvent.LangPressFragment;
import com.example.hiddenseek.navigation_button.map.MarkerClickEvent.MarkerClickFragment;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static androidx.core.content.ContextCompat.getSystemService;
import static com.example.hiddenseek.data.FileController.createFileDir;
import static com.example.hiddenseek.data.FileController.readFile;
import static com.example.hiddenseek.data.FileController.writeFile;
import static com.example.hiddenseek.data.FirebaseListener.getUserInfo;
import static com.example.hiddenseek.data.Helper.addItemInDatabase;
import static com.example.hiddenseek.data.Helper.meterToDegree;
import static com.example.hiddenseek.data.Helper.rewriteSunOrBoxInFile;
import static com.example.hiddenseek.data.TimeController.readLastLogTime;
import static com.example.hiddenseek.data.TimeController.writeCurrentTime;
import static com.example.hiddenseek.navigation_button.map.BoxSet.initBoxSet;
import static com.example.hiddenseek.navigation_button.map.SunShine.initSunShineSet;

public class MapFragment extends Fragment implements LangPressFragment.NoticeDialogListener {

    private String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private List<GeoPoint> sunShineList;
    private List<Pair<GeoPoint, Integer>> boxList;
    private MapView map = null;
    private GeoLocation myCurrentLocation;
    private Map<String, Marker> markerMap = new HashMap<>();

    // used for bomb
    private GeoQuery geoQuery = null;
    private GeoFire geoFire;

    private DatabaseReference ref;
    private Marker trapMarker;
    private Marker special_seed_marker;
    private OverlayManager overlayManager;
    private IMapController mapController;
    private View view = null;
    // used for special seed
    private DatabaseReference global_seed_ref;

    // declaration of overlays
    private CompassOverlay mCompassOverlay = null;
    private MyLocationNewOverlay mLocationOverlay = null;
    private Overlay longPressOverlay = null;
    private RotationGestureOverlay mRotationGestureOverlay = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_map, container, false);
        createNotificationChannel();
        return view;
    }

    public void createMap() {
        map = (MapView) view.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        mapController = map.getController();
        overlayManager = map.getOverlayManager();
        boxLocationMarker = new HashMap<>();
        // set current position overlay for map
        GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(getActivity());
        gpsMyLocationProvider.addLocationSource(LocationManager.NETWORK_PROVIDER);
        gpsMyLocationProvider.addLocationSource(LocationManager.GPS_PROVIDER);
        mLocationOverlay = new MyLocationNewOverlay(gpsMyLocationProvider, map);
        mLocationOverlay.enableMyLocation();
        mLocationOverlay.enableFollowLocation();
        overlayManager.add(mLocationOverlay);
        // set compass overlay
        mCompassOverlay = new CompassOverlay(getActivity(), new InternalCompassOrientationProvider(getActivity()), map);
        mCompassOverlay.enableCompass();
        overlayManager.add(mCompassOverlay);
        // set rotationgesture overlay
        mRotationGestureOverlay = new RotationGestureOverlay(map);
        mRotationGestureOverlay.setEnabled(true);
        // set multi touch overlay
        map.setMultiTouchControls(true);
        overlayManager.add(mRotationGestureOverlay);

        // longpress handler
        longPressOverlay = new Overlay() {
            @Override
            public boolean onLongPress(MotionEvent e, MapView mapView) {
                DialogFragment langPressFragment = new LangPressFragment();
                langPressFragment.show(getChildFragmentManager(), null);
                return true;
            }
        };
        overlayManager.add(longPressOverlay);

        FloatingActionButton dm = view.findViewById(R.id.dailymission);
        dm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment fragment = new DaliyMissionFragment(mLocationOverlay.getMyLocation());
                fragment.show(getFragmentManager(), "view");
            }
        });

        FloatingActionButton gtml = view.findViewById(R.id.gotomylocation);

        ref = FirebaseDatabase.getInstance().getReference("map");
        geoFire = new GeoFire(ref);
        mLocationOverlay.runOnFirstFix(new Runnable() {
            @Override
            public void run() {
                // use runOnUiThread to solve CalledFromWrongThreadException error！
                getActivity().runOnUiThread(new Runnable() {
                    // click to locate on my current location
                    @Override
                    public void run() {
                        mapController.setCenter(mLocationOverlay.getMyLocation());
                        mapController.setZoom(18.0);
                        gtml.setVisibility(View.VISIBLE);
                        // tm.setVisibility(View.VISIBLE);
                        dm.setVisibility(View.VISIBLE);
                        gtml.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mapController.animateTo(mLocationOverlay.getMyLocation());
                            }
                        });
                    }
                });
                // find all traps around current location with radius 100km
                myCurrentLocation = new GeoLocation(mLocationOverlay.getMyLocation().getLatitude(),
                        mLocationOverlay.getMyLocation().getLongitude());
                File lastLogTime = new File(createFileDir(getContext(), currentUserId, "sunShineAndBox"),
                        "lastLogTime");
                File lastSunshine = new File(createFileDir(getContext(), currentUserId, "sunShineAndBox"), "ssList");
                File lastBox = new File(createFileDir(getContext(), currentUserId, "sunShineAndBox"), "boxList");

                if (!lastLogTime.exists() || !lastSunshine.exists() || !lastBox.exists()) {
                    installMarkersAndTasks();
                } else {
                    if (over24H()) {
                        installMarkersAndTasks();
                    } else {
                        sunShineList = readLastSunShine(getContext(), currentUserId);
                        boxList = readLastBox(getContext(), currentUserId);
                    }
                }
                createSs(sunShineList);
                createBox(boxList);
                // setGlobalListener();
                // rtListener();
                geoQuery = geoFire.queryAtLocation(myCurrentLocation, 100000);

                geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                    Drawable icon = getResources().getDrawable(R.drawable.ic_baseline_not_listed_location_24,
                            getActivity().getTheme());

                    @Override
                    public void onKeyEntered(String key, GeoLocation location) {
                        trapMarker = new Marker(map);
                        GeoPoint markerPostition = new GeoPoint(location.latitude, location.longitude);
                        trapMarker.setTitle("confused treasure box");
                        trapMarker.setPosition(markerPostition);
                        trapMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                        trapMarker.setIcon(icon);
                        trapMarker.setId(key);
                        overlayManager.add(trapMarker);
                        markerMap.put(key, trapMarker);
                        trapMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker, MapView mapView) {
                                DialogFragment fragment = new MarkerClickFragment(marker, mapView, boxLocationMarker,
                                        mLocationOverlay.getMyLocation(), false, getContext());
                                fragment.show(getChildFragmentManager(), null);
                                return false;
                            }
                        });
                    }

                    @Override
                    public void onKeyExited(String key) {
                        markerMap.get(key).remove(map);
                    }

                    @Override
                    public void onKeyMoved(String key, GeoLocation location) {
                        Log.e("onKeyMoved", String.format("Key %s moved within the search area to [%f,%f]", key,
                                location.latitude, location.longitude));
                    }

                    @Override
                    public void onGeoQueryReady() {
                        Log.e("onGeoQueryReady", "All initial data has been loaded and events have been fired!");
                    }

                    @Override
                    public void onGeoQueryError(DatabaseError error) {
                        Log.e("onGeoQueryError", "There was an error with this query: " + error);
                    }
                });
                SharedPreferences sharedPref = getContext().getSharedPreferences("notification", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                global_seed_ref = FirebaseDatabase.getInstance().getReference("global_seed");

                Drawable special_seed_icon = getResources().getDrawable(R.drawable.ic_baseline_catching_pokemon_24,
                        getActivity().getTheme());
                ChildEventListener childEventListener = new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot,
                            @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                        special_seed_marker = new Marker(map);
                        String id = snapshot.getKey();
                        String location = (String) snapshot.child("property/location").getValue();
                        String[] l = location.split("-");
                        GeoPoint marker_position = new GeoPoint(Double.parseDouble(l[0]), Double.parseDouble(l[1]));
                        special_seed_marker.setPosition(marker_position);
                        special_seed_marker.setId(id);
                        special_seed_marker.setIcon(special_seed_icon);
                        special_seed_marker.setTitle("Please approach me first");
                        markerMap.put(id, special_seed_marker);
                        overlayManager.add(special_seed_marker);
                        double distance = marker_position.distanceToAsDouble(mLocationOverlay.getMyLocation());
                        // if special seed is less than 5000m far away from current location, and if
                        // user did not get Notification for current special seed, then
                        // notify user
                        if (distance <= 5000d) {
                            special_seed_marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                                @Override
                                public boolean onMarkerClick(Marker marker, MapView mapView) {
                                    DialogFragment fragment = new MarkerClickFragment(marker, mapView,
                                            boxLocationMarker, mLocationOverlay.getMyLocation(), true, getContext());
                                    fragment.show(getChildFragmentManager(), null);
                                    return false;
                                }
                            });
                            if (sharedPref.getBoolean(id, false) == false) {
                                Intent intent = new Intent(getContext(), MainActivity.class);
                                // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                // Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                                PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, 0);
                                NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(),
                                        "_message").setSmallIcon(R.drawable.ic_baseline_catching_pokemon_24)
                                                .setContentTitle("Harry Up!")
                                                .setContentText(
                                                        "There is new Special Seed on the map! Find it and catch it!")
                                                .setStyle(new NotificationCompat.BigTextStyle())
                                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                                .setContentIntent(pendingIntent).setAutoCancel(true)
                                                .setDefaults(NotificationCompat.DEFAULT_ALL);
                                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat
                                        .from(getContext());
                                notificationManagerCompat.notify(100, builder.build());
                                editor.putBoolean(id, true);
                                editor.commit();
                            }
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot,
                            @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {
                        overlayManager.remove(markerMap.get(snapshot.getKey()));
                        editor.remove(snapshot.getKey());
                    }

                    @Override
                    public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot,
                            @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                };
                global_seed_ref.addChildEventListener(childEventListener);
            }
        });
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Special Seed Notification";
            String description = "enable it to get notified when special seed appears";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("_message", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(getContext(), NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void installMarkersAndTasks() {
        writeCurrentTime(getContext(), currentUserId);
        sunShineList = initSunShineSet(myCurrentLocation);
        boxList = initBoxSet(myCurrentLocation);
        DailyMission dayMission = new DailyMission();
        dayMission.installDailyMission(getContext(), currentUserId, false);
        ArrayList<String> ssPositions = new ArrayList<>();
        ArrayList<String> boxPositions = new ArrayList<>();
        for (int i = 0; i < sunShineList.size(); i++) {
            String s = gson.toJson(sunShineList.get(i));
            ssPositions.add(s);
        }
        for (int i = 0; i < boxList.size(); i++) {
            String sGP = gson.toJson(boxList.get(i).first);
            String sS = gson.toJson(boxList.get(i).second);
            Pair<String, String> pair = new Pair<>(sGP, sS);
            String p = gson.toJson(pair);
            boxPositions.add(p);
        }
        writeCurrentSunShineOrBox(ssPositions, getContext(), currentUserId, "ssList");
        writeCurrentSunShineOrBox(boxPositions, getContext(), currentUserId, "boxList");
    }

    public List<GeoPoint> readLastSunShine(Context c, String userName) {
        List<GeoPoint> returnList = new ArrayList<>();
        try {
            ArrayList<String> ssList = (ArrayList<String>) readFile(c, userName, "sunShineAndBox", "ssList");
            for (int i = 0; i < ssList.size(); i++) {
                JSONObject jsonObject = new JSONObject(ssList.get(i));
                String la = jsonObject.getString("mLatitude");
                String lo = jsonObject.getString("mLongitude");
                GeoPoint gp = new GeoPoint(Double.parseDouble(la), Double.parseDouble(lo));
                returnList.add(gp);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return returnList;
    }

    Gson gson = new Gson();

    public List<Pair<GeoPoint, Integer>> readLastBox(Context c, String userName) {
        List<Pair<GeoPoint, Integer>> returnList = new ArrayList<>();
        try {
            ArrayList<String> ssList = (ArrayList<String>) readFile(c, userName, "sunShineAndBox", "boxList");
            for (int i = 0; i < ssList.size(); i++) {
                Pair<String, String> pair = gson.fromJson(ssList.get(i), Pair.class);
                JSONObject jsonObject = new JSONObject((String) pair.first);
                String ss = (String) pair.second;
                String la = jsonObject.getString("mLatitude");
                String lo = jsonObject.getString("mLongitude");
                GeoPoint gp = new GeoPoint(Double.parseDouble(la), Double.parseDouble(lo));
                int score = Integer.parseInt(ss);
                returnList.add(new Pair<GeoPoint, Integer>(gp, score));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return returnList;
    }

    @Override
    public void onResume() {
        super.onResume();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Configuration.getInstance().setUserAgentValue("OBP_Tuto/1.0");
        createMap();
        //map.onResume();
    }

    @Override
    public void onPause() {
        map.onPause();
        overlayManager.clear();
        if(geoQuery != null){
            geoQuery.removeAllListeners();
        }
        super.onPause();
    }

    // longpress click handler
    @Override
    public void onDialogPositiveClick(DialogFragment dialogFragment) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
        Map<String, Object> userInfo = getUserInfo();
        long score = (long) userInfo.get("score");
        if (score >= 300) {
            GeoPoint markerLocation = new GeoPoint(myCurrentLocation.latitude, myCurrentLocation.longitude);
            // create an entry in firebase
            DatabaseReference newLocation = ref.push();
            geoFire.setLocation(newLocation.getKey(),
                    new GeoLocation(markerLocation.getLatitude(), markerLocation.getLongitude()));
            Map<String, String> result = new HashMap<>();
            result.put("type", "bomb");
            result.put("Uid", String.valueOf(currentUserId));
            newLocation.child("property").setValue(result);
            Task bomb_add = userRef.child("score").setValue(score - 300);
            bomb_add.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull @NotNull Task task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Bomb successfully added", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), R.string.bomb_adding_failed, Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            Toast.makeText(getContext(), R.string.fail_to_add_bomb_no_money, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialogFragment) {
        Toast.makeText(getContext(), "Task canceled", Toast.LENGTH_SHORT).show();
    }

    /**
     * Randomly generate a list with 10 GeoPoint, 10 GeoPoints locate in cycle with
     * current position as center and radius is 1000
     *
     * @return
     */

    HashMap<Marker, Integer> boxLocationMarker;

    /**
     * Randomly generate 10 Sunshine within a radius of 1000 meters with the current
     * position as the center, and monitor each Sunshine. If you click on the
     * Sunshine within 200 meters from the Sunshine, the Sunshine will disappear
     **/
    private void createSs(List<GeoPoint> sunShineList) {
        for (int i = 0; i < sunShineList.size(); i++) {
            Marker marker = new Marker(map);
            marker.setPosition(sunShineList.get(i));
            Drawable icon = getResources().getDrawable(R.drawable.ic_baseline_wb_sunny_24, getActivity().getTheme());
            marker.setIcon(icon);
            marker.setTitle("SunShine");
            marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker, MapView mapView) {
                    GeoPoint center = mLocationOverlay.getMyLocation();
                    double distance = center.distanceToAsDouble(
                            new GeoPoint(marker.getPosition().getLatitude(), marker.getPosition().getLongitude()));
                    if (distance < 50d) {
                        marker.remove(mapView);
                        rewriteSunOrBoxInFile(getContext(), marker, "ssList");
                        addItemInDatabase("sun", 1);
                        Toast.makeText(getContext(), "Got sunshine!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Please approach me first!", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
            overlayManager.add(marker);
        }
    }

    private void createBox(List<Pair<GeoPoint, Integer>> boxList) {
        GeoPoint center = new GeoPoint(myCurrentLocation.latitude, myCurrentLocation.longitude);
        for (int i = 0; i < boxList.size(); i++) {
            Pair<GeoPoint, Integer> box = boxList.get(i);
            Marker marker = new Marker(map);
            marker.setPosition(box.first);
            Drawable icon = getResources().getDrawable(R.drawable.ic_baseline_not_listed_location_24,
                    getActivity().getTheme());
            marker.setIcon(icon);
            marker.setTitle("confused treasure box");
            marker.setId("local");
            marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker, MapView mapView) {
                    DialogFragment fragment = new MarkerClickFragment(marker, mapView, boxLocationMarker,
                            mLocationOverlay.getMyLocation(), false, getContext());
                    fragment.show(getChildFragmentManager(), null);
                    return true;
                }
            });
            boxLocationMarker.put(marker, box.second);
            overlayManager.add(marker);
        }
    }

    /**
     * @param Location      locationList of the certain item
     * @param c             the context of the certain activity
     * @param currentUserID
     * @param itemList      ssList or boxList
     */
    public void writeCurrentSunShineOrBox(ArrayList<String> Location, Context c, String currentUserID,
            String itemList) {
        writeFile(c, currentUserID, "sunShineAndBox", itemList, Location, false);
    }

    private boolean over24H() {
        long currentTime = new Date().getTime();
        long lastUpdateTime = readLastLogTime(getContext(), currentUserId);
        long ms = currentTime - lastUpdateTime;
        return ms > 24 * 60 * 60 * 1000;
    }
}