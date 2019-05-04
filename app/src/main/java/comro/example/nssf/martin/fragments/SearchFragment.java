package comro.example.nssf.martin.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.support.v7.widget.Toolbar;

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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import comro.example.nssf.martin.R;
import comro.example.nssf.martin.StylistsMapView;
import comro.example.nssf.martin.dataModels.CurrentLocation;
import comro.example.nssf.martin.dataModels.Style;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Button searchButton;
    private Spinner genderSpinner,  pricesSpinner;
    private DatabaseReference ref;
    private EditText stylesTxt, locationTxt;
    private StorageReference storageReference;
    private FirebaseAuth auth;
    private String userId;
    private Toolbar toolbar;
    private CoordinatorLayout coordinatorLayout;
    private static final int PERMISSIONS_REQUEST = 1;
    private SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private CurrentLocation currentLocation;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private final int LOCATION_PERMISSION_REQUEST = 99;
    private FusedLocationProviderClient fusedLocationProviderClient;


    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        ref = FirebaseDatabase.getInstance().getReference().child("styles");
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();


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

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        checkPermissions();

//        LocationManager lm = (LocationManager) (getActivity()).getSystemService(LOCATION_SERVICE);

        storageReference = FirebaseStorage.getInstance().getReference().child(userId).child("style_image");


//        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            Toast.makeText(getActivity(), "Please enable location services", Toast.LENGTH_SHORT).show();
////            getActivity().finish();
//        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_style, container, false);

        searchButton = view.findViewById(R.id.searchBtton);
        genderSpinner = view.findViewById(R.id.gender_spinner);
        stylesTxt = view.findViewById(R.id.styles_txt);
        pricesSpinner = view.findViewById(R.id.prices_spinner);
        locationTxt = view.findViewById(R.id.location_txt);
        coordinatorLayout = view.findViewById(R.id.search_coordinator_layout);
        toolbar = view.findViewById(R.id.search_toolbar);

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)(getActivity())).getSupportActionBar().setTitle("Search Style");

        DrawerLayout drawer = getActivity().findViewById(R.id.drawer);

        //create hamburger menu icon and set listener to it
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawer.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        genderSpinner.setOnItemSelectedListener (new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected (AdapterView<?> adapterView, View view, int i, long l) {
                String femalePriceRanges[] = getResources().getStringArray(R.array.price_ranges_female);
                String malePriceRanges[] = getResources().getStringArray(R.array.price_ranges_male);

                ArrayAdapter<String> adapter;

                switch (adapterView.getItemAtPosition(i).toString()){
                    case "male":
                        // attaching data adapter to spinner
                        adapter  = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, malePriceRanges);
                        // Drop down layout style - list view with radio button
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        pricesSpinner.setAdapter(adapter);
                        break;

                    case "female":
                        // attaching data adapter to spinner
                        adapter  = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, femalePriceRanges);
                        // Drop down layout style - list view with radio button
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        pricesSpinner.setAdapter(adapter);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        final int[] count = {0};
        final DatabaseReference[] locationsRef = {FirebaseDatabase.getInstance().getReference().child("stylists")};
        searchButton.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String selectedGender = genderSpinner.getSelectedItem().toString().trim();
                final String selectedStyle = stylesTxt.getText().toString().trim();
                final String selectedPrice = pricesSpinner.getSelectedItem().toString().trim();
                final String location = locationTxt.getText().toString().trim();

                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final ArrayList<Style> arrayList = new ArrayList<>();
                        final Bundle[] bundle = {new Bundle()};
                        final Iterable<DataSnapshot> customerStyles = dataSnapshot.getChildren();

                        for(DataSnapshot ds: customerStyles){
                            final String gender = ds.child("styleGender").getValue().toString();
                            final String style = ds.child("styleName").getValue().toString();
                            final String price = ds.child("price_range").getValue().toString();
                            final String imageUrl = ds.child("imageUrl").getValue().toString();
                            final String id = ds.child("stylistId").getValue().toString();
                            final String salonId = ds.child("salonId").getValue().toString();

                            //if location is input
                            if(!location.equals("")) {
                                Log.d("input location", location + " " + selectedGender + "= " + gender + " " + selectedStyle + "= " + style + " " + selectedPrice + " " + price);
                                if (selectedGender.equals(gender) && selectedStyle.equals(style) && selectedPrice.equals(price)) {
                                    locationsRef[0].addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            if (dataSnapshot.child(id).child("salons_details").child(salonId).child("address").getValue().toString().contains(location)) {
                                                Log.d("address", dataSnapshot.child(id).child("salons_details").child(salonId).child("address").getValue().toString() + " " + location);
                                                count[0]++;
                                                Style snapshotStyle = new Style(style, gender, price, imageUrl, id, salonId);
                                                arrayList.add(snapshotStyle);

                                                saveArrayList(arrayList,"list");
                                                editor.putInt("count", count[0]);
                                                editor.commit();
                                            }

                                            Log.d("count in interface", String.valueOf(count[0]));

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

                                                saveArrayList(arrayList,"list");
                                                editor.putInt("count", count[0]);
                                                editor.commit();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                        }

                        int newCount = sharedPreferences.getInt("count",0);

                        Log.d("count in interface", String.valueOf(newCount));
                        //if there's no record
                        if(!(newCount > 0)){
                            editor.clear().commit();
                            Snackbar.make(coordinatorLayout, "Style doesn't exist", Snackbar.LENGTH_LONG).show();
                        }
                        else{
                            Intent intent = new Intent(getActivity(), StylistsMapView.class);
                            ArrayList<Style> styles = getArrayList("list");
                            editor.clear().commit();
                            bundle[0].putSerializable("styles",  styles);
                            intent.putExtras(bundle[0]);
                            startActivity(intent);
                            getActivity().finish();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }


    public void saveArrayList (ArrayList<Style> arrayList, String key){
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString(key, json);
        editor.commit();
    }

    public ArrayList<Style> getArrayList(String key){
        Gson gson = new Gson();
        String list = sharedPreferences.getString(key, null);
        Type type = new TypeToken<ArrayList<Style>>(){}.getType();
        return gson.fromJson(list, type);
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
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                LOCATION_PERMISSION_REQUEST);
                    }
                });
        snackbar.show();
    }


    public  void checkPermissions(){
        // check if permission has not been granted
        if(ContextCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            //if not, explain to the user
            if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)){
                createSnackBar("location is needed please");
            }
            // no need for explanation, just request permissions
            else{
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
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
    public void onStop() {
        super.onStop();
        fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    public void onPause() {
        super.onPause();
        fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

}
