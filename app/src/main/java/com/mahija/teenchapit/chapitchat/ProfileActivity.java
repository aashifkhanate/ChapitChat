package com.mahija.teenchapit.chapitchat;

import android.app.ProgressDialog;
import android.media.Image;
import android.os.PersistableBundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileFriendsCount, mProfileName, mProfileStatus;
    protected Button button1, button2;

    private DatabaseReference mRef;

    private FirebaseUser mCurrent_user;

    private String mCurrent_state, user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mRef = FirebaseDatabase.getInstance().getReference();
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();
        user_id = getIntent().getStringExtra("user_id");
        mCurrent_state = "notFriends";


        mProfileName = (TextView)findViewById(R.id.profile_displayName);
        mProfileStatus = (TextView)findViewById(R.id.profile_status);
        mProfileFriendsCount = (TextView)findViewById(R.id.profile_totalFriends);
        mProfileImage = (ImageView)findViewById(R.id.profile_image);
        button1 = (Button)findViewById(R.id.profile_send_rqst_btn);
        button2 = (Button)findViewById(R.id.profile_decline_btn);

        Log.d("user_id", user_id);
        Log.d("mCurrent_user", mCurrent_user.getUid());

        if(user_id.equals(mCurrent_user.getUid())) {
            mCurrent_state = "sole";
            button1.setVisibility(View.GONE);
            button2.setVisibility(View.GONE);
        }

        mRef.child("Users").child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                mProfileName.setText(display_name);
                mProfileStatus.setText(status);
                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.batman).into(mProfileImage);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        if(!user_id.equals(mCurrent_user.getUid()))
        mRef.child("Friend_req").child(mCurrent_user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(user_id)){
                    String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                    if(req_type.equals("received")){
                        mCurrent_state = "reqReceived";
                    }else if(req_type.equals("sent")){
                        mCurrent_state = "reqSent";
                    }
                }else{
                    mRef.child("Friends").child(mCurrent_user.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(user_id)){
                                mCurrent_state = "friends";
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
                buttonDoer();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        if(!user_id.equals(mCurrent_user.getUid()))
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button1.setEnabled(false);
                profileDoer();
            }
        });

        if(!user_id.equals(mCurrent_user.getUid()))
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button2.setEnabled(false);
                anotherDoer();
            }
        });

    }

    private void anotherDoer() {
        mRef.child("Friend_req").child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mRef.child("Friend_req").child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mCurrent_state = "notFriends";
                        buttonDoer();
                    }
                });
            }
        });
        button2.setEnabled(true);
    }

    private void profileDoer() {
        Toast.makeText(this, mCurrent_state, Toast.LENGTH_SHORT).show();
        switch (mCurrent_state){
            case "notFriends":
                DatabaseReference newNotificationref = mRef.child("notifications").child(user_id).push();
                String newNotificationId = newNotificationref.getKey();

                HashMap<String, String> notificationData = new HashMap<>();
                notificationData.put("from", mCurrent_user.getUid());
                notificationData.put("type", "request");

                Map requestMap = new HashMap<>();
                requestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id + "/request_type","sent");
                requestMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid() + "/request_type","received");
                requestMap.put("notifications/" + user_id + "/" + newNotificationId, notificationData);

                mRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if(databaseError != null){
                            Toast.makeText(ProfileActivity.this, "There was some error in sending request", Toast.LENGTH_SHORT).show();
                        }else{
                            mCurrent_state = "reqSent";
                            buttonDoer();
                        }
                    }
                });
                button1.setEnabled(true);
                return;
            case "reqSent":
                mRef.child("Friend_req").child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mRef.child("Friend_req").child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mCurrent_state = "notFriends";
                                buttonDoer();
                            }
                        });
                    }
                });
                button1.setEnabled(true);
                return;
            case "reqReceived":
                final String currentDate = DateFormat.getDateInstance().format(new Date());
                Map friendsMap = new HashMap();
                friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id + "/date", currentDate);
                friendsMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid() + "/date", currentDate);
                friendsMap.put("Firends_req/" + mCurrent_user.getUid() + "/" + user_id, null);
                friendsMap.put("Firends_req/" + user_id + "/" + mCurrent_user.getUid(), null);
                mRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if(databaseError == null){
                            mCurrent_state = "friends";
                            buttonDoer();
                        }else{
                            String error = databaseError.getMessage();
                            Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                button1.setEnabled(true);
                return;
            case "friends":
                Map unfriendMap = new HashMap();
                unfriendMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id, null);
                unfriendMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid(), null);

                mRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if(databaseError == null){
                            mCurrent_state = "notFriends";
                            buttonDoer();
                        }else{
                            String error = databaseError.getMessage();
                            Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                button1.setEnabled(true);
                return;
            default:
                button1.setEnabled(true);
                return;
        }
    }

    private String buttonDoer() {
        button2.setVisibility(View.INVISIBLE);
        switch(mCurrent_state) {
            case "notFriends":
                button1.setText("Send Request");
                return mCurrent_state;
            case "reqSent":
                button1.setText("Cancel Request");
                return mCurrent_state;
            case "reqReceived":
                button2.setVisibility(View.VISIBLE);
                button1.setText("Accept");
                button2.setText("Reject");
                return mCurrent_state;
            case "friends":
                button1.setText("Unfriend");
                return mCurrent_state;
            default:
                return mCurrent_state;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRef.child("Users").child(mCurrent_user.getUid()).child("online").setValue(ServerValue.TIMESTAMP);
    }
}
