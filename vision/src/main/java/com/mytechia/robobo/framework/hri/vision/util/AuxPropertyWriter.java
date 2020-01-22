package com.mytechia.robobo.framework.hri.vision.util;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class AuxPropertyWriter {

    Properties properties;
    File dir;
    String fileName;
    File propFile;
    public AuxPropertyWriter(){
        dir = new File(Environment.getExternalStorageDirectory() + "/properties");
        dir.mkdirs();
        fileName = "cameraCalibration.properties";
        propFile = new File(dir, fileName);

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(propFile);
        } catch (FileNotFoundException e) {
            createPropFile();
            try {
                fileInputStream = new FileInputStream(propFile);
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }

        }


        properties = new Properties();
        try {

            //FIXME Puede saltar un NullPointerException por culpa del fileInputStream
            if (fileInputStream == null){
                throw new FileNotFoundException();
            }else {
                properties.load(fileInputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void storeConf(String key, String value) {

        properties.setProperty(key,value);

    }

    public String retrieveConf(String key,String defValue) {
        String prop = properties.getProperty(key,defValue);
        return prop;


    }

    public void removeConf(String key) {
        properties.remove(key);
    }

    public void reset() {
        properties.clear();
    }

    public synchronized void commitConf() {
        FileOutputStream fos = null;
        try {

            fos = new FileOutputStream(propFile,false);
            properties.store(fos,"Shared preferences of the Robobo environment");

            fos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void createPropFile(){

        File dir = new File (Environment.getExternalStorageDirectory() + "/properties");

        dir.mkdirs();
        dir.setWritable(true);
        dir.setReadable(true);

        File file = new File(dir, "cameraCalibration.properties");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
