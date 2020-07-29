package com.example.forest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ActionTakenActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 1;
    EditText etEventDesc;
    MaterialButton btnCamera,submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_taken);

        if (HomeActivity.description.length()!=0) {
            EditText editText = (EditText) findViewById(R.id.etEventDesc);
            editText.setText(HomeActivity.description, TextView.BufferType.EDITABLE);
        }

        btnCamera = findViewById(R.id.btnCamera);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etEventDesc = findViewById(R.id.etEventDesc);
                HomeActivity.description = etEventDesc.getText().toString();
                Log.d("got", HomeActivity.description);
                callCamera();
            }
        });
        submit = findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendrequest();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case CAMERA_REQUEST:

                Bundle extras = data.getExtras();

                if (extras != null) {
                    Bitmap yourImage = extras.getParcelable("data");
                    // convert bitmap to byte
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    yourImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte imageInByte[] = stream.toByteArray();
                    HomeActivity.encodedImage = Base64.encodeToString(imageInByte, Base64.DEFAULT);


                    // Inserting Contacts
//                    Log.d("Insert: ", String.valueOf(imageInByte[0]));
                    Log.d("Insert: ", HomeActivity.encodedImage+ HomeActivity.description);
                    Intent i = new Intent(ActionTakenActivity.this,
                            ActionTakenActivity.class);
                    startActivity(i);
                    finish();
                }
                Toast.makeText(this, "Photo has been taken", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void callCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST);
        intent.setType("image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 0);
        intent.putExtra("aspectY", 0);
        intent.putExtra("outputX", 250);
        intent.putExtra("outputY", 200);
    }

    private void sendrequest() throws JSONException {
        Log.d("sending image", HomeActivity.encodedImage);
        Log.d("description", HomeActivity.description);
        Map<String, Object> data = new HashMap<>();
        data.put("image", HomeActivity.encodedImage);
        data.put("description", HomeActivity.description);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("report").document("animal_report")
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("done", "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("fail", "Error writing document", e);
                    }
                });
        //else
        //no internet so some error shown
    }

}