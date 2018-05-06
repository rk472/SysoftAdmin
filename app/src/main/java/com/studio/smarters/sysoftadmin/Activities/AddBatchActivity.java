package com.studio.smarters.sysoftadmin.Activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.studio.smarters.sysoftadmin.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class AddBatchActivity extends AppCompatActivity {

    private EditText nameText,startText,timingText;
    private Button addButton;
    private ProgressDialog loadingBar;
    private ToggleButton newButton;
    private Map m;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_batch);
        nameText=findViewById(R.id.batch_name);
        startText=findViewById(R.id.batch_start);
        timingText=findViewById(R.id.batch_timing);
        addButton=findViewById(R.id.add_batch_button);
        newButton=findViewById(R.id.batch_new);
        newButton.setChecked(false);
        loadingBar=new ProgressDialog(this);
        loadingBar.setTitle("Please wait");
        loadingBar.setMessage("Please wait while we are adding the batch...");
        loadingBar.setCancelable(false);
        loadingBar.setCanceledOnTouchOutside(false);
        startText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c=Calendar.getInstance();
                new DatePickerDialog(AddBatchActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        c.set(Calendar.YEAR, year);
                        c.set(Calendar.MONTH, monthOfYear);
                        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDate(c);
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public  void onClick(View v) {
                loadingBar.show();
                String name=nameText.getText().toString();
                String start=startText.getText().toString();
                String timing=timingText.getText().toString();
                if(TextUtils.isEmpty(name) || TextUtils.isEmpty(start) || TextUtils.isEmpty(timing)){
                    Toast.makeText(AddBatchActivity.this, "You must fill all the fields...", Toast.LENGTH_SHORT).show();
                }else{
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1,1)
                            .start(AddBatchActivity.this);
                    m=new HashMap();
                    m.put("batch_name",name);
                    m.put("batch_start",start);
                    m.put("batch_timing",timing);
                    m.put("new",newButton.isChecked());

                    /*FirebaseDatabase.getInstance().getReference("news/upcoming_batches").push().updateChildren(m).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            Toast.makeText(MainActivity.this, "Batch added successfully...", Toast.LENGTH_SHORT).show();
                        }
                    });*/
                }
                loadingBar.dismiss();
            }
        });
    }

    private void updateDate(Calendar c) {
        String month=getMonthForInt(c.get(Calendar.MONTH));
        int date=c.get(Calendar.DAY_OF_MONTH);
        startText.setText(month+" "+date);
    }
    String getMonthForInt(int num) {
        String month = "wrong";
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();
        if (num >= 0 && num <= 11 ) {
            month = months[num];
        }
        return month;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        StorageReference storeProfileImage= FirebaseStorage.getInstance().getReference().child("upcoming_batches");
        final ProgressDialog loadingBar=new ProgressDialog(this);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            Bitmap thumb_bitmap = null;
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                loadingBar.setMessage("We are uploading the image..");
                loadingBar.setTitle("Please Wait");
                loadingBar.show();
                Uri resultUri = result.getUri();
                File thumb_filePath=new File(resultUri.getPath());
                try{
                    thumb_bitmap=new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxHeight(200)
                            .setQuality(20)
                            .compressToBitmap(thumb_filePath);
                }catch (Exception e){
                    e.printStackTrace();
                }

                ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
                final byte[] mbyte=byteArrayOutputStream.toByteArray();
                final DatabaseReference db= FirebaseDatabase.getInstance().getReference().child("news").child("upcoming_batches");
                final String name=db.push().getKey();
                final StorageReference thumbFilePath=storeProfileImage.child(name+".jpg");
                UploadTask uploadTask=thumbFilePath.putBytes(mbyte);
                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                        String thumb_downloadUrl=thumb_task.getResult().getDownloadUrl().toString();
                        m.put("image",thumb_downloadUrl);
                        db.child(name).updateChildren(m).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@android.support.annotation.NonNull Task<Void> task) {
                                loadingBar.dismiss();
                                Toast.makeText(AddBatchActivity.this, "Successfully Uploaded...", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(AddBatchActivity.this,error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
