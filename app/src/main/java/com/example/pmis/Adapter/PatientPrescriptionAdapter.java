package com.example.pmis.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pmis.AddPrescriptionActivity;
import com.example.pmis.Helpers.GetDoctorName;
import com.example.pmis.Helpers.LoggedUserData;
import com.example.pmis.Model.Clinic;
import com.example.pmis.Model.DrugPrescription;
import com.example.pmis.Model.DrugPrescriptionMain;
import com.example.pmis.Model.Patient;
import com.example.pmis.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PatientPrescriptionAdapter extends RecyclerView.Adapter {
    private static final String TAG = "PRESCRIPTION_ADAPTER";
    List<DrugPrescriptionMain> fetchPrescriptionMainList;
    public Context context;
    public String docName ,key;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference presRef, pdfRef, patientRef;
    private String patientKey, userID;
    private String clinicName, clinicAddress, clinicLogoUrl, clinicContactNo;
    private Uri clinicLogo;


    public PatientPrescriptionAdapter(Context context,List<DrugPrescriptionMain> fetchPrescriptionMainList, String docName, String patientKey){
        this.fetchPrescriptionMainList = fetchPrescriptionMainList;
        this.context = context;
        this.docName = docName;
        this.patientKey = patientKey;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.prescription_card_view, parent, false);
       ViewHolderClass viewHolderClass = new ViewHolderClass(view);
       return  viewHolderClass;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolderClass viewHolderClass = (ViewHolderClass)holder;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DrugPrescriptionMain drugPrescriptionMain = fetchPrescriptionMainList.get(position);
        key = drugPrescriptionMain.getKey();
        Log.d(TAG, "KEYS: " + patientKey + " " + key);
        viewHolderClass.tvPPresAdded.setText(drugPrescriptionMain.getDate());
        viewHolderClass.tvPPresDentist.setText(docName);
        viewHolderClass.tvPPresUpdate.setText(drugPrescriptionMain.getDateUpdated());
        viewHolderClass.ibPPresEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AddPrescriptionActivity.class);
                intent.putExtra("patientKey", patientKey);
                intent.putExtra("action", "edit");
                intent.putExtra("prescriptionKey", fetchPrescriptionMainList.get(position).getKey());

                context.startActivity(intent);

            }
        });
        viewHolderClass.ibPPresDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Query deleteQuery =  ref.child("Prescription").child(patientKey).orderByChild("key").equalTo(drugPrescriptionMain.getKey());
                deleteQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            ds.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(v.getContext(),"Item Deleted Succesfully", Toast.LENGTH_LONG).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(v.getContext(),"Item not deleted! please try again.", Toast.LENGTH_LONG).show();

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        viewHolderClass.ibPPresPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoggedUserData loggedUserData = new LoggedUserData();
                 userID = loggedUserData.userID();
                GetDoctorName getDoctorName = new GetDoctorName();
                String docName = getDoctorName.docName();
                pdfRef = mFirebaseDatabase.getReference("Prescription").child(patientKey).child(key);
                pdfRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        getClinicData(snapshot);
                    }

                    private void getClinicData(DataSnapshot prescriptionDataSnapshot) {

                        mFirebaseDatabase = FirebaseDatabase.getInstance();
                        DatabaseReference ref = mFirebaseDatabase.getReference("Clinic").child(userID);
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                clinicName = snapshot.getValue(Clinic.class).getClinicName();
                                clinicAddress = snapshot.getValue(Clinic.class).getAddress();
                                clinicContactNo = snapshot.getValue(Clinic.class).getContactNo();
                                clinicLogoUrl = snapshot.getValue(Clinic.class).getPhotoUrl();
                                FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance();
                                StorageReference mStorageReference = mFirebaseStorage.getReference("images/clinicPic/" + userID);
                                mStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        clinicLogo = uri;
                                        getPatientInfo(prescriptionDataSnapshot);


                                    }
                                });

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    private void getPatientInfo(DataSnapshot prescriptionDataSnapshot) {
        patientRef = mFirebaseDatabase.getReference("Patient").child(userID).child(patientKey);
        patientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                PdfDocument myPdfDocument = new PdfDocument();
                Paint paint = new Paint();
                Paint forLinePaint = new Paint();
                Paint solidLinePaint = new Paint();
                PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(300,450,1).create();
                PdfDocument.Page myPage = myPdfDocument.startPage(myPageInfo);

                Canvas canvas = myPage.getCanvas();
                Typeface typeface = ResourcesCompat.getFont(context,R.font.montserrat);
                paint.setTypeface(typeface);
                paint.setTextSize(12f);
                paint.setColor(Color.BLACK);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(clinicName, canvas.getWidth()/2,20, paint);
                paint.setTextSize(8f);
                canvas.drawText(clinicAddress,canvas.getWidth()/2, 35,paint);
                canvas.drawText(clinicContactNo, canvas.getWidth()/2, 44,paint);

                paint.setTextAlign(Paint.Align.LEFT);
                paint.setTextSize(8.5f);
                canvas.drawText(docName, 20,60,paint);
                paint.setTextSize(7f);
                canvas.drawText("General Dentist",20,68,paint);
                paint.setTextSize(7f);
                canvas.drawText("License No. 563242612",20,75,paint);

                paint.setTextSize(7f);
                paint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(prescriptionDataSnapshot.getValue(DrugPrescriptionMain.class).getDate(),280,75, paint);


                solidLinePaint.setStyle(Paint.Style.STROKE);
                solidLinePaint.setStrokeWidth(1);
                canvas.drawLine(20,80,280,80, solidLinePaint);

                paint.setTextAlign(Paint.Align.LEFT);
                paint.setTextSize(7f);
                canvas.drawText("Name: " ,20,100,paint);
                canvas.drawLine(65,100,280,100, solidLinePaint);
                String fullName = snapshot.getValue(Patient.class).getFirstName() + ' ' +
                        snapshot.getValue(Patient.class).getMiddleName() + ' ' +
                        snapshot.getValue(Patient.class).getLastName();
                paint.setTextSize(8f);
                canvas.drawText( fullName,69,97,paint);

                paint.setTextSize(7f);
                canvas.drawText("Address: " ,20,120,paint);
                canvas.drawLine(65,120,280,120, solidLinePaint);
                paint.setTextSize(8f);
                canvas.drawText(snapshot.getValue(Patient.class).getAddress(),69,117,paint);

                paint.setTextSize(7f);
                canvas.drawText("Birth Date: " ,20,140,paint);
                canvas.drawLine(65,140,110,140, solidLinePaint);
                paint.setTextSize(8f);
                canvas.drawText(snapshot.getValue(Patient.class).getBirthDate(),69,137, paint);
                paint.setTextSize(7f);
                canvas.drawText("Sex: " ,115,140,paint);
                canvas.drawLine(130,140,175,140, solidLinePaint);
                paint.setTextSize(8f);
                canvas.drawText(snapshot.getValue(Patient.class).getSex(),134,137, paint);


                Resources res = context.getResources();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = false;
                Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.rxlogo, options);
                Bitmap rxLogo = Bitmap.createScaledBitmap(bitmap, 40,40,false);
                Matrix matrix = new Matrix();
                matrix.preTranslate(30,150);
                canvas.drawBitmap(Bitmap.createScaledBitmap(bitmap, 40,40, false),matrix,paint);

                int yDrugInfo = 220;
                 int yFrequency = 170;
                int yDuration = 190;
                Typeface roboto = ResourcesCompat.getFont(context,R.font.roboto_mono_thin);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTypeface(roboto);

                for(DataSnapshot ds: prescriptionDataSnapshot.child("drugList").getChildren()){
                    paint.setTextSize(14f);
                    canvas.drawText(ds.getValue(DrugPrescription.class).getDrugInfo() , canvas.getWidth()/2, yDrugInfo, paint);
                    paint.setTextSize(12f);
                    yDrugInfo = yDrugInfo + 15;
                    canvas.drawText(ds.getValue(DrugPrescription.class).getFrequency() , canvas.getWidth()/2, yDrugInfo, paint);
                    yDrugInfo = yDrugInfo + 15;
                    paint.setTextSize(12f);
                    canvas.drawText(ds.getValue(DrugPrescription.class).getDuration() , canvas.getWidth()/2, yDrugInfo, paint);
                    yDrugInfo = yDrugInfo + 30;
                }



                String fileName = snapshot.getValue(Patient.class).getFirstName().trim() +
                        snapshot.getValue(Patient.class).getLastName().trim()  + ".pdf";
                myPdfDocument.finishPage(myPage);
                File file = new File(context.getExternalFilesDir("/"),fileName);

                try{
                    myPdfDocument.writeTo(new FileOutputStream(file));

                } catch (IOException e){
                    e.printStackTrace();
                }
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                myPdfDocument.close();
                Intent target = new Intent(Intent.ACTION_VIEW);
                target.setDataAndType(Uri.fromFile(file),"application/pdf");
                target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Intent intent = Intent.createChooser(target, "Open File");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }






    @Override
    public int getItemCount() {
        return fetchPrescriptionMainList.size();
    }
    public class ViewHolderClass extends RecyclerView.ViewHolder {
        TextView tvPPresUpdate, tvPPresDentist, tvPPresAdded;
        ImageButton ibPPresPDF, ibPPresEdit, ibPPresDelete;
        public ViewHolderClass(@NonNull View itemView) {
            super(itemView);
            tvPPresUpdate = itemView.findViewById(R.id.tvPPresUpdate);
            tvPPresDentist = itemView.findViewById(R.id.tvPPresDentist);
            tvPPresAdded = itemView.findViewById(R.id.tvPPresAdded);
            ibPPresPDF = itemView.findViewById( R.id.ibPPresPDF);
            ibPPresEdit = itemView.findViewById( R.id.ibPPresEdit);
            ibPPresDelete = itemView.findViewById( R.id.ibPPresDelete);
        }
    }
}
