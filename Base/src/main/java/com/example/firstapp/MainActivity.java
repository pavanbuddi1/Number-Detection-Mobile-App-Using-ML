package com.example.firstapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    public ImageView myImage;
    public Button myButton;
    public Uri image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myImage = findViewById(R.id.captureImage);
        myButton = findViewById(R.id.button4);

        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("success", "inside onclick listener");
               if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                   if(checkSelfPermission(Manifest.permission.CAMERA) ==
                           PackageManager.PERMISSION_DENIED){
                       String[] permission = {Manifest.permission.CAMERA};
                       requestPermissions(permission, PERMISSION_CODE);
                   }
                   else{
                       openCamera();
                   }
               }
               else{
                   openCamera();
               }
            }
        });
    }

    private void openCamera() {
        Log.d("success", "opening camera");
        ContentValues vals = new ContentValues();
        vals.put(MediaStore.Images.Media.TITLE, "New Pic");
        vals.put(MediaStore.Images.Media.DESCRIPTION, "Camera");
        image = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, vals);

        Intent cameraIntent =  new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            Log.d("success", "sending the image to activity 2");
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            intent.putExtra("my_image", image.toString());
            startActivity(intent);
        }
    }
}

