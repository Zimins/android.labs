package com.labs.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MarkerActivity extends AppCompatActivity {

    // viewPager
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;
    private List<String> uriImages = new ArrayList<>();

    private SQLiteDatabase database;

    long idMarker;

    private String mCurrentPhotoPath = "";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker);

        // Запрашиваем права на чтение/запись
        requestPermissions(new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        // Экземпляр класса DBHelper
        DBHelper dbHelper = new DBHelper(this);

        // Управление БД
        database = dbHelper.getWritableDatabase();

        Intent intent = getIntent();

        // Получаем фотографии маркера
        idMarker = intent.getLongExtra("idMarker", 0);
        Cursor cursor = database.query(DBHelper.TABLE_PHOTO, new String[]{DBHelper.KEY_URI},
                DBHelper.KEY_ID_MARKER + "=" + idMarker,
                null, null, null, null);

        if (cursor.moveToFirst()) {
            int uriIdx = cursor.getColumnIndex(DBHelper.KEY_URI);
            do {
                uriImages.add(cursor.getString(uriIdx));
            } while (cursor.moveToNext());
        }
        cursor.close();

        // Если нет фотографий, выводим соответсвующее информационное окно
        if (uriImages.isEmpty()) {
            AlertDialog alertDialog = new AlertDialog.Builder(MarkerActivity.this).create();
            alertDialog.setTitle("Информация");
            alertDialog.setMessage("Здесь ещё нет фотографий :( " +
                    "\nВы можете сделать снимок или загрузить фото из галереи");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Ок",
                    (dialog, which) -> dialog.dismiss());
            alertDialog.show();
        }

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        viewPager = findViewById(R.id.viewPhotos);
        adapter = new ViewPagerAdapter(MarkerActivity.this, uriImages);
        viewPager.setAdapter(adapter);

        Button btnOpenGallery = findViewById(R.id.btnOpenGallery);
        Button btnTakePhoto = findViewById(R.id.btnTakePhoto);

        btnTakePhoto.setOnClickListener(v -> {
            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePicture.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    Log.i("error", Objects.requireNonNull(ex.getMessage()));
                }
                if (photoFile != null) {
                    takePicture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    startActivityForResult(takePicture, 0);
                }
            }
        });

        btnOpenGallery.setOnClickListener(v -> {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhoto, 1);
        });

        setTitle(intent.getStringExtra("title"));
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
        switch (requestCode) {
            case 0: {
                if (resultCode == RESULT_OK) {
                    uri = mCurrentPhotoPath;
                }
                break;
            }
            case 1: {
                if (resultCode == RESULT_OK) {
                    uri = imageReturnedIntent.getData().getPath().substring(5);
                    mCurrentPhotoPath = uri;
                }
                break;
            }
            default: {

            }
        }

        uriImages.add(uri);
        adapter.notifyDataSetChanged();
        viewPager.setCurrentItem(adapter.getCount() - 1);

        // Вставляем uri фотографии в БД
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.KEY_ID_MARKER, idMarker);
        contentValues.put(DBHelper.KEY_URI, uri);

        database.insert(DBHelper.TABLE_PHOTO, null, contentValues);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Сохраняем позицию viewPager
        outState.putInt("viewPagerCurrentItem", viewPager.getCurrentItem());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        viewPager.setCurrentItem(savedInstanceState.getInt("viewPagerCurrentItem", 0));
    }
}