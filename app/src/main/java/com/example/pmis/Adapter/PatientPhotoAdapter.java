package com.example.pmis.Adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.pmis.AddMedicalHistoryActivity;
import com.example.pmis.Helpers.LoadingDialog;
import com.example.pmis.Model.MedicalHistory;
import com.example.pmis.Model.PatientPhotos;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PatientPhotoAdapter extends RecyclerView.Adapter{
    private static final String TAG = "Patient Photos";
    List<PatientPhotos> fetchPatientPhotos;
    public Context context;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference presRef;
    private StorageReference mStorageRef;
    private String patientKey, medicalHistoryKey;
    private Bitmap bitmap;
    private LoadingDialog loadingDialog;
    private SharedPreferences sharedPreferences;
    private static final String KEY_NAME = "name";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_CONTACT_NO = "contactNo";
    private static final String KEY_DOC_NAME = "docName";
    private static final String KEY_LICENSE = "license";
    private static final String KEY_DEGREE = "degree";

    private static final String SHARED_PREF_NAME = "myPref";

    public PatientPhotoAdapter(Context context, List<PatientPhotos> fetchPatientPhotos, String patientKey){
        this.fetchPatientPhotos = fetchPatientPhotos;
        this.context = context;
        this.patientKey = patientKey;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.medical_history_card_view, parent, false);
        ViewHolderClass viewHolderClass = new ViewHolderClass(view);
        return viewHolderClass;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, context.MODE_PRIVATE);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        ViewHolderClass viewHolderClass = (ViewHolderClass)holder;
        PatientPhotos patientPhotos = fetchPatientPhotos.get(position);
        medicalHistoryKey = patientPhotos.getKey();
        viewHolderClass.tvMedDate.setText(patientPhotos.getDate());
        viewHolderClass.tvMedCaption.setText(patientPhotos.getCaption());
        StorageReference medicalHistoryRef = mStorageRef.child("images/patientPhotos/"+ patientKey + '/' + patientPhotos.getImageUrl());

        medicalHistoryRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d(TAG, "download url is: " + uri.toString());

                Glide
                        .with(context)
                        .asBitmap()
                        .load(uri)
                        .fitCenter()
                        .into(viewHolderClass.ivMedThumbnail);
            }
        });
        viewHolderClass.ibMedDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure you want to Delete this item?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Query deleteQuery = ref.child("PatientPhotos").child(patientKey).orderByChild("key").equalTo(patientPhotos.getKey());
                        deleteQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds: snapshot.getChildren()){
                                    ds.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(v.getContext(),"Item Deleted Successfully", Toast.LENGTH_LONG).show();
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
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();

            }
        });
        viewHolderClass.ibMedEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog = new LoadingDialog((Activity) context);
                loadingDialog.startLoadingDialog();
                StorageReference medicalHistoryRef = mStorageRef.child("images/patientPhotos/"+ patientKey + '/' + fetchPatientPhotos.get(position).getImageUrl());
                medicalHistoryRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d(TAG, "download url is: " + uri.toString());
                        //   try {
                        Glide.with(context).asBitmap().load(uri)
                                .into(new CustomTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                        SimpleDateFormat format = new SimpleDateFormat("MMM dd yyyy", Locale.getDefault());
                                        String currentDate = format.format(new Date());
                                        loadingDialog.dismissDialog();
                                        bitmap = downSample(resource);

                                        PdfDocument pdfDocument = new PdfDocument();
                                        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth() + 40, bitmap.getHeight() + 170, 1).create();
                                        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                                        Canvas canvas = page.getCanvas();

                                        Typeface typeface = ResourcesCompat.getFont(context, R.font.montserrat);
                                        Paint paint = new Paint();
                                        Paint solidLinePaint = new Paint();
                                        paint.setTypeface(typeface);
                                        paint.setTextSize(12f);
                                        paint.setColor(Color.BLACK);
                                        paint.setTextAlign(Paint.Align.CENTER);
                                        canvas.drawText(sharedPreferences.getString(KEY_NAME, ""), (bitmap.getWidth() + 40) / 2, 20, paint);
                                        paint.setTextSize(8f);
                                        canvas.drawText(sharedPreferences.getString(KEY_ADDRESS, ""), (bitmap.getWidth() + 40) / 2, 35, paint);
                                        canvas.drawText(sharedPreferences.getString(KEY_CONTACT_NO, ""), (bitmap.getWidth() + 40) / 2, 44, paint);

                                        paint.setTextAlign(Paint.Align.LEFT);
                                        paint.setTextSize(8.5f);
                                        canvas.drawText(sharedPreferences.getString(KEY_DOC_NAME, ""), 20, 60, paint);
                                        paint.setTextSize(7f);
                                        canvas.drawText(sharedPreferences.getString(KEY_DEGREE, ""), 20, 68, paint);
                                        paint.setTextSize(7f);
                                        canvas.drawText(sharedPreferences.getString(KEY_LICENSE, ""), 20, 75, paint);

                                        paint.setTextSize(7f);
                                        paint.setTextAlign(Paint.Align.RIGHT);
                                        canvas.drawText(currentDate, (bitmap.getWidth() + 20), 75, paint);
                                        solidLinePaint.setStyle(Paint.Style.STROKE);
                                        solidLinePaint.setStrokeWidth(1);
                                        canvas.drawLine(20, 80, bitmap.getWidth() + 20, 80, solidLinePaint);

                                        canvas.drawBitmap(bitmap,20,110,null);
                                        canvas.drawText(fetchPatientPhotos.get(position).getCaption(), bitmap.getWidth() / 2, bitmap.getHeight() + 140, paint);
                                        pdfDocument.finishPage(page);
                                        String fileName = fetchPatientPhotos.get(position).getKey() + ".pdf";
                                        File file = new File(context.getExternalFilesDir("/"),fileName);

                                        try{
                                            pdfDocument.writeTo(new FileOutputStream(file));

                                        } catch (IOException e){
                                            e.printStackTrace();
                                        }
                                        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                                        StrictMode.setVmPolicy(builder.build());
                                        pdfDocument.close();
                                        Intent target = new Intent(Intent.ACTION_VIEW);
                                        target.setDataAndType(Uri.fromFile(file),"application/pdf");
                                        target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                        target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        Intent intent = Intent.createChooser(target, "Open File");
                                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        context.startActivity(intent);
                                        Log.d(TAG, "HEY IM HERE");
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {

                                    }
                                });
                        //  Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);


