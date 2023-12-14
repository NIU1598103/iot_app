package com.example.iotapp;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.iotapp.data.UserData;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UserConfigActivity extends AppCompatActivity {

    private Button homeBtn;

    private EditText phoneInput, nameInput, plateInput;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userconfig);

        homeBtn = findViewById(R.id.btnHome);
        phoneInput = findViewById(R.id.phoneInput);
        nameInput = findViewById(R.id.nameInput);
        plateInput = findViewById(R.id.plateInput);
        homeBtn.setOnClickListener(v -> navigateHome());

        // Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String userId = UserData.getInstance().getUserId();

        if (userId == null) {
            navigateHome();
        } else {
            db.collection("users")
                    .whereEqualTo("id", userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documents = task.getResult().getDocuments();

                            if (documents.isEmpty()) {
                                Log.d(TAG, "No results");
                                return;
                            }

                            DocumentSnapshot userData = documents.get(0);

                            Log.d(TAG, "userdata: " + userData.getData());

                            phoneInput.setText((CharSequence) userData.get("phone"));
                            nameInput.setText((CharSequence) userData.get("name"));
                            plateInput.setText((CharSequence) userData.get("numberplate"));
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    });
        }
    }

    private void navigateHome() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
