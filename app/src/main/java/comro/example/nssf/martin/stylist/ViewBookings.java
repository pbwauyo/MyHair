package comro.example.nssf.martin.stylist;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import comro.example.nssf.martin.R;
import comro.example.nssf.martin.dataModels.Message;

public class ViewBookings extends AppCompatActivity {
    private DatabaseReference customerDetailsRef, stylistDetailsRef;
    private String replyId, stylistId, stylistName, customerId, customerName, imageUrl;
    private Message message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_bookings);

        customerDetailsRef = FirebaseDatabase.getInstance().getReference().child("customers");
        stylistId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        stylistDetailsRef = FirebaseDatabase.getInstance().getReference().child("stylists").child(stylistId);
    }

    public void processRequest(final String messageId, final String requestStatus){
        replyId = customerDetailsRef.push().getKey();
        stylistDetailsRef.child(messageId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                stylistName = dataSnapshot.child("name").getValue().toString();
                imageUrl = dataSnapshot.child("imageUrl").getValue().toString();
                customerName = dataSnapshot.child("notifications").child("requests").child(messageId).child("senderName").getValue().toString();
                customerId = dataSnapshot.child("notifications").child("requests").child(messageId).child("senderId").getValue().toString();

                if(requestStatus.equals("accepted")) {
                    message = new Message("request accepted", stylistName, stylistId, imageUrl, customerName, customerId, messageId, "accepted");
                }
                else{
                    message = new Message("request declined", stylistName, stylistId, imageUrl, customerName, customerId, messageId, "declined");
                    dataSnapshot.child("notifications").child("requests").child(messageId).getRef().removeValue();
                }
                customerDetailsRef.child(customerId).child("requests").child(replyId).setValue(message);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
