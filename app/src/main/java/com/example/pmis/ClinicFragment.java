package com.example.pmis;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ClinicFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ClinicFragment extends Fragment {
    private View view;
    private CardView cvDrugList, cvClinicInfo, cvPatientList,cvProcedures;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private TextView tvClinicName, tvClinicAddress;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private String userID;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ClinicFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ClinicFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ClinicFragment newInstance(String param1, String param2) {
        ClinicFragment fragment = new ClinicFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_clinic, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        cvClinicInfo = (CardView)view.findViewById(R.id.cvClinicInfo);
        cvClinicInfo.setOnClickListener(clinicInfo);
        cvProcedures = (CardView)view.findViewById(R.id.cvProcedures);
        cvProcedures.setOnClickListener(procedureList);
        cvDrugList = (CardView)view.findViewById(R.id.cvDrugList);
        cvDrugList.setOnClickListener(drugList);
        tvClinicName = view.findViewById(R.id.tvClinicName);
        tvClinicAddress = view.findViewById(R.id.tvClinicAddress);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference clinicRef = mFirebaseDatabase.getReference("Clinic").child(userID);
        clinicRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvClinicName.setText(snapshot.child("clinicName").getValue(String.class));
                tvClinicAddress.setText(snapshot.child("address").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        cvPatientList = view.findViewById(R.id.cvPatientList);
        cvPatientList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),PatientActivity.class);
                startActivity(intent);
            }
        });
    }
    public final View.OnClickListener clinicInfo = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getContext(),HomeActivity.class);
            startActivity(intent);
        }
    };
    public final View.OnClickListener drugList = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getContext(),DrugListActivity.class);
            startActivity(intent);
        }
    };
    public final View.OnClickListener procedureList = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getContext(),ProceduresActivity.class);
            startActivity(intent);
        }
    };
}