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

import com.studio.smarters.sysoftadmin.R;

import id.zelory.compressor.Compressor;

public class AddCourseActivity extends AppCompatActivity {

    private EditText nameText,durationText,feeText;
    private Button uploadButton;
    private DatabaseReference courseRef;
    private Map m;
    private String name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);
        nameText=findViewById(R.id.add_course_name);
        durationText=findViewById(R.id.add_course_duration);
        feeText=findViewById(R.id.add_course_fee);
        uploadButton=findViewById(R.id.course_add_button);
        courseRef= FirebaseDatabase.getInstance().getReference().child("courses");
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name=nameText.getText().toString();
                String duration=durationText.getText().toString();
                String fee=feeText.getText().toString();
                if(TextUtils.isEmpty(name) || TextUtils.isEmpty(fee) || TextUtils.isEmpty(duration)){
                    Toast.makeText(AddCourseActivity.this, "You must fill all the fields.....", Toast.LENGTH_SHORT).show();
                }else{
                    m=new HashMap();
                    m.put("name",name);
                    m.put("duration",duration);
                    m.put("fee",fee);
                    m.put("doc","none");
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1,1)
                            .start(AddCourseActivity.this);
                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        StorageReference storeProfileImage= FirebaseStorage.getInstance().getReference().child("course_pic");
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
                            .setQuality(50)
                            .compressToBitmap(thumb_filePath);
                }catch (Exception e){
                    e.printStackTrace();
                }
                ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
                final byte[] mbyte=byteArrayOutputStream.toByteArray();
                final StorageReference thumbFilePath=storeProfileImage.child(name+".jpg");
                UploadTask uploadTask=thumbFilePath.putBytes(mbyte);
                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                        String thumb_downloadUrl=thumb_task.getResult().getDownloadUrl().toString();
                        DatabaseReference db= FirebaseDatabase.getInstance().getReference().child("course");
                        m.put("image",thumb_downloadUrl);
                        db.push().updateChildren(m).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@android.support.annotation.NonNull Task<Void> task) {
                                loadingBar.dismiss();
                                Toast.makeText(AddCourseActivity.this, "Successfully Uploaded...", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(AddCourseActivity.this,error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
