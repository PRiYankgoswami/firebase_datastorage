package com.example.priyank.firebase_storage;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
{
    Button btn;
    TextView msg;
    ProgressBar pb;


    FirebaseDatabase db;
    DatabaseReference ref;


    FirebaseStorage storage;
    StorageReference sref;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn=(Button)findViewById(R.id.button2);
        msg=(TextView)findViewById(R.id.msg);
        pb=(ProgressBar)findViewById(R.id.progressBar2);



        db=FirebaseDatabase.getInstance();
        ref=db.getReference().child("file list");


        storage=FirebaseStorage.getInstance();
        sref=storage.getReference().child("Files");



        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fileChooser();

            }
        });
    }


    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }


    void fileChooser()
    {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), 11);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==11 && resultCode==RESULT_OK && data.getData()!=null)
        {
            uploadFile(data.getData());
        }
        else
        {
            pb.setProgress(0);
            msg.setText("");
        }


    }

    void uploadFile(Uri uri)
    {
        btn.setEnabled(false);
        msg.setText("uploading....");
        pb.setProgress(0);

        final String filename=getFileName(uri);

        sref.child(filename).putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                String du=taskSnapshot.getMetadata().getDownloadUrl()+"";

                Map<String,String> data=new HashMap<>();
                data.put("name",filename);
                data.put("path",du);

                btn.setEnabled(true);
                ref.push().setValue(data);
                Toast.makeText(getApplicationContext(),"Succssfully Upload",Toast.LENGTH_LONG).show();
                msg.setText("Succesfully Upload");


            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {


                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                msg.setText("Uploading " + ((int) progress) + "%...");
                pb.setProgress(((int) progress));


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                btn.setEnabled(true);
                msg.setText("Please try again later");


            }
        });

    }

}
