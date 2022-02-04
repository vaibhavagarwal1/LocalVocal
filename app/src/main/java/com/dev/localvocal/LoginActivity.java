package com.dev.localvocal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    //UI views
    private EditText emailEt, passwordEt;
    private TextView forgetTv, noAccountTv;
    private Button loginBtn, loginMobBtn;

    //firebase and progress dialog
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //init UI View
        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        forgetTv = findViewById(R.id.forgetTv);
        noAccountTv = findViewById(R.id.noAccountTv);
        loginBtn = findViewById(R.id.loginBtn);
        loginMobBtn = findViewById(R.id.loginMobBtn);
        //init firebase and progress dialog
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        noAccountTv.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterUserActivity.class)));

        forgetTv.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgetPasswordActivity.class)));

        loginBtn.setOnClickListener(v -> loginUser());

        loginMobBtn.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, OTPVerificationActivity.class)));

    }

    private String email, password;
    private void loginUser() {
        email = emailEt.getText().toString().trim();
        password = passwordEt.getText().toString().trim();

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid Email pattern...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Enter password...", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Logging In...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    //logged in successfully
                    makeMeOnline();
                })
                .addOnFailureListener(e -> {
                    //failed logging in
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void makeMeOnline() {
        //after logging in, make user online
        progressDialog.setMessage("Checking User...");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "true");

        //update value to DB
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //update successfully
                        checkUserType();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed updating
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserType() {
        //if user is Vendor, start main vendor activity
        //if user is consumer, start main user activity

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()) {
                            String accountType = "" + ds.child("accountType").getValue();
                            if(accountType.equals("Vendor")) {
                                progressDialog.dismiss();
                                //user is vendor
                                startActivity(new Intent(LoginActivity.this, MainVendorActivity.class));
                                finish();
                            } else if(accountType.equals("User")) {
                                progressDialog.dismiss();
                                //user is vendor
                                startActivity(new Intent(LoginActivity.this, MainUserActivity.class));
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}