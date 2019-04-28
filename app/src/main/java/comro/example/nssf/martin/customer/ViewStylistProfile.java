package comro.example.nssf.martin.customer;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import comro.example.nssf.martin.R;

public class ViewStylistProfile extends AppCompatActivity {
    private String stylistId;
    private ImageView profilePic;
    private RatingBar ratingBar;
    private TextView nameTxt, stylesTxt, followersTxt, emailTxt, phoneTxt, locationTxt ;
    private Button followBtn, requestBtn;
    private String name, styles, followers, email, phone, location, rating, profileP;
    private String currentUserId;

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

        Intent intent = getIntent();
        stylistId = intent.getStringExtra("stylist_id");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //check if customer is following stylist
        final DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference().child("customers").child(currentUserId);
        followingRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                name = dataSnapshot.child("name").getValue().toString();
                email = dataSnapshot.child("email").getValue().toString();
                phone = dataSnapshot.child("contact").getValue().toString();
                location = dataSnapshot.child("location").getValue().toString();

                //to be implemented
//                profileP = dataSnapshot.child("").getValue().toString();
//                styles = dataSnapshot.child("").getValue().toString();

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

                //set number of styles
                if(!dataSnapshot.child("styles_number").exists()){
                    stylesTxt.setText("0");
                }
                else{
                    styles = dataSnapshot.child("styles_number").getValue().toString();
                    stylesTxt.setText(styles);
                }

//                Glide.with(ViewStylistProfile.this).
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
