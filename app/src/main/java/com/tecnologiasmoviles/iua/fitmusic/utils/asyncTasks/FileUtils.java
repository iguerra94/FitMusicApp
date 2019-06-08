package com.tecnologiasmoviles.iua.fitmusic.utils.asyncTasks;

import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FileUtils {
    public static String saveFile(String fileUrl) {
        try {
            String name = Uri.parse(fileUrl).getLastPathSegment().split("/")[2];

            File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    name);

            String savedFilePath = outputFile.getAbsolutePath();

            if (!outputFile.exists()) {
                URL url = new URL(fileUrl);//Create Download URl
                HttpURLConnection c = (HttpURLConnection) url.openConnection();//Open Url Connection
                c.setRequestMethod("GET");//Set Request Method to "GET" since we are grtting data
                c.connect();//connect the URL Connection

                FileOutputStream fos = new FileOutputStream(outputFile);//Get OutputStream for NewFile Location

                InputStream is = c.getInputStream();//Get InputStream for connection

                byte[] buffer = new byte[1024];//Set buffer type
                int len1 = 0;//init length
                while ((len1 = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len1);//Write new file
                }

                //Close all connection after doing task
                fos.close();
                is.close();
            }

            return savedFilePath;
        } catch (Exception e) {
            //Read exception if something went wrong
            e.printStackTrace();
        }

        return null;
    }

}
