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

    private EditText nameTxt, emailTxt, contactTxt, pswdTxt, confirmPswdTxt;
    private FirebaseDatabase database;
    private TextView save, login;
    private String name, email, contact, pswd, confirmPswd, time;
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
                name = nameTxt.getText().toString().trim();
                email = emailTxt.getText().toString().trim();
                contact = contactTxt.getText().toString().trim();
                pswd = pswdTxt.getText().toString().trim();
                confirmPswd = confirmPswdTxt.getText().toString().trim();

                nameTxt.setText("");
                emailTxt.setText("");
                contactTxt.setText("");
                pswdTxt.setText("");
                confirmPswdTxt.setText("");

                if(!(name.isEmpty() && email.isEmpty() && contact.isEmpty() && pswd.isEmpty() && confirmPswd.isEmpty())) {
                    progressDialog.show();
                    if (pswd.equals(confirmPswd)) {
                        auth.createUserWithEmailAndPassword(email, pswd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressDialog.dismiss();
                                if (task.isSuccessful()) {
                                    String userId = auth.getCurrentUser().getUid();
                                    StylistDetails stylistDetails = new StylistDetails(name, email, contact, pswd, time);
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
                        confirmPswdTxt.setError("passwords don't match");
                    }
                }
                else {
                    Toast.makeText(StylistSignUp.this, "please fill in all fields", Toast.LENGTH_LONG).show();
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
