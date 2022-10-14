package com.example.logbook_lekhanh;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.example.logbook_lekhanh.databinding.ActivityMainBinding;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    Handler mainHandler = new Handler();
    ProgressDialog progressDialog;

    private ArrayList<String> imageUrlArray = new ArrayList<>();
    private int i = 0;
    boolean isValidated = false;

    private static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        imageUrlArray = SharePreferencesConfig.readArrayfromPref(this);
        if (imageUrlArray == null){
            Picasso.get().load(imageUrlArray.get(0)).into(binding.ImageViewRender);
        }

        binding.btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageUrlArray != null ){
                    if (i > 1){
                        binding.ImageViewRender.setImageBitmap(null);
                        if(i <= imageUrlArray.size()){
                            --i;
                            Picasso.get().load(imageUrlArray.get(i-1)).into(binding.ImageViewRender);
                        }
                    }else if (i == 1){
                        Toast.makeText(MainActivity.this, "This is the first image", Toast.LENGTH_SHORT).show();
                    }else if (imageUrlArray.size() == 0){
                        Toast.makeText(MainActivity.this, "There are no image that have been add", Toast.LENGTH_SHORT).show();
                    }


                }
            }
        });

        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageUrlArray != null){
                    if (imageUrlArray.size() == 0){
                        Toast.makeText(MainActivity.this, "There are no image that have been add", Toast.LENGTH_SHORT).show();
                    }else if (i < imageUrlArray.size()){
                        binding.ImageViewRender.setImageBitmap(null);
                        ++i;
                        Picasso.get().load(imageUrlArray.get(i-1)).into(binding.ImageViewRender);
                    }else if (i == 0){
                        binding.ImageViewRender.setImageBitmap(null);
                        Picasso.get().load(imageUrlArray.get(i-1)).into(binding.ImageViewRender);
                        i++;
                    }else if (i == imageUrlArray.size()){
                        Toast.makeText(MainActivity.this, "This is the last image", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        binding.btnSummit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = binding.textFieldUrl.getText().toString();
                if(URL_Validate(url)){
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        new fetchImage(url).start();
                        isValidated = true;
                    }else{
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                },REQUEST_CODE);
                    }
                }else{
                    Toast.makeText(MainActivity.this, "This is not a valid Url", Toast.LENGTH_SHORT).show();
                }
                binding.textFieldUrl.setText("");
                isValidated = false;
            }
        });

        binding.btnSaveGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    saveImageToGallery();
                }else{
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                            },REQUEST_CODE);
                }
            }
        });

    }

    private void saveImageToGallery(){
        Uri images;
        ContentResolver contentResolver = getContentResolver();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            images = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        }else{
            images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, System.currentTimeMillis()+".jpg");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "images/*");
        Uri uri = contentResolver.insert(images,contentValues);

        try {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) binding.ImageViewRender.getDrawable();
            Bitmap bitmap = bitmapDrawable.getBitmap();

            OutputStream outputStream = contentResolver.openOutputStream(Objects.requireNonNull(uri));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            Objects.requireNonNull(outputStream);
            Toast.makeText(MainActivity.this, "Successfully saved image to the gallery", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Toast.makeText(MainActivity.this, "Fail to save image to the gallery", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public static boolean URL_Validate(String url){
        try{
            new URL(url).toURI();
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    class fetchImage extends Thread{
        String url;
        Bitmap bitmap;
        fetchImage(String url){
            this.url = url;
        }
        @Override
        public void run(){
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setMessage("Fetching image in progress...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                }
            });
            InputStream inputStream = null;
            try {
                inputStream = new java.net.URL(url).openStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
                imageUrlArray.add(url);
                i++;
//                Toast.makeText(MainActivity.this, "Fetch successfully, URL have been save to the Array.", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(MainActivity.this, "Fail to fetch image to the imageView.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    binding.ImageViewRender.setImageBitmap(bitmap);
//                    saveImage();
                }
            });


            //save push image to the gallery
            Uri images;
            ContentResolver contentResolver = getContentResolver();
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                images = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            }else{
                images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, System.currentTimeMillis()+".jpg");
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "images/*");
            Uri uri = contentResolver.insert(images,contentValues);

            try {
                OutputStream outputStream = contentResolver.openOutputStream(Objects.requireNonNull(uri));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                Objects.requireNonNull(outputStream);
//                Toast.makeText(MainActivity.this, "Successfully saved image to the gallery", Toast.LENGTH_SHORT).show();
            }catch (Exception e){
                Toast.makeText(MainActivity.this, "Fail to save image to the gallery", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }


            //Save array of url using Share Preferences
            SharePreferencesConfig.writeArrayinPref(getApplicationContext(), imageUrlArray);

        }
    }
}