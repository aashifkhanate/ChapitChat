package com.mahija.teenchapit.chapitchat;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    FirebaseAuth mAuth;

    //Searching
    private EditText mSearchField;
    private ImageButton mSearchBtn;

    private RecyclerView mUsersList;

    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mSearchField = (EditText)findViewById(R.id.user_search_field);
        mSearchBtn = (ImageButton) findViewById(R.id.user_search_btn);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference("Users");
        mAuth = FirebaseAuth.getInstance();

        mUsersList = (RecyclerView)findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        mUsersDatabase.child(mAuth.getCurrentUser().getUid()).child("online").setValue("true");

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchText = mSearchField.getText().toString();

                Query firebaseSearchQuery = mUsersDatabase.orderByChild("name").startAt(searchText).endAt(searchText + "\uf8ff");

                FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                        Users.class,
                        R.layout.users_single_layout,
                        UsersViewHolder.class,
                        firebaseSearchQuery
                ) {
                    @Override
                    protected void populateViewHolder(UsersViewHolder usersViewHolder, final Users users, int position) {
                        usersViewHolder.setName(users.getName());
                        usersViewHolder.setStatus(users.getStatus());
                        usersViewHolder.setUserImage(users.getThumb_image(), getApplicationContext());

                        final String user_id = getRef(position).getKey();

                        usersViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent profileIntent = new Intent(UsersActivity.this, ProfileActivity.class);
                                profileIntent.putExtra("user_id", user_id);

                                Pair[] pairs = new Pair[3];

                                pairs[0] = new Pair<View, String>(view.findViewById(R.id.user_single_image), "profileImage");
                                pairs[1] = new Pair<View, String>(view.findViewById(R.id.user_single_name), "userName");
                                pairs[2] = new Pair<View, String>(view.findViewById(R.id.user_single_status), "userStatus");

                                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(UsersActivity.this, pairs);
                                startActivity(profileIntent, options.toBundle());
                            }
                        });
                    }
                };

                mUsersList.setAdapter(firebaseRecyclerAdapter);
            }
        });

    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setName(String name){
            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }
        public void setStatus(String status){
            TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);
        }
        public void setUserImage(String thumb_image, Context ctx){
            CircleImageView userImageView = (CircleImageView)mView.findViewById(R.id.user_single_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.batman).into(userImageView);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        mUsersDatabase.child(mAuth.getCurrentUser().getUid()).child("online").setValue(ServerValue.TIMESTAMP);
    }
}
