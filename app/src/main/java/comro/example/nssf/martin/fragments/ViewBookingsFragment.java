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
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import comro.example.nssf.martin.R;
import comro.example.nssf.martin.adapters.BookingsAdapter;
import comro.example.nssf.martin.dataModels.Booking;
import comro.example.nssf.martin.dataModels.Message;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewBookingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class ViewBookingsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private DatabaseReference customerDetailsRef, stylistDetailsRef, requestsRef;
    private String replyId, stylistId, stylistName, customerId, customerName, imageUrl, styleName;
    private Message message;
    private BookingsAdapter bookingsAdapter;
    private ArrayList<Booking> bookingsList;
    private Booking booking;
    private Toolbar toolbar;

    private String cusName, stylName, stylImage, status, id, contact;

    public ViewBookingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ViewBookingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ViewBookingsFragment newInstance(String param1, String param2) {
        ViewBookingsFragment fragment = new ViewBookingsFragment();
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
        layoutManager = new LinearLayoutManager(getActivity());
        customerDetailsRef = FirebaseDatabase.getInstance().getReference().child("customers");
        stylistId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        stylistDetailsRef = FirebaseDatabase.getInstance().getReference().child("stylists").child(stylistId);
        requestsRef = stylistDetailsRef.child("notifications").child("requests");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_bookings, container, false);

        toolbar = view.findViewById(R.id.bookings_toolbar);
        recyclerView = view.findViewById(R.id.bookings_list);

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)(getActivity())).getSupportActionBar().setTitle("Bookings");

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

        recyclerView.setLayoutManager(layoutManager);
        bookingsList = new ArrayList<>();

        requestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    cusName = snapshot.child("senderName").getValue().toString();
                    status = snapshot.child("status").getValue().toString();
                    stylImage = snapshot.child("styleImage").getValue().toString();
                    styleName = snapshot.child("styleName").getValue().toString();
                    id = snapshot.child("messageId").getValue().toString();
                    contact = snapshot.child("senderContact").getValue().toString();

                    booking = new Booking(stylImage, cusName, styleName, status, id, contact);
                    bookingsList.add(booking);
                }

                bookingsAdapter = new BookingsAdapter(bookingsList, getActivity(), new BookingsAdapter.OnItemClickListener() {
                    @Override
                    public void onAcceptButtonClick(View view, int position) {
                        processRequest(bookingsList.get(position).getId(), "accepted");
                    }

                    @Override
                    public void onDeclineButtonClick(View view, int position) {
                        processRequest(bookingsList.get(position).getId(), "declined");
                        bookingsList.remove(position);
                        //bookingsAdapter.notifyDataSetChanged();
                        bookingsAdapter.notifyItemRemoved(position);
                        bookingsAdapter.notifyItemRangeChanged(position, bookingsList.size());
                        Toast.makeText(getActivity(), "size: " + bookingsList.size() + "position: " + position, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onChatButtonClick(View view, int position) {

                    }

                    @Override
                    public void onCallButtonClick(View view, int position) {
                        dialPhoneNumber(bookingsList.get(position).getPhoneNumber());
                    }
                });

                recyclerView.setAdapter(bookingsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

    public void processRequest(final String messageId, final String requestStatus){
        //replyId = customerDetailsRef.push().getKey();
        stylistDetailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String url;
                stylistName = dataSnapshot.child("name").getValue().toString();

                imageUrl = "";
                if(dataSnapshot.child("imageUrl").exists()) {
                    imageUrl = dataSnapshot.child("imageUrl").getValue().toString();
                }

                url = imageUrl.equals("")? "" : imageUrl;

                customerName = dataSnapshot.child("notifications").child("requests").child(messageId).child("senderName").getValue().toString();
                customerId = dataSnapshot.child("notifications").child("requests").child(messageId).child("senderId").getValue().toString();
                styleName = dataSnapshot.child("notifications").child("requests").child(messageId).child("styleName").getValue().toString();
                String img= dataSnapshot.child("notifications").child("requests").child(messageId).child("styleImage").getValue().toString();
                String stylistContact = dataSnapshot.child("contact").getValue().toString();
                String customerContact = dataSnapshot.child("notifications").child("requests").child(messageId).child("senderContact").getValue().toString();

                if(requestStatus.equals("accepted")) {
                    dataSnapshot.child("notifications").child("requests").child(messageId).child("status").getRef().setValue("accepted");
                    message = new Message("request accepted", stylistName, stylistId, url, customerName, customerId, messageId, "accepted", styleName, img, stylistContact, customerContact);
                }
                else{
                    message = new Message("request declined", stylistName, stylistId, url, customerName, customerId, messageId, "declined", styleName, img, stylistContact, customerContact);
                    dataSnapshot.child("notifications").child("requests").child(messageId).getRef().removeValue();
                }
                customerDetailsRef.child(customerId).child("requests").child(messageId).setValue(message);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void dialPhoneNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }



}
