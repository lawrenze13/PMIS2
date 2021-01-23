package com.example.pmis.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pmis.Model.PaymentReportFacade;
import com.example.pmis.Model.ReportAppointment;
import com.example.pmis.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ReportAppointmentAdapter extends RecyclerView.Adapter{
    private static final String TAG = "REPORT_PAYMENT_ADAPTER";
    List<ReportAppointment> fetchReportAppointment;
    String name;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef, drugRef;
    private String userID;

    public Context context;
    public ReportAppointmentAdapter(Context context,List<ReportAppointment> fetchReportAppointment){
        this.fetchReportAppointment = fetchReportAppointment;
        this.context = context;

    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_appointment_card_view, parent, false);
        ViewHolderClass viewHolderClass = new ViewHolderClass(view);
        return viewHolderClass;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolderClass viewHolderClass = (ViewHolderClass)holder;
        ReportAppointment reportAppointment = fetchReportAppointment.get(position);
        viewHolderClass.tvRAName.setText(reportAppointment.getPatientName());
        viewHolderClass.tvRADate.setText(reportAppointment.getDate());
        viewHolderClass.tvRAStart.setText(reportAppointment.getStartTime());
        viewHolderClass.tvRAEnd.setText(reportAppointment.getEndTime());
        viewHolderClass.tvRAStatus.setText(reportAppointment.getStatus());
    }

    @Override
    public int getItemCount() {
        return fetchReportAppointment.size();
    }
    public class ViewHolderClass extends RecyclerView.ViewHolder {
        TextView tvRAName, tvRADate, tvRAStart, tvRAEnd, tvRAStatus;

        public ViewHolderClass(@NonNull View itemView) {
            super(itemView);
            tvRAName = itemView.findViewById(R.id.tvRAName);
            tvRADate = itemView.findViewById(R.id.tvRADate);
            tvRAStart = itemView.findViewById(R.id.tvRAStart);
            tvRAEnd = itemView.findViewById(R.id.tvRAEnd);
            tvRAStatus = itemView.findViewById( R.id.tvRAStatus);

        }
    }
}
