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
import android.widget.ImageView;
import android.widget.TextView;

import comro.example.nssf.martin.R;
import comro.example.nssf.martin.fragments.RegisterStyleFragment;
import comro.example.nssf.martin.fragments.StylistHomePageFragment;

public class StylistMainPage extends AppCompatActivity {
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private View navHeader;
    private Toolbar toolbar;
    private ImageView profilePicture;
    private TextView name;

    private int navItemIndex = 0;

    private final String TAG_HOME = "Home";
    private final String TAG_REGISTER_STYLE= "RegisterStyle";
    private final String TAG_EDIT_STYLE = "EditStyle";
    private final String TAG_VIEW_BOOKINGS = "ViewBookings";
    private final String TAG_VIEW_HISTORY = "ViewHistory";
    private String CURRENT_TAG = TAG_HOME;
    private final String privacyPolicy = "This is our privacy policy";
    private final String aboutUs = "Kitiyo Martin\nNamuyomba Angella\nAtuhaire Diana";
    private AlertDialog.Builder alertDialog;
    private AlertDialog dialog;
    private CoordinatorLayout coordinatorLayout;

    private Handler mHandler;
    private String[] activities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stylist_main_page);

//        toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        activities = getResources().getStringArray(R.array.activities);

        navigationView = findViewById(R.id.s_nav_view);
        mHandler = new Handler();
        drawerLayout = findViewById(R.id.drawer_layout);

        if (savedInstanceState == null) {
            navItemIndex = 0;
            CURRENT_TAG = TAG_HOME;
            loadFragment();
        }
//        coordinatorLayout = findViewById(R.id.coordinator_layout);
        //initialise alert dialog
        alertDialog = new AlertDialog.Builder(this);



        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.nav_home:
                        navItemIndex = 0;
                        CURRENT_TAG = TAG_HOME;
                        break;

                    case R.id.nav_add_salon:
                        navItemIndex = 1;
                        CURRENT_TAG = "AddSalon";
                        break;

                    case R.id.nav_register_style:
                        navItemIndex = 2;
                        CURRENT_TAG = TAG_REGISTER_STYLE;
                        break;

                    case R.id.nav_edit_style:
                        navItemIndex = 3;
                        CURRENT_TAG = TAG_EDIT_STYLE;
                        break;

                    case R.id.viewBookings:
                        navItemIndex = 4;
                        CURRENT_TAG = TAG_VIEW_BOOKINGS;
                        break;

                    case R.id.nav_view_history:
                        navItemIndex = 5;
                        CURRENT_TAG = TAG_VIEW_HISTORY;
                        break;

                    case R.id.nav_settings:
                        navItemIndex = 6;
                        break;

                    case R.id.nav_about_us:
                        navItemIndex = 7;
                        break;

                    case R.id.nav_privacy_policy:
                        navItemIndex = 8;
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
                    case 7:
                        drawerLayout.closeDrawers();
                        dialog = createAlertDialog(alertDialog, aboutUs);
                        dialog.show();
                        break;
                    case 8:
                        drawerLayout.closeDrawers();
                        dialog = createAlertDialog(alertDialog, privacyPolicy);
                        dialog.show();
                        break;
                    default:
                        loadFragment();
                }
                return true;
            }
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer){
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
                CURRENT_TAG = TAG_HOME;
                loadFragment();
                return;
            }
        }

        super.onBackPressed();
    }

    private AlertDialog createAlertDialog(AlertDialog.Builder builder, String message){
        builder.setTitle("Developers");
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        return builder.create();
    }

}
