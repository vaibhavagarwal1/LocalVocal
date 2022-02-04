package com.dev.localvocal;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
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
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.chaos.view.PinView;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hbb20.CountryCodePicker;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class RegisterUserActivity extends AppCompatActivity implements LocationListener {

    //permission constants
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    //image pick constant
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;
    //TAG
    private static final String TAG = "MAIN_TAG";
    //UI Views
    private ImageButton backBtn, gpsBtn, moreBtn;
    private CircularImageView profileIv;
    private EditText nameEt, phoneEt, countryEt, stateEt, cityEt, addressEt;
    private EditText emailEt, passwordEt, cPasswordEt;
    private Button registerBtn, codeSubmitBtn;
    private TextView alreadyRegisteredTv, dobTv, genderTv, titleTv, resendCodeTv;
    private CountryCodePicker countryCodeCcp;
    private RelativeLayout dataRl, otpVerificationRl;
    private PinView codePv;
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
    private String name, phone, country, state, city, address, email, password, countryCode;
    private String cPassword, dob, gender;
    //OTP My Verification ID
    private String mVerificationId;
    //OTP Resend Token
    private PhoneAuthProvider.ForceResendingToken forceResendingToken;
    //OTP On Verification State changes call back
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        //init UI Views
        backBtn = findViewById(R.id.backBtn);
        titleTv = findViewById(R.id.titleTv);
        gpsBtn = findViewById(R.id.gpsBtn);
        moreBtn = findViewById(R.id.moreBtn);
        profileIv = findViewById(R.id.profileIv);
        nameEt = findViewById(R.id.nameEt);
        phoneEt = findViewById(R.id.phoneEt);
        countryEt = findViewById(R.id.countryEt);
        stateEt = findViewById(R.id.stateEt);
        cityEt = findViewById(R.id.cityEt);
        addressEt = findViewById(R.id.addressEt);
        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        cPasswordEt = findViewById(R.id.cPasswordEt);
        registerBtn = findViewById(R.id.registerBtn);
        alreadyRegisteredTv = findViewById(R.id.alreadyRegisteredTv);
        dobTv = findViewById(R.id.dobTv);
        genderTv = findViewById(R.id.genderTv);
        countryCodeCcp = findViewById(R.id.countryCodeCcp);
        dataRl = findViewById(R.id.dataRl);
        otpVerificationRl = findViewById(R.id.otpVerificationRl);
        codeSubmitBtn = findViewById(R.id.codeSubmitBtn);
        codePv = findViewById(R.id.codePv);
        resendCodeTv = findViewById(R.id.resendCodeTv);

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //init permission array
        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        showDataUI();

        backBtn.setOnClickListener(v -> {
            if (titleTv.getText().toString().trim().equals("Register User")) {
                onBackPressed();
            } else {
                showDataUI();
            }
        });

        dobTv.setOnClickListener(v -> datePickerDialog());

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

        alreadyRegisteredTv.setOnClickListener(v -> {
            //open register vendor activity
            startActivity(new Intent(RegisterUserActivity.this, LoginActivity.class));
            finish();
        });

        genderTv.setOnClickListener(v -> genderDialog());

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                //This callback will be invoked in two situations:
                //1 - Instant Verification. In some cases the phone number can be Instantly
                //    verified without needing to send or enter a verification code.
                //2 - Auto Retrieval. On some devices Google Play Services can automatically
                //    detect the incoming verification SMS and perform it without user actions.
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                //This callback is invoked in an invalid request for verification made,
                //for instance if the phone nimber format is not valid.
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                //The SMS verification code has been sent to the provided phone number, we now need
                //to ask the user to enter the code and the construct a credential by combining the
                //code with a verification ID
                mVerificationId = s;
            }
        };

        registerBtn.setOnClickListener(v -> {
            //for Register User, go to Verification Code
            //showOTPVerificationUI();
            inputData();
        });

        resendCodeTv.setOnClickListener(v -> resendVerificationCode(phone, forceResendingToken));

        codeSubmitBtn.setOnClickListener(v -> {
            String code = codePv.getText().toString().trim();
            if (TextUtils.isEmpty(code) || !(code.length() == 6)) {
                Toast.makeText(RegisterUserActivity.this, "Please Enter the full Verification Code...", Toast.LENGTH_SHORT).show();
            } else {
                verifyPhoneNumberWithCode(mVerificationId, code);
            }
        });

        //popup Menu
        PopupMenu popupMenu = new PopupMenu(RegisterUserActivity.this, moreBtn);
        //add Menu item to our menu
        popupMenu.getMenu().add("Register Vendor");
        popupMenu.getMenu().add("Register Admin");
        //handle menu item click
        popupMenu.setOnMenuItemClickListener(item -> {
            if(item.getTitle() == "Register Vendor") {
                startActivity(new Intent(RegisterUserActivity.this, RegisterVendorActivity.class));
                finish();
            } else if(item.getTitle() == "Register Admin") {
                startActivity(new Intent(RegisterUserActivity.this, RegisterAdminActivity.class));
                finish();
            }
            return true;
        });

        //show more options
        moreBtn.setOnClickListener(v -> {
            //show popup menu
            popupMenu.show();
        });

    }

    private void startPhoneNumberVerification(String phone) {
        progressDialog.setMessage("Verifying Phone Number");

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
        progressDialog.dismiss();
    }

    private void resendVerificationCode(String phone, PhoneAuthProvider.ForceResendingToken token) {
        progressDialog.setMessage("Resending Code");

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .setForceResendingToken(token)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        progressDialog.setMessage("Verifying Code");

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        firebaseAuth.getCurrentUser().linkWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    //Successfully linked the credentials, Now adding all the data into database
                    saveToFirebaseDatabase();
                })
                .addOnFailureListener(e -> {
                    //failed to connect Credentials
                    progressDialog.dismiss();
                    Toast.makeText(RegisterUserActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showOTPVerificationUI() {
        //show OTPVerification UI and hide Data UI
        dataRl.setVisibility(View.GONE);
        otpVerificationRl.setVisibility(View.VISIBLE);

        moreBtn.setVisibility(View.GONE);
        gpsBtn.setVisibility(View.GONE);

        titleTv.setText("OTP Verification");
    }

    private void showDataUI() {
        //hide Data UI and show OTPVerification UI
        dataRl.setVisibility(View.VISIBLE);
        otpVerificationRl.setVisibility(View.GONE);

        moreBtn.setVisibility(View.VISIBLE);
        gpsBtn.setVisibility(View.VISIBLE);

        titleTv.setText("Register User");
    }

    private void inputData() {
        //get data
        name = nameEt.getText().toString().trim();
        countryCode = countryCodeCcp.getSelectedCountryCode().trim();
        phone = "+" + countryCode + "" + phoneEt.getText().toString().trim();
        country = countryEt.getText().toString().trim();
        state = stateEt.getText().toString().trim();
        city = cityEt.getText().toString().trim();
        address = addressEt.getText().toString().trim();
        email = emailEt.getText().toString().trim();
        password = passwordEt.getText().toString().trim();
        cPassword = cPasswordEt.getText().toString().trim();
        dob = dobTv.getText().toString().trim();
        gender = genderTv.getText().toString().trim();

        //validate data
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Enter Name...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Enter Phone Number...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(dob)) {
            Toast.makeText(this, "Select Date of Birth...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(gender)) {
            Toast.makeText(this, "Select Gender...", Toast.LENGTH_SHORT).show();
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
                    //saveToFirebaseDatabase();
                    showOTPVerificationUI();
                    startPhoneNumberVerification(phone);
                })
                .addOnFailureListener(e -> {
                    //failed to create account
                    progressDialog.dismiss();
                    Toast.makeText(RegisterUserActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            hashMap.put("name", "" + name);
            hashMap.put("phone", "" + phone);
            hashMap.put("gender", "" + gender);
            hashMap.put("country", "" + country);
            hashMap.put("state", "" + state);
            hashMap.put("city", "" + city);
            hashMap.put("address", "" + address);
            hashMap.put("dob", "" + dob);
            hashMap.put("email", "" + email);
            hashMap.put("latitude", "" + latitude);
            hashMap.put("longitude", "" + longitude);
            hashMap.put("timestamp", "" + timeStamp);
            hashMap.put("accountType", "User");
            hashMap.put("online", "true");
            hashMap.put("profileImage", "");

            //save to DB
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).setValue(hashMap)
                    .addOnSuccessListener(aVoid -> {
                        //DB Updated
                        progressDialog.dismiss();
                        startActivity(new Intent(RegisterUserActivity.this, MainUserActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        //failed to add to db
                        progressDialog.dismiss();
                        startActivity(new Intent(RegisterUserActivity.this, MainUserActivity.class));
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
                            hashMap.put("name", "" + name);
                            hashMap.put("phone", "" + phone);
                            hashMap.put("gender", "" + gender);
                            hashMap.put("country", "" + country);
                            hashMap.put("state", "" + state);
                            hashMap.put("city", "" + city);
                            hashMap.put("address", "" + address);
                            hashMap.put("dob", "" + dob);
                            hashMap.put("email", "" + email);
                            hashMap.put("latitude", "" + latitude);
                            hashMap.put("longitude", "" + longitude);
                            hashMap.put("timestamp", "" + timeStamp);
                            hashMap.put("accountType", "User");
                            hashMap.put("online", "true");
                            hashMap.put("profileImage", "" + downloadImageUri);

                            //save to DB
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                            reference.child(firebaseAuth.getUid()).setValue(hashMap)
                                    .addOnSuccessListener(aVoid -> {
                                        //DB Updated
                                        progressDialog.dismiss();
                                        startActivity(new Intent(RegisterUserActivity.this, MainUserActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        //failed to add to db
                                        progressDialog.dismiss();
                                        startActivity(new Intent(RegisterUserActivity.this, MainUserActivity.class));
                                        finish();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterUserActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void genderDialog() {
        String[] options = {"Male", "Female"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Gender")
                .setItems(options, (dialog, which) -> {
                    //get picked gender
                    String gender = options[which];
                    //set pick gender
                    genderTv.setText(gender);
                }).show();
    }

    private void datePickerDialog() {
        //Get current date to set on Calendar
        Calendar calendar = Calendar.getInstance();
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        //Date pick dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            DecimalFormat mFormat = new DecimalFormat("00");
            String pDay = mFormat.format(dayOfMonth);
            String pMonth = mFormat.format(month);
            String pYear = "" + year;
            String pDate = pDay + "/" + pMonth + "/" + pYear;
            dobTv.setText(pDate);
        }, mYear, mMonth, mDay);

        //show Dialog
        datePickerDialog.show();
    }

    private void showImagePickDialog() {
        //option to display in dialog
        String[] options = {"Camera", "Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, (dialog, which) -> {
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
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
    }

    private void findAddress() {
        //find address, country, state, city
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            String address = addresses.get(0).getAddressLine(0); //Complete Address
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();

            //set Address
            countryEt.setText(country);
            addressEt.setText(address);
            cityEt.setText(city);
            stateEt.setText(state);

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