package comro.example.nssf.martin.customer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import comro.example.nssf.martin.R;
import comro.example.nssf.martin.TrackerService;
import comro.example.nssf.martin.dataModels.CurrentLocation;
import comro.example.nssf.martin.dataModels.Style;
import comro.example.nssf.martin.stylist.AddSalon;

public class SearchStyle extends AppCompatActivity {
    private Button searchButton;
    private Spinner genderSpinner,  pricesSpinner;
    private DatabaseReference ref;
    private EditText stylesTxt, locationTxt;
    private StorageReference storageReference;
    private FirebaseAuth auth;
    private String userId;
    private CurrentLocation currentLocation;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private final int LOCATION_PERMISSION_REQUEST = 99;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_style);

        ref = FirebaseDatabase.getInstance().getReference().child("styles");
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();

        coordinatorLayout = findViewById(R.id.search_style_coordinator_layout);
        searchButton =findViewById(R.id.searchBtton);
        genderSpinner = findViewById(R.id.gender_spinner);
        stylesTxt = findViewById(R.id.styles_txt);
        pricesSpinner = findViewById(R.id.prices_spinner);
        locationTxt = findViewById(R.id.location_txt);

        // create location call back
        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult){
                Location location = locationResult.getLastLocation();
                onLocationChange(location);
            }
        };

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

//        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        checkPermissions();

        storageReference = FirebaseStorage.getInstance().getReference().child(userId).child("style_image");

        genderSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String femalePriceRanges[] = getResources().getStringArray(R.array.price_ranges_female);
                String malePriceRanges[] = getResources().getStringArray(R.array.price_ranges_male);

                ArrayAdapter<String> adapter;

                switch (adapterView.getItemAtPosition(i).toString()){
                    case "male":
                        // attaching data adapter to spinner
                        adapter  = new ArrayAdapter<>(SearchStyle.this, android.R.layout.simple_spinner_item, malePriceRanges);
                        // Drop down layout style - list view with radio button
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        pricesSpinner.setAdapter(adapter);
                        break;

                    case "female":
                        // attaching data adapter to spinner
                        adapter  = new ArrayAdapter<>(SearchStyle.this, android.R.layout.simple_spinner_item, femalePriceRanges);
                        // Drop down layout style - list view with radio button
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        pricesSpinner.setAdapter(adapter);
                        break;
                }
            }
        });


