package comro.example.nssf.martin;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static comro.example.nssf.martin.UtilityFunctions.getEmojiByUnicode;

public class ChatActivity extends AppCompatActivity {
    private FirebaseRecyclerAdapter<ChatMessage, MessageViewHolder> mFirebaseAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView mMessageRecyclerView;
    private ProgressBar mProgressBar;
    private String senderId, senderAccountType, receiverAccountType;
    private DatabaseReference stylistsRef, customersRef, generalRef, finalRef;
    private ImageView mSendButton;
    private EditText mMessageEditText;
    private ImageView mAddMessageImageView;
    private SnapshotParser<ChatMessage> parser;
    private String receiverId;
    private DatabaseReference senderMessagesRef, receiverMessagesRef;
    private CoordinatorLayout coordinatorLayout;
    private StorageReference storageReference;
    private Uri filePath;
    private final int REQUEST_IMAGE_CAPTURE = 1;
    private final int PICK_IMAGE_REQUEST = 71;
    private String currentPhotoPath;
    private ArrayList<Integer> choices = new ArrayList<>();
    private final String[] imageIntents = {"Take picture", "Choose from gallery"};
    private TextView senderNameTxt, typingStatusTxt;
    private Toolbar toolbar;
    private TextView rateStylistTxt;
    private RatingBar ratingBar;
    private ChatMessage chatMessage;

    private AlertDialog.Builder alertDialog, ratingDialog;
    private AlertDialog dialog;
    LinearLayout.LayoutParams layoutParams;
    LinearLayout.LayoutParams imageViewLayoutParams;
    private float intialRating = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        senderAccountType = intent.getStringExtra("senderAccountType");
        receiverAccountType = intent.getStringExtra("receiverAccountType");
        receiverId = intent.getStringExtra("receiverId");

        storageReference = FirebaseStorage.getInstance().getReference().child("chat_images");

        senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mMessageEditText = findViewById(R.id.messageEditText);
        mSendButton = findViewById(R.id.sendButton);
        mAddMessageImageView = findViewById(R.id.addMessageImageView);
        mSendButton = findViewById(R.id.sendButton);
        mMessageRecyclerView = findViewById(R.id.messageRecyclerView);
        mProgressBar = findViewById(R.id.chatProgressBar);
        senderNameTxt = findViewById(R.id.name);
        typingStatusTxt = findViewById(R.id.typing_status);
        rateStylistTxt = findViewById(R.id.rate);

        ratingDialog = new AlertDialog.Builder(this);

        if(senderAccountType.equals("stylists")){
            rateStylistTxt.setVisibility(View.INVISIBLE);
        }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        coordinatorLayout = findViewById(R.id.coordinator_layout);

        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setReverseLayout(true);
        mLinearLayoutManager.setStackFromEnd(false);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        generalRef = FirebaseDatabase.getInstance().getReference();

        senderMessagesRef = generalRef.child(senderAccountType).child(senderId).child("chat_messages").child(receiverId);
        receiverMessagesRef = generalRef.child(receiverAccountType).child(receiverId).child("chat_messages").child(senderId);

