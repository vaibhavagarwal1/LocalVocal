package com.dev.localvocal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //make fullscreen
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        firebaseAuth = FirebaseAuth.getInstance();

        //start login activity after 2 sec
        new Handler().postDelayed(() -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if(user == null) {
                //user not logged in start login activity
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            } else {
                //user logged in, check user type
                checkUserType();
            }
        }, 2000);
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
                                //user is vendor
                                startActivity(new Intent(SplashActivity.this, MainVendorActivity.class));
                                finish();
                            } else if(accountType.equals("User")) {
                                //user is vendor
                                startActivity(new Intent(SplashActivity.this, MainUserActivity.class));
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