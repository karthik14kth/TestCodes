package com.example.karthik.myapplication;

import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
// ****for old one****
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
//new imports
import java.util.ArrayList;
import android.location.LocationManager;
import android.location.Criteria;
import android.graphics.Color;
import com.google.android.gms.maps.model.PolylineOptions;
import org.w3c.dom.Document;
import com.google.android.gms.maps.model.LatLng;





//import com.google.android.gms.;



//Add marker package
import com.google.android.gms.maps.model.Marker;

// import org.w3c.dom.Document;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

//*****old one here****
   private GoogleMap mMap;

    //****commented above two methods*******
//This class just initializes the map

    //@Override
    //protected void onCreate(Bundle savedInstanceState) {
       // super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        //SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
         //       .findFragmentById(R.id.map);
       // mapFragment.getMapAsync(this);
      //  }

    //This method is to find where your location current location is and tries to mark it
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        mMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        mMap.setMyLocationEnabled(true);

        //not sure of ROMA

        Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(37.7750, 122.4183)).title("San Francisco").snippet("Nice Place").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_cast_light)));



    }


//*****commented above two methods*****

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    //*****commented old one*****

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }


}
