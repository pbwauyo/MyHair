package comro.example.nssf.martin.customer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import comro.example.nssf.martin.MyAdapter;
import comro.example.nssf.martin.R;
import comro.example.nssf.martin.dataModels.Style;

public class StylistList extends AppCompatActivity {
    MyAdapter myAdapter;
    RecyclerView recyclerView;
    ArrayList<Style> arrayList;
    RecyclerView.LayoutManager layoutManager;
    StorageReference storageReference;
    FirebaseAuth auth;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_stylist_list);

        Bundle bundle = getIntent().getExtras();
        arrayList = (ArrayList<Style>) bundle.getSerializable("styles");

        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference().child(userId).child("style_image");

        myAdapter = new MyAdapter(StylistList.this, arrayList, storageReference);
//        recyclerView = findViewById(R.id.stylist_list);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(myAdapter);
    }
}