//        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show();
//            finish();
//        }

        searchButton.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String selectedGender = genderSpinner.getSelectedItem().toString();
                final String selectedStyle = stylesTxt.getText().toString();
                final String selectedPrice = pricesSpinner.getSelectedItem().toString();
                final String location = locationTxt.getText().toString();

                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        final String  style, price, imageUrl, id, fileExt;
                        final ArrayList<Style> arrayList = new ArrayList<>();
                        //Style snapshotStyle;
                        final Bundle[] bundle = {new Bundle()};
                        final Iterable<DataSnapshot> customerStyles = dataSnapshot.getChildren();
                        final DatabaseReference[] locationsRef = {FirebaseDatabase.getInstance().getReference().child("stylists")};
                        final int[] count = {0};

                        for(DataSnapshot ds: customerStyles){
                            final String gender = ds.child("styleGender").getValue().toString();
                            final String style = ds.child("styleName").getValue().toString();
                            final String price = ds.child("price_range").getValue().toString();
                            final String imageUrl = ds.child("imageUrl").getValue().toString();
                            final String id = ds.child("stylistId").getValue().toString();
                            final String salonId = ds.child("salonId").getValue().toString();

                            //if location is input
                            if(!location.equals("")) {
                                if (selectedGender.equals(gender) && selectedStyle.equals(style) && selectedPrice.equals(price)) {

                                    locationsRef[0].addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Log.d("to string()", dataSnapshot.child(id).getValue().toString()  + " " +
                                                    dataSnapshot.child(id).getValue().toString().contains(location));

                                            if(dataSnapshot.child(id).child("salons_details").child(salonId).child("address").getValue().toString().contains(location)){
                                                count[0]++;
                                                Style snapshotStyle = new Style(style, gender, price, imageUrl, id, salonId);
                                                arrayList.add(snapshotStyle);
                                            }

//                                            if (dataSnapshot.child(id).child("salons_details").child(salonId).getValue().toString().contains(location)) {
//                                                count[0]++;
//                                                Style snapshotStyle = new Style(style, gender, price, imageUrl, id, salonId);
//                                                arrayList.add(snapshotStyle);
//                                                Log.d("in if", style + " " + gender + " " + price);
//                                                Log.d("selected in if", selectedGender + " " + selectedStyle + " " + selectedPrice);
//                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                            // if location is not input
                            else {
                                if (selectedGender.equals(gender) && selectedStyle.equals(style) && selectedPrice.equals(price)) {

                                    locationsRef[0].addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            double latitude = Double.parseDouble(dataSnapshot.child(id).child("salons_details").child(salonId).child("latitude").getValue().toString());
                                            double longitude = Double.parseDouble(dataSnapshot.child(id).child("salons_details").child(salonId).child("longitude").getValue().toString());
                                            double distanceBetween;

                                            //start point
                                            Location start = new Location("start");
                                            start.setLatitude(currentLocation.getLatitude());
                                            start.setLongitude(currentLocation.getLongitude());

                                            //stop point
                                            Location stop = new Location("stop");
                                            stop.setLatitude(latitude);
                                            stop.setLongitude(longitude);

                                            //distance between
                                            distanceBetween = start.distanceTo(stop);

                                            if(distanceBetween <= 50000){
                                                count[0]++;
                                                Style snapshotStyle = new Style(style, gender, price, imageUrl, id, salonId);
                                                arrayList.add(snapshotStyle);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                        }

                        //if there's no record
                        if(!(count[0] >0)){
                            Toast.makeText(SearchStyle.this,"Style doesn't exist", Toast.LENGTH_LONG).show();
                        }
                        else{
                            Intent intent = new Intent(SearchStyle.this, StylistsMapView.class);
                            bundle[0].putSerializable("styles",  arrayList);
                            intent.putExtras(bundle[0]);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }

                });


            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST:
                // permission granted
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //addSalon.setEnabled(true);
                    getLastLocation();
                }
                // permission denied therefore, close application
                else{
                    createSnackBar("please allow request permissions");
                }
        }

    }

//    private void startTrackerService() {
//        startService(new Intent(this, TrackerService.class));
//    }

    public void onLocationChange(Location location) {
        currentLocation = new CurrentLocation(location.getLatitude(), location.getLongitude());
    }

    @SuppressLint("MissingPermission")
    public void getLastLocation(){
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful()){
                    Location location = task.getResult();
                    if(location == null){
                        try {
                            fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                        }
                        catch (SecurityException e){
                            createSnackBar(e.getMessage());
                        }
                    }
                    else{
                        onLocationChange(location);
                    }
                }
                else{
                    createSnackBar("unable to get current location");
                }

            }
        });
    }

    private void createSnackBar(String message){
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, message, Snackbar.LENGTH_INDEFINITE)
                .setAction("Fix it", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(SearchStyle.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                LOCATION_PERMISSION_REQUEST);
                    }
                });
        snackbar.show();
    }


    public  void checkPermissions(){
        // check if permission has not been granted
        if(ContextCompat.checkSelfPermission(SearchStyle.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            //if not, explain to the user
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                createSnackBar("location is needed please");
            }
            // no need for explanation, just request permissions
            else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST);
            }
        }
        // permission already granted, therefore perform tasks
        else{
//            addSalon.setEnabled(true);
            getLastLocation();
        }
    }

    // life cycle methods
    @Override
    protected void onStop() {
        super.onStop();
        fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

}
