package com.natacha.m.carthias.alc40phase2challenge_android;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Natacha Carthias.
 */

public class FirebaseUtil {
    public static FirebaseDatabase mFirebaseDatabase;
    public static DatabaseReference mDatabaseReference;
    private static FirebaseUtil firebaseUtil;
    public static ArrayList<TravelDeal> mDeals;

    //declare variables to implement authentication
    public static FirebaseAuth mFirebaseAuth;
    public static FirebaseAuth.AuthStateListener mAuthListener;

    //implement firebase storage
    public static FirebaseStorage mStorage;
    public static StorageReference mStorageRef;

    private static ListActivity caller;
    private static int RC_SIGN_IN = 123;

    //private empty constructor to prevent class from being instantiated from outside of class
    private FirebaseUtil(){}

    //prevent access to forbidden features of normal users
    public static boolean isAdmin;

    public static void  openFbReference(String ref, final ListActivity callerActivity) {
        if (firebaseUtil == null){
            firebaseUtil = new FirebaseUtil();
            mFirebaseDatabase = FirebaseDatabase.getInstance();

            //initialize firebase auth
            mFirebaseAuth = FirebaseAuth.getInstance();
            caller = callerActivity;
            mAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    //check if there is logged in user
                    if(firebaseAuth.getCurrentUser() == null) {
                        FirebaseUtil.signIn();
                    }
                    else {
                        //retrieve user id of current user
                        String userId = firebaseAuth.getUid();
                        //check if user is an administrator
                        checkAdmin(userId);
                    }
                    Toast.makeText(callerActivity.getBaseContext(), "Welcome back!", Toast.LENGTH_LONG).show();


                }


            };

            //call method to connect to the firebase storage
            connectStorage();

        }

        //moved so that new array list gets called every time class is called
        mDeals = new ArrayList<TravelDeal>();
        mDatabaseReference = mFirebaseDatabase.getReference().child(ref);
    }

    //sign in authentication method
    private static void signIn() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

                //Create and launch sign-in inten
                caller.startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(providers)
                                .build(),
                        RC_SIGN_IN);
    }


    //check if logged user is an administrator
    private static void checkAdmin(String uid) {
        FirebaseUtil.isAdmin=false;
        DatabaseReference ref = mFirebaseDatabase.getReference().child("administrators").child(uid);
        ChildEventListener listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                FirebaseUtil.isAdmin=true;
                caller.showMenu();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        ref.addChildEventListener(listener);
    }

    //attach listener for authentication
    public static void attachListener() {
        mFirebaseAuth.addAuthStateListener(mAuthListener);
    }

    //detach listener for authentication
    public static void detachListener() {
        mFirebaseAuth.removeAuthStateListener(mAuthListener);
    }

    //method to connect to the firebase storage
    public static void connectStorage() {
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference().child("deals_pictures");
    }

}
