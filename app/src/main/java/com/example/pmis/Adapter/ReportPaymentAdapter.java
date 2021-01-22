package com.example.pmis.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pmis.Model.Installment;
import com.example.pmis.Model.PaymentReportFacade;
import com.example.pmis.Model.Procedures;
import com.example.pmis.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ReportPaymentAdapter  extends RecyclerView.Adapter{
    private static final String TAG = "REPORT_PAYMENT_ADAPTER";
    List<PaymentReportFacade> fetchPaymentReportFacade;
    String name;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef, drugRef;
    private String userID;
    private String type;
    public Context context;
    public ReportPaymentAdapter(Context context,List<PaymentReportFacade> fetchPaymentReportFacade, String type){
        this.fetchPaymentReportFacade = fetchPaymentReportFacade;
        this.context = context;
        this.type = type;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_payment_card_view, parent, false);
        ViewHolderClass viewHolderClass = new ViewHolderClass(view);
        return viewHolderClass;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolderClass viewHolderClass = (ViewHolderClass)holder;
        PaymentReportFacade paymentReportFacade = fetchPaymentReportFacade.get(position);
        if(type.equals("FULL PAYMENT")){
            viewHolderClass.tvPRName.setText(paymentReportFacade.getPatientName());
            viewHolderClass.tvPRDate.setText(paymentReportFacade.getDate());
            viewHolderClass.tvPRAmount.setText(paymentReportFacade.getTotal());
            viewHolderClass.tvPRTotal.setText(paymentReportFacade.getTotal());
            viewHolderClass.tvPRCount.setText("1");
        }else if(type.equals("INSTALLMENT")){
            viewHolderClass.tvPRName.setText(paymentReportFacade.getPatientName());
            viewHolderClass.tvPRDate.setText(paymentReportFacade.getDate());
            viewHolderClass.tvPRAmount.setText(paymentReportFacade.getTotal());
            double amountTotal = 0;
            int count = 0;
            for(Installment installment: paymentReportFacade.getInstallmentList()){
                double amount = Double.parseDouble(installment.getAmount());
                amountTotal = amountTotal + amount;
                count++;
            }
            viewHolderClass.tvPRTotal.setText(String.valueOf(amountTotal));
            viewHolderClass.tvPRCount.setText(String.valueOf(count));
        }
    }

    @Override
    public int getItemCount() {
        return fetchPaymentReportFacade.size();
    }
    public class ViewHolderClass extends RecyclerView.ViewHolder {
        TextView tvPRName, tvPRDate, tvPRAmount, tvPRTotal, tvPRCount;

        public ViewHolderClass(@NonNull View itemView) {
            super(itemView);
            tvPRName = itemView.findViewById(R.id.tvPRName);
            tvPRDate = itemView.findViewById(R.id.tvPRDate);
            tvPRAmount = itemView.findViewById(R.id.tvPRAmount);
            tvPRTotal = itemView.findViewById(R.id.tvPRTotal);
            tvPRCount = itemView.findViewById( R.id.tvPRCount);

        }
    }
}
