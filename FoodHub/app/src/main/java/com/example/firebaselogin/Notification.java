package com.example.firebaselogin;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class Notification extends AppCompatActivity {
    Button button,notify;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore db;
    BottomNavigationView bottomNavigationView;

    RecyclerView recyclerView;

    ArrayList<Listing_notification> listing_notifications;

    RecycleviewAdapter recycleviewAdapter;
    ProgressDialog progressDialog;
    CollectionReference collectionReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        //Progress dialog som kommer opp vis det tar langtid for å laste in data
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Fetching Data...");
        progressDialog.show();

        recyclerView = findViewById(R.id.notification_list_id);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        listing_notifications = new ArrayList<Listing_notification>();
        recycleviewAdapter = new RecycleviewAdapter(Notification.this,listing_notifications);
        recyclerView.setAdapter(recycleviewAdapter);
        EventChangeListener2();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(Notification.this,
                    Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(Notification.this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // LYTTER FOR NAVBAR
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        // RESETTER BOBBLA RUNDT ELEMENT I NAVBAR TIL NÅVÆRENDE FANE
        bottomNavigationView.setSelectedItemId(R.id.nav_alerts);

    }


//    private void EventChangeListener1() {
//       db.collection("users")
//           .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                @Override
//                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                    if(error != null){
//                        if(progressDialog.isShowing())
//                            progressDialog.dismiss();
//                        Log.e("Firestore Error",error.getMessage());
//                        return;
//                    }
//                   for (DocumentChange dc : value.getDocumentChanges()) {
//
//                        if (dc.getType() == DocumentChange.Type.ADDED) {
//                            listing_notifications.add(dc.getDocument().toObject(Listing_notification.class));
//
//                       }
//                        recycleviewAdapter.notifyDataSetChanged();
//                       if (progressDialog.isShowing())
//                            progressDialog.dismiss();
//                   }
//
//              }
//           });
//    }
    //Fills the Notification Page with Orders with just Title User name and Phone number, with a button to make the reservation.
    private void EventChangeListener2(){
        db.collection("listings")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error != null){
                            if(progressDialog.isShowing())
                                progressDialog.dismiss();
                            Log.e("Firestore Error",error.getMessage());
                            return;
                        }
                        for (DocumentChange dc : value.getDocumentChanges()){

                            if (dc.getType() == DocumentChange.Type.ADDED){
                                listing_notifications.add(dc.getDocument().toObject(Listing_notification.class));

                            }
                            recycleviewAdapter.notifyDataSetChanged();
                            if(progressDialog.isShowing())
                                progressDialog.dismiss();
                        }
                    }
                });
        for (int i = 0; i < listing_notifications.size(); i++) {

            String userid = listing_notifications.get(i).userid;

            DocumentReference documentReference = db.collection("users").document(userid);

            int j = i;
            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    String name = value.getString("name");
                    String phone = value.getString("phoneNumber");

                    listing_notifications.get(j).setName(name);
                    listing_notifications.get(j).setPhoneNumber(phone);
                }
            });
        }
    }

    //Makes the Notification for the User clicking the Notification brings user to Notification page
    public void makeNotification(View view){
        String chanelID = "CHANNEL_ID_NOTIFICATION";
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), chanelID);
        builder.setSmallIcon(R.drawable.picnic)
                .setContentTitle("FoodHub Reservation")
                .setContentTitle("You made a Reservation")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent intent = new Intent(getApplicationContext(), Notification.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("data","Some value");

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                0,intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            NotificationChannel notificationChannel =
                    notificationManager.getNotificationChannel(chanelID);
            if (notificationChannel == null){
                int importance = NotificationManager.IMPORTANCE_HIGH;
                notificationChannel = new NotificationChannel(chanelID,
                        "some description", importance);
                notificationChannel.setLightColor(Color.GREEN);
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        notificationManager.notify(0,builder.build());
    }

    // LYTTER FOR BOTTOM NAVIGATION
            private final BottomNavigationView.OnNavigationItemSelectedListener navListener =
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        int itemId = item.getItemId();

                        if (itemId == R.id.nav_market) {
                            Intent marketIntent = new Intent(Notification.this, MainActivity.class);
                            startActivity(marketIntent);
                            finish();
                            return true;
                        }

                        // ENDRE TIL ALERT-CLASS NÅR DEN ER LAGET:
                        else if (itemId == R.id.nav_alerts) {
                            return true;
                        }

                        else if (itemId == R.id.nav_profile) {
                            Intent alertIntent = new Intent(Notification.this, ProfilePage.class);
                            startActivity(alertIntent);
                            finish();
                            return true;
                        }
                        return false;
                    }
            };


}