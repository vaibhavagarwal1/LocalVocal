package com.dev.localvocal;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterVendorActivity extends AppCompatActivity implements LocationListener {

    //permission constants
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    //image pick constant
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;
    //UI Views
    private ImageButton backBtn, gpsBtn;
    private CircularImageView profileIv;
    private EditText firmNameEt, firmTypeEt, gstNoEt, panNoEt, companyStatusEt;
    private EditText nameEt, phoneEt, countryEt, stateEt, cityEt, addressEt;
    private EditText emailEt, passwordEt, cPasswordEt, designationEt, pincodeEt;
    private Button registerBtn;
    private TextView registerAdminTv;
    //permission arrays
    private String[] locationPermissions;
    private String[] cameraPermissions;
    private String[] storagePermissions;
    //image picked uri
    private Uri imageUri;
    //Location
    private LocationManager locationManager;
    private double latitude = 0.0, longitude = 0.0;
    //Firebase
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private String firmName, firmType, gstNo, panNo, companyStatus, name, phone;
    private String country, state, city, address, email, password, cPassword;
    private String designation, pincode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_vendor);

        //init UI Views
        backBtn = findViewById(R.id.backBtn);
        gpsBtn = findViewById(R.id.gpsBtn);
        profileIv = findViewById(R.id.profileIv);
        firmNameEt = findViewById(R.id.firmNameEt);
        firmTypeEt = findViewById(R.id.firmTypeEt);
        gstNoEt = findViewById(R.id.gstNoEt);
        panNoEt = findViewById(R.id.panNoEt);
        companyStatusEt = findViewById(R.id.companyStatusEt);
        nameEt = findViewById(R.id.nameEt);
        phoneEt = findViewById(R.id.phoneEt);
        countryEt = findViewById(R.id.countryEt);
        stateEt = findViewById(R.id.stateEt);
        cityEt = findViewById(R.id.cityEt);
        addressEt = findViewById(R.id.addressEt);
        pincodeEt = findViewById(R.id.pincodeEt);
        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        cPasswordEt = findViewById(R.id.cPasswordEt);
        registerBtn = findViewById(R.id.registerBtn);
        registerAdminTv = findViewById(R.id.registerAdminTv);
        designationEt = findViewById(R.id.designationEt);

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //init permission array
        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        backBtn.setOnClickListener(v -> onBackPressed());

        gpsBtn.setOnClickListener(v -> {
            //detect Current Location
            if (checkLocationPermission()) {
                //already allowed
                detectLocation();
            } else {
                //not allowed. request
                requestLocationPermission();
            }
        });

        profileIv.setOnClickListener(v -> {
            //pick image
            showImagePickDialog();
        });

        registerBtn.setOnClickListener(v -> {
            //register User
            inputData();
        });

        registerAdminTv.setOnClickListener(v -> {
            //open register vendor activity
            startActivity(new Intent(RegisterVendorActivity.this, RegisterAdminActivity.class));
        });
    }

    private void inputData() {
        //input data
        firmName = firmNameEt.getText().toString().trim();
        firmType = firmTypeEt.getText().toString().trim();
        gstNo = gstNoEt.getText().toString().trim();
        panNo = panNoEt.getText().toString().trim();
        companyStatus = companyStatusEt.getText().toString().trim();
        name = nameEt.getText().toString().trim();
        phone = phoneEt.getText().toString().trim();
        country = countryEt.getText().toString().trim();
        state = stateEt.getText().toString().trim();
        city = cityEt.getText().toString().trim();
        address = addressEt.getText().toString().trim();
        email = emailEt.getText().toString().trim();
        password = passwordEt.getText().toString().trim();
        cPassword = cPasswordEt.getText().toString().trim();
        designation = designationEt.getText().toString().trim();
        pincode = pincodeEt.getText().toString().trim();

        //validate data
        if (TextUtils.isEmpty(firmName)) {
            Toast.makeText(this, "Enter Firm Name...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(firmType)) {
            Toast.makeText(this, "Enter Firm Type...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!validateGST(gstNo)) {
            Toast.makeText(this, "Enter valid GST No....", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!validatePAN(panNo)) {
            Toast.makeText(this, "Enter valid PAN No....", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(companyStatus)) {
            Toast.makeText(this, "Enter Company Status...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Enter Name...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Enter Phone No....", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(designation)) {
            Toast.makeText(this, "Enter Designation....", Toast.LENGTH_SHORT).show();
            return;
        }
        if (latitude == 0.0 || longitude == 0.0) {
            Toast.makeText(this, "Please click GPS button to detect location...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid Email pattern...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be atleast 6 character long...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(cPassword)) {
            Toast.makeText(this, "Password doesn't match...", Toast.LENGTH_SHORT).show();
            return;
        }

        createAccount();
    }

    private void createAccount() {
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        //create account
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    //account Created
                    saveToFirebaseDatabase();
                })
                .addOnFailureListener(e -> {
                    //failed to create account
                    progressDialog.dismiss();
                    Toast.makeText(RegisterVendorActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveToFirebaseDatabase() {
        progressDialog.setMessage("Saving Account Info...");

        String timeStamp = "" + System.currentTimeMillis();

        if (imageUri == null) {
            //save info without image

            //setup data to save
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("uid", "" + firebaseAuth.getUid());
            hashMap.put("firmName", "" + firmName);
            hashMap.put("firmType", "" + firmType);
            hashMap.put("gstNo", "" + gstNo);
            hashMap.put("panNo", "" + panNo);
            hashMap.put("companyStatus", "" + companyStatus);
            hashMap.put("name", "" + name);
            hashMap.put("phone", "" + phone);
            hashMap.put("country", "" + country);
            hashMap.put("state", "" + state);
            hashMap.put("city", "" + city);
            hashMap.put("address", "" + address);
            hashMap.put("pincode", "" + pincode);
            hashMap.put("designation", "" + designation);
            hashMap.put("email", "" + email);
            hashMap.put("latitude", "" + latitude);
            hashMap.put("longitude", "" + longitude);
            hashMap.put("timestamp", "" + timeStamp);
            hashMap.put("accountType", "Vendor");
            hashMap.put("online", "true");
            hashMap.put("shopOpen", "true");
            hashMap.put("profileImage", "");

            //save to DB
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).setValue(hashMap)
                    .addOnSuccessListener(aVoid -> {
                        //DB Updated
                        progressDialog.dismiss();
                        startActivity(new Intent(RegisterVendorActivity.this, MainVendorActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        //failed to add to db
                        progressDialog.dismiss();
                        startActivity(new Intent(RegisterVendorActivity.this, MainVendorActivity.class));
                        finish();
                    });
        } else {
            //save info with image

            //name and path of the image
            String filePathAndName = "profile_image/" + "" + firebaseAuth.getUid();
            //upload image
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        //get url of uploaded image
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        Uri downloadImageUri = uriTask.getResult();

                        if (uriTask.isSuccessful()) {
                            //setup data to save
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("uid", "" + firebaseAuth.getUid());
                            hashMap.put("firmName", "" + firmName);
                            hashMap.put("firmType", "" + firmType);
                            hashMap.put("gstNo", "" + gstNo);
                            hashMap.put("panNo", "" + panNo);
                            hashMap.put("companyStatus", "" + companyStatus);
                            hashMap.put("name", "" + name);
                            hashMap.put("phone", "" + phone);
                            hashMap.put("country", "" + country);
                            hashMap.put("state", "" + state);
                            hashMap.put("city", "" + city);
                            hashMap.put("address", "" + address);
                            hashMap.put("pincode", "" + pincode);
                            hashMap.put("designation", "" + designation);
                            hashMap.put("email", "" + email);
                            hashMap.put("latitude", "" + latitude);
                            hashMap.put("longitude", "" + longitude);
                            hashMap.put("timestamp", "" + timeStamp);
                            hashMap.put("accountType", "Vendor");
                            hashMap.put("online", "true");
                            hashMap.put("shopOpen", "true");
                            hashMap.put("profileImage", "" + downloadImageUri);

                            //save to DB
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                            reference.child(firebaseAuth.getUid()).setValue(hashMap)
                                    .addOnSuccessListener(aVoid -> {
                                        //DB Updated
                                        progressDialog.dismiss();
                                        startActivity(new Intent(RegisterVendorActivity.this, MainVendorActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        //failed to add to db
                                        progressDialog.dismiss();
                                        startActivity(new Intent(RegisterVendorActivity.this, MainVendorActivity.class));
                                        finish();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterVendorActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private boolean validatePAN(String panNo) {
        // Regex to check valid PAN Card number.
        String regex = "[A-Z]{5}[0-9]{4}[A-Z]";

        // Compile the ReGex
        Pattern p = Pattern.compile(regex);

        // If the PAN Card number
        // is empty return false
        if (panNo == null) {
            return false;
        }

        // Pattern class contains matcher() method
        // to find matching between given
        // PAN Card number using regular expression.
        Matcher m = p.matcher(panNo);

        // Return if the PAN Card number
        // matched the ReGex
        return m.matches();
    }

    private boolean validateGST(String gstNo) {
        // Regex to check valid
        // GST (Goods and Services Tax) number
        String regex = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$";

        // Compile the ReGex
        Pattern p = Pattern.compile(regex);

        // If the string is empty
        // return false
        if (gstNo == null) {
            return false;
        }

        // Pattern class contains matcher()
        // method to find the matching
        // between the given string
        // and the regular expression.
        Matcher m = p.matcher(gstNo);

        // Return if the string
        // matched the ReGex
        return m.matches();
    }

    private void showImagePickDialog() {
        //option to display in dialog
        String[] options = {"Camera", "Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            //camera Clicked
                            if (checkCameraPermission()) {
                                //camera permission
                                pickFromCamera();
                            } else {
                                //not allowed, request
                                requestCameraPermission();
                            }
                        } else {
                            //gallery Clicked
                            if (checkStoragePermission()) {
                                //storage permissions allowed
                                pickFromGallery();
                            } else {
                                //not allowed, request
                                requestStoragePermission();
                            }
                        }
                    }
                })
                .show();
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image Description");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private void detectLocation() {
        Toast.makeText(this, "Please wait...", Toast.LENGTH_LONG).show();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    private void findAddress() {
        //find address, country, state, city, pinCode
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            String address = addresses.get(0).getAddressLine(0); //Complete Address
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String pincode = addresses.get(0).getPostalCode();

            //set Address
            countryEt.setText(country);
            addressEt.setText(address);
            cityEt.setText(city);
            stateEt.setText(state);
            pincodeEt.setText(pincode);

        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                (PackageManager.PERMISSION_GRANTED);
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, locationPermissions, LOCATION_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) ==
                (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        //location detected
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        findAddress();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        //GPS/Location is disabled
        Toast.makeText(this, "Please Turn on Location...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted) {
                        //permission allowed
                        detectLocation();
                    } else {
                        //permission denied
                        Toast.makeText(this, "Location permission is necessary...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        //permission allowed
                        pickFromCamera();
                    } else {
                        //permission denied
                        Toast.makeText(this, "Camera permissions are necessary...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        //permission allowed
                        pickFromGallery();
                    } else {
                        //permission denied
                        Toast.makeText(this, "Storage permission is necessary...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //get picked image
                imageUri = data.getData();
                //set to imageView
                profileIv.setImageURI(imageUri);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //set to imageView
                profileIv.setImageURI(imageUri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}