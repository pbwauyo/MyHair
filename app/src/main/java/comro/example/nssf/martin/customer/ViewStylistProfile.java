package comro.example.nssf.martin.customer;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import comro.example.nssf.martin.R;
import comro.example.nssf.martin.dataModels.Message;
import de.hdodenhof.circleimageview.CircleImageView;

public class ViewStylistProfile extends AppCompatActivity {
    private String stylistId;
    private CircleImageView profilePic;
    private RatingBar ratingBar;
    private TextView nameTxt, stylesTxt, followersTxt, emailTxt, phoneTxt, locationTxt ;
    private Button followBtn, requestBtn;
    private String name, styles, followers, email, phone, location, rating;
    private String currentUserId;
    private String requestId;
    private String stylistName, styleName;
    private String senderName, imageUrl, styleImg, stylistContact;
    private DatabaseReference notifRef, detailsRef, followingRef;
    private Message message;
    private ProgressBar progressBar;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_stylist_profile);

        profilePic = findViewById(R.id.profile_pic);//
        nameTxt = findViewById(R.id.view_name);
        stylesTxt = findViewById(R.id.styles);//
        followersTxt = findViewById(R.id.followers);
        emailTxt = findViewById(R.id.email);
        phoneTxt = findViewById(R.id.phone);
        locationTxt = findViewById(R.id.location);
        followBtn = findViewById(R.id.follow_btn);
        requestBtn = findViewById(R.id.request_btn);
        ratingBar = findViewById(R.id.rating);

        linearLayout = findViewById(R.id.styles_layout);

        progressBar = findViewById(R.id.arrowProgressBar);

        Intent intent = getIntent();
        stylistId = intent.getStringExtra("stylist_id");
        stylistName = intent.getStringExtra("stylist_name");
        styleName = intent.getStringExtra("style_name");
        styleImg = intent.getStringExtra("style_image");
        stylistContact = intent.getStringExtra("phone_number");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        followingRef = FirebaseDatabase.getInstance().getReference();
        notifRef = FirebaseDatabase.getInstance().getReference().child("stylists").child(stylistId).child("notifications").child("requests");

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent stylesIntent = new Intent(ViewStylistProfile.this, ViewAllStyles.class);
                stylesIntent.putExtra("stylist_id", stylistId);
                startActivity(stylesIntent);
            }
        });

        //check if customer is following stylist
        detailsRef = FirebaseDatabase.getInstance().getReference().child("customers").child(currentUserId);
        detailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("following").exists()) {

                    for (DataSnapshot ds : dataSnapshot.child("following").getChildren())

                        if (ds.child(stylistId).exists()) {
                            followBtn.setText(R.string.unfollow);
                            break;
                        }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("stylists").child(stylistId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //try to load profile image only if node exists
                if(dataSnapshot.child("imageUrl").exists()){
                    String imageUrl = dataSnapshot.child("imageUrl").getValue().toString();
                    if(!imageUrl.isEmpty()){
                        profilePic.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                        //load profile picture into profile page ImageView
                        Picasso.get()
                                .load(imageUrl)
                                .fit()
                                .centerCrop()
                                .rotate(90)
                                .into(profilePic, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        progressBar.setVisibility(View.GONE);
                                        profilePic.setVisibility(View.VISIBLE);
                                    }

                                    @Override
                                    public void onError(Exception e) {

                                    }
                                });
                    }
                }

                name = dataSnapshot.child("name").getValue().toString();
                email = dataSnapshot.child("email").getValue().toString();
                phone = dataSnapshot.child("contact").getValue().toString();
                location = dataSnapshot.child("location").getValue().toString();

                nameTxt.setText(name);
                emailTxt.setText(email);
                phoneTxt.setText(phone);
                locationTxt.setText(location);

                //set followers
                if(!dataSnapshot.child("followers").exists()){
                    followersTxt.setText("0");
                }
                else{
                    followers = dataSnapshot.child("followers").getValue().toString();
                    followersTxt.setText(followers);
                }

                //set rating bar
                if(!dataSnapshot.child("rating").exists()){
                    ratingBar.setRating(0);
                }
                else{
                    rating = dataSnapshot.child("rating").getValue().toString();
                    ratingBar.setRating(Float.parseFloat(rating));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // send request notifications
        requestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestId = notifRef.push().getKey();

                detailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        senderName = dataSnapshot.child("name").getValue().toString();
                        String senderNumber = dataSnapshot.child("contact").getValue().toString();
                        if(dataSnapshot.child("imageUrl").exists() && !dataSnapshot.child("imageUrl").getValue().toString().isEmpty()) {
                            imageUrl = dataSnapshot.child("imageUrl").getValue().toString();
                            message = new Message("hair request", senderName, currentUserId, imageUrl, stylistName, stylistId, requestId, "", styleName, styleImg, senderNumber, stylistContact);
                        }
                        else {
                            message = new Message("hair request", senderName, currentUserId, "", stylistName, stylistId, requestId, "", styleName, styleImg, senderNumber, stylistContact);
                        }
                        Toast.makeText(ViewStylistProfile.this, "Request sent successfully", Toast.LENGTH_SHORT).show();
                        notifRef.child(requestId).setValue(message);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });


        followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("stylists").child(stylistId).child("followers");
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            int currentFollowers = Integer.parseInt(dataSnapshot.getValue().toString());

                            if (followBtn.getText().equals("UNFOLLOW")) {
                                currentFollowers--;
                                ref.setValue(currentFollowers);
                                followBtn.setText(R.string.follow);

                                //remove the stylist
                                followingRef.child("following").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            if (ds.getValue().toString().equals(stylistId)) {
                                                ds.getRef().removeValue();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            } else {
                                currentFollowers++;
                                ref.setValue(currentFollowers);
                                followingRef.child("following").push().setValue(stylistId);
                                followBtn.setText(R.string.unfollow);
                            }
                        }
                        else {
                            dataSnapshot.getRef().setValue(1);
                            followingRef.child("following").push().setValue(stylistId);
                            followBtn.setText(R.string.unfollow);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
        });
    }
}
