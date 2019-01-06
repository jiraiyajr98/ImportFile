package com.intern.ankan.csvreader;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.provider.DocumentsContract.isDocumentUri;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int ACTIVITY_CHOOSE_FILE1 = 951;
    private static final String TAG = "MAIN ACTIVITY";
    private static final String contents = "New FILE NEW Contents";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button readCSV = (Button) findViewById(R.id.read);

        Button getPath = (Button)findViewById(R.id.getpath);

        Button createFile = (Button)findViewById(R.id.createfile);


        createFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewFile();
            }
        });


        readCSV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(checkPermission())
                    Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    else
                    requestPermission();

            }
        });

        getPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCSVFile();
            }
        });
    }

    private void createNewFile() {

        File folder = new File(Environment.getExternalStorageDirectory() + "/MQTT_Logs");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if(success) {
            Toast.makeText(this, "Success Folder", Toast.LENGTH_SHORT).show();
            String temp = Environment.getExternalStorageDirectory() + "/MQTT_Logs/" + System.currentTimeMillis() + ".txt";
            try {
                FileOutputStream fos = new FileOutputStream(temp);
                fos.write(contents.getBytes());
                fos.close();
                Toast.makeText(this, "Written", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed Writing", Toast.LENGTH_SHORT).show();
            }


        }
        else
            Toast.makeText(this, "Failed Folder", Toast.LENGTH_SHORT).show();

    }


    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);

        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
                , Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);

    }

    private void selectCSVFile(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        //intent.setType("text/comma-separated-values");
      //  intent.setType("text/plain");

        File csvFile = null;
        try {
            csvFile = makeFile();

        } catch (IOException e) {
            e.printStackTrace();
        }
        Uri uri = FileProvider.getUriForFile(this,"com.intern.ankan.csvreader",csvFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

        startActivityForResult(Intent.createChooser(intent, "Open CSV"), ACTIVITY_CHOOSE_FILE1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted && cameraAccepted)
                        Toast.makeText(this, "Granted", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        switch (requestCode){

            case ACTIVITY_CHOOSE_FILE1: {

                if (resultCode == RESULT_OK ){
                    if(data != null) {

                            Log.d(TAG,"PATH -> "+data.getData());
                            Toast.makeText(this, "Data-> "+data.getData(), Toast.LENGTH_SHORT).show();
                            //contentResolver(data.getData());
                            String path = getPath(MainActivity.this,data.getData());
                            Log.d(TAG,"PATHN -> "+path);
                            Toast.makeText(this, "PathN "+path , Toast.LENGTH_SHORT).show();

                        try {
                            InputStream inputStream = getContentResolver().openInputStream(data.getData());
                            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                            String mLine;
                            while ((mLine = r.readLine()) != null) {

                                String token[] = mLine.split(",");
                                Log.d(TAG,"Token 1 -> "+token[0]);
                                Log.d(TAG,"Token 2 -> "+token[1]);
                                Log.d(TAG,"Token 3 -> "+token[2]);
                                Log.d(TAG,"Token 4 -> "+token[3]);
                                Log.d(TAG,"Token 5 -> "+token[4]);


                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        }
                    }
                else
                    Toast.makeText(this, "Data Null", Toast.LENGTH_SHORT).show();
            }
        }
    }

    File makeFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String csvFile = "IMAGE_" + timeStamp + "_";
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File temp = File.createTempFile(csvFile,".csv", storageDirectory);

        return  temp;

    }


    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            System.out.println("getPath() uri: " + uri.toString());
            System.out.println("getPath() uri authority: " + uri.getAuthority());
            System.out.println("getPath() uri path: " + uri.getPath());

            // ExternalStorageProvider
            if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                System.out.println("getPath() docId: " + docId + ", split: " + split.length + ", type: " + type);

                // This is for checking Main Memory
                if ("primary".equalsIgnoreCase(type)) {
                    if (split.length > 1) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    } else {
                        return Environment.getExternalStorageDirectory() + "/";
                    }
                    // This is for checking SD Card
                } else {
                    return "storage" + "/" + docId.replace(":", "/");
                }

            }
        }
        return null;
    }


    void readCSV(String path){

        int line;
        try {
            FileInputStream fis = openFileInput(path);
            while ((line=fis.read()) != -1) {
                Log.d(TAG,""+(char)line);
            }
            Toast.makeText(this, "File Found", Toast.LENGTH_SHORT).show();

        } catch (FileNotFoundException e) {
            Toast.makeText(this, "File Not Found", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
