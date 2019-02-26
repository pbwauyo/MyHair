package comro.example.nssf.martin.stylist;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
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

import comro.example.nssf.martin.R;
import comro.example.nssf.martin.dataModels.CurrentLocation;
import comro.example.nssf.martin.dataModels.Salon;

public class AddSalon extends AppCompatActivity {

    private EditText salonName, salonAddress;
    private Button addSalon;
    private Toolbar toolbar;
    private DatabaseReference databaseReference;
    private final int LOCATION_PERMISSION_REQUEST = 99;
    public static final String TAG = StylistSignUp.class.getSimpleName();
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final String message = "Permissions are mandatory to use this app";
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private CoordinatorLayout coordinatorLayout;
    private CurrentLocation currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_salon);

        salonName = findViewById(R.id.new_salon_name);
        salonAddress = findViewById(R.id.new_salon_address);
        addSalon = findViewById(R.id.add_new_salon);
        toolbar = findViewById(R.id.add_salon_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add salon");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        addSalon.setEnabled(false);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        coordinatorLayout = findViewById(R.id.coordinator_layout);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("stylists").child(userId);

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

        checkPermissions();

        addSalon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = salonName.getText().toString().trim();
                final String address = salonAddress.getText().toString().trim();
                final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("saloon_locations");


                if(addSalon.isEnabled()) {
                    //clear text
                    salonName.setText("");
                    salonAddress.setText("");

                    DatabaseReference salonDetailsRef = databaseReference.child("salons_details");
                    DatabaseReference salonIdsRef = FirebaseDatabase.getInstance().getReference();
                    String salonId = databaseReference.child("salons_details").push().getKey();

                    salonDetailsRef.child(salonId).setValue(new Salon(name, address, currentLocation.getLatitude(), currentLocation.getLongitude(), salonId));
                    databaseReference.child("salon_names").push().setValue(name);
                    salonIdsRef.child("salonIds").child(name).setValue(salonId);

                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getChildrenCount() != 0 ) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    if (!snapshot.getValue().toString().equals(address)) {
                                        ref.push().setValue(address);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                    fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
                }
                else{
                    createSnackBar("please enable location services first");
                }
            }
        });

    }


    public void onLocationChange(Location location) {
        currentLocation = new CurrentLocation(location.getLatitude(), location.getLongitude());
    }

    public  void checkPermissions(){
        // check if permission has not been granted
        if(ContextCompat.checkSelfPermission(AddSalon.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            //if not, explain to the user
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                createSnackBar(message);
            }
            // no need for explanation, just request permissions
            else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST);
            }
        }
        // permission already granted, therefore perform tasks
        else{
            addSalon.setEnabled(true);
            getLastLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST:
                // permission granted
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    addSalon.setEnabled(true);
                    getLastLocation();
                }
                // permission denied therefore, close application
                else{
                    createSnackBar(message);
                }
        }
    }

    private void createSnackBar(String message){
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, message, Snackbar.LENGTH_INDEFINITE)
                .setAction("Fix it", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(AddSalon.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                LOCATION_PERMISSION_REQUEST);
                    }
                });
        snackbar.show();
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

    @Override
    protected void onResume() {
        super.onResume();

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

}
