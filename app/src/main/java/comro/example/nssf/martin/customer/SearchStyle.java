package comro.example.nssf.martin.customer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import java.util.List;

import comro.example.nssf.martin.R;
import comro.example.nssf.martin.TrackerService;
import comro.example.nssf.martin.dataModels.Style;

public class SearchStyle extends AppCompatActivity {
    Button searchButton;
    Spinner genderSpinner,  pricesSpinner;
    DatabaseReference ref;
    EditText stylesTxt, locationTxt;
    StorageReference storageReference;
    FirebaseAuth auth;
    private String userId;
//    private List<String> locations;
    private static final int PERMISSIONS_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_style);

        ref = FirebaseDatabase.getInstance().getReference().child("styles");
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();

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

        searchButton =findViewById(R.id.searchBtton);
       // location =findViewById(R.id.locatingsearchBtton2);
        genderSpinner = findViewById(R.id.gender_spinner);
        stylesTxt = findViewById(R.id.styles_txt);
        pricesSpinner = findViewById(R.id.prices_spinner);
        locationTxt = findViewById(R.id.location_txt);

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Check location permission is granted - if it is, start
        // the service, otherwise request the permission
        int permission = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            startTrackerService();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST);
        }


        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String selectedGender = genderSpinner.getSelectedItem().toString();
                final String selectedStyle = stylesTxt.getText().toString();
                final String selectedPrice = pricesSpinner.getSelectedItem().toString();
                final String location = locationTxt.getText().toString();

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
                            if(!location.equals("")) {
                                if (selectedGender.equals(gender) && selectedStyle.equals(style) && selectedPrice.equals(price)) {
                                    locationsRef[0].addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.child(id).getValue().toString().contains(location)) {
                                                count[0]++;
                                                Style snapshotStyle = new Style(style, gender, price, imageUrl, id, salonId);
                                                arrayList.add(snapshotStyle);
                                                Log.d("in if", style + " " + gender + " " + price);
                                                Log.d("selected in if", selectedGender + " " + selectedStyle + " " + selectedPrice);
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

//                                        Log.d("out of if", style + " " + gender + " " + price);
//                                        Log.d("selected out of if", selectedGender + " " + selectedStyle + " " + selectedPrice);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        if (requestCode == PERMISSIONS_REQUEST && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Start the service when the permission is granted
            startTrackerService();
        } //else {
//            finish();
//        }

    }

    private void startTrackerService() {
        startService(new Intent(this, TrackerService.class));
    }
}
