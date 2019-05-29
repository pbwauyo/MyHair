package comro.example.nssf.martin.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.Toolbar;
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
import java.util.Date;

import comro.example.nssf.martin.R;
import comro.example.nssf.martin.UtilityFunctions;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StylistHomePageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StylistHomePageFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Toolbar toolbar;
    private ImageView profilePic;
    private TextView nameTxt, emailTxt, phoneNoTxt, locationTxt;
    private String name, email, phoneNo, location;
    private FloatingActionButton editProfilePic;
    private String currentPhotoPath;
    private CoordinatorLayout coordinatorLayout;
    FirebaseAuth auth;
    FirebaseStorage firebaseStorage;
    private DatabaseReference databaseReference, stylistsRef;
    private ProgressBar progressBar, arrowProgressBar;
    private final int REQUEST_TAKE_PHOTO = 1;
    private String userId;
    private StorageReference profileImageRef;
    //ArrayList<String> results = new ArrayList<>();
    private String dpUrl;

    public StylistHomePageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StylistHomePageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StylistHomePageFragment newInstance(String param1, String param2) {
        StylistHomePageFragment fragment = new StylistHomePageFragment();
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


        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("stylists").child(userId);
        stylistsRef = FirebaseDatabase.getInstance().getReference().child("stylists");
        profileImageRef = FirebaseStorage.getInstance().getReference().child("stylist_profile_pictures");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stylist_home_page, container, false);

        profilePic = view.findViewById(R.id.profile_pic);
        nameTxt = view.findViewById(R.id.s_profile_name);
        emailTxt = view.findViewById(R.id.s_profile_email);
        locationTxt = view.findViewById(R.id.s_profile_location);
        phoneNoTxt = view.findViewById(R.id.s_profile_number);
        toolbar = view.findViewById(R.id.stylist_toolbar);
        coordinatorLayout = view.findViewById(R.id.coordinator_layout);
        progressBar = view.findViewById(R.id.progress_bar);
        arrowProgressBar = view.findViewById(R.id.arrowProgressBar);
        editProfilePic = view.findViewById(R.id.s_fab);

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)(getActivity())).getSupportActionBar().setTitle("Home");


        DrawerLayout drawer = getActivity().findViewById(R.id.drawer_layout);

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

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                name = dataSnapshot.child("name").getValue().toString();
                email = dataSnapshot.child("email").getValue().toString();
                //location = dataSnapshot.child("locationTxt").getValue().toString();
                phoneNo = dataSnapshot.child("contact").getValue().toString();

                nameTxt.setText(name);
                emailTxt.setText(email);
               // locationTxt.setText(location);
                phoneNoTxt.setText(phoneNo);

                //only set image if image url exists
//                if(dataSnapshot.child("imageUrl").exists()){
//                    dpUrl = dataSnapshot.child("imageUrl").getValue().toString();
//                    Picasso.get()
//                            .load(dpUrl)
//                            .centerCrop()
//                            .fit()
//                            .into(profilePic);
//                }

               // Log.d("first_name", results.get(0) + results.get(1) + results.get(2) + results.get(3));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        Log.d("nameTxt", results.get(0) + results.get(1) + results.get(2) + results.get(3));

        editProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        databaseReference.child("imageUrl").addValueEventListener(new ValueEventListener() {
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
                                .into(profilePic, new Callback() {
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
            UtilityFunctions.uploadProfileImage(profileImageRef, uri, coordinatorLayout, progressBar, stylistsRef, userId);
        }
    }

}
