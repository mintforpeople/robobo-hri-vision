package com.mytechia.robobo.framework.hri.vision.util;

import android.os.Environment;
import android.util.Log;

import com.mytechia.robobo.framework.RoboboManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AuxPropertyWriter {

    private static final String TAG = "AuxPropertyWriter";
    private RoboboManager manager;
    private Properties properties;
    private File dir;
    private String fileName;
    private File propFile;

    public AuxPropertyWriter(String fileName, RoboboManager manager) {
        this.manager = manager;
        this.fileName = fileName;
        properties = new Properties();

        dir = new File(Environment.getExternalStorageDirectory() + "/properties");
        propFile = new File(dir, fileName);

        dir.mkdirs();

//        fileName = "cameraCalibration.properties";

        if (!propFile.exists()) {
            loadDefaults();
        } else {
            try {
                properties.load(new FileInputStream(propFile));
            } catch (IOException e) {
                Log.e(TAG, "Unable to access file " + fileName);
                e.printStackTrace();
            }
        }


    }

    private boolean loadDefaults() {


        try {
            InputStream inputStream = manager.getApplicationContext().getAssets().open(fileName);
            properties.load(inputStream);
        } catch (IOException e) {
            Log.e(TAG, "Error loading default values of " + fileName);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void storeConf(String key, String value) {

        properties.setProperty(key, value);

    }

    public String retrieveConf(String key, String defValue) {
        return properties.getProperty(key, defValue);
    }
    public String retrieveConf(String key) {
        return properties.getProperty(key);
    }

    public void removeConf(String key) {
        properties.remove(key);
    }

    public void reset() {
        properties.clear();
    }

    public synchronized void commitConf() {
        FileOutputStream fos = null;
        if (!propFile.exists())
            createPropFile();

        try {
            fos = new FileOutputStream(propFile, false);
            properties.store(fos, "Shared preferences of the Robobo environment");
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createPropFile() {

//        File dir = new File(Environment.getExternalStorageDirectory() + "/properties");

        dir.mkdirs();
        dir.setWritable(true);
        dir.setReadable(true);

        File file = new File(dir, fileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
