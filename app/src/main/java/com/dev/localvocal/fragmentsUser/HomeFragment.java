package com.dev.localvocal.fragmentsUser;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.localvocal.Constants;
import com.dev.localvocal.LoginActivity;
import com.dev.localvocal.ProfileEditUserActivity;
import com.dev.localvocal.R;
import com.dev.localvocal.adapters.AdapterCategories;
import com.dev.localvocal.models.ModelCategories;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class HomeFragment extends Fragment {

    //context for Fragment
    Context context;
    //ArrayList for Categories
    ArrayList<ModelCategories> modelCategories;
    //Adapter for category
    AdapterCategories adapterCategories;
    //UI views
    private RecyclerView popularCategoryRv;
    private ImageButton editProfileBtn, logoutBtn;
    private ImageView profileIv;
    private TextView nameTv, emailTv, phoneTv;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //init UI Views
        popularCategoryRv = view.findViewById(R.id.popularCategoryRv);
        editProfileBtn = view.findViewById(R.id.editProfileBtn);
        logoutBtn = view.findViewById(R.id.logoutBtn);
        profileIv = view.findViewById(R.id.profileIv);
        nameTv = view.findViewById(R.id.nameTv);
        emailTv = view.findViewById(R.id.emailTv);
        phoneTv = view.findViewById(R.id.phoneTv);

        //init firebase and progressDialog
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //load my info
        loadMyInfo();

        logoutBtn.setOnClickListener(v -> {
            //make offline
            //sign out
            //go to login activity
            makeMeOffline();
        });

        editProfileBtn.setOnClickListener(v -> {
            //Start edit profile activity
            startActivity(new Intent(context, ProfileEditUserActivity.class));
        });

        initPopularCategories();

        return view;
    }

    private void initPopularCategories() {
        //initializing ArrayList
        modelCategories = new ArrayList<>();
        for (int index = 0; index < Constants.categoriesIcons.length; index++) {
            ModelCategories model = new ModelCategories(Constants.categoriesIcons[index], Constants.categoriesNames[index]);
            modelCategories.add(model);
        }

        //Design Relative Layout
        GridLayoutManager layoutManager = new GridLayoutManager(context, 4);
        popularCategoryRv.setLayoutManager(layoutManager);
        //initialize Adapter
        adapterCategories = new AdapterCategories(getContext(), modelCategories);
        //set adapter to recycler view
        popularCategoryRv.setAdapter(adapterCategories);
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
                    Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
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
                            String email = "" + ds.child("email").getValue();
                            String phone = "" + ds.child("phone").getValue();
                            String profileImage = "" + ds.child("profileImage").getValue();

                            nameTv.setText(name);
                            emailTv.setText(email);
                            phoneTv.setText(phone);
                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray).into(profileIv);
                            } catch (Exception e) {
                                profileIv.setImageResource(R.drawable.ic_person_gray);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}