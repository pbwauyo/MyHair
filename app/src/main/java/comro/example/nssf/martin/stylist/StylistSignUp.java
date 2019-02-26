package comro.example.nssf.martin.stylist;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import comro.example.nssf.martin.R;
import comro.example.nssf.martin.dataModels.StylistDetails;
import comro.example.nssf.martin.Login;

public class StylistSignUp extends AppCompatActivity  {

    private EditText nameTxt, emailTxt, contactTxt, locationTxt, pswdTxt, confirmPswdTxt;
    private FirebaseDatabase database;
    private TextView save, login;
    private String name, email, contact, location, pswd, confirmPswd, time;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stylist_sign_up);

        //find views by their ids
        nameTxt = findViewById(R.id.stylist_name);
        emailTxt = findViewById(R.id.stylist_email);
        contactTxt = findViewById(R.id.stylist_phone);
        locationTxt = findViewById(R.id.stylist_location);
        pswdTxt = findViewById(R.id.stylistPassword);
        confirmPswdTxt = findViewById(R.id.stylistConfirmpassword);
        save = findViewById(R.id.save_stylist_info);
        login = findViewById(R.id.s_lin);

        //initialise firebase auth instance
        auth = FirebaseAuth.getInstance();

        //create progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Creating account");
        progressDialog.setMessage("please wait..");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        //set listener for the sign up button
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                name = nameTxt.getText().toString().trim();
                email = emailTxt.getText().toString().trim();
                contact = contactTxt.getText().toString().trim();
                location = locationTxt.getText().toString().trim();
                pswd = pswdTxt.getText().toString().trim();
                confirmPswd = confirmPswdTxt.getText().toString().trim();

                nameTxt.setText("");
                emailTxt.setText("");
                contactTxt.setText("");
                locationTxt.setText("");
                pswdTxt.setText("");
                confirmPswdTxt.setText("");

                if (pswd.equals(confirmPswd)) {
                    auth.createUserWithEmailAndPassword(email, pswd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                String userId = auth.getCurrentUser().getUid();
                                StylistDetails stylistDetails = new StylistDetails(name, email, contact, location, pswd, time);
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("stylists").child(userId);
                                ref.setValue(stylistDetails);
                                Toast.makeText(StylistSignUp.this, "User created successfully", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(StylistSignUp.this, Login.class));
                            } else {
                                Toast.makeText(StylistSignUp.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(StylistSignUp.this, "Passwords do not match", Toast.LENGTH_LONG).show();
                }

            }
        });

        //set listener for the login button
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StylistSignUp.this, Login.class));
            }
        });
    }






}
