package com.example.firebaselogin;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.firebaselogin.databinding.ActivityProductPageBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//FragmentActivity
//AppCompatActivity
public class ProductPage extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityProductPageBinding binding;
    private final int FINE_PREMISSION_CODE = 1;
    FirebaseAuth auth;
    FirebaseFirestore db;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    String documentID;
    TextView maps_location_desc, title_desc, exp_date_desc, description_desc, categories_dsc;
    ImageView product_image_desc;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_page);
        //binding = ActivityMapsBinding.inflate(getLayoutInflater());
        //setContentView(binding.getRoot());

        Intent marketIntent = getIntent();
        documentID = marketIntent.getStringExtra("docId");

        title_desc = findViewById(R.id.title_lbl);
        exp_date_desc = findViewById(R.id.expDate_lbl);
        description_desc = findViewById(R.id.description_lbl);
        categories_dsc = findViewById(R.id.categories_lbl);
        product_image_desc = findViewById(R.id.product_image);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        maps_location_desc = findViewById(R.id.maps_location_lbl);


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, FINE_PREMISSION_CODE);
        }

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();



        DocumentReference documentReference = db.collection("listings").document(documentID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                //Checks if the current user is the same as the user that the listing belongs to and, if true adds update and delete buttons
                if (currentUser.getUid().equals(value.getString("userid"))) {
                    Button btnupdate=(Button)findViewById(R.id.update);
                    btnupdate.setVisibility(View.VISIBLE);

                    Button btndelete=(Button)findViewById(R.id.delete);
                    btndelete.setVisibility(View.VISIBLE);
                    btndelete.setOnClickListener(view -> deleteListing());

                    Button btnreserve = (Button)findViewById(R.id.reserve);
                    btnreserve.setVisibility(View.GONE);
                }


                String location = value.getString("city");
                title_desc.setText("Title: " + value.getString("title"));
                exp_date_desc.setText("Expiration date: " + value.getString("expdate"));
                description_desc.setText("Description: " + value.getString("description"));
                categories_dsc.setText("Categories: " + value.getString("categories"));

                //Fetches the image based on the name of the document
                StorageReference storageReference = FirebaseStorage.getInstance().getReference(value.getId());
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri urlImage) {
                        Glide.with(getApplicationContext())
                                .load(urlImage)
                                .into(product_image_desc);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Glide.with(getApplicationContext())
                                .load(R.drawable.no_image)
                                .into(product_image_desc);
                    }
                });

                List<Address> addressList = null;

                if (!location.isEmpty()) {
                    Geocoder geocoder = new Geocoder(ProductPage.this, Locale.getDefault());


                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (addressList != null && !addressList.isEmpty()) {
                        maps_location_desc.setText("Location: " + location);
                        Address address = (Address) addressList.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));
                    } else {

                        Query query = db.collection("locations").whereEqualTo("city", location.toLowerCase());

                        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    QuerySnapshot querySnapshot = task.getResult();

                                    if (querySnapshot != null && !querySnapshot.isEmpty()) {

                                        db.collection("locations").whereEqualTo("city", location.toLowerCase()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {

                                                    DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                                    String documentID = documentSnapshot.getId();

                                                    DocumentReference locRef = db.collection("locations").document(documentID);

                                                    locRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                                                            String city_fire = value.getString("city");
                                                            Double lat = value.getDouble("lat");
                                                            Double lng = value.getDouble("lng");

                                                            maps_location_desc.setText("Location: " + city_fire);

                                                            LatLng latLng = new LatLng(lat, lng);
                                                            mMap.addMarker(new MarkerOptions().position(latLng).title(city_fire));
                                                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));

                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                    else {
                                        maps_location_desc.setText("Location: not found");
                                        Toast.makeText(ProductPage.this, "Location not found!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });


                    }
                }

                else {
                    maps_location_desc.setText("Location: ????");
                    Toast.makeText(ProductPage.this, "Location is empty!", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }


    /*
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == FINE_PREMISSION_CODE) {
            if ()
        }
    }*/

    public void navigateBack(View view) {
        startActivity(new Intent(ProductPage.this, MainActivity.class));
        finish();
    }

    private Button.OnClickListener deleteListing() {
        db.collection("listings").document(documentID)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                        Toast.makeText(ProductPage.this, "Listing deleted", Toast.LENGTH_LONG).show();
                        Intent mainintent = new Intent(ProductPage.this, MainActivity.class);
                        startActivity(mainintent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
        return null;
    }


}