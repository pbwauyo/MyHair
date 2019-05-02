package comro.example.nssf.martin.stylist;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

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
import comro.example.nssf.martin.fragments.RegisterStyleFragment;
import comro.example.nssf.martin.fragments.StylistHomePageFragment;
import de.hdodenhof.circleimageview.CircleImageView;

public class StylistMainPage extends AppCompatActivity {
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private View navHeader;
    private Toolbar toolbar;
    private CircleImageView profilePicture;
    private TextView name;
    private RatingBar ratingBar;

    private int navItemIndex = 0;

    private DatabaseReference detailsRef;

    private final String TAG_PROFILE = "Profile";
    private final String TAG_REGISTER_STYLE= "Register Style";
   // private final String TAG_EDIT_STYLE = "Edit Style";
    //private final String TAG_ADD_SALON = "Add Salon";
    private String CURRENT_TAG = TAG_PROFILE;
    private final String PRIVACY_POLICY = "This application may only be used for the intended purposes";
    private final String ABOUT_US = "Kitiyo Martin\nNamuyomba Angella\nAtuhaire Diana";
    private AlertDialog.Builder alertDialog;
    private AlertDialog dialog;
    private CoordinatorLayout coordinatorLayout;
    private FirebaseAuth auth;
    private String userId;
    private FirebaseAuth.AuthStateListener authStateListener;

    private Handler mHandler;
    private String[] activities;

    private String userName;
    private String dpUrl;
    private float rating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stylist_main_page);
        FirebaseMessaging.getInstance().subscribeToTopic("hairRequests");
        FirebaseMessaging.getInstance().unsubscribeFromTopic("hairRequestReplies");

//        toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        navHeader = navigationView.getHeaderView(0);

        name = navHeader.findViewById(R.id.s_name);
        ratingBar = navHeader.findViewById(R.id.rating_bar);
        profilePicture = navHeader.findViewById(R.id.stylist_profile_picture);

        auth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (auth.getCurrentUser() == null) {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("hairRequests");
                    startActivity(new Intent(StylistMainPage.this, Login.class));
                    finish();
                }
            }
        };
        auth.addAuthStateListener(authStateListener);

        userId = auth.getCurrentUser().getUid();

        detailsRef = FirebaseDatabase.getInstance().getReference().child("stylists").child(userId);

        activities = getResources().getStringArray(R.array.activities);

        navigationView = findViewById(R.id.s_nav_view);
        mHandler = new Handler();
        drawerLayout = findViewById(R.id.drawer_layout);

        if (savedInstanceState == null) {
            navItemIndex = 0;
            CURRENT_TAG = TAG_PROFILE;
            loadFragment();
        }
//        coordinatorLayout = findViewById(R.id.coordinator_layout);
        //initialise alert dialog
        alertDialog = new AlertDialog.Builder(this);

    }

    @Override
    public void onStart(){
        super.onStart();
        loadNavHeader();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.nav_profile:
                        navItemIndex = 0;
                        CURRENT_TAG = TAG_PROFILE;
                        break;

                    case R.id.nav_add_salon:
                        navItemIndex = 1;
                        // CURRENT_TAG = TAG_ADD_SALON;
                        break;

                    case R.id.nav_register_style:
                        navItemIndex = 2;
                        CURRENT_TAG = TAG_REGISTER_STYLE;
                        break;

                    case R.id.viewBookings:
                        navItemIndex = 3;
                        break;

                    case R.id.nav_about_us:
                        navItemIndex = 4;
                        break;

                    case R.id.nav_privacy_policy:
                        navItemIndex = 5;
                        break;

                    case R.id.nav_sign_out:
                        navItemIndex = 6;
                        break;
                }

                if (menuItem.isChecked()){
                    menuItem.setChecked(false);
                }
                else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);

                // determine whether to load fragment or show dialog
                switch (navItemIndex){
                    case 1:
                        drawerLayout.closeDrawers();
                        startActivity(new Intent(StylistMainPage.this, AddSalon.class));
                        break;

                    case 3:
                        //start view bookings activity
                        break;

                    case 4:
                        drawerLayout.closeDrawers();
                        dialog = createAlertDialog(alertDialog, "Developers", ABOUT_US);
                        dialog.show();
                        break;

                    case 5:
                        drawerLayout.closeDrawers();
                        dialog = createAlertDialog(alertDialog, "Privacy policy", PRIVACY_POLICY);
                        dialog.show();
                        break;

                    case 6:
                        drawerLayout.closeDrawers();
                        signOut();
                        break;

                    default:
                        loadFragment();
                }
                return true;
            }
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.openDrawer, R.string.closeDrawer){
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    void loadNavHeader(){

        detailsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userName = dataSnapshot.child("name").getValue().toString();

                //only set rating if it exists
                if(dataSnapshot.child("rating").exists()){
                    rating = Float.parseFloat((dataSnapshot.child("rating").getValue().toString()));
                    ratingBar.setRating(rating);
                }

                //only set profile picture if it exists
                if(dataSnapshot.child("imageUrl").exists()){
                    dpUrl = dataSnapshot.child("imageUrl").getValue().toString();
                    Picasso.get()
                            .load(dpUrl)
                            .centerCrop()
                            .fit()
                            .into(profilePicture);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private Fragment getDisplayFragment(){
        switch(navItemIndex){
            case 0:
                return new StylistHomePageFragment();
            case 2:
                return new RegisterStyleFragment();
            default:
                return new StylistHomePageFragment();
        }
    }

    private void loadFragment(){
//        getSupportActionBar().setTitle(activities[navItemIndex]);

        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG)!=null){
            drawerLayout.closeDrawers();
        }

        Runnable pendingRunnable = new Runnable() {
            @Override
            public void run() {
//                coordinatorLayout.setVisibility(View.GONE);

                Fragment fragment = getDisplayFragment();
//                toolbar = fragment.getView().findViewById(R.id.stylist_toolbar);
//                setSupportActionBar(toolbar);
//                getSupportActionBar().setTitle(activities[navItemIndex]);


                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.stylist_frame, fragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };

        if(pendingRunnable!=null){
            mHandler.post(pendingRunnable);
        }

        drawerLayout.closeDrawers();

    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
            return;
        }

        boolean shouldLoadHomeFragOnBackPress = true;
        if (shouldLoadHomeFragOnBackPress) {
            if (navItemIndex != 0) {
                navItemIndex = 0;
                CURRENT_TAG = TAG_PROFILE;
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
}
