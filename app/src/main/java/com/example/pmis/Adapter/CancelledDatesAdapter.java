package com.example.pmis.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pmis.Model.PatientScheduleFacade;
import com.example.pmis.R;

import java.util.List;

public class CancelledDatesAdapter extends RecyclerView.Adapter {
    private static final String TAG = "CANCELLED_DATES";
    List<PatientScheduleFacade> fetchPatientScheduleFacade;
  private Context context;

    public CancelledDatesAdapter(Context context,List<PatientScheduleFacade> fetchPatientScheduleFacade){
        this.fetchPatientScheduleFacade = fetchPatientScheduleFacade;
        this.context = context;

    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.appointment_statistics_card_view, parent,false);
        ViewHolderClass viewHolderClass = new ViewHolderClass(view);
        return viewHolderClass;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolderClass viewHolderClass = (ViewHolderClass)holder;

        PatientScheduleFacade patientScheduleFacade = fetchPatientScheduleFacade.get(position);
        Log.d(TAG, "PATIENT DATE: " + patientScheduleFacade.getDate());
        viewHolderClass.tvDate.setText(patientScheduleFacade.getDate());
        String time = patientScheduleFacade.getStartTime() + " - " + patientScheduleFacade.getEndTime();
        viewHolderClass.tvTime.setText(time);
    }

    @Override
    public int getItemCount() {
        return fetchPatientScheduleFacade.size();
    }
    private class ViewHolderClass extends RecyclerView.ViewHolder {
        TextView tvDate, tvTime;
        public ViewHolderClass(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);

        }
    }
}
