package comro.example.nssf.martin.stylist;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

import comro.example.nssf.martin.R;
import comro.example.nssf.martin.dataModels.StylistInformation;

public class StyleDetails extends AppCompatActivity {
    EditText styleNameTxt, salonNameTxt, salonLocationTxt, costStyleTxt;
    Spinner styleGender;
    FirebaseDatabase database;
    Button styleButtonsave;
    private String style_name, salon_name, salon_location, cost, style_gender;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_style_details);

        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReferenceFromUrl("gs://prototype-5625e.appspot.com/").child(userId).child("style_images");
        databaseReference = FirebaseDatabase.getInstance().getReference();

        editPhoto = findViewById(R.id.editImage);
        stylePhoto = findViewById(R.id.imageView);
        styleNameTxt = findViewById(R.id.style_name);
        salonNameTxt = findViewById(R.id.salonName);
        salonLocationTxt = findViewById(R.id.salonLocation);
        costStyleTxt = findViewById(R.id.costStyle);
        styleButtonsave = findViewById(R.id.styleButtonSave);
        styleGender = findViewById(R.id.style_gender_spinner);
        ref = FirebaseDatabase.getInstance().getReference().child("styles");

        styleButtonsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                style_name = styleNameTxt.getText().toString().trim();
                salon_name = salonNameTxt.getText().toString().trim();
                salon_location = salonLocationTxt.getText().toString().trim();
                cost = costStyleTxt.getText().toString().trim();
                style_gender = styleGender.getSelectedItem().toString();


                styleNameTxt.setText("");
                salonNameTxt.setText("");
                costStyleTxt.setText("");
                salonLocationTxt.setText("");
//                if (stylePhoto.getDrawable() != null) {
//                    stylePhoto.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_add_black_24dp));
//                }

                styleGender.setPrompt("choose gender");
//                StylistInformation stylistInformation = new StylistInformation(style_name, salon_name, salon_location, style_gender, cost);
//                ref.push().setValue(stylistInformation);
                uploadImage(storageReference, ref, style_name, salon_name, salon_location, style_gender, cost, userId);
//                Toast.makeText(StyleDetails.this, "Information saved successfully", Toast.LENGTH_LONG).show();

            }
        });

        editPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                dispatchTakePictureIntent();
                chooseImage();
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
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
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }


    private void uploadImage(final StorageReference storageRef, final DatabaseReference databaseReference, final String styleName, final String salonName, final String location, final String gender, final String styleCost, final String id) {

        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
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
                        Toast.makeText(StyleDetails.this, "task upload unsuccessful: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        throw task.getException();
                    }
                    // Continue with the task to get the download URL
//                    Log.d("download url", storageRef.getDownloadUrl().getResult().getPath());
//                    Log.d("download url", storageRef.getDownloadUrl().toString());
                    return imageRef.getDownloadUrl();
                }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            String downloadURL = downloadUri.toString();
                            Toast.makeText(StyleDetails.this, "Details uploaded successfully", Toast.LENGTH_SHORT).show();
                             Log.d("download uri", downloadURL);
                            progressDialog.dismiss();
//                            StylistInformation stylistInformation = new StylistInformation(styleName, salonName, location, gender, styleCost, downloadURL, id, "");
//                            databaseReference.push().setValue(stylistInformation);
                   } else {
                            Log.d("else", "task incomplete");
                            progressDialog.dismiss();
                            Toast.makeText(StyleDetails.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();

                        }
                    }
                });

//            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                        @Override
//                        public void onSuccess(Uri uri) {
//                            Log.d("onSuccess", "onSuccess: uri= "+ uri.toString());
//                            progressDialog.dismiss();
//                            StylistInformation stylistInformation = new StylistInformation(styleName, salonName, location, gender, styleCost, uri.toString());
//                            databaseReference.push().setValue(stylistInformation);
//                        }
//                    });
//
//                }
//            });

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
}
