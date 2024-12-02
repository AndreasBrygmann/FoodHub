package com.example.firebaselogin;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.UUID;

public class NewListing extends AppCompatActivity {

    //private DatabaseReference foodhub;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth;
    FirebaseStorage storage;
    StorageReference storageReference;

    TextInputEditText editTextTitle, editTextCity, editTextExpDate, editTextDescripton;

    ScrollView sv;
    Button photobtn, submitbtn;

    ListView listViewData;
    ArrayAdapter<String> adapter;
    String[] categoriesNo = {"Bakervarer", "Frukt og grønt", "Fisk og skalldyr", "meieri og egg", "Drikke", "Kylling og fjærkre", "Kjøtt", "Dessert og snacks"};
    String[] categoriesEn = {"Baked goods", "Fruits and vegetables", "Fish and seafood", "Dairy and eggs", "Drink", "Chicken and poultry", "Meat", "Dessert and snacks"};
    public ImageView productpic;
    public Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_listing);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(NewListing.this, "You must be logged in to create a listing", Toast.LENGTH_LONG).show();
            return;
        } else {
            Toast.makeText(NewListing.this, "You are logged in", Toast.LENGTH_LONG).show();
        }

        editTextTitle = findViewById(R.id.title);
        editTextCity = findViewById(R.id.city);
        editTextExpDate = findViewById(R.id.expDate);
        editTextDescripton = findViewById(R.id.description);
        photobtn = findViewById(R.id.photo_btn);
        submitbtn = findViewById(R.id.submit_btn);

        //Adds list to categories to variable and new listing
        listViewData = findViewById(R.id.categoryListView);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice,categoriesEn);
        listViewData.setAdapter(adapter);

        productpic = findViewById(R.id.imageView);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        photobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        submitbtn.setOnClickListener(view -> {
            String title, city, expdate, description;
            title = String.valueOf(editTextTitle.getText());
            city = String.valueOf(editTextCity.getText());
            expdate = String.valueOf(editTextExpDate.getText());
            description = String.valueOf(editTextDescripton.getText());

            ArrayList<String> selectedCategories = new ArrayList<String>();
            String currentItem;
            //Adds selected categories to an array
            for (int i=0;i<listViewData.getCount();i++){
                if (listViewData.isItemChecked(i)) {
                    currentItem = (String) listViewData.getItemAtPosition(i);
                    selectedCategories.add(currentItem);
                }
            }

            if (TextUtils.isEmpty(title)) {
                Toast.makeText(NewListing.this, "Enter a title", Toast.LENGTH_LONG).show();
                return;
            }

            if (TextUtils.isEmpty(city)) {
                Toast.makeText(NewListing.this, "Enter you city", Toast.LENGTH_LONG).show();
                return;
            }

            if (TextUtils.isEmpty(expdate)) {
                Toast.makeText(NewListing.this, "Enter the expiry date", Toast.LENGTH_LONG).show();
                return;
            }

            if (TextUtils.isEmpty(description)) {
                Toast.makeText(NewListing.this, "Enter a description", Toast.LENGTH_LONG).show();
                return;
            }

            String userid = user.getUid();

            Listing listing = new Listing(title, city, expdate, description, userid, selectedCategories);


            //Adds listings to firebase with a set name (not in use)
            //db.collection("listings").document("new-id").set(listing);

            //Adds listing to firebase with a random id
            db.collection("listings")
                    .add(listing)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                            String imgRef;
                            imgRef = documentReference.getId();
                            uploadImage(imgRef);
                            Toast.makeText(NewListing.this, "Listing added", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
                            Toast.makeText(NewListing.this, "Could not add listing", Toast.LENGTH_LONG).show();
                        }
                    });

        });

    }



    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            imageUri = data.getData();
            productpic.setImageURI(imageUri);
            Toast.makeText(NewListing.this, "Photo added", Toast.LENGTH_LONG).show();
        }
    }

    private void uploadImage(String imgRef) {
        /*final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Uploading product image...");
        pd.show();*/
        final String randomKey = UUID.randomUUID().toString();
        //StorageReference riverRef = storageReference.child("images/");
        StorageReference riverRef = storageReference.child(imgRef);

        riverRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Snackbar.make(findViewById(android.R.id.content), "Image Uploaded", Snackbar.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@androidx.annotation.NonNull Exception e) {
                        Toast.makeText(NewListing.this, "Failed to upload image", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
