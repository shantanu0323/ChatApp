package com.chat.bridge;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private static final String TAG = "FriendsFragment";
    private RecyclerView friendsList;

    private DatabaseReference friendsDatabase;
    private DatabaseReference usersDatabase;
    private FirebaseAuth mAuth;

    private String currentUserId;

    private View mainView;

    public static FriendsFragment newInstance() {

        Bundle args = new Bundle();

        FriendsFragment fragment = new FriendsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_friends, container, false);

        friendsList = (RecyclerView) mainView.findViewById(R.id.friendsList);
        mAuth = FirebaseAuth.getInstance();

        currentUserId = mAuth.getCurrentUser().getUid();

        friendsDatabase = FirebaseDatabase.getInstance().getReference()
                .child("Friends").child(currentUserId);
        friendsDatabase.keepSynced(true);
        usersDatabase = FirebaseDatabase.getInstance().getReference()
                .child("Users");
        usersDatabase.keepSynced(true);
        friendsList.setHasFixedSize(true);
        friendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,
                R.layout.users_item_layout,
                FriendsViewHolder.class,
                friendsDatabase
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, final Friends friends, int position) {

                String listUserId = getRef(position).getKey();
                Log.i(TAG, "populateViewHolder: listUserId : " + listUserId);
//                Log.e(TAG, "populateViewHolder: ", );
                usersDatabase.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String userName = dataSnapshot.child("name").getValue().toString();
                        String thumbnail = dataSnapshot.child("thumbnail").getValue().toString();
                        String userOnline;
                        if (dataSnapshot.hasChild("online")) {
                            userOnline = dataSnapshot.child("online").getValue().toString();
                            viewHolder.setOnline(userOnline);
                        }
                        Log.e(TAG, "onDataChange: userName : " + userName);
                        Log.e(TAG, "onDataChange: thumbnail : " + thumbnail);
                        viewHolder.setName(userName);
                        viewHolder.setThumbnail(getActivity(), thumbnail);
                        viewHolder.setDate(friends.getSinceDate());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        friendsList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;
        private String online;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setDate(String date) {
            TextView userNameView = (TextView) mView.findViewById(R.id.tvStatus);
            userNameView.setText("since : " + date);
        }

        public void setName(String name) {
            TextView userNameView = (TextView) mView.findViewById(R.id.tvName);
            userNameView.setText(name);
        }

        public void setThumbnail(Context context, String thumbnail) {
            CircularImageView ivThumbnail = (CircularImageView) mView.findViewById(R.id.thumbnail);
            Picasso.with(context).load(thumbnail).placeholder(R.drawable.default_image).into(ivThumbnail);
        }

        public void setOnline(String online) {
            ImageView ivOnline = (ImageView) mView.findViewById(R.id.userOnlineIcon);
            if (online.equals("true")) {
                ivOnline.setVisibility(View.VISIBLE);
            } else {
                ivOnline.setVisibility(View.INVISIBLE);
            }
        }
    }
}
