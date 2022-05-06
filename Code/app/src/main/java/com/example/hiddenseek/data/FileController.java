package com.example.hiddenseek.data;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FileController {
    public static void getUserIcon(Context c, String userId, ImageView userImage)  {

        FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("icon").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    if(task.getResult().exists()){
                        String fileName = task.getResult().getValue(String.class);
                        File CacheRoot = c.getFilesDir();

                        // ImageView in the Activity
                        try {
                            String UserDirPath = CacheRoot.getCanonicalPath()
                                    + File.separator + "icons";
                            File localFile = new File(UserDirPath,fileName);
                            File[] tempFiles = localFile.listFiles();
                            if (localFile.exists()) {
                                Glide.with(c)
                                        .load(tempFiles[0])
                                        .into(userImage);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                    else{
                    }
                }
            }
        });
    }

    public static String createFileDir(Context c, String currentUserID,String FileDir) {
        File CacheRoot = c.getFilesDir();
        String UserDirPath = null;
        try {
            UserDirPath = CacheRoot.getCanonicalPath()
                    + File.separator + currentUserID
                    + File.separator + FileDir;
        } catch (IOException e) {
            e.printStackTrace();
        }
        File fromUserDir = new File(UserDirPath);
        if (!fromUserDir.exists()) {
            fromUserDir.mkdirs();
        }
        return UserDirPath;
    }

    public static void writeFile(Context c, String currentUserID,String FileDir, String FileName,Object jsonObject,boolean append){
        try {
            File currentFile = new File(createFileDir(c, currentUserID,FileDir), FileName);
            FileOutputStream fos = new FileOutputStream(currentFile, append);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(jsonObject);
            fos.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Object readFile(Context c, String currentUserID,String FileDir, String FileName){
        Object jsonObject=null;
        File des =new File(createFileDir(c, currentUserID,FileDir), FileName);
        jsonObject=readFileWithFile(des);
        return jsonObject;
    }
    public static Object readFileWithFile(File des){
        Object jsonObject=null;
        try {
            if (des.exists()) {
                FileInputStream fis = new FileInputStream(des);
                ObjectInputStream ois = new ObjectInputStream(fis);
                jsonObject = ois.readObject();
                fis.close();
                ois.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static void deleteFiles(Context c, String currentUID, String fromUID) {
        File CacheRoot = c.getFilesDir();
        try {
            String path = CacheRoot.getCanonicalPath() +
                    File.separator + currentUID +
                    File.separator + fromUID;
            File file = new File(path);
            deleteFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                deleteFile(f);
            }
            file.delete();
        } else if (file.exists()) {
            file.delete();
        }
    }

}
