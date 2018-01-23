package com.mahija.teenchapit.chapitchat;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.GONE;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private RecyclerView mRequestList;

    private DatabaseReference mRequestDatabase;
    private DatabaseReference mRef;

    private String current_user_id;

    View v;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        v = inflater.inflate(R.layout.fragment_requests, container, false);
        // Inflate the layout for this fragment

        mRequestList = (RecyclerView)v.findViewById(R.id.request_list);

        current_user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(current_user_id);
        mRef = FirebaseDatabase.getInstance().getReference();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mRequestList.setHasFixedSize(true);
        mRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Requests, reqViewHolder> reqAdapter = new FirebaseRecyclerAdapter<Requests, reqViewHolder>(
                Requests.class,
                R.layout.request_card_view,
                RequestsFragment.reqViewHolder.class,
                mRequestDatabase
        ) {
            @Override
            protected void populateViewHolder(final reqViewHolder viewHolder, Requests model, int i) {

                final String list_user_id = getRef(i).getKey();

                mRef.child("Users").child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String naam = dataSnapshot.child("name").getValue().toString();
                        String image = dataSnapshot.child("thumb_image").getValue().toString();
                        viewHolder.setName(naam);
                        viewHolder.setUserImage(image, getContext());

                        v.findViewById(R.id.request_accept).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                v.setVisibility(View.GONE);
                                final String currentDate = DateFormat.getDateInstance().format(new Date());
                                Map friendsMap = new HashMap();
                                friendsMap.put("Friends/" + current_user_id + "/" + list_user_id + "/date", currentDate);
                                friendsMap.put("Friends/" + list_user_id + "/" + current_user_id + "/date", currentDate);
                                friendsMap.put("Firends_req/" + current_user_id + "/" + list_user_id, null);
                                friendsMap.put("Firends_req/" + list_user_id + "/" + current_user_id, null);
                                mRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    }
                                });
                            }
                        });
                        v.findViewById(R.id.request_decline).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                v.setVisibility(View.GONE);
                                mRef.child("Friend_req").child(current_user_id).child(list_user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mRef.child("Friend_req").child(list_user_id).child(current_user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                            }
                                        });
                                    }
                                });
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        mRequestList.setAdapter(reqAdapter);

    }

    public static class reqViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public reqViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setName(String name){
            TextView userNameView = (TextView) mView.findViewById(R.id.request_user_name);
            userNameView.setText(name);
        }

        public void setUserImage(String thumb_image, Context ctx){
            CircleImageView userImageView = (CircleImageView)mView.findViewById(R.id.request_single_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.batman).into(userImageView);
        }

    }
}
