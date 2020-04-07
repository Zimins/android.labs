package com.labs.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MarkerActivity extends AppCompatActivity {

    Button takePhoto;
    Button openGallery;
    SubsamplingScaleImageView imageView;
    private String mCurrentPhotoPath = "";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        imageView = findViewById(R.id.imageView);
        String oldUri = intent.getStringExtra("oldUri");
        if (oldUri == null || oldUri.isEmpty()) {
            imageView.setImage(ImageSource.resource(R.drawable.empty));
        } else {
            imageView.setImage(ImageSource.uri(oldUri));
        }

        imageView.setOnClickListener(v -> {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhoto , 1);
        });

        openGallery = findViewById(R.id.btnOpenGallery);
        takePhoto = findViewById(R.id.btnTakePhoto);
        takePhoto.setOnClickListener(v -> {
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
            requestPermissions(permissions, 1);
            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePicture.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    Log.i("error", ex.getMessage());
                }
                if (photoFile != null) {
                    takePicture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    startActivityForResult(takePicture, 0);
                }
            }
        });

        openGallery.setOnClickListener(v -> {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhoto , 1);
        });

        String s = intent.getStringExtra("title");
        setTitle(s);
    }

    private File createImageFile() throws IOException {
        @SuppressLint("SimpleDateFormat")
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        if (resultCode == 0) {
            return;
        }

        String uri = null;
        switch(requestCode) {
            case 0:
                if(resultCode == RESULT_OK){
                    uri = mCurrentPhotoPath;
                }
                break;
            case 1:
                if(resultCode == RESULT_OK){
                    uri = imageReturnedIntent.getData().getPath().substring(5);
                    mCurrentPhotoPath = uri;
                }
                break;
        }

        imageView.setImage(ImageSource.uri(uri));
        getIntent().putExtra("uri", uri);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("oldUri", mCurrentPhotoPath == null ? "" : mCurrentPhotoPath);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        mCurrentPhotoPath = savedInstanceState.getString("oldUri");
        if (mCurrentPhotoPath.isEmpty()) {
            imageView.setImage(ImageSource.resource(R.drawable.empty));
        } else {
            imageView.setImage(ImageSource.uri(mCurrentPhotoPath));
        }
    }

    @Override
    public void finish() {
        setResult(mCurrentPhotoPath.isEmpty() ? 0 : 1, getIntent());
        super.finish();
    }
}