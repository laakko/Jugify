package com.jukka.jugify;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class FileService {

    protected static boolean fileExists(Context ctx, String filename) {
        return new File(ctx.getFilesDir().getAbsolutePath() + "/" + filename).exists();
    }

    protected static File createFile(Context ctx, String filename) {

        File file = new File(ctx.getFilesDir(), filename);
        return file;
    }

    protected static String readFile(Context ctx, String filename) {

        File file;

        if (fileExists(ctx, filename)) {
            file = new File(ctx.getFilesDir().getAbsolutePath() + "/" + filename);

            int length = (int) file.length();
            byte[] bytes = new byte[length];

            try {

                FileInputStream in = new FileInputStream(ctx.getFilesDir().getAbsolutePath() + "/" + filename);
                try {
                    in.read(bytes);
                } finally {
                    in.close();
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            return new String(bytes);

        } else {
            return "";
        }
    }

    protected static boolean writeFile(Context ctx, String filename, String output){

        if(!fileExists(ctx, filename)) {
            createFile(ctx, filename);
        }

        FileOutputStream out;
        try {
            out = ctx.openFileOutput(filename, Context.MODE_APPEND);
            out.write(output.getBytes());
            out.close();
            return true;
        } catch(IOException ioe) {
            return false;
        }
    }

    protected static boolean removeItem(Context ctx, String filename, String itemToRemove) {

        try {
            File file = new File(ctx.getFilesDir().getAbsolutePath() + "/" + filename);
            File temp = File.createTempFile("tempfile", ".txt", file.getParentFile());


            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(ctx.getFilesDir().getAbsolutePath() + "/" + filename), "UTF-8"));
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(temp), "UTF-8"));


            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replace(itemToRemove+"-", "");
                writer.println(line);
            }

            reader.close();
            writer.close();
            file.delete();
            temp.renameTo(file);

            return true;


        } catch(IOException ioe) {
            Log.i("PERKELE", "PERKELE");
            ioe.printStackTrace();
            return false;
        }


    }

    protected static boolean fileContainsString(Context ctx, String filename, String fileToContain) {

        String contents = readFile(ctx, filename);
        if(contents.contains(fileToContain)) {
            return true;
        } else {
            return false;
        }

    }
}
