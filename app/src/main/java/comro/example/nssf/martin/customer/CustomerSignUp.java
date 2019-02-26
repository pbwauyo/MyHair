package comro.example.nssf.martin.customer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import comro.example.nssf.martin.R;
import comro.example.nssf.martin.dataModels.CustomerDetails;
import comro.example.nssf.martin.Login;

public class CustomerSignUp extends AppCompatActivity {
    private EditText nameTxt, emailTxt, contactTxt, pswdTxt, confirmPswdTxt;
    private FirebaseDatabase database;
    private TextView signUp, login;
    private String name, email, contact, pswd, confirmPswd;
    private FirebaseAuth auth;
    private DatabaseReference ref;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_sign_up);
        ref  = FirebaseDatabase.getInstance().getReference().child("customers");

        nameTxt = findViewById(R.id.customer_name);
        emailTxt = findViewById(R.id.customer_email);
        contactTxt = findViewById(R.id.customer_contact);
        pswdTxt = findViewById(R.id.customer_password);
        confirmPswdTxt = findViewById(R.id.confirm_customer_password);
        login = findViewById(R.id.c_lin);
        signUp = findViewById(R.id.sup);

        auth = FirebaseAuth.getInstance();

        //create progress dialog
        progressDialog = new ProgressDialog(CustomerSignUp.this);
        progressDialog.setTitle("Creating account");
        progressDialog.setMessage("please wait...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
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

                if(pswd.equals(confirmPswd)) {
                    auth.createUserWithEmailAndPassword(email, pswd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();
                            if(task.isSuccessful()){
                                Toast.makeText(CustomerSignUp.this, "User created successfully", Toast.LENGTH_LONG).show();
                                String userId = auth.getCurrentUser().getUid();
                                CustomerDetails customerDetails = new CustomerDetails(name, email, contact, pswd);
                                ref.child(userId).setValue(customerDetails);
                                startActivity(new Intent(CustomerSignUp.this, Login.class));
                            }
                            else{
                                Toast.makeText(CustomerSignUp.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }
                else {
                    progressDialog.dismiss();
                    Toast.makeText(CustomerSignUp.this, "Invalid username or password", Toast.LENGTH_LONG).show();
                }
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CustomerSignUp.this, Login.class));
            }
        });



    }
}
