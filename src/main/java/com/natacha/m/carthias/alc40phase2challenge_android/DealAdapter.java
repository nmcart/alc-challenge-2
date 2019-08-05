package com.natacha.m.carthias.alc40phase2challenge_android;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Natacha Carthias.
 */

public class DealAdapter extends RecyclerView.Adapter<DealAdapter.DealViewHolder> {
    //declare variables
    ArrayList<TravelDeal> deals;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    //listens every time an item is added and shows items to user
    private ChildEventListener mChildEventListener;
    private ImageView imageDeal;


    public DealAdapter(){
        //reference to traveldeals note
        FirebaseUtil.openFbReference("traveldeals", null);

        //gets instance of firebase database
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        //reference to traveldeals note
        mDatabaseReference = FirebaseUtil.mDatabaseReference;
        //list array of deals to populate recycler view
        deals = FirebaseUtil.mDeals;
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //populates traveldeal object with data snapshot
                TravelDeal td = dataSnapshot.getValue(TravelDeal.class);
                Log.d("Deal: ", td.getTitle());
                td.setId(dataSnapshot.getKey());
                deals.add(td);
                notifyItemInserted(deals.size()-1);
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
        //add listener to database reference
        mDatabaseReference.addChildEventListener(mChildEventListener);
    }


    @NonNull
    @Override
    public DealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.rv_row, parent, false);
        return new DealViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DealViewHolder holder, int position) {
    TravelDeal deal = deals.get(position);
    holder.bind(deal);

    }

    @Override
    public int getItemCount() {
        return deals.size();
    }

    public class DealViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //variable for text view
        TextView tvTitle;
        TextView tvDescription;
        TextView tvPrice;
        public DealViewHolder(@NonNull View itemView) {
            super(itemView);
            //reference to component in xml
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            tvDescription = (TextView) itemView.findViewById(R.id.tvDescription);
            tvPrice = (TextView) itemView.findViewById(R.id.tvPrice);

            imageDeal = (ImageView) itemView.findViewById(R.id.imageDeal);
            itemView.setOnClickListener(this);
        }

        //bind data passed to layout of row
        public void bind(TravelDeal deal) {
            //puts the deal's title into the textview from constructor
            tvTitle.setText(deal.getTitle());
            tvDescription.setText(deal.getDescription());
            tvPrice.setText(deal.getPrice());

            showImage(deal.getImageUrl());
        }

        @Override
        public void onClick(View v) {
            //gets position of item clicked
            int position = getAdapterPosition();
            Log.d("Click", String.valueOf(position));

            //gets position of deal selected via deals array list
            TravelDeal selectedDeal = deals.get(position);

            Intent intent = new Intent(itemView.getContext(), DealActivity.class);
            intent.putExtra("Deal", selectedDeal);
            itemView.getContext().startActivity(intent);


        }

        //method to display imageview using picasso
        private void showImage(String url) {
            if (url != null && !url.isEmpty()) {
                Picasso.get().load(url).resize(160, 160).centerCrop().into(imageDeal);
            }
        }
    }
}
