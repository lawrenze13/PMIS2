package com.example.pmis;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.pmis.Model.AppointmentStatus;
import com.example.pmis.Model.Clinic;
import com.example.pmis.Model.UserInfo;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Set;

public class HomeActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {

    }
    private static final String SHARED_PREF_NAME = "myPref";
    private static final String KEY_NAME = "name";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_CONTACT_NO = "contactNo";
    private static final String KEY_DOC_NAME = "docName";
    private static final String KEY_LICENSE = "license";
    private static final String KEY_DEGREE = "degree";
    private static final String TAG = "HOME_ACTIVITY";
    private AppBarConfiguration mAppBarConfiguration;
    private TextView lblFullName, lblClinic;
    private Button btnSetupProfile;
    private ImageView imgProfile;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private String userID;
    private StorageReference storageReference;
    private FirebaseStorage firebaseStorage;
    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;
    private BottomAppBar bottomAppBar;
    private FloatingActionButton floatingActionButton2;
    private String selectedItem = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference("Users");
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        floatingActionButton2 = (FloatingActionButton) findViewById(R.id.floatingActionButton2);
        floatingActionButton2.setOnClickListener(addButton);
        bottomAppBar = (BottomAppBar) findViewById(R.id.bottomAppBar);
        bottomAppBar.replaceMenu(R.menu.app_bar_menu);
        bottomAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch (id){
                    case R.id.menuAbout:
                        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
                        connectedRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                boolean connected = snapshot.getValue(Boolean.class);
                                if(connected){
                                    Toast.makeText(HomeActivity.this, "You're currently Online.", Toast.LENGTH_LONG).show();
                                }else{
                                    Toast.makeText(HomeActivity.this, "You're currently Offline.", Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        break;
                        case R.id.menuSettings:
                            SettingsNavigationFragment settingsNavigationFragment = new SettingsNavigationFragment();
                            settingsNavigationFragment.show(getSupportFragmentManager(), "TAG");
                            break;

                }
                return false;
            }
        });
//        bottomAppBar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SettingsNavigationFragment settingsNavigationFragment = new SettingsNavigationFragment();
//                settingsNavigationFragment.show(getSupportFragmentManager(), "TAG");
//            }
//        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_dashboard, R.id.profileFragment, R.id.clinicFragment, R.id.patientFragment,  R.id.appointmentFragment, R.id.reportBetaFragment, R.id.statisticsFragment)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        drawerItems();
        clinicFragmentHeader();
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference myRef = mFirebaseDatabase.getReference("Clinic").child(userID);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChildren()) {
                    String name = snapshot.getValue(Clinic.class).getClinicName();
                    String contactNo = snapshot.getValue(Clinic.class).getContactNo();
                    String address = snapshot.getValue(Clinic.class).getAddress();
                    String degree = snapshot.getValue(Clinic.class).getDegree();
                    String license = snapshot.getValue(Clinic.class).getLicense();
                    String photourl = snapshot.getValue(Clinic.class).getPhotoUrl();
                    editor.putString(KEY_NAME, name );
                    editor.putString(KEY_CONTACT_NO, contactNo );
                    editor.putString(KEY_ADDRESS,address);
                    editor.putString(KEY_DEGREE,degree);
                    editor.putString(KEY_LICENSE, license);
                    editor.putString("photoURL", photourl);
                    editor.apply();
//                    if (!snapshot.exists()) {
//                        Intent intent = new Intent(HomeActivity.this, NewUserActivity.class);
//                        startActivity(intent);
//                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private final View.OnClickListener addButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String[] choices = {"Add Patient", "Add Appointment", "Add Payment"};
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            builder.setTitle("Add");
            builder.setSingleChoiceItems(choices,0 , new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    selectedItem = choices[which];

                }
            });
            builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(selectedItem.equals("Add Patient") || selectedItem.equals("")){
                        Intent intent = new Intent(HomeActivity.this, AddPatientActivity.class);
                        intent.putExtra("action", "add");
                        startActivity(intent);
                    }else if(selectedItem.equals("Add Appointment")){
                        Intent intent = new Intent(HomeActivity.this, PatientActivity.class);
                        startActivity(intent);
                    }else if(selectedItem.equals("Add Payment")){
                        Intent intent = new Intent(HomeActivity.this, PatientActivity.class);
                        startActivity(intent);
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    };
    private void clinicFragmentHeader() {

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_logout:
                mAuth.signOut();
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        bottomAppBar.replaceMenu(R.menu.app_bar_menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }



    @SuppressLint("SetTextI18n")
    public void drawerItems(){


       myRef.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot datasnapshot) {
               setDrawerHeader(datasnapshot);
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });
    }

    public void setDrawerHeader(DataSnapshot datasnapshot) {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        lblFullName = (TextView)headerView.findViewById(R.id.lblFullName);
        imgProfile = (ImageView) headerView.findViewById(R.id.imgProfile);
        lblClinic = (TextView)headerView.findViewById(R.id.lblClinic);

        String fullName;
            UserInfo uInfo = new UserInfo();
            uInfo.setFirstName(datasnapshot.child(userID).getValue(UserInfo.class).getFirstName());
            uInfo.setLastName(datasnapshot.child(userID).getValue(UserInfo.class).getLastName());
            uInfo.setAge(datasnapshot.child(userID).getValue(UserInfo.class).getAge());
            uInfo.setEmail(datasnapshot.child(userID).getValue(UserInfo.class).getEmail());
            uInfo.setSex(datasnapshot.child(userID).getValue(UserInfo.class).getSex());

            fullName = uInfo.firstName + " " + uInfo.lastName;
              String  docName = "Dr. " +  fullName + " D.M.D";
              editor.putString(KEY_DOC_NAME, docName);
              editor.apply();

        lblClinic.setText(uInfo.email);
            lblFullName.setText(fullName);
        String photoUrl = datasnapshot.getValue(UserInfo.class).getPhotoUrl();
            storageReference = FirebaseStorage.getInstance().getReference().child("images/profilePics/" + userID);
            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide
                            .with(getApplicationContext())
                            .asBitmap()
                            .load(uri)
                            .centerCrop()
                            .into(imgProfile);
                }
            });


    }

}