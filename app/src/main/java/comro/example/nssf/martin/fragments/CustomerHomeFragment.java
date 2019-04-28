package comro.example.nssf.martin.fragments;


import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import comro.example.nssf.martin.Constants;
import comro.example.nssf.martin.FetchAddressIntentService;
import comro.example.nssf.martin.ImageAdapter;
import comro.example.nssf.martin.R;
import me.relex.circleindicator.CircleIndicator;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CustomerHomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CustomerHomeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Toolbar toolbar;
    private ImageView profilePicView, editName;
    private FloatingActionButton editPicFab;
    private TextView emailTxt, phoneNoTxt, locationTxt, nameTxt;
    private EditText newNameTxt;
    protected Location lastLocation;
    private String currentPhotoPath;
    CoordinatorLayout coordinatorLayout;
    String userId;
    DatabaseReference detailsRef;
    private final int REQUEST_TAKE_PHOTO = 1;
    private AddressResultReceiver resultReceiver;
    ViewPager viewPager;
    int currentPage = 0;
    CircleIndicator circleIndicator;

    private Integer imageIds[] = {R.drawable.image_1, R.drawable.image_2, R.drawable.image_3, R.drawable.image_4,
                                                                        R.drawable.image_5, R.drawable.image_6};
    private ArrayList<Integer> recommendations = new ArrayList<>();

    public CustomerHomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CustomerHomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CustomerHomeFragment newInstance(String param1, String param2) {
        CustomerHomeFragment fragment = new CustomerHomeFragment();
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
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        detailsRef = FirebaseDatabase.getInstance().getReference().child("customers").child(userId);
        resultReceiver = new AddressResultReceiver(new Handler());

        recommendations.addAll(Arrays.asList(imageIds));

//        detailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                double latitude = Double.parseDouble(dataSnapshot.child("location").child("latitude").getValue().toString());
//                double longitude = Double.parseDouble(dataSnapshot.child("location").child("longitude").getValue().toString());
//
//                // configure location
//                Location location = new Location("customer location");
//                location.setLatitude(latitude);
//                location.setLongitude(longitude);
//
//                lastLocation = location;
//                startIntentService();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_customer_home, container, false);

        toolbar = view.findViewById(R.id.customer_toolbar);
        profilePicView = view.findViewById(R.id.customer_profile_pic);
        editPicFab = view.findViewById(R.id.customer_fab);
        nameTxt = view.findViewById(R.id.customer_profile_name);
        emailTxt = view.findViewById(R.id.customer_profile_email);
        phoneNoTxt = view.findViewById(R.id.customer_profile_number);
        locationTxt = view.findViewById(R.id.customer_profile_location);
        editName = view.findViewById(R.id.edit_name);
        newNameTxt = view.findViewById(R.id.edit_customer_profile_name);
        coordinatorLayout = view.findViewById(R.id.coordinator_layout);

        editName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameTxt.setVisibility(View.GONE);
                newNameTxt.setVisibility(View.VISIBLE);
            }
        });

        editPicFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        detailsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String email = dataSnapshot.child("email").getValue().toString();
                String phoneNo = dataSnapshot.child("contact").getValue().toString();

                //set views
                nameTxt.setText(name);
                emailTxt.setText(email);
                phoneNoTxt.setText(phoneNo);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)(getActivity())).getSupportActionBar().setTitle("Home");

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

        init(view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    protected void startIntentService() {
        Intent intent = new Intent(getActivity(), FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, resultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, lastLocation);
        getActivity().startService(intent);
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultData == null) {
                return;
            }

            // Display the address string
            // or an error message sent from the intent service.
            String addressOutput = resultData.getString(Constants.RESULT_DATA_KEY);

            if (addressOutput == null) {
                addressOutput = "";
            }

            if (resultCode == Constants.SUCCESS_RESULT) {
                String resultsArray[] = TextUtils.split(addressOutput, System.getProperty("line.separator"));
                locationTxt.setText(resultsArray[0]);
            }

        }
    }

    public void init(View view){

        viewPager = view.findViewById(R.id.customer_view_pager);
        circleIndicator = view.findViewById(R.id.indicator);

        viewPager.setAdapter(new ImageAdapter(getActivity(), recommendations));
        circleIndicator.setViewPager(viewPager);

        // Auto start of viewpager
        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            public void run() {
                if (currentPage == imageIds.length) {
                    currentPage = 0;
                }
                viewPager.setCurrentItem(currentPage++, true);
            }
        };
        Timer swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(Update);
            }
        }, 2500, 2500);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Snackbar.make(coordinatorLayout, ex.getMessage(), Snackbar.LENGTH_LONG);

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity(),
                        "comro.example.nssf.martin.fragments",
                        photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);

            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {

            File file = new File(currentPhotoPath);
            Picasso.get()
                    .load(Uri.fromFile(file))
                    .fit()
                    .centerCrop()
                    .into(profilePicView);
        }
    }

}
