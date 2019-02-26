package comro.example.nssf.martin;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import comro.example.nssf.martin.customer.CustomerSignUp;
import comro.example.nssf.martin.customer.SearchStyle;
import comro.example.nssf.martin.stylist.Stylist;
import comro.example.nssf.martin.stylist.StylistMainPage;
import comro.example.nssf.martin.stylist.StylistSignUp;

public class Login extends AppCompatActivity {
    private EditText loginEmailTxt, loginPasswordTxt;
    private TextView loginbtn, signUpBtn;
    private FirebaseDatabase database;
    private String email,  password;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;
    private AlertDialog.Builder alertDialog;
    AlertDialog dialog;
    private final String[] accountTypes = {"Customer", "Stylist"};
    private ArrayList<Integer> choices = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginEmailTxt= findViewById(R.id.loginEmail);
        signUpBtn = findViewById(R.id.signUpBtn);
        loginPasswordTxt = findViewById(R.id.loginPassword);
        loginbtn = findViewById(R.id.loginbtn);

        choices.add(0);

        // create alert dialog
        alertDialog = new AlertDialog.Builder(Login.this);
        alertDialog.setTitle("Choose account type");
        alertDialog.setSingleChoiceItems(accountTypes, 0, new DialogInterface.OnClickListener() {
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
                        startActivity(new Intent(Login.this, CustomerSignUp.class));
                        break;

                    case 1:
                        startActivity(new Intent(Login.this, StylistSignUp.class));
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

        // create progress dialog
        progressDialog = new ProgressDialog(Login.this);
        progressDialog.setTitle("Logging you in");
        progressDialog.setMessage("please wait...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        // initialise firebase auth
        auth = FirebaseAuth.getInstance();

        // add listener to login button
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();

                password = loginPasswordTxt.getText().toString().trim();
                email = loginEmailTxt.getText().toString().trim();

                loginEmailTxt.setText("");
                loginPasswordTxt.setText("");

                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if(task.isSuccessful()){

                            Toast.makeText(Login.this, "success", Toast.LENGTH_LONG).show();

                            final DatabaseReference ref =  FirebaseDatabase.getInstance().getReference();
                            final Boolean[] exists = new Boolean[1];
                            final String userId = auth.getCurrentUser().getUid();

                            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    exists[0] = dataSnapshot.child("customers").child(userId).exists();
                                    Log.d("ondatachange", exists[0].toString());

                                    if(exists[0].equals(true)){
                                        startActivity(new Intent(Login.this, SearchStyle.class));
                                    }
                                    else{
                                        startActivity(new Intent(Login.this, StylistMainPage.class));
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                        }
                        else{
                            Toast.makeText(Login.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });

        // add listener to sign up button
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

    }


}