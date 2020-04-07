package com.labs.myapplication;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;

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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Map<LatLng, String> mapMarkers = new HashMap<>();
    LatLng curMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        curMarker = new LatLng(58.0076424, 56.18747043);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(latLng -> {
            Marker newMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(latLng.toString()));
            mapMarkers.put(newMarker.getPosition(), "");
            curMarker = newMarker.getPosition();
        });

        mMap.setOnMarkerClickListener(marker -> {
            Intent intent = new Intent(getBaseContext(), MarkerActivity.class);
            intent.putExtra("title", marker.getTitle());
            intent.putExtra("oldUri", mapMarkers.get(marker.getPosition()));

            curMarker = marker.getPosition();
            startActivityForResult(intent, 0);
            return true;
        });

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curMarker, 15));

        for (LatLng m : mapMarkers.keySet()) {
            mMap.addMarker(new MarkerOptions().position(m).title(m.toString()));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == 1) {
            String uri = intent.getStringExtra("uri");
            mapMarkers.put(curMarker, uri);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("markers", (Serializable) mapMarkers);
        outState.putParcelable("curMarker", curMarker);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        mapMarkers = (Map<LatLng, String>) savedInstanceState.getSerializable("markers");
        curMarker = savedInstanceState.getParcelable("curMarker");
    }
}
