package com.labs.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.util.Pair;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    // Карта
    private GoogleMap mMap;

    // Последний выбранный/добавленный маркер (для сдвига камеры)
    LatLng curMarker;

    // Класс для работы с БД
    DBHelper dbHelper;
    SQLiteDatabase database;

    LocationManager locationManager;

    Circle cRadius;
    GroundOverlay goPerson;

    List<Marker> markers;
    SeekBar seekRadius;
    ImageButton btnFindMe;

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Запрашиваем права на доступ к местоположению
        requestPermissions(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        }, 1);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Для открытия камеры
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        // Для отправки HTTP запросов
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Экземпляр класса DBHelper
        dbHelper = new DBHelper(this);

        // Управление БД
        database = dbHelper.getWritableDatabase();

        markers = new ArrayList<>();

        seekRadius = findViewById(R.id.seekRadius);
        btnFindMe = findViewById(R.id.btnFindMe);

        seekRadius.setMin(10);
        seekRadius.setMax(500);
        seekRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Toast.makeText(getApplicationContext(), "Радиус обнаружения: " + progress + " м.",
                        Toast.LENGTH_SHORT).show();
                cRadius.setRadius(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        btnFindMe.setOnClickListener(v -> {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cRadius.getCenter(), 20));
        });
    }

    @SuppressLint({"MissingPermission", "NewApi"})
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Обрабатываем клик на карту
        mMap.setOnMapClickListener(latLng -> {

            // Диалог с запросом имени
            Dialog dialog = new Dialog(MapsActivity.this);
            dialog.setContentView(R.layout.dialog_request_name);
            dialog.setCancelable(false);

            Button btnSave = dialog.findViewById(R.id.btnSave);
            Button btnCancel = dialog.findViewById(R.id.btnCancel);
            TextInputEditText inpName = dialog.findViewById(R.id.inpName);
            CheckBox cbIsPublic = dialog.findViewById(R.id.cbIsPublic);

            // Обработка нажатия на кнопку "сохранить"
            btnSave.setOnClickListener(v -> {

                Marker newMarker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(String.valueOf(inpName.getText())));
                curMarker = newMarker.getPosition();

                Long id = 0L;

                if (cbIsPublic.isChecked()) {
                    id = RequestService.addMarker(newMarker.getTitle(),
                            newMarker.getPosition().latitude, newMarker.getPosition().longitude);
                } else {
                    ContentValues contentValues = new ContentValues();

                    contentValues.put(DBHelper.KEY_NAME, newMarker.getTitle());
                    contentValues.put(DBHelper.KEY_LATITUDE, newMarker.getPosition().latitude);
                    contentValues.put(DBHelper.KEY_LONGITUDE, newMarker.getPosition().longitude);
                    id = database.insert(DBHelper.TABLE_MARKER, null, contentValues);
                }

                newMarker.setTag(new Pair(cbIsPublic.isChecked(), id));
                markers.add(newMarker);

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

            Pair<Boolean, Long> tag = (Pair<Boolean, Long>) marker.getTag();

            intent.putExtra("idMarker", tag.second);
            intent.putExtra("title", marker.getTitle());
            intent.putExtra("isPublic", tag.first);

            curMarker = marker.getPosition();
            startActivityForResult(intent, 0);
            return true;
        });

        // Если уже использовались координаты GPS
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        LatLng curPos = location != null
                ? new LatLng(location.getLatitude(), location.getLongitude())
                : new LatLng(58, 58);

        // Основная иконка (я)
        goPerson = mMap.addGroundOverlay(new GroundOverlayOptions()
                .position(curPos, 2)
                .image(BitmapDescriptorFactory
                        .fromBitmap(Bitmap.createBitmap(
                                BitmapFactory.decodeResource(getBaseContext().getResources(),
                                        R.drawable.person)))));

        // Радиус обнаружения маркеров
        cRadius = mMap.addCircle(new CircleOptions()
                .center(curPos)
                .radius(10)
                .fillColor(Color.argb((float) 0.1, 0, 180, 240))
                .strokeColor(Color.argb((float) 0.1, 0, 0, 240))
        );

        // Добавляем маркеры из локальной БД на карту
        Cursor cursor = database.query(DBHelper.TABLE_MARKER, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(
                                cursor.getDouble(cursor.getColumnIndex(DBHelper.KEY_LATITUDE)),
                                cursor.getDouble(cursor.getColumnIndex(DBHelper.KEY_LONGITUDE)))
                        ).title(cursor.getString(cursor.getColumnIndex(DBHelper.KEY_NAME))));
                marker.setTag(new Pair(false, cursor.getLong(cursor.getColumnIndex(DBHelper.KEY_ID))));
                markers.add(marker);
            } while (cursor.moveToNext());
        }
        cursor.close();

        // Добавляем маркеры с сервера
        try {
            for (MarkerDto markerDto : RequestService.getMarkers()) {
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(
                                markerDto.getLatitude(), markerDto.getLongitude())
                        ).title(markerDto.getName()));
                marker.setTag(new Pair(true, markerDto.getId()));
                markers.add(marker);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Подключаем слушателя
        while (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) { }
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000,
                1,
                locationListener
        );
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Отключаем слушателя
        locationManager.removeUpdates(locationListener);
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

    private LocationListener locationListener = new LocationListener() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onLocationChanged(Location location) {
            LatLng curPos = new LatLng(location.getLatitude(), location.getLongitude());

            cRadius.setCenter(curPos);
            goPerson.setPosition(curPos);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(cRadius.getCenter()));

            // Смотрим, не приближаемся ли мы к какому либо маркеру
            for (Marker marker : markers) {
                double dist = SphericalUtil.computeDistanceBetween(marker.getPosition(), goPerson.getPosition());

                // Если приблизились к какому-либо маркеру, выдаем сообщение
                if (dist <= cRadius.getRadius()) {
                    Toast.makeText(getApplicationContext(),
                            String.format("Вы близки к \"%s\"\nРасстояние по прямой: %d м.",
                                    marker.getTitle(), (int) dist),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (LocationManager.GPS_PROVIDER.equals(provider)) {
                Log.i("GPS", "Статус GPS: " + status);
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            if (LocationManager.GPS_PROVIDER.equals(provider)) {
                Toast.makeText(getApplicationContext(),
                        "GPS включен",
                        Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            if (LocationManager.GPS_PROVIDER.equals(provider)) {
                Toast.makeText(getApplicationContext(),
                        "Включите GPS...",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };
}
