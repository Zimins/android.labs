package com.labs.myapplication;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    // Карта
    private GoogleMap mMap;

    // Последний выбранный/добавленный маркер (для сдвига камеры)
    LatLng curMarker;

    // Класс для работы с БД
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Центр Перми
        curMarker = new LatLng(58.0076424, 56.18747043);

        // Настройка прав
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        // Экземпляр класса DBHelper
        dbHelper = new DBHelper(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Управление БД
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // Обрабатываем клик на карту
        mMap.setOnMapClickListener(latLng -> {
            // Диалог с запросом имени
            Dialog dialog = new Dialog(MapsActivity.this);
            dialog.setContentView(R.layout.dialog_request_name);
            dialog.setCancelable(false);

            Button btnSave = dialog.findViewById(R.id.btnSave);
            Button btnCancel = dialog.findViewById(R.id.btnCancel);
            TextInputEditText inpName = dialog.findViewById(R.id.inpName);

            // Обработка нажатия на кнопку "сохранить"
            btnSave.setOnClickListener(v -> {
                ContentValues contentValues = new ContentValues();

                Marker newMarker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(String.valueOf(inpName.getText())));

                curMarker = newMarker.getPosition();

                contentValues.put(DBHelper.KEY_NAME, newMarker.getTitle());
                contentValues.put(DBHelper.KEY_LATITUDE, newMarker.getPosition().latitude);
                contentValues.put(DBHelper.KEY_LONGITUDE, newMarker.getPosition().longitude);

                newMarker.setTag(database.insert(DBHelper.TABLE_MARKER, null, contentValues));
                dialog.dismiss();
            });

            btnCancel.setOnClickListener(v -> {
                dialog.dismiss();
            });

            dialog.show();
        });

        // Обработка нажатия на маркер
        mMap.setOnMarkerClickListener(marker -> {
            Intent intent = new Intent(getBaseContext(), MarkerActivity.class);
            intent.putExtra("idMarker", (Long) marker.getTag());
            intent.putExtra("title", marker.getTitle());

            curMarker = marker.getPosition();
            startActivityForResult(intent, 0);
            return true;
        });

        // Добавляем маркеры из БД на карту
        Cursor cursor = database.query(DBHelper.TABLE_MARKER, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(
                                cursor.getDouble(cursor.getColumnIndex(DBHelper.KEY_LATITUDE)),
                                cursor.getDouble(cursor.getColumnIndex(DBHelper.KEY_LONGITUDE)))
                        ).title(cursor.getString(cursor.getColumnIndex(DBHelper.KEY_NAME))));
                marker.setTag(cursor.getLong(cursor.getColumnIndex(DBHelper.KEY_ID)));
            } while (cursor.moveToNext());
        }
        cursor.close();

        // Двигаем камеру
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curMarker, 15));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("curMarker", curMarker);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        curMarker = savedInstanceState.getParcelable("curMarker");
    }
}
