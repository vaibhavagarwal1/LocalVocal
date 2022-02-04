package com.dev.localvocal;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.dev.localvocal.fragmentsUser.HomeFragment;
import com.dev.localvocal.fragmentsUser.NotificationFragment;
import com.dev.localvocal.fragmentsUser.OrdersFragment;
import com.dev.localvocal.fragmentsUser.ProfileFragment;
import com.dev.localvocal.fragmentsUser.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MainUserActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private ImageButton logoutBtn;
    private BottomNavigationView navigationView;

    //fragments
    private Fragment homeFragment, notificationFragment, ordersFragment, profileFragment, searchFragment;
    private Fragment activeFragment;
    private FragmentManager fragmentManager;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);

        //init UI Views
        logoutBtn = findViewById(R.id.logoutBtn);
        navigationView = findViewById(R.id.navigationView);

        //init Fragments
        initFragments();

        //init firebase and progressDialog
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
        checkUser();

        navigationView.setOnNavigationItemSelectedListener(this);
    }

    private void initFragments() {
        homeFragment = new HomeFragment();
        searchFragment = new SearchFragment();
        ordersFragment = new OrdersFragment();
        notificationFragment = new NotificationFragment();
        profileFragment = new ProfileFragment();

        fragmentManager = getSupportFragmentManager();
        activeFragment = homeFragment;

        fragmentManager.beginTransaction()
                .add(R.id.frameLayout, homeFragment, "Home Fragment")
                .commit();

        fragmentManager.beginTransaction()
                .add(R.id.frameLayout, searchFragment, "Search Fragment")
                .hide(searchFragment)
                .commit();

        fragmentManager.beginTransaction()
                .add(R.id.frameLayout, ordersFragment, "Orders Fragment")
                .hide(ordersFragment)
                .commit();

        fragmentManager.beginTransaction()
                .add(R.id.frameLayout, notificationFragment, "Notifications Fragment")
                .hide(notificationFragment)
                .commit();

        fragmentManager.beginTransaction()
                .add(R.id.frameLayout, profileFragment, "Profile Fragment")
                .hide(profileFragment)
                .commit();
    }

    private void loadHomeFragment() {
        fragmentManager.beginTransaction().hide(activeFragment).show(homeFragment).commit();
        activeFragment = homeFragment;
    }

    private void loadSearchFragment() {
        fragmentManager.beginTransaction().hide(activeFragment).show(searchFragment).commit();
        activeFragment = searchFragment;
    }

    private void loadOrderFragment() {
        fragmentManager.beginTransaction().hide(activeFragment).show(ordersFragment).commit();
        activeFragment = ordersFragment;
    }

    private void loadNotificationFragment() {
        fragmentManager.beginTransaction().hide(activeFragment).show(notificationFragment).commit();
        activeFragment = notificationFragment;
    }

    private void loadProfileFragment() {
        fragmentManager.beginTransaction().hide(activeFragment).show(profileFragment).commit();
        activeFragment = profileFragment;
    }

    private void makeMeOffline() {
        //after logging in, make user online
        progressDialog.setMessage("Logging Out...");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "false");

        //update value to DB
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(aVoid -> {
                    //update successfully
                    firebaseAuth.signOut();
                    checkUser();
                })
                .addOnFailureListener(e -> {
                    //failed updating
                    progressDialog.dismiss();
                    Toast.makeText(MainUserActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MainUserActivity.this, LoginActivity.class));
            finish();
        } else {
            loadMyInfo();
        }
    }

    private void loadMyInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String name = "" + ds.child("name").getValue();
                            String accountType = "" + ds.child("accountType").getValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //handle bottom nav clicks
        switch (item.getItemId()) {
            case R.id.nav_home:
                //load Home Fragment
                loadHomeFragment();
                return true;
            case R.id.nav_search:
                //load Search Fragment
                loadSearchFragment();
                return true;
            case R.id.nav_orders:
                //load Order Fragment
                loadOrderFragment();
                return true;
            case R.id.nav_notify:
                //load Notification Fragment
                loadNotificationFragment();
                return true;
            case R.id.nav_profile:
                //load Profile Fragment
                loadProfileFragment();
                return true;
        }
        return false;
    }

}