package com.example.camcapture_offloading3;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button button;
    Button button_exit;
    Bitmap bitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);
        button_exit = findViewById(R.id.button_exit);

        // Check if we have permission to access the phone's camera. If Not, ask user for permission.
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.CAMERA
            }, 100);
        }

        // If "Not Interested" button is clicked, Exit the application
        button_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                System.exit(0);
            }
        });


        // If "Capture!" button is clicked, open the camera to click picture
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("MainActivity", "Going to capture image");
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // "startActivityForResult" OR "onActivityResult" function, in order to capture the result in bitmap
                //startForResult.launch(intent);
                startActivityForResult(intent, 100);

            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==100)
        {
            bitmap = (Bitmap) data.getExtras().get("data");
            Log.i("Got image", "Going to start new intent");
            // Start an intent to go to the next screen with the bitmap image to upload
            Intent intent1 = new Intent(getApplicationContext(), UploadActivity.class);
            intent1.putExtra("data", bitmap);
            startActivity(intent1);
        }
    }
}