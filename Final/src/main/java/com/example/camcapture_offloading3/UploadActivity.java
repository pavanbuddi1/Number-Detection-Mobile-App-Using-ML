package com.example.camcapture_offloading3;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jayway.jsonpath.JsonPath;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadActivity extends AppCompatActivity {

    ImageView imageview;
    ImageView imageview_1;
    ImageView imageview_2;
    ImageView imageview_3;
    ImageView imageview_4;
    TextView textview_1;
    Button button_upload;
    Button button_display;
    Button button_save;
    Bitmap bitmap;
    String result;
    TextView textview;
    int number_of_clients = 4;
    ArrayList<Bitmap> chunkedImages = new ArrayList<>(number_of_clients);
    ArrayList<String> encodedImages = new ArrayList<>(number_of_clients);
    ArrayList<OkHttpClient> okHttpClients = new ArrayList<>(number_of_clients);
    ArrayList<Request> requests = new ArrayList<>(number_of_clients);
    ArrayList<String> results = new ArrayList<>(number_of_clients);

    ArrayList<String> ipAddresses = new ArrayList<>(number_of_clients);

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // Get the intent that started this activity, and extract the image
        Intent intent1 = getIntent();
        bitmap = (Bitmap) intent1.getParcelableExtra("data");
        button_display = findViewById(R.id.button_disp);
        button_save = findViewById(R.id.button_save);
        imageview = findViewById(R.id.imageview);
        imageview_1 = findViewById(R.id.imageview_1);
        imageview_2 = findViewById(R.id.imageview_2);
        imageview_3 = findViewById(R.id.imageview_3);
        imageview_4 = findViewById(R.id.imageview_4);
        textview_1 = findViewById(R.id.textView_1);
        imageview.setImageBitmap(bitmap);
        textview = findViewById(R.id.textView2);
        splitImage(bitmap, number_of_clients);

        ipAddresses.add("https://87fa-2600-8800-1303-c600-908b-11e0-fda1-cbe3.ngrok.io");
        ipAddresses.add("https://dbda-2600-8800-1303-c600-3c40-4fda-de8-1370.ngrok.io");
        ipAddresses.add("https://993a-2600-8800-1303-c600-3836-b892-6f19-8fc5.ngrok.io");
        ipAddresses.add("https://7fbf-2600-8800-1303-c600-4565-c3d5-c66a-3f54.ngrok.io");

        getEncoded();

        button_upload = findViewById(R.id.button_upload);
        button_upload.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onClick(View view) {
                Log.i("GSending files", "wait");
                Toast.makeText(UploadActivity.this, "Sending the Files. Please Wait ...", Toast.LENGTH_SHORT).show();

                for(int i=0; i<number_of_clients; i++)
                {
                    Toast.makeText(UploadActivity.this, "Making okHttpClient", Toast.LENGTH_SHORT).show();
                    OkHttpClient okHttpClient_tmp = new OkHttpClient();
                    okHttpClients.add(okHttpClient_tmp);
                    RequestBody formbody = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("encodedImage", encodedImages.get(i)).build();
                    Request request_tmp = new Request.Builder().url(ipAddresses.get(i)).post(formbody).build();
                    requests.add(request_tmp);
                }

                Log.i("Made okHttpClient and requests", "wait");

                for(int j=0; j<number_of_clients; j++)
                {
                    sendToServer(okHttpClients.get(j), requests.get(j));
                }

                Log.i("Done", "Now you can display results");
                //displayOutput();

            }
        });

        button_display.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    int max_accuracy = 0;
                    int max_accuracy_digit = 0;
                    for(int k=0; k<results.size(); k++)
                    {
                        JSONObject json = null;
                        try {
                            json = new JSONObject(results.get(k));
                            int curr_accuracy = json.getInt("accuracy");
                            if(curr_accuracy>max_accuracy)
                            {
                                max_accuracy = curr_accuracy;
                                max_accuracy_digit = json.getInt("number");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                    textview_1.setText(String.valueOf(max_accuracy_digit));
                //}
            }
        });

        ActivityCompat.requestPermissions(UploadActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        ActivityCompat.requestPermissions(UploadActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable bitmapdrawable = (BitmapDrawable) imageview.getDrawable();
                Bitmap bitmap_result = bitmapdrawable.getBitmap();
                FileOutputStream outputstream = null;
                File file = Environment.getExternalStorageDirectory();
                File dir = new File(file.getAbsolutePath() + "/" + textview_1.getText().toString());
                Log.i("in save", dir.toString());
                dir.mkdirs();
                String filename = String.format("%d.png",System.currentTimeMillis());
                Log.i("in save", filename.toString());
                File outfile = new File(dir,filename);
                try{
                    outputstream = new FileOutputStream(outfile);
                }catch (Exception e){
                    e.printStackTrace();
                }
                bitmap_result.compress(Bitmap.CompressFormat.PNG, 100, outputstream);
                try{
                    outputstream.flush();
                }catch (Exception e){
                    e.printStackTrace();
                }
                try{
                    outputstream.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
                Log.i("in save", "saved");

            }
        });
    }

    private boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(UploadActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(result == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        else{
            return false;
        }
    }

    private void requestPermissions(){
        if( ActivityCompat.shouldShowRequestPermissionRationale(UploadActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            Toast.makeText(UploadActivity.this, "Write permissions", Toast.LENGTH_SHORT).show();
        }else{
            ActivityCompat.requestPermissions(UploadActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    private void saveImage(Bitmap bitmap, @NonNull String name) throws IOException {
        boolean saved;
        OutputStream fos;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Context mContext = null;
            ContentResolver resolver = mContext.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + textview.getText());
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            fos = resolver.openOutputStream(imageUri);
        } else {
            String imagesDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).toString() + File.separator + textview.getText();
            File file = new File(imagesDir);
            if (!file.exists()) {
                file.mkdir();
            }
            File image = new File(imagesDir, name + ".png");
            fos = new FileOutputStream(image);
        }
        saved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.flush();
        fos.close();
    }

    private void displayOutput()
    {
        String to_display = "";
        int max_accuracy = 0;
        String max_accu_digit = "";
        for(int k=0; k<results.size(); k++)
        {
            int accuracy = Integer.valueOf(JsonPath.read(results.get(k), "accuracy"));
            Log.i("Next accuracy : ", String.valueOf(accuracy));
            if(accuracy>max_accuracy)
            {
                max_accuracy = accuracy;
                max_accu_digit = JsonPath.read(results.get(k), "number");
                Log.i("Next digit : ", max_accu_digit);
            }
            to_display = to_display+results.get(k);
        }
        Log.i("to_display", to_display);
        Log.i("Classified digit : ", max_accu_digit);
        //textview_1.setText(to_display);
        textview_1.setText("Classified digit : "+max_accu_digit);
    }

    private void sendToServer(OkHttpClient okHttpClient_tmp, Request request_tmp)
    {
        okHttpClient_tmp.newCall(request_tmp).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                call.cancel();
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //TextView responseText = findViewById(R.id.responseText);
                        //responseText.setText("Failed to Connect to Server");
                        Toast.makeText(UploadActivity.this, "Failed to Connect to Server", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("okhttp", "onResponse");
                        //TextView responseText = findViewById(R.id.responseText);
                        try {
                            String r = (response.body().string());
                            results.add(r);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void createDirectoryAndSaveFile(String fileName) {

        File direct = new File(Environment.getExternalStorageDirectory() + "/DirName");

        if (!direct.exists()) {
            File wallpaperDirectory = new File("DirName/");
            wallpaperDirectory.mkdirs();
        }

        File file = new File("DirName/", fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String saveToInternalStorage(String filename){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir(filename, Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"image.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    private void splitImage(Bitmap bm, int chunk)
    {
        int rows, cols;
        int chunkHeight, chunkWidth;
        rows = cols = (int) Math.sqrt(chunk);
        chunkHeight = bm.getHeight()/rows;
        chunkWidth = bm.getWidth()/cols;
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bm, bm.getWidth(), bm.getHeight(), true);
        int yCoord = 0;
        for(int x = 0; x < rows; x++) {
            int xCoord = 0;
            for(int y = 0; y < cols; y++) {
                chunkedImages.add(Bitmap.createBitmap(scaledBitmap, xCoord, yCoord, chunkWidth, chunkHeight));
                xCoord += chunkWidth;
            }
            yCoord += chunkHeight;
        }
        Log.i("Inside splitImage","Image is split");
        imageview_1.setImageBitmap(chunkedImages.get(0));
        imageview_2.setImageBitmap(chunkedImages.get(1));
        imageview_3.setImageBitmap(chunkedImages.get(2));
        imageview_4.setImageBitmap(chunkedImages.get(3));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getEncoded()
    {
        for(int i=0; i<number_of_clients; i++)
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            chunkedImages.get(i).compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            encodedImages.add(Base64.getEncoder().encodeToString(imageBytes));
        }

    }
}