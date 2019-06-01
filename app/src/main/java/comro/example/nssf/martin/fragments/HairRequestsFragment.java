package comro.example.nssf.martin.fragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import comro.example.nssf.martin.ChatActivity;
import comro.example.nssf.martin.R;
import comro.example.nssf.martin.adapters.HairRequestAdapter;
import comro.example.nssf.martin.dataModels.HairRequest;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HairRequestsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HairRequestsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private HairRequestAdapter adapter;
    private ArrayList<HairRequest> hairRequests;
    private HairRequest hairRequest;
    private DatabaseReference detailsRef;
    private String userId;
    private String styleImageUrl, stylistName, styleName, phoneNumber, id, status;

    public HairRequestsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HairRequestsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HairRequestsFragment newInstance(String param1, String param2) {
        HairRequestsFragment fragment = new HairRequestsFragment();
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
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        detailsRef = FirebaseDatabase.getInstance().getReference().child("customers").child(userId);
        hairRequests = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hair_requests, container, false);
        recyclerView = view.findViewById(R.id.hair_requests_list);
        Toolbar toolbar = view.findViewById(R.id.requests_toolbar);

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)(getActivity())).getSupportActionBar().setTitle("Hair requests");

        DrawerLayout drawer = getActivity().findViewById(R.id.drawer);

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

        detailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.child("requests").getChildren()){
                    styleImageUrl = snapshot.child("styleImage").getValue().toString();
                    stylistName = snapshot.child("senderName").getValue().toString();
                    styleName = snapshot.child("styleName").getValue().toString();
                    phoneNumber = snapshot.child("senderContact").getValue().toString();
                    id = snapshot.child("messageId").getValue().toString();
                    status = snapshot.child("status").getValue().toString();
                    String stylistId = snapshot.child("senderId").getValue().toString();

                    hairRequest = new HairRequest(styleImageUrl, stylistName, styleName, phoneNumber, id, status, stylistId);
                    hairRequests.add(hairRequest);
                }

                adapter = new HairRequestAdapter(hairRequests, getActivity(), new HairRequestAdapter.OnItemClickListener() {
                    @Override
                    public void onChatButtonClick(View view, int position) {
                        Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                        chatIntent.putExtra("receiverAccountType", "stylists");
                        chatIntent.putExtra("senderAccountType", "customers");
                        chatIntent.putExtra("receiverId", hairRequests.get(position).getStylistId());
                        startActivity(chatIntent);
                    }

                    @Override
                    public void onDeleteButtonClick(View view, int position) {
                        dataSnapshot.child("requests").child(hairRequests.get(position).getId()).getRef().removeValue();
                        hairRequests.remove(position);
                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemRangeChanged(position, hairRequests.size());
                    }

                    @Override
                    public void onCallButtonClick(View view, int position) {
                        dialPhoneNumber(hairRequests.get(position).getPhoneNumber());
                    }
                });

                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    public void dialPhoneNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

}
