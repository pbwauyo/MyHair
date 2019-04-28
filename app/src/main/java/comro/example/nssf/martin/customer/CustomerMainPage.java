package comro.example.nssf.martin.customer;

import android.content.DialogInterface;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toolbar;

import comro.example.nssf.martin.R;
import comro.example.nssf.martin.fragments.CustomerHomeFragment;
import comro.example.nssf.martin.fragments.SearchFragment;

public class CustomerMainPage extends AppCompatActivity {
    private NavigationView navigationView;
    private FrameLayout frameLayout;
    private String TAG_HOME = "CustomerHome";
    private String TAG_SEARCH = "SearchStyle";
    private String TAG_VIEW_ALL = "ViewAll";
    private String TAG_NOTIFS = "Notifications";
    private String TAG_CHATS = "Chats";
   // private String TAG_
    private DrawerLayout drawer;
    private String CURRENT_TAG = TAG_HOME;
    private int navItemIndex = 0;
    private Handler mHandler;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_main_page);

        navigationView = findViewById(R.id.customer_nav_view);
        frameLayout = findViewById(R.id.customer_frame);
        drawer = findViewById(R.id.drawer);
        mHandler = new Handler();

        if (savedInstanceState == null) {
            navItemIndex = 0;
            CURRENT_TAG = TAG_HOME;
            loadFragment();
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.customer_home:
                        navItemIndex = 0;
                        CURRENT_TAG = TAG_HOME;
                        break;

                    case R.id.search_style:
                        navItemIndex = 1;
                        CURRENT_TAG = TAG_SEARCH;
                        break;

                    case R.id.view_all:
                        navItemIndex = 2;
                        CURRENT_TAG = TAG_VIEW_ALL;
                        break;

                    case R.id.customer_notifs:
                        navItemIndex = 3;
                        CURRENT_TAG = TAG_NOTIFS;
                        break;

                    case R.id.customer_chats:
                        navItemIndex = 4;
                        CURRENT_TAG = TAG_CHATS;
                        break;

                    case R.id.privacy_policy:
                        navItemIndex = 5;
                        break;

                    case R.id.about_us:
                        navItemIndex = 6;
                        break;

                    case R.id.settings:
                        navItemIndex = 7;
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
                return new CustomerHomeFragment();
            case 1:
                return  new SearchFragment();
            default:
                return new CustomerHomeFragment();
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


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
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
