package comro.example.nssf.martin.customer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

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
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import comro.example.nssf.martin.Login;
import comro.example.nssf.martin.R;
import comro.example.nssf.martin.dataModels.CurrentLocation;
import comro.example.nssf.martin.fragments.CustomerProfileFragment;
import comro.example.nssf.martin.fragments.SearchFragment;
import de.hdodenhof.circleimageview.CircleImageView;

public class CustomerMainPage extends AppCompatActivity {
    private NavigationView navigationView;
    private FrameLayout frameLayout;
    private String TAG_PROFILE = "Customer Home";
    private String TAG_SEARCH = "Search Style";
//    private String TAG_REQUESTS = "Hair Requests";
    private final String ABOUT_US = "Atuhaire Diana\nNamuyomba Angella\nKitiyo Martin";
    private final String PRIVACY_POLICY = "This application may only be used for the intended purposes";
   // private String TAG_
    private DrawerLayout drawer;
    private String CURRENT_TAG = TAG_SEARCH;
    private int navItemIndex = 1;
    private Handler mHandler;
    private Toolbar toolbar;
    //AlertDialog dialog;
    private FirebaseAuth auth;
    private AlertDialog.Builder builder;
    private FirebaseAuth.AuthStateListener authStateListener;
    private DatabaseReference detailsRef;
    private String userId;

    private View navHeader;
    private TextView nameTxt;
    private CircleImageView profilePic;

    private String dpUrl, name;

    private CurrentLocation currentLocation;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private final int LOCATION_PERMISSION_REQUEST = 99;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_main_page);
        FirebaseMessaging.getInstance().unsubscribeFromTopic("hairRequests");
        FirebaseMessaging.getInstance().subscribeToTopic("hairRequestReplies");

        navigationView = findViewById(R.id.customer_nav_view);
        navHeader = navigationView.getHeaderView(0);
        nameTxt = navHeader.findViewById(R.id.nav_name);
        profilePic = navHeader.findViewById(R.id.nav_image);

        frameLayout = findViewById(R.id.customer_frame);
        drawer = findViewById(R.id.drawer);
        mHandler = new Handler();
        builder = new AlertDialog.Builder(this);

        auth = FirebaseAuth.getInstance();

        if (savedInstanceState == null) {
            navItemIndex = 1;
            CURRENT_TAG = TAG_SEARCH;
            loadFragment();
        }

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(auth.getCurrentUser() == null){
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("hairRequestReplies");
                    startActivity(new Intent(CustomerMainPage.this, Login.class));
                    finish();
                }
            }
        };

        auth.addAuthStateListener(authStateListener);

        userId = auth.getCurrentUser().getUid();
        detailsRef = FirebaseDatabase.getInstance().getReference().child("customers").child(userId);
    }

    @Override
    public void onStart(){
        super.onStart();
        loadNavHeader();

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

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.customer_profile:
                        navItemIndex = 0;
                        CURRENT_TAG = TAG_PROFILE;
                        break;

                    case R.id.search_style:
                        navItemIndex = 1;
                        CURRENT_TAG = TAG_SEARCH;
                        break;

                    case R.id.customer_requests:
                        navItemIndex = 2;
                        break;

                    case R.id.privacy_policy:
                        navItemIndex = 3;
                        break;

                    case R.id.about_us:
                        navItemIndex = 4;
                        break;

                    case R.id.sign_out:
                        navItemIndex = 5;
                        break;
                }

                if (menuItem.isChecked()){
                    menuItem.setChecked(false);
                }
                else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);

                switch (navItemIndex){
                    case 0:
                        loadFragment();
                        break;
                    case 1:
                        loadFragment();
                        break;
                    case 2:
                        break;
                    case 3:
                        drawer.closeDrawers();
                        createAlertDialog(builder, "Privacy policy", PRIVACY_POLICY).show();
                        break;
                    case 4:
                        drawer.closeDrawers();
                        createAlertDialog(builder, "Developers", ABOUT_US).show();
                        break;
                    case 5:
                        drawer.closeDrawers();
                        signOut();
                        break;
                }

                return true;
            }

        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, R.string.openDrawer, R.string.closeDrawer){
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        drawer.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }


    public Fragment getDisplayFragment(){
        switch (navItemIndex){
            case 0:
                return new CustomerProfileFragment();
            case 1:
                return  new SearchFragment();
            default:
                return new CustomerProfileFragment();
        }
    }

    public void loadFragment(){
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG)!=null){
            drawer.closeDrawers();
        }

        Runnable pendingRunnable = new Runnable() {
            @Override
            public void run() {

                Fragment fragment = getDisplayFragment();

                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.customer_frame, fragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };

        if(pendingRunnable!=null){
            mHandler.post(pendingRunnable);
        }

        drawer.closeDrawers();
    }

    public void loadNavHeader(){
        detailsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                name = dataSnapshot.child("name").getValue().toString();
                nameTxt.setText(name);

                //only set image if image url exists
                if(dataSnapshot.child("imageUrl").exists()){
                    dpUrl = dataSnapshot.child("imageUrl").getValue().toString();
                    Picasso.get()
                            .load(dpUrl)
                            .centerCrop()
                            .fit()
                            .into(profilePic);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }

        boolean shouldLoadHomeFragOnBackPress = true;
        if (shouldLoadHomeFragOnBackPress) {
            if (navItemIndex != 1) {
                navItemIndex = 1;
                CURRENT_TAG = TAG_SEARCH;
                loadFragment();
                return;
            }
        }

        super.onBackPressed();
    }

    private AlertDialog createAlertDialog(AlertDialog.Builder builder, String title, String message){
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        return builder.create();
    }

    private void signOut(){
        auth.signOut();
    }

    public  void checkPermissions(){
        // check if permission has not been granted
        if(ContextCompat.checkSelfPermission(this,
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

    public void onLocationChange(Location location) {
        currentLocation = new CurrentLocation(location.getLatitude(), location.getLongitude());
        detailsRef.child("location").setValue(currentLocation);
    }

    @SuppressLint("MissingPermission")
    public void getLastLocation(){
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
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

    private void createSnackBar(String message){
        Snackbar snackbar = Snackbar
                .make(drawer, message, Snackbar.LENGTH_LONG)
                .setAction("Fix it", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(CustomerMainPage.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                LOCATION_PERMISSION_REQUEST);
                    }
                });
        snackbar.show();
    }

}