//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                    }
                });
            }
        });
        viewHolderClass.ivMedThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.image_view_dialog);
                Window window = dialog.getWindow();
                WindowManager.LayoutParams wlp = window.getAttributes();
                wlp.gravity = Gravity.CENTER;
                wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                ImageView imageView = (ImageView) dialog.findViewById(R.id.ivDialog);
                StorageReference medicalHistoryRef = mStorageRef.child("images/patientPhotos/"+ patientKey + '/' + patientPhotos.getImageUrl());
                medicalHistoryRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d(TAG, "download url is: " + uri.toString());
                        Glide
                                .with(context)
                                .asBitmap()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .centerInside()
                                .load(uri)
                                .into(imageView);
                    }
                });
                Button btnBack = (Button) dialog.findViewById(R.id.btnBack);
                btnBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
    }
    private Bitmap downSample(Bitmap resource) {
        Bitmap newBitmap = Bitmap.createBitmap( 300, 450, Bitmap.Config.ARGB_8888);
        float scalesX = 300 / (float) resource.getWidth();
        float scaleY = 450 / (float) resource.getHeight();
        float pivotX = 0;
        float pivotY = 0;
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scalesX, scaleY,pivotX,pivotY);
        Canvas canvas = new Canvas(newBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(resource, 0 , 0 , new Paint(Paint.FILTER_BITMAP_FLAG));
        return  newBitmap;
    }

    @Override
    public int getItemCount() {
        return fetchPatientPhotos.size();
    }
    public static class ViewHolderClass extends RecyclerView.ViewHolder {
        ImageView ivMedThumbnail;
        TextView tvMedCaption, tvMedDate;
        ImageButton ibMedEdit, ibMedDelete;
        public ViewHolderClass(@NonNull View itemView) {
            super(itemView);
            ivMedThumbnail = itemView.findViewById(R.id.ivMedThumbnail);
            tvMedCaption = itemView.findViewById(R.id.tvMedCaption);
            tvMedDate = itemView.findViewById(R.id.tvMedDate);
            ibMedDelete = itemView.findViewById(R.id.ibMedDelete);
            ibMedEdit = itemView.findViewById(R.id.ibMedPDF);
        }
    }
}