        //create ratingbar dialog
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.rating_bar_dialog, null);
        ratingBar = dialogLayout.findViewById(R.id.rating_bar);
        ratingBar.setRating(intialRating);
        ratingDialog.setView(dialogLayout);
        ratingDialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String emoji = getEmojiByUnicode(0x1F61A);

                if(!(ratingBar.getRating() == intialRating)) {
                    generalRef.child("stylists").child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            float currentRating;
                            float totalRaters;
                            float averageRating;

                            boolean ratedAlready = false;
                            //determine if user has already rated
                            for (DataSnapshot snapshot : dataSnapshot.child("raters").getChildren()) {
                                if (snapshot.getValue().toString().equals(senderId)) {
                                    ratedAlready = true;
                                }
                            }
                            //if user has ever rated stylist
                            if (ratedAlready) {
                                totalRaters = (float) dataSnapshot.child("raters").getChildrenCount();
                                currentRating = Float.parseFloat(dataSnapshot.child("rating").getValue().toString());
                                averageRating = (currentRating + ratingBar.getRating()) / totalRaters;
                                dataSnapshot.child("rating").getRef().setValue(averageRating);
                            }
                            //if user has never rated stylist
                            else {
                                dataSnapshot.child("raters").getRef().push().setValue(senderId);
                                totalRaters = (float) dataSnapshot.child("raters").getChildrenCount();

                                if (dataSnapshot.child("rating").exists()) {
                                    currentRating = Float.parseFloat(dataSnapshot.child("rating").getValue().toString());
                                    averageRating = (currentRating + ratingBar.getRating()) / totalRaters;
                                    dataSnapshot.child("rating").getRef().setValue(averageRating);
                                } else {
                                    dataSnapshot.child("rating").getRef().setValue(ratingBar.getRating());
                                }
                            }

                            Snackbar.make(coordinatorLayout, "Rating successful ".concat(emoji), Snackbar.LENGTH_LONG).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    Snackbar.make(coordinatorLayout, "invalid rating", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        rateStylistTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if()
                ratingDialog.create().show();
            }
        });

        choices.add(0);

        // create alert dialog
        alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Select an option");
        alertDialog.setSingleChoiceItems(imageIntents, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        choices.clear();
                        choices.add(0);
                        break;

                    case 1:
                        choices.clear();
                        choices.add(1);
                        break;
                }
            }
        });

        alertDialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (choices.get(0)){
                    case 0:
                        dispatchTakePictureIntent();
                        break;

                    case 1:
                        chooseImage();
                        break;

                }
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        dialog = alertDialog.create();

        parser = new SnapshotParser<ChatMessage>() {
            @NonNull
            @Override
            public ChatMessage parseSnapshot(@NonNull DataSnapshot dataSnapshot) {
                return dataSnapshot.getValue(ChatMessage.class);
            }
        };

        generalRef.child(receiverAccountType).child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String senderName = dataSnapshot.child("name").getValue().toString();
                senderNameTxt.setText(senderName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseRecyclerOptions<ChatMessage> options = new FirebaseRecyclerOptions.Builder<ChatMessage>().setQuery(senderMessagesRef, parser).build();
        mFirebaseAdapter = new FirebaseRecyclerAdapter<ChatMessage, MessageViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MessageViewHolder viewHolder, int position, @NonNull ChatMessage message) {
                mProgressBar.setVisibility(ProgressBar.GONE);
                //layoutParams = (LinearLayout.LayoutParams) viewHolder.messageTextView.getLayoutParams();
                final ProgressDialog progressDialog = new ProgressDialog(ChatActivity.this);
                progressDialog.setTitle("Loading image");
                progressDialog.setMessage("please wait...");

                if (!message.getBody().isEmpty()) {
                    viewHolder.messageImageView.setVisibility(ImageView.GONE);
                    viewHolder.messageTextView.setVisibility(TextView.VISIBLE);
                    viewHolder.messageTextView.setText(message.getBody());

                    //if senderid is equal to the current user
                    if(message.getSenderId().equals(senderId)){
                        viewHolder.messageTextView.setBackground(ContextCompat.getDrawable(ChatActivity.this, R.drawable.sent_message));
//                        layoutParams = (LinearLayout.LayoutParams) viewHolder.messageTextView.getLayoutParams();
//                        layoutParams.gravity = Gravity.END;
//                        viewHolder.messageTextView.setLayoutParams(layoutParams);
                    }
                    else {
                        viewHolder.messageTextView.setBackground(ContextCompat.getDrawable(ChatActivity.this, R.drawable.received_message));
//                        layoutParams = (LinearLayout.LayoutParams) viewHolder.messageTextView.getLayoutParams();
//                        layoutParams.gravity = Gravity.START;
//                        viewHolder.messageTextView.setLayoutParams(layoutParams);
                    }
                }
                else if (!message.getImageMessage().isEmpty()) {
                    progressDialog.show();
                    Picasso.get()
                            .load(message.getImageMessage())
                            .fit()
                            .centerCrop()
                            .into(viewHolder.messageImageView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    progressDialog.dismiss();
                                }

                                @Override
                                public void onError(Exception e) {

                                }
                            });
                    viewHolder.messageImageView.setVisibility(ImageView.VISIBLE);
                    viewHolder.messageTextView.setVisibility(TextView.GONE);
                }

                //viewHolder.messengerTextView.setText(message.getSenderName());
                String dp = message.getSenderImageUrl();

                if(!dp.isEmpty()){
                    imageViewLayoutParams = (LinearLayout.LayoutParams)viewHolder.messengerImageView.getLayoutParams();
                    imageViewLayoutParams.gravity = Gravity.END;
                    viewHolder.messengerImageView.setLayoutParams(imageViewLayoutParams);
                    Picasso.get()
                            .load(dp)
                            .fit()
                            .rotate(90)
                            .centerCrop()
                            .into(viewHolder.messengerImageView);
                }

            }

            @NonNull
            @Override
            public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                return new MessageViewHolder(LayoutInflater.from(ChatActivity.this).inflate(R.layout.chat_message_row, viewGroup, false));
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int messageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (messageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mMessageRecyclerView.setAdapter(mFirebaseAdapter);

        generalRef.child(senderAccountType).child(senderId).child("typing_status").child(receiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    String typingStatus = dataSnapshot.getValue().toString();
                    if (typingStatus.equals("typing")){
                        typingStatusTxt.setText(getString(R.string.typing));
                    }
                    else if(typingStatus.equals("done")){
                        typingStatusTxt.setText("");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        receiverMessagesRef.child("online_status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String onlineStatus = dataSnapshot.getValue().toString();
                    if(onlineStatus.equals("online")){
                        //ty
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                generalRef.child(receiverAccountType).child(receiverId).child("typing_status").child(senderId).setValue("typing");
            }

            @Override
            public void afterTextChanged(Editable s) {
                generalRef.child(receiverAccountType).child(receiverId).child("typing_status").child(senderId).setValue("done");
            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String body = mMessageEditText.getText().toString().trim();
                mMessageEditText.setText("");
                generalRef.child(senderAccountType).child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(!body.isEmpty()) {
                            String senderImageUrl="";
                            String senderName = dataSnapshot.child("name").getValue().toString();
                            if(dataSnapshot.child("imageUrl").exists()) {
                                senderImageUrl = dataSnapshot.child("imageUrl").getValue().toString();
                            }

                            chatMessage = new ChatMessage(body, senderId, senderImageUrl, "", senderName);
                            receiverMessagesRef.push().setValue(chatMessage);
                            senderMessagesRef.push().setValue(chatMessage);
                        }
                        else {
                            mMessageEditText.setError("can't send empty message");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        mAddMessageImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Snackbar.make(coordinatorLayout, ex.getMessage(), Snackbar.LENGTH_LONG);

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(ChatActivity.this,
                        "comro.example.nssf.martin",
                        photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void uploadImage() {

        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(ChatActivity.this);
            progressDialog.setTitle("Sending message");
            progressDialog.show();

            UploadTask uploadTask = storageReference.putFile(filePath);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        Log.d("taskUpload", task.getException().getMessage());
                        Toast.makeText(ChatActivity.this, "task upload unsuccessful: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        throw task.getException();
                    }
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        //String emoji = getEmojiByUnicode(0x1F61A);
                        final String url = task.getResult().toString();
                        generalRef.child(senderAccountType).child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String senderName = dataSnapshot.child("name").getValue().toString();
                                String senderImageUrl = dataSnapshot.child("imageUrl").getValue().toString();

                                ChatMessage chatMessage = new ChatMessage("", senderId, senderImageUrl, url, senderName);
                                receiverMessagesRef.push().setValue(chatMessage);
                                senderMessagesRef.push().setValue(chatMessage);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }
                    else {
                        Log.d("else", "task incomplete");
                        Toast.makeText(ChatActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();

                    }
                }
            });

            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                            .getTotalByteCount());
                    progressDialog.setMessage("please wait..."+(int)progress+"%");
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            File file = new File(currentPhotoPath);
            filePath = Uri.fromFile(file);
            uploadImage();
        }
        else if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            uploadImage();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onPause() {
        mFirebaseAdapter.stopListening();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAdapter.startListening();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        ImageView messageImageView;
        TextView messengerTextView;
        CircleImageView messengerImageView;

        MessageViewHolder(View v) {
            super(v);
            messageTextView =  itemView.findViewById(R.id.messageTextView);
            messageImageView = itemView.findViewById(R.id.messageImageView);
            messengerTextView = itemView.findViewById(R.id.messengerTextView);
            messengerImageView = itemView.findViewById(R.id.messengerImageView);
        }
    }
}
