package comro.example.nssf.martin.customer;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import comro.example.nssf.martin.R;
import comro.example.nssf.martin.adapters.StylesAdapter;
import comro.example.nssf.martin.dataModels.Message;
import comro.example.nssf.martin.dataModels.Style;

public class ViewAllStyles extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ArrayList<Style> arrayList;
    private RecyclerView.LayoutManager layoutManager;
    private StylesAdapter adapter;
    private DatabaseReference ref;
    private String stylistId;
    private String stylistName, stylistContact, customerContact, customerId, customerName, customerImage, styleName, styleImage;
    private Message message;
    private HashMap<String, String> hashMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_styles);

        Intent intent = getIntent();
        stylistId = intent.getStringExtra("stylist_id");
        arrayList = new ArrayList<>();
        hashMap = new HashMap<>();

        recyclerView = findViewById(R.id.all_styles_list);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        ref = FirebaseDatabase.getInstance().getReference().child("styles");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name, cost, image;
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    if(snapshot.child("stylistId").getValue().toString().equals(stylistId)){
                        name = snapshot.child("styleName").getValue().toString();
                        cost = snapshot.child("styleCost").getValue().toString();
                        image = snapshot.child("imageUrl").getValue().toString();
                        arrayList.add(new Style(name, cost, image));
                        hashMap.put("name", name);
                        hashMap.put("cost", cost);
                        hashMap.put("image", image);
                    }
                }

                adapter = new StylesAdapter(ViewAllStyles.this, arrayList, new StylesAdapter.OnItemClickListener() {
                    @Override
                    public void onRequestButtonClick(View view, int position) {
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                        final String requestId = databaseReference.push().getKey();
                        styleImage = arrayList.get(position).getimage();
                        styleName = arrayList.get(position).getName();

                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                stylistName = dataSnapshot.child("stylists").child(stylistId).child("name").getValue().toString();
                                stylistContact = dataSnapshot.child("stylists").child(stylistId).child("contact").getValue().toString();
                                customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                customerName = dataSnapshot.child("customers").child("name").getValue().toString();
                                customerContact = dataSnapshot.child("customers").child("contact").getValue().toString();

                                if(dataSnapshot.child("imageUrl").exists() && !dataSnapshot.child("imageUrl").getValue().toString().isEmpty()) {
                                    customerImage = dataSnapshot.child("customers").child("imageUrl").getValue().toString();
                                    message = new Message("new hair request", customerName, customerId, customerImage, stylistName, stylistId, requestId, "", styleName, styleImage, customerContact, stylistContact );
                                }
                                else{
                                    message = new Message("new hair request", customerName, customerId, "", stylistName, stylistId, requestId, "", styleName, styleImage, customerContact, stylistContact );
                                }
                                dataSnapshot.child("stylists").child(stylistId).child("notifications").child("requests").getRef().setValue(message);
                                Toast.makeText(ViewAllStyles.this, "Request sent successfully", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });

                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Log.d("arrayList size", String.valueOf(arrayList.size()));
        Log.d("hashmap size", String.valueOf(hashMap.size()));


    }
}
