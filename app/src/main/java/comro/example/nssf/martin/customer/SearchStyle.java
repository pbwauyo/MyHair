package comro.example.nssf.martin.customer;

import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import comro.example.nssf.martin.R;
import comro.example.nssf.martin.StylistsMapView;
import comro.example.nssf.martin.dataModels.Style;

public class SearchStyle extends AppCompatActivity {
    private Button searchButton;
    private Spinner genderSpinner,  pricesSpinner;
    private DatabaseReference ref;
    private EditText stylesTxt, locationTxt;
    private StorageReference storageReference;
    private FirebaseAuth auth;
    private String userId;
    private final int LOCATION_PERMISSION_REQUEST = 99;
    private CoordinatorLayout coordinatorLayout;
    private Location customerLocation, salonLocation;
    private DatabaseReference detailsRef;
    private double customerLatitude, salonLatitude, customerLongitude, salonLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_style);

        ref = FirebaseDatabase.getInstance().getReference().child("styles");
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();

        coordinatorLayout = findViewById(R.id.search_style_coordinator_layout);
        searchButton =findViewById(R.id.searchBtton);
        genderSpinner = findViewById(R.id.gender_spinner);
        stylesTxt = findViewById(R.id.styles_txt);
        pricesSpinner = findViewById(R.id.prices_spinner);
        locationTxt = findViewById(R.id.location_txt);

        detailsRef = FirebaseDatabase.getInstance().getReference().child("customers").child(userId);

        storageReference = FirebaseStorage.getInstance().getReference().child(userId).child("style_image");

        genderSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String femalePriceRanges[] = getResources().getStringArray(R.array.price_ranges_female);
                String malePriceRanges[] = getResources().getStringArray(R.array.price_ranges_male);

                ArrayAdapter<String> adapter;

                switch (adapterView.getItemAtPosition(i).toString()){
                    case "male":
                        // attaching data adapter to spinner
                        adapter  = new ArrayAdapter<>(SearchStyle.this, android.R.layout.simple_spinner_item, malePriceRanges);
                        // Drop down layout style - list view with radio button
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        pricesSpinner.setAdapter(adapter);
                        break;

                    case "female":
                        // attaching data adapter to spinner
                        adapter  = new ArrayAdapter<>(SearchStyle.this, android.R.layout.simple_spinner_item, femalePriceRanges);
                        // Drop down layout style - list view with radio button
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        pricesSpinner.setAdapter(adapter);
                        break;
                }
            }
        });


//        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show();
//            finish();
//        }

        searchButton.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String selectedGender = genderSpinner.getSelectedItem().toString();
                final String selectedStyle = stylesTxt.getText().toString().trim();
                final String selectedPrice = pricesSpinner.getSelectedItem().toString();
                final String location = locationTxt.getText().toString().trim();

                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        final String  style, price, imageUrl, id, fileExt;
                        final ArrayList<Style> arrayList = new ArrayList<>();
                        //Style snapshotStyle;
                        final Bundle[] bundle = {new Bundle()};
                        final Iterable<DataSnapshot> customerStyles = dataSnapshot.getChildren();
                        final DatabaseReference[] locationsRef = {FirebaseDatabase.getInstance().getReference().child("stylists")};
                        final int[] count = {0};

                        for(DataSnapshot ds: customerStyles){
                            final String gender = ds.child("styleGender").getValue().toString();
                            final String style = ds.child("styleName").getValue().toString();
                            final String price = ds.child("price_range").getValue().toString();
                            final String imageUrl = ds.child("imageUrl").getValue().toString();
                            final String id = ds.child("stylistId").getValue().toString();
                            final String salonId = ds.child("salonId").getValue().toString();

                            //if location is input
                            boolean styleExists = selectedGender.equals(gender) && selectedStyle.equals(style) && selectedPrice.equals(price);
                            if(!location.equals("")) {
                                if (styleExists) {
                                    locationsRef[0].addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Log.d("to string()", dataSnapshot.child(id).getValue().toString()  + " " +
                                                    dataSnapshot.child(id).getValue().toString().contains(location));

                                            if(dataSnapshot.child(id).child("salons_details").child(salonId).child("address").getValue().toString().contains(location)){
                                                count[0]++;
                                                Style snapshotStyle = new Style(style, gender, price, imageUrl, id, salonId);
                                                arrayList.add(snapshotStyle);
                                            }

//                                            if (dataSnapshot.child(id).child("salons_details").child(salonId).getValue().toString().contains(location)) {
//                                                count[0]++;
//                                                Style snapshotStyle = new Style(style, gender, price, imageUrl, id, salonId);
//                                                arrayList.add(snapshotStyle);
//                                                Log.d("in if", style + " " + gender + " " + price);
//                                                Log.d("selected in if", selectedGender + " " + selectedStyle + " " + selectedPrice);
//                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                            // if location is not input
                            else {
                                if (styleExists) {
                                    locationsRef[0].addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            //salon location
                                            final Location salonLocation = new Location("salon_location");
                                            salonLatitude = Double.parseDouble(dataSnapshot.child(id).child("salons_details").child(salonId).child("latitude").getValue().toString());
                                            salonLongitude = Double.parseDouble(dataSnapshot.child(id).child("salons_details").child(salonId).child("longitude").getValue().toString());
                                            salonLocation.setLatitude(salonLatitude);
                                            salonLocation.setLongitude(salonLongitude);

                                            detailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    //customer location
                                                    customerLocation = new Location("customer_location");
                                                    customerLatitude = Double.parseDouble(dataSnapshot.child("location").child("latitude").getValue().toString());
                                                    customerLongitude = Double.parseDouble(dataSnapshot.child("location").child("longitude").getValue().toString());
                                                    customerLocation.setLatitude(customerLatitude);
                                                    customerLocation.setLongitude(customerLongitude);
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });

                                            //distance between customer and salon
                                            double distanceBetween;
                                            distanceBetween = customerLocation.distanceTo(salonLocation);

                                            Log.d("distance between", String.valueOf(distanceBetween));

                                            if(distanceBetween <= 50000){
                                                count[0]++;
                                                Style snapshotStyle = new Style(style, gender, price, imageUrl, id, salonId);
                                                arrayList.add(snapshotStyle);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                        }

                        //if there's no record
                        if(!(count[0] >0)){
                            Toast.makeText(SearchStyle.this,"Style doesn't exist", Toast.LENGTH_LONG).show();
                        }
                        else{
                            Intent intent = new Intent(SearchStyle.this, StylistsMapView.class);
                            bundle[0].putSerializable("styles",  arrayList);
                            intent.putExtras(bundle[0]);
                            startActivity(intent);
                            finish();
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
