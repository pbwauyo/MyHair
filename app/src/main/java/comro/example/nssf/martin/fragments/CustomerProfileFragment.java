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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
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
import comro.example.nssf.martin.UtilityFunctions;
import me.relex.circleindicator.CircleIndicator;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CustomerProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CustomerProfileFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Toolbar toolbar;
    private ImageView profilePicView;
    private FloatingActionButton editPicFab;
    private TextView emailTxt, phoneNoTxt, locationTxt, nameTxt;
    private EditText editNameTxt;
    protected Location lastLocation;
    private String currentPhotoPath;
    private EditText editEmailTxt, editNumberTxt;

    CoordinatorLayout coordinatorLayout;
    String userId, dpUrl;
    DatabaseReference detailsRef, customersRef;
    private ProgressBar progressBar, arrowProgressBar;
    private final int REQUEST_TAKE_PHOTO = 1;
    private AddressResultReceiver resultReceiver;
    ViewPager viewPager;
    int currentPage = 0;
    private CircleIndicator circleIndicator;

    private Integer imageIds[] = {R.drawable.image_1, R.drawable.image_2, R.drawable.image_3, R.drawable.image_4,
                                                                        R.drawable.image_5, R.drawable.image_6};
    private ArrayList<Integer> recommendations = new ArrayList<>();

    private StorageReference profileImageRef;

    private ImageView confirmName, confirmEmail, confirmNumber, changeName, changeEmail, changeNumber, locationPin;

    private ImageView cancelName, cancelNumber, cancelEmail;

    public CustomerProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CustomerProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CustomerProfileFragment newInstance(String param1, String param2) {
        CustomerProfileFragment fragment = new CustomerProfileFragment();
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
        customersRef = FirebaseDatabase.getInstance().getReference().child("customers");
        resultReceiver = new AddressResultReceiver(new Handler());

        profileImageRef = FirebaseStorage.getInstance().getReference().child("customer_profile_pictures").child(userId);

        recommendations.addAll(Arrays.asList(imageIds));

        detailsRef.child("location").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    double latitude = Double.parseDouble(dataSnapshot.child("latitude").getValue().toString());
                    double longitude = Double.parseDouble(dataSnapshot.child("longitude").getValue().toString());

                    // configure location
                    Location location = new Location("customer location");
                    location.setLatitude(latitude);
                    location.setLongitude(longitude);

                    lastLocation = location;
                    startIntentService();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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
        progressBar = view.findViewById(R.id.progress_bar);
        arrowProgressBar = view.findViewById(R.id.arrowProgressBar);

        locationPin = view.findViewById(R.id.location_pin);

        editNameTxt = view.findViewById(R.id.edit_customer_profile_name);
        editEmailTxt = view.findViewById(R.id.edit_customer_profile_email);
        editNumberTxt = view.findViewById(R.id.edit_customer_profile_number);

        changeName = view.findViewById(R.id.change_name);
        changeEmail = view.findViewById(R.id.change_email);
        changeNumber = view.findViewById(R.id.change_number);

        confirmName = view.findViewById(R.id.done_edit_name);
        confirmEmail = view.findViewById(R.id.done_edit_email);
        confirmNumber = view.findViewById(R.id.done_edit_number);

        cancelName = view.findViewById(R.id.cancel_edit_name);
        cancelEmail = view.findViewById(R.id.cancel_edit_email);
        cancelNumber = view.findViewById(R.id.cancel_edit_number);

        coordinatorLayout = view.findViewById(R.id.coordinator_layout);

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

        changeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameTxt.setVisibility(View.GONE);
                editNameTxt.setVisibility(View.VISIBLE);
                changeName.setVisibility(View.GONE);
                confirmName.setVisibility(View.VISIBLE);
                cancelName.setVisibility(View.VISIBLE);
            }
        });

        confirmName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = editNameTxt.getText().toString();

                if(!newName.isEmpty()) {
                    editNameTxt.setVisibility(View.GONE);
                    nameTxt.setVisibility(View.VISIBLE);
                    confirmName.setVisibility(View.GONE);
                    changeName.setVisibility(View.VISIBLE);
                    cancelName.setVisibility(View.GONE);

                    detailsRef.child("name").setValue(newName);
                }
                else {
                    Snackbar.make(coordinatorLayout, "Please input a name", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        cancelName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editNameTxt.setVisibility(View.GONE);
                nameTxt.setVisibility(View.VISIBLE);
                confirmName.setVisibility(View.GONE);
                changeName.setVisibility(View.VISIBLE);
                cancelName.setVisibility(View.GONE);
            }
        });

        changeNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNoTxt.setVisibility(View.GONE);
                editNumberTxt.setVisibility(View.VISIBLE);
                changeNumber.setVisibility(View.GONE);
                confirmNumber.setVisibility(View.VISIBLE);
                cancelNumber.setVisibility(View.VISIBLE);
            }
        });

        confirmNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newNumber = editNumberTxt.getText().toString();
                if(!newNumber.isEmpty()) {
                    editNumberTxt.setVisibility(View.GONE);
                    phoneNoTxt.setVisibility(View.VISIBLE);
                    confirmNumber.setVisibility(View.GONE);
                    changeNumber.setVisibility(View.VISIBLE);
                    cancelNumber.setVisibility(View.GONE);

                    detailsRef.child("contact").setValue(newNumber);
                }
                else {
                    Snackbar.make(coordinatorLayout, "Please input a number", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        cancelNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editNumberTxt.setVisibility(View.GONE);
                phoneNoTxt.setVisibility(View.VISIBLE);
                confirmNumber.setVisibility(View.GONE);
                changeNumber.setVisibility(View.VISIBLE);
                cancelNumber.setVisibility(View.GONE);
            }
        });

        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailTxt.setVisibility(View.GONE);
                editEmailTxt.setVisibility(View.VISIBLE);
                changeEmail.setVisibility(View.GONE);
                confirmEmail.setVisibility(View.VISIBLE);
                cancelEmail.setVisibility(View.VISIBLE);
            }
        });

        confirmEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newEmail = editEmailTxt.getText().toString();

                if(!newEmail.isEmpty()) {
                    editEmailTxt.setVisibility(View.GONE);
                    emailTxt.setVisibility(View.VISIBLE);
                    confirmEmail.setVisibility(View.GONE);
                    changeEmail.setVisibility(View.VISIBLE);
                    cancelEmail.setVisibility(View.GONE);

                    detailsRef.child("email").setValue(newEmail);
                }
                else {
                    Snackbar.make(coordinatorLayout, "Please input an email", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        cancelEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editEmailTxt.setVisibility(View.GONE);
                emailTxt.setVisibility(View.VISIBLE);
                confirmEmail.setVisibility(View.GONE);
                changeEmail.setVisibility(View.VISIBLE);
                cancelEmail.setVisibility(View.GONE);
            }
        });

        locationPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(coordinatorLayout, "Your location", Snackbar.LENGTH_SHORT).show();
            }
        });

        editPicFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        detailsRef.child("imageUrl").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    String imageUrl = dataSnapshot.getValue().toString();
                    if (!imageUrl.equals("")) {
                        arrowProgressBar.setVisibility(View.VISIBLE);
                        //load profile picture into profile page ImageView
                        Picasso.get()
                                .load(imageUrl)
                                .fit()
                                .centerCrop()
                                .rotate(90)
                                .into(profilePicView, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        arrowProgressBar.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onError(Exception e) {

                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        detailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                Snackbar.make(coordinatorLayout, ex.getMessage(), Snackbar.LENGTH_LONG).show();

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity(),
                        "comro.example.nssf.martin",
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
            Uri uri = Uri.fromFile(file);
            UtilityFunctions.uploadImage(profileImageRef, uri, coordinatorLayout, progressBar, customersRef, userId);
        }
    }

}
