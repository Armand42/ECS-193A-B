package com.google.cloud.android.speech;

import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class FileService {

    /*
    Returns contents of file as a string
    @filepath = path to file you're reading from
     */
    public static String readFromFile(String filepath) throws IOException {
        // Create new file object from given filepath
        File file = new File(filepath);
        StringBuilder text = new StringBuilder();
        // Append string to file along with line breakers
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
            // Set text of scriptBody to be what we read from the file
            return text.toString();
        }
        catch (IOException e) {
            throw new IOException(e);
        }
    }


    // Deletes a speech and associated files (script and video(s))
    public static void deleteSpeech(String scriptFilePath) throws Exception {
        // Deletes script
        File script = new File(scriptFilePath);
        if (!script.delete()) {
            throw new Exception("Error deleting script");
        }
    }
    // Create a new file in our speech scripts dir with given filename
    public static String writeToFile(String speechName, String speechText, String speechScriptPath) throws Exception {
        File file = new File(speechScriptPath, speechName);
        // Responsible for the write operation
        FileOutputStream outputStream = null;
        try {
            if(!file.exists())
                file.createNewFile();
            // Second argument of FileOutputStream constructor indicates whether
            // to append or create new file if one exists -- for now we're creating a new file
            outputStream = new FileOutputStream(file, false);

            outputStream.write(speechText.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }

        return file.toString();
    }
}
