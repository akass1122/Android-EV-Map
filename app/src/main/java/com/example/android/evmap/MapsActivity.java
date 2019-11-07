package com.example.android.evmap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;

import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


//import android.location.LocationListener;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.android.libraries.places.api.Places;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import android.location.Address;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {


    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastlocation;
    private Marker currentLocationmMarker;
    public static final int REQUEST_LOCATION_CODE = 99;
    int PROXIMITY_RADIUS = 50000;
    double latitude,longitude;
    double latFromAddress = -33.87365;
    double lngFromAddress = 151.20689;
    String EVcharging = "EV+charging+stations";
    EditText tf_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MapActivityAA", "AAAAAAA");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps1);


        tf_location =  findViewById(R.id.TF_location);

        String apiKey = getString(R.string.google_maps_key);
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission();

        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode)
        {
            case REQUEST_LOCATION_CODE:
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=  PackageManager.PERMISSION_GRANTED)
                    {
                        if(client == null)
                        {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else
                {
                    Toast.makeText(this,"Permission Denied" , Toast.LENGTH_LONG).show();
                }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }
    protected synchronized void buildGoogleApiClient() {
        client = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        client.connect();
    }

    @Override
    public void onLocationChanged(Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        lastlocation = location;
        if(currentLocationmMarker != null)
        {
            currentLocationmMarker.remove();

        }
        Log.d("lat = ",""+latitude);
        LatLng latLng = new LatLng(location.getLatitude() , location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentLocationmMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(10));

        if(client != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(client,this);
        }
    }

    public void onClick(View v)
    {
        Object dataTransfer[] = new Object[2];
        GetAndShowNearbyPlaces getAndShowNearbyPlaces = new GetAndShowNearbyPlaces();
        String url;

        switch(v.getId())
        {

            case R.id.B_main_EVcharging:
                mMap.clear();

                //String EVcharging = "EV+charging+stations";
                url = buildURLforBusinessSearch(latitude, longitude, EVcharging);


                dataTransfer[0] = mMap;
                dataTransfer[1] = url;

                getAndShowNearbyPlaces.execute(dataTransfer);
                Toast.makeText(MapsActivity.this, "Showing Nearby EV charging", Toast.LENGTH_SHORT).show();



                break;
//            case R.id.B_schools:
//                mMap.clear();
//                String school = "school";
//                url = buildURLforKeywordSearch(latitude, longitude, school);
//                dataTransfer[0] = mMap;
//                dataTransfer[1] = url;
//
//                getAndShowNearbyPlaces.execute(dataTransfer);
//                Toast.makeText(MapsActivity.this, "Showing Nearby Schools", Toast.LENGTH_SHORT).show();
//                break;

            case R.id.B_search: // GEOCODER: show location based on entered location
                mMap.clear();

                tf_location =  findViewById(R.id.TF_location);
                String location = tf_location.getText().toString();
                List<Address> addressList;


                if(!location.equals(""))
                {
                    Geocoder geocoder = new Geocoder(this);

                    try {
                        addressList = geocoder.getFromLocationName(location, 5);

                        if(addressList != null && addressList.size() >= 1)
                        {
                            latFromAddress = addressList.get(0).getLatitude();
                            lngFromAddress = addressList.get(0).getLongitude();
                            LatLng latLng = new LatLng(latFromAddress , lngFromAddress);
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(latLng);
                                markerOptions.title(location);
                                mMap.addMarker(markerOptions);
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
                        }
                        //=====================================================
                        url = buildURLforBusinessSearch(latFromAddress, lngFromAddress, EVcharging);


                        dataTransfer[0] = mMap;
                        dataTransfer[1] = url;

                        getAndShowNearbyPlaces.execute(dataTransfer);
                        Toast.makeText(MapsActivity.this, "Showing EV charging stations near entered location", Toast.LENGTH_SHORT).show();
                       //======================================================
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;


        }
    }


    private String buildURLforKeywordSearch(double latitude , double longitude , String searchStr)
    {
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location="+latitude+","+longitude);

        googlePlaceUrl.append("&radius="+PROXIMITY_RADIUS);
        googlePlaceUrl.append("&keyword="+searchStr);
        googlePlaceUrl.append("&sensor=true");
        //googlePlaceUrl.append("&key="+"AIzaSyBLEPBRfw7sMb73Mr88L91Jqh3tuE4mKsE");
        googlePlaceUrl.append("&key="+"AIzaSyCf0eLTEerAe9pzbB-mFWLe_LifjQRhEoA");
        Log.d("MapsActivity_GET_URL", "url = "+googlePlaceUrl.toString());
        return googlePlaceUrl.toString();
    }
    private String buildURLforBusinessSearch(double latitude , double longitude , String searchStr)
    {
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location="+latitude+","+longitude);
        googlePlaceUrl.append("&radius="+PROXIMITY_RADIUS);
        googlePlaceUrl.append("&keyword="+searchStr);
        googlePlaceUrl.append("&opennow=true");
        googlePlaceUrl.append("&key=AIzaSyCf0eLTEerAe9pzbB-mFWLe_LifjQRhEoA");
        Log.d("MapsActivity_EVcharging", "url = "+googlePlaceUrl.toString());
        return googlePlaceUrl.toString();
    }

    private String buildURLforTextSearch(double latitude , double longitude , String searchStr)
    {
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location="+latitude+","+longitude);
        googlePlaceUrl.append("&radius="+PROXIMITY_RADIUS);
        googlePlaceUrl.append("&textsearch="+searchStr);
        googlePlaceUrl.append("&sensor=true");
        //googlePlaceUrl.append("&key="+"AIzaSyBLEPBRfw7sMb73Mr88L91Jqh3tuE4mKsE");
        googlePlaceUrl.append("&key="+"AIzaSyCf0eLTEerAe9pzbB-mFWLe_LifjQRhEoA");
        Log.d("MapsActivity_GET_URL", "url = "+googlePlaceUrl.toString());
        return googlePlaceUrl.toString();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(100);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }
    }


    public boolean checkLocationPermission()
    {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)  != PackageManager.PERMISSION_GRANTED )
        {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION },REQUEST_LOCATION_CODE);
            }
            else
            {
                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION },REQUEST_LOCATION_CODE);
            }
            return false;

        }
        else
            return true;
    }


    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }
    private final class GetAndShowNearbyPlaces extends AsyncTask<Object, String, String> {
        private String googlePlacesData;
        private GoogleMap mMap;
        String url;

        @Override
        protected String doInBackground(Object... objects){
            mMap = (GoogleMap)objects[0];
            url = (String)objects[1];

            LoadDataViaURL loadDataViaURL = new LoadDataViaURL();
            try {
                googlePlacesData = loadDataViaURL.loadDataString(url);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return googlePlacesData; // a string
        }

        @Override
        protected void onPostExecute(String s){

            List<HashMap<String, String>> nearbyPlaceList;
            JsonToHashMapParser parser = new JsonToHashMapParser();
            nearbyPlaceList = parser.parse(s);
            Log.d("NearbyPlacesList","Got List<HashMap>");
            showNearbyPlaces(nearbyPlaceList);
        }

        private void showNearbyPlaces(List<HashMap<String, String>> nearbyPlaceList)


        {
            for(int i = 0; i < nearbyPlaceList.size(); i++)
            {
                MarkerOptions markerOptions = new MarkerOptions();
                HashMap<String, String> googlePlace = nearbyPlaceList.get(i);

                String placeName = googlePlace.get("place_name");
                String vicinity = googlePlace.get("vicinity");
                double lat = Double.parseDouble( googlePlace.get("lat"));
                double lng = Double.parseDouble( googlePlace.get("lng"));
                double rating = Double.parseDouble(googlePlace.get("rating"));
                String place_id = googlePlace.get("place_id");
                //String reference = googlePlace.get("reference");
                LatLng latLng = new LatLng( lat, lng);
                markerOptions.position(latLng);
                markerOptions.title(placeName + " : "+ vicinity);
                markerOptions.snippet("Open now. Rating: "+ rating + "Place id: "+ place_id);
                if (rating >= 4.5) {
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

                } else {
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                }
                Marker myMarker = mMap.addMarker(markerOptions);
                myMarker.setTag(googlePlace);

                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(10));


                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

                    @Override
                    public void onInfoWindowClick(Marker marker) {

                        Log.d("InfoClick", "LatLong: "+ marker.getPosition() );
                        double lat = marker.getPosition().latitude;
                        double lng = marker.getPosition().longitude;

                     Intent intent = new Intent(MapsActivity.this, com.example.android.evmap.StationActivity.class);
                    HashMap<String, String> curPlaceHashMap = (HashMap<String, String>) marker.getTag();

                    //intent.putExtra("place_id", place_id);

                    intent.putExtra("vicinity", curPlaceHashMap.get("vicinity"));
                        intent.putExtra("place_id", curPlaceHashMap.get("place_id"));
                        //intent.putExtra("lat", curPlaceHashMap.get("lat"));
                        //intent.putExtra("lng", curPlaceHashMap.get("lng"));
                        intent.putExtra("place_name", curPlaceHashMap.get("place_name"));
                        Log.d("MAPS_ACT_ Name: ", curPlaceHashMap.get("place_name"));
                        Log.d("MAPS_ACT_ Vicinity: ", curPlaceHashMap.get("vicinity"));
                        intent.putExtra("rating", curPlaceHashMap.get("rating"));
                    intent.putExtra("lat", lat);
                    intent.putExtra("lng", lng);
//                    intent.putExtra("place_name", placeName);
//                    intent.putExtra("rating", rating);
//                        intent.putExtra("vicinity", vicinity);

                    startActivity(intent);
                    }
                });


            }
        }
    }


}
