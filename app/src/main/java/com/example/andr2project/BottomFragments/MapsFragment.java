package com.example.andr2project.BottomFragments;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.andr2project.R;
import com.example.andr2project.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MapsFragment extends Fragment implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private Marker m;
    private FirebaseUser user;
    private DatabaseReference reference;
    private FirebaseFirestore db;
    private Marker z;
    private LatLng currentloc = new LatLng(52.0209, 5.2069);

    private List<Marker> waypoints;
    private List<Marker> userLocations;


    public MapsFragment() {
        // require a empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_maps, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.db = FirebaseFirestore.getInstance();
//        binding = ActivityMapsBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.


        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

//        SupportMapFragment mapFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);

        LocationManager lm = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);


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
        //Grab current location and set marker of current location of user

        m = mMap.addMarker(new MarkerOptions().position(currentloc).title("Your location"));
        System.out.println(currentloc);
        System.out.println("YOUVE BEEN HERE XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentloc));


        //Get User UID

        //Push current location to database of user  THIS NEEDS TO BE LATER CHANGED TO USING UID OF USER TO UPDATE LOC
        DocumentReference docRef = db.collection("Users").document("tDdXFeWj0JP9jkbfBmsS");
        GeoPoint gp = new GeoPoint(currentloc.latitude, currentloc.longitude);
        docRef.update("location", gp);



        //users
        db.collection("Users")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@com.google.firebase.database.annotations.Nullable QuerySnapshot value,
                                        @com.google.firebase.database.annotations.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            System.out.println("Error listening.");
                            return;
                        }

                        db.collection("Users").get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if(task.isSuccessful()){

                                            List<MarkerOptions> nearbyMarkers = new ArrayList<MarkerOptions>();
                                            for(QueryDocumentSnapshot document : task.getResult()){
                                                GeoPoint usersGeoLocation = document.get("location", GeoPoint.class);
                                                LatLng usersLocation = new LatLng(usersGeoLocation.getLatitude(), usersGeoLocation.getLongitude());
                                                String usersName = document.get("name", String.class);

                                                if(!usersName.equals("currentuser")) {
                                                    System.out.println(document.get("name"));
                                                    MarkerOptions markerOptions = new MarkerOptions().position(usersLocation).title(usersName);
                                                    nearbyMarkers.add(markerOptions);
                                                }

                                            }
                                            for(MarkerOptions a : nearbyMarkers){
                                                mMap.addMarker(a);
                                            }
                                        }
                                        else{
                                            System.out.println("ERROR");
                                        }
                                    }
                                });
                    }
                });








        //idk

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("waypoints")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                LatLng location = document.get("/location", LatLng.class);
                                String team = document.get("/owner", String.class);
                                String title = document.getString("/name");
                                System.out.println(team);
                                Marker marker = null;
                                switch (team) {
                                    case "Blue":
                                        marker = mMap.addMarker(new MarkerOptions().position(location).title(title).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                                        break;
                                    case "Red":
                                        marker = mMap.addMarker(new MarkerOptions().position(location).title(title).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                                        break;
                                    case "Yellow":
                                        marker = mMap.addMarker(new MarkerOptions().position(location).title(title).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                                        break;
                                    default:
                                        marker = mMap.addMarker(new MarkerOptions().position(location).title(title).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                        break;
                                }
                            }
                        } else {
                            System.out.println("Error getting data from firestore");
                        }
                    }
                });
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        /*
        if(m == null){
            m = this.mMap.addMarker(new MarkerOptions().position(new LatLng(0,0)));
        }
        m.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
        //Push current location to database of user  THIS NEEDS TO BE LATER CHANGED TO USING UID OF USER TO UPDATE LOC
        DocumentReference docRef = db.collection("Users").document("tDdXFeWj0JP9jkbfBmsS");
        GeoPoint gp = new GeoPoint(location.getLatitude(), location.getLongitude());
        docRef.update("location", gp);
        currentloc = new LatLng(location.getLatitude(), location.getLongitude());
        */
    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        LocationListener.super.onLocationChanged(locations);
    }

    @Override
    public void onFlushComplete(int requestCode) {
        LocationListener.super.onFlushComplete(requestCode);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }

    public Boolean isInRange(LatLng l, GeoPoint a){

        LatLng minrangeloc = new LatLng(l.latitude - 0.1 ,l.longitude - 1);
        LatLng maxrangeloc = new LatLng(l.latitude + 0.1 ,l.longitude + 1);
        System.out.println("*****************************************************************");
        System.out.println(minrangeloc);
        System.out.println(maxrangeloc);
        GeoPoint mingp = new GeoPoint(minrangeloc.latitude,minrangeloc.longitude);
        GeoPoint maxgp = new GeoPoint(maxrangeloc.latitude, maxrangeloc.longitude);
        if(a.getLatitude() < maxgp.getLatitude() && a.getLongitude() < maxgp.getLongitude() || a.getLatitude() > mingp.getLatitude() && a.getLongitude() > mingp.getLongitude()){
            return true;
        }
        return false;

    }
}
