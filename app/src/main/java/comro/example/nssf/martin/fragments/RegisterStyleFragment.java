package comro.example.nssf.martin.fragments;


import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;

import comro.example.nssf.martin.R;
import comro.example.nssf.martin.dataModels.StylistInformation;
import comro.example.nssf.martin.stylist.StyleDetails;
import comro.example.nssf.martin.stylist.StylistMainPage;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegisterStyleFragment#newInstance} factory method to
 * create an instance of this fragments.
 */
public class RegisterStyleFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragments initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    EditText styleNameTxt, costStyleTxt;
    Spinner styleGender, salonNameSpinner;
    FirebaseDatabase database;
    Button styleButtonsave;
    private String style_name, salon_name, cost, style_gender;
    FirebaseAuth auth;
    DatabaseReference ref;
    ImageView editPhoto, stylePhoto;
    final int REQUEST_IMAGE_CAPTURE = 1;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    DatabaseReference databaseReference;
    String userId, fileExtension;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;
    private Toolbar toolbar;


    public RegisterStyleFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragments using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragments RegisterStyleFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RegisterStyleFragment newInstance(String param1, String param2) {
        RegisterStyleFragment fragment = new RegisterStyleFragment();
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
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReferenceFromUrl("gs://prototype-5625e.appspot.com/").child(userId).child("style_images");
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragments
        View view = inflater.inflate(R.layout.fragment_register_style, container, false);

        editPhoto = view.findViewById(R.id.editImage);
        stylePhoto = view.findViewById(R.id.imageView);
        styleNameTxt= view.findViewById(R.id.style_name);
        salonNameSpinner = view.findViewById(R.id.salonName);
        costStyleTxt = view.findViewById(R.id.costStyle);
        styleButtonsave = view.findViewById(R.id.styleButtonSave);
        styleGender = view.findViewById(R.id.style_gender_spinner);
        toolbar = view.findViewById(R.id.fragment_toolbar);

        DatabaseReference salonNamesRef = FirebaseDatabase.getInstance().getReference().child("stylists").child(userId).child("salon_names");
        final ArrayList<String> salonNames = new ArrayList<>();
        salonNamesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayAdapter<String> adapter;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    salonNames.add(snapshot.getValue().toString());
                }

                adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, salonNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                salonNameSpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ((AppCompatActivity)(getActivity())).setSupportActionBar(toolbar);
        ((AppCompatActivity)(getActivity())).getSupportActionBar().setTitle("Register style");

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



        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == StyleDetails.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            stylePhoto.setImageBitmap(imageBitmap);
        } else if (requestCode == PICK_IMAGE_REQUEST && resultCode == StyleDetails.RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            fileExtension = getFileExtension(filePath);

            Log.d("uri tostring()", filePath.getPath());
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                stylePhoto.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    public String getFileExtension(Uri uri) {
        ContentResolver cR = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadImage(final StorageReference storageRef, final DatabaseReference databaseReference, final String styleName, final String salonName, final String gender, final String styleCost, final String id) {

        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            final StorageReference imageRef = storageRef.child(styleName.concat("." + fileExtension));
            UploadTask uploadTask = imageRef.putFile(filePath);
            Log.d("file extension", styleName.concat("." + fileExtension));
            Log.d("storage reference", storageRef.getPath());

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    Log.d("then", "execute then() started....");
                    if (!task.isSuccessful()) {
                        Log.d("taskUpload", task.getException().getMessage());
                        Toast.makeText(getActivity(), "task upload unsuccessful: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        throw task.getException();
                    }

                    return imageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        final String priceRange;

                        priceRange = setPriceRange(Long.parseLong(styleCost), gender);

                        if(!priceRange.equals("0")) {
                            Uri downloadUri = task.getResult();
                            final String downloadURL = downloadUri.toString();
                            Toast.makeText(getActivity(), "Details uploaded successfully", Toast.LENGTH_SHORT).show();
                            Log.d("download uri", downloadURL);
                            progressDialog.dismiss();

                            DatabaseReference salonIdsRef = FirebaseDatabase.getInstance().getReference().child("salonIds");

                            salonIdsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String salonId = dataSnapshot.child(salonName).getValue().toString();
                                    DatabaseReference styleRef = databaseReference.push();
                                    final DatabaseReference stylesNumberRef = FirebaseDatabase.getInstance().getReference().child("stylists").child(id);

                                    StylistInformation stylistInformation = new StylistInformation(styleName, salonName, gender, styleCost, downloadURL, id, salonId);

                                    styleRef.setValue(stylistInformation);
                                    styleRef.child("price_range").setValue(priceRange);

                                    //number of styles
                                    stylesNumberRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.child("styles_number").exists()){
                                                int styles = Integer.parseInt(dataSnapshot.child("styles_number").getValue().toString());
                                                styles++;
                                                stylesNumberRef.child("styles_number").setValue(styles);
                                            }
                                            else{
                                                stylesNumberRef.child("styles_number").setValue(1);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                        else {
                            costStyleTxt.setError("invalid price");
                        }

                    } else {
                        Log.d("else", "task incomplete");
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_LONG).show();

                    }
                }
            });

            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                            .getTotalByteCount());
                    progressDialog.setMessage("Uploaded "+(int)progress+"%");
                }
            });
        }

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ref = FirebaseDatabase.getInstance().getReference().child("styles");

        styleButtonsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                style_name = styleNameTxt.getText().toString().trim();
                salon_name = salonNameSpinner.getSelectedItem().toString().trim();
                cost = costStyleTxt.getText().toString().trim();
                style_gender = styleGender.getSelectedItem().toString().trim();

                styleNameTxt.setText("");
                costStyleTxt.setText("");

                uploadImage(storageReference, ref, style_name, salon_name, style_gender, cost, userId);

            }
        });

        stylePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                dispatchTakePictureIntent();
                chooseImage();
            }
        });

    }

    public String setPriceRange(long cost, String gender){
        String priceRange = "0";
        String femalePriceRanges[] = getResources().getStringArray(R.array.price_ranges_female);
        String malePriceRanges[] = getResources().getStringArray(R.array.price_ranges_male);

        if(gender.equals("female")){
            if(cost >= 10000 && cost <= 15000){
                priceRange = femalePriceRanges[0];
            }
            else if(cost >= 16000 && cost <= 25000){
                priceRange = femalePriceRanges[1];
            }
            else if(cost >= 26000 && cost <= 50000){
                priceRange = femalePriceRanges[2];
            }
            else if(cost >= 51000 && cost <= 100000){
                priceRange = femalePriceRanges[3];
            }
            else if(cost > 100000){
                priceRange = femalePriceRanges[4];
            }
            else{
                return "0";
            }
        }

        else if (gender.equals("male")){
            if(cost >= 1000 && cost <= 4900){
                priceRange = malePriceRanges[0];
            }
            else if(cost >= 5000 && cost <= 10000){
                priceRange = malePriceRanges[1];
            }
            else if(cost > 10000){
                priceRange = malePriceRanges[2];
            }
            else{
                return "0";
            }
        }

        return priceRange;
    }

}
