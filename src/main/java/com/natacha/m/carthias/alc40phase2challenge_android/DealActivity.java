package com.natacha.m.carthias.alc40phase2challenge_android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;



public class DealActivity extends AppCompatActivity {

    //variables containing firebase objects and reference
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    //constant for button functionality to upload image
    private static final int PICTURE_RESULT = 42;

    //variables
    EditText txtTitle;
    EditText txtDescription;
    EditText txtPrice;
    TravelDeal deal;
    ImageView imageView;

    private AdView mAdView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);

       MobileAds.initialize(this, "ca-app-pub-5978778104330137~5107983181");

       mAdView = findViewById(R.id.adView);
       AdRequest adRequest = new AdRequest.Builder().build();
       mAdView.loadAd(adRequest);

        //gets instance of firebase database
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        //reference to traveldeals note
        mDatabaseReference = FirebaseUtil.mDatabaseReference;

        //reference to edit text components in XML
        txtTitle = (EditText) findViewById(R.id.txtTitle);
        txtDescription = (EditText) findViewById(R.id.txtDescription);
        txtPrice = (EditText) findViewById(R.id.txtPrice);
        imageView = (ImageView) findViewById(R.id.image);

        //accesses the deal activity when clicking on a deal
        Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getSerializableExtra("Deal");
        if (deal==null) {
            deal = new TravelDeal();
        }
        this.deal = deal;
        //sets text for each text view
        txtTitle.setText(deal.getTitle());
        txtDescription.setText(deal.getDescription());
        txtPrice.setText(deal.getPrice());

        //showImage(deal.getImageUrl());

        //reference to button in XML
        Button btnImage = findViewById(R.id.btnImage);
        //button click functionality
        btnImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent.createChooser(intent, "Insert Picture"), PICTURE_RESULT);
            }
        });
    }

    //implement image upload
    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            StorageReference ref = FirebaseUtil.mStorageRef.child(imageUri.getLastPathSegment());
            ref.putFile(imageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String downloadUrl;
                    Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uri.isComplete());
                    Uri url = uri.getResult();
                    downloadUrl = url.toString();
                    String pictureName = taskSnapshot.getStorage().getPath();
                    deal.setImageUrl(downloadUrl);
                    deal.setImageName(pictureName);
                    showImage(downloadUrl);
                }
            });
        }
    }

    @Override
    //Override to allow writing and deleting to the database using the menu options
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.save_menu:
                saveDeal();
                Toast.makeText(this, "Deal saved", Toast.LENGTH_LONG).show();
                clean();
                backToList();
                return true;

            case R.id.delete_menu:
                deleteDeal();
                Toast.makeText(this, "Deal Deleted", Toast.LENGTH_LONG).show();
                backToList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    //To implement custom menu xml
    public boolean onCreateOptionsMenu(Menu menu) {
        //creates menu from xml menu resource
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);

        //if user is an administrator then the save and delete menu options are visible
        if(FirebaseUtil.isAdmin) {
            menu.findItem(R.id.delete_menu).setVisible(true);
            menu.findItem(R.id.save_menu).setVisible(true);
            //if user is an administrator then edit text are enabled
            enableEditTexts(true);
        }
        //if not administrator then menu options are not visible
        else {
            menu.findItem(R.id.delete_menu).setVisible(false);
            menu.findItem(R.id.save_menu).setVisible(false);
            //if user is not an administrator then the edit text are disabled
            enableEditTexts(false);
            findViewById(R.id.btnImage).setEnabled(false);
        }
        return true;
    }

    private void saveDeal() {
        //create or edit an existing deal
        deal.setTitle(txtTitle.getText().toString());
        deal.setDescription(txtDescription.getText().toString());
        deal.setPrice(txtPrice.getText().toString());

        //if the deal does not exist, push for creation of new node in database
        if(deal.getId()==null) {
            mDatabaseReference.push().setValue(deal);
        }
        //if deal exists edit the node
        else {
            mDatabaseReference.child(deal.getId()).setValue(deal);
        }

    }
    //delete the deal
    private void deleteDeal() {
        //check if deal exists

        //if deal does not exist
        if (deal == null) {
            Toast.makeText(this, "Please save the deal before deleting", Toast.LENGTH_SHORT).show();
            return;
        }
        //if deal does exist, delete the node
        mDatabaseReference.child(deal.getId()).removeValue();
        if(deal.getImageName() !=null && !deal.getImageName().isEmpty()) {
            StorageReference picRef = FirebaseUtil.mStorage.getReference().child(deal.getImageName());
            picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Delete Image", "Image Successfully Deleted");

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                Log.d("Delete Image", e.getMessage());
                }
            });
        }
    }
    //return to list activity after saving
    private void backToList() {
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }



    private void clean() {
        txtTitle.setText("");
        txtPrice.setText("");
        txtDescription.setText("");
        txtTitle.requestFocus();
    }

    //allows edit texts to be editable depending on if user is an administrator
    private void enableEditTexts(boolean isEnabled) {
        txtTitle.setEnabled(isEnabled);
        txtDescription.setEnabled(isEnabled);
        txtPrice.setEnabled(isEnabled);
    }
       //method to display imageview using picasso
    private void showImage(String url) {
        if (url != null && !url.isEmpty()) {
            Picasso.get().load(url).resize(100, 100).centerCrop().into(imageView);
        }
    }


}
