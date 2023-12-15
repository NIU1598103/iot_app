package com.example.iotapp.data;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CloudLogs {
    private static CloudLogs sInstance;

    private CloudLogs() {}

    public static CloudLogs getInstance() {
        if (sInstance == null) {
            sInstance = new CloudLogs();
        }

        return sInstance;
    }

    public void mockLogEdgeToCloud(String numberPlate) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> log = new HashMap<>();
        log.put("numberplate", numberPlate);

        db.collection("logs")
                .add(log)
                .addOnSuccessListener((OnSuccessListener<DocumentReference>) documentReference -> Log.d(TAG, "New LOG written with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w(TAG, "Error adding LOG", e));
    }
}
