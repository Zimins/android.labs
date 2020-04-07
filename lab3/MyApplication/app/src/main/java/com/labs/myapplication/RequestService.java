package com.labs.myapplication;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

class RequestService {

    private static final String API_PREFIX = "http://192.168.100.6:8080/";

    static List<MarkerDto> getMarkers() throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(API_PREFIX + "markers");
        HttpResponse response = client.execute(request);

        JSONArray markers = new JSONArray(EntityUtils.toString(response.getEntity(), "UTF-8"));

        List<MarkerDto> markerDtos = new ArrayList<>();

        for (int i = 0; i < markers.length(); i++) {
            JSONObject marker = markers.getJSONObject(i);
            markerDtos.add(new MarkerDto(
                    Long.valueOf((Integer) marker.get("id")),
                    (String) marker.get("name"),
                    (Double) marker.get("latitude"),
                    (Double) marker.get("longitude")
            ));
        }
        
        return markerDtos;
    }

    static Long addMarker(String name, Double latitude, Double longitude) {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(API_PREFIX + "marker/add");

        try {
            List<NameValuePair> params = new ArrayList<>(1);
            params.add(new BasicNameValuePair("name", name));
            params.add(new BasicNameValuePair("latitude", String.valueOf(latitude)));
            params.add(new BasicNameValuePair("longitude", String.valueOf(longitude)));

            post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            HttpResponse response = client.execute(post);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            String line = "";
            while ((line = rd.readLine()) != null) {
                return Long.parseLong(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0L;
    }

    static List<String> getUriImages(long idMarker) throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(API_PREFIX + "photos?markerId=" + idMarker);
        HttpResponse response = client.execute(request);

        JSONArray markers = new JSONArray(EntityUtils.toString(response.getEntity(), "UTF-8"));

        List<String> uriImages = new ArrayList<>();

        for (int i = 0; i < markers.length(); i++) {
            JSONObject marker = markers.getJSONObject(i);
            uriImages.add((String) marker.get("uri"));
        }

        return uriImages;
    }

    static Long addImage(Long idMarker, String uri) {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(API_PREFIX + "photo/add");

        try {
            List<NameValuePair> params = new ArrayList<>(1);
            params.add(new BasicNameValuePair("markerId", String.valueOf(idMarker)));
            params.add(new BasicNameValuePair("uri", String.valueOf(uri)));

            post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            HttpResponse response = client.execute(post);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            String line = "";
            while ((line = rd.readLine()) != null) {
                return Long.parseLong(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0L;
    }
}
