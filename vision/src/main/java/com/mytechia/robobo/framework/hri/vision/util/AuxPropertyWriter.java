package com.mytechia.robobo.framework.hri.vision.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.mytechia.robobo.framework.RoboboManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class AuxPropertyWriter {

    private static final String TAG = "AuxPropertyWriter";
    private RoboboManager manager;
    private Properties properties;
    private Context appContext;
    private Uri propFileUri;
    private String propFilePath;

    public AuxPropertyWriter(Context context, String fileName, RoboboManager manager) {
        this.manager = manager;
        this.appContext = context;
        properties = new Properties();

        ContentResolver resolver = appContext.getContentResolver();
        InputStream inputStream;

        // FIXME: We are not using URIs for lower than Q. Just use regular file opening to get to the file
        try{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                propFileUri = createMediaStoreFile(fileName);
            } else {
                propFilePath = createLegacyFile(fileName);
            }
        } catch(IOException ex){
            ex.printStackTrace();
        }
        properties = new Properties();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                inputStream = resolver.openInputStream(propFileUri);
            } else {
                inputStream = new FileInputStream(propFilePath);
            }
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        ContentResolver resolver = appContext.getContentResolver();
        OutputStream fos = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                fos =  resolver.openOutputStream(propFileUri);
            } else {
                fos = new FileOutputStream(propFilePath);
            }
            properties.store(fos,"Shared preferences of the Robobo environment");
            fos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Uri createMediaStoreFile(String fileName) throws IOException{
        ContentResolver resolver = appContext.getContentResolver();
        // Obtiene la Uri para el almacenamiento externo usando MediaStore
        Uri externalUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

        String[] projection = new String[]{
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.RELATIVE_PATH
        };
        String selection = MediaStore.Files.FileColumns.DISPLAY_NAME + " LIKE ? AND " +
                MediaStore.Files.FileColumns.RELATIVE_PATH + "=?";
        String[] selectionArgs = {  fileName+"%.properties",  Environment.DIRECTORY_DOCUMENTS + "/properties/" };
        Cursor cursor = resolver.query(
                externalUri,
                projection,
                selection,
                selectionArgs,
                null
        );

        Boolean fileExists = false;
        Uri newFileUri = null;
        while (cursor.moveToNext()) {
            // Get values of columns for a given video
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
            long id = cursor.getLong(idColumn);
            String name = cursor.getString(nameColumn);
            newFileUri = externalUri.buildUpon().appendPath(Long.toString(id)).build();
            if (!fileExists) fileExists = true;
        }

        if(!fileExists){
            // Crea un ContentValues con los detalles del nuevo archivo
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/properties/");
            contentValues.put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName+".properties");

            // Crea el archivo utilizando OutputStream
            newFileUri = resolver.insert(externalUri, contentValues);
            OutputStream outputStream = resolver.openOutputStream(newFileUri);
            if (outputStream != null) {
                outputStream.close();
            }
        }
        return newFileUri;
    }

    private String createLegacyFile(String fileName) throws IOException{

        // Código para versiones anteriores a 29 con MediaStore.Files sin RELATIVE_PATH
        File documentsDirectory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "properties");

        if (!documentsDirectory.exists()) {
            documentsDirectory.mkdirs();
        }

        File file = new File(documentsDirectory, fileName+".properties");
        if (!file.exists()){
            OutputStream outputStream = new FileOutputStream(file);
            if (outputStream != null) {
                outputStream.close();
            }
        }
        return file.getPath();
    }


}
