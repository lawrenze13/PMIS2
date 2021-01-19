package com.example.pmis;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;


public class SettingsNavigationFragment extends BottomSheetDialogFragment {

    public SettingsNavigationFragment(){

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings_navigation, container, false);
        NavigationView navigationView = view.findViewById(R.id.settingsNavigations);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id){
                    case R.id.menuProfileInfo:
                        Toast.makeText(getActivity(), "PROFILE INFO", Toast.LENGTH_LONG).show();
                        break;
                    case R.id.menuChangePass:
                        Toast.makeText(getActivity(), "change", Toast.LENGTH_LONG).show();
                        break;
                    case R.id.menuLogout:
                        Toast.makeText(getActivity(), "Logout", Toast.LENGTH_LONG).show();
                        break;
                }
                return false;
            }
        });
        return view;
    }
}