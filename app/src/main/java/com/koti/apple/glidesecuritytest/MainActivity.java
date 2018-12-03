package com.koti.apple.glidesecuritytest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView ivLoadImage;
    private Button btSaveScript;
    private Button btDeleteScript;
    private static final String TAG = "MainActivity";
    private String localFilename;
    private static final int REQUEST_CODE = 1;
    // o/p: {'brand': 'Ford', 'model': 'Mustang', 'year': 1964}
    //http://goo.gl/gEgYUd
    //https://pbs.twimg.com/profile_images/630285593268752384/iD1MkFQ0.png
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        ivLoadImage = findViewById(R.id.iv_load_image);
        btSaveScript = findViewById(R.id.bt_Save_script);
        btDeleteScript = findViewById(R.id.bt_delete_script);
        btSaveScript.setOnClickListener(this);
        btDeleteScript.setOnClickListener(this);
        Glide.with(this)
                .load("http://goo.gl/gEgYUd")
                .apply(new RequestOptions().override(600, 300))
                .into(ivLoadImage);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_Save_script:
                if (Build.VERSION.SDK_INT >= 23) {
                    if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "Permission is granted");
                        createPythonScriptFile();
                    } else {
                        Log.d(TAG, "Permission is revoked");
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                    }
                }
                break;
            case R.id.bt_delete_script:
                deletePythonScriptFile(localFilename);
                break;
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private void createPythonScriptFile() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        String saveFileName = "/PythonDictionaryObjectCreate.py";
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e(TAG, "Failed to get asset file list.", e);
        }
        if (files != null)
            for (String filename : files) {
                if (filename.endsWith(".py")) {
                    String dir = Environment.getExternalStorageDirectory() + File.separator + "0999";
                    //create folder
                    File folder = new File(dir); //folder name
                    folder.mkdirs();
                    //create file
                    File file = new File(dir, filename);
                    Log.e(TAG, "created File path " + dir);
                    InputStream in = null;
                    OutputStream out = null;
                    try {
                        in = assetManager.open(filename);
                        out = new FileOutputStream(file);
                        copyFile(in, out);
                        localFilename = dir+saveFileName;
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to copy asset file: " + filename, e);
                        e.printStackTrace();
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    showToast("Created script file in 0999 directory");
                }
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            createPythonScriptFile();
        }
    }

    public void deletePythonScriptFile(String fullPath) {
        try {
            File file = new File(fullPath);
            if (file.exists()) {
                file.delete();
                showToast("Deleted script file in 0999 directory");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
