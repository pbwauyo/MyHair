package comro.example.nssf.martin.customer;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import comro.example.nssf.martin.InfoWindowAdapter;
import comro.example.nssf.martin.R;
import comro.example.nssf.martin.dataModels.Style;

public class StylistsMapView extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Toolbar toolbar;
    private ArrayList<Style> arrayList;
    HashMap<String, Marker> markers = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stylists_map_view);
        Bundle bundle = getIntent().getExtras();
        arrayList = (ArrayList<Style>) bundle.getSerializable("styles");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        toolbar = findViewById(R.id.map_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        DatabaseReference locationsRef = FirebaseDatabase.getInstance().getReference().child("stylists");
        final LatLng[] location = new LatLng[1];
        final MarkerOptions markerOptions = new MarkerOptions();

        locationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(Style style : arrayList){
                    double latitude = Double.parseDouble(dataSnapshot.child(style.getId()).child("saloon_details").child(style.getSalonId()).child("latitude").toString());
                    double longitude = Double.parseDouble(dataSnapshot.child(style.getId()).child("saloon_details").child(style.getSalonId()).child("longitude").toString());
                    String styleImage = style.getimage();
                    String salonName = dataSnapshot.child(style.getId()).child("saloon_details").child(style.getSalonId()).child("name").toString();
                    String stylistName = dataSnapshot.child(style.getId()).child("name").toString();
                    String name = style.getName();
                    location[0] = new LatLng(latitude, longitude);

//                    markerOptions.alpha(0.7f);
//                    markerOptions.title(name);
                    markerOptions.position(location[0]);
                    markers.put(name,mMap.addMarker(markerOptions));

                    mMap.setInfoWindowAdapter(new InfoWindowAdapter(getApplicationContext(), stylistName, salonName, styleImage));
                }



                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Marker marker : markers.values()) {
                    builder.include(marker.getPosition());
                }
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
