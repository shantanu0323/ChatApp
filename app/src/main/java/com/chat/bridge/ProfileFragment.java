package com.chat.bridge;


import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {
    // CONSTANT FIELDS
    private static final String TAG = "ProfileFragment";
    private static final int NOT_FRIENDS = 0;
    private static final int REQUEST_SENT = 1;
    private static final int REQUEST_RECEIVED = 2;
    private static final int FRIENDS = 3;
    private static String RECEIVED;
    private static String SENT;
    private static Drawable BG_SEND;
    private static Drawable BG_CANCEL;
    private static Drawable BG_UNFRIEND;
    private static Drawable BG_ACCEPT;


    String userId;
    private ImageView profilepic;
    private LinearLayout actionContainer;
    private TextView tvDisplayName;
    private TextView tvStatus;
    private TextView tvTotalFriends;
    private ProgressDialog progressDialog;
    private ProgressBar progressBar;
    private Button bAction;
    private Button bDeclineRequest;

    private DatabaseReference userDatabase;
    private DatabaseReference friendRequests;
    private DatabaseReference friends;
    private DatabaseReference rootRef;
    private DatabaseReference notifications;

    private String currentUserId;
    private int friendshipStatus;

    public static ProfileFragment newInstance(String userId) {

        Bundle args = new Bundle();
        args.putString("userId", userId);
        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        Bundle args = this.getArguments();
        this.userId = args.getString("userId");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(currentUserId);
        friendRequests = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        friends = FirebaseDatabase.getInstance().getReference().child("Friends");
        notifications = FirebaseDatabase.getInstance().getReference().child("Notifications");
        rootRef = FirebaseDatabase.getInstance().getReference();

        findViews(root);
        initConstants();
//        friendshipStatus = NOT_FRIENDS;
        populateViews(userId);
        return root;
    }

    private void initConstants() {
        SENT = getString((R.string.sent));
        RECEIVED = getString(R.string.received);
        BG_CANCEL = getResources().getDrawable(R.drawable.bg_cancel_request);
        BG_UNFRIEND = BG_CANCEL;
        BG_SEND = getResources().getDrawable(R.drawable.bg_send_request);
        BG_ACCEPT = getResources().getDrawable(R.drawable.bg_accept_request);
    }

    private void populateViews(final String userId) {
        DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userId)) {
                    HashMap<String, String> currentUser = (HashMap<String, String>) dataSnapshot.child(userId).getValue();
                    tvDisplayName.setText(currentUser.get("name"));
                    tvStatus.setText(currentUser.get("status"));
                    String imageUrl = currentUser.get("image");
                    if (!imageUrl.equalsIgnoreCase("default")) {
                        Picasso.with(getActivity()).load(imageUrl).placeholder(R.drawable.default_image).into(profilepic);
                    }
                }
//                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
//                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "onCancelled: Retreiving Failed : " + databaseError.getMessage());
                Snackbar.make(getActivity().getCurrentFocus(), "Sorry!!! I could not load your profile right now... :\n" + databaseError.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
        if (currentUserId.equals(userId)) {
            updateAction(R.string.own_profile);

        } else {
            friendRequests.child(currentUserId).child(userId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild("requestType")) {
                                String requestType = dataSnapshot.child("requestType").getValue().toString();
                                Log.i(TAG, "onDataChange: requestType : " + requestType);
                                if (requestType.equals(SENT)) {
                                    friendshipStatus = REQUEST_SENT;
                                    updateAction(R.string.cancel);
                                } else if (requestType.equals(RECEIVED)) {
                                    friendshipStatus = REQUEST_RECEIVED;
                                    updateAction(R.string.accept);
                                }
                            } else {
                                // Request not sent yet or cancelled or accepted
                                if (friendshipStatus == NOT_FRIENDS) {
                                    updateAction(R.string.sent);
                                } else if (friendshipStatus == FRIENDS) {
                                    updateAction(R.string.accepted);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
            friends.child(currentUserId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(userId)) {
                                // FRIENDS
                                friendshipStatus = FRIENDS;
                                updateAction(R.string.accepted);
                            } else {
                                // NOT_FRIENDS

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }

    private void updateAction(int action) {
        switch (action) {
            case R.string.cancel:
                bDeclineRequest.setVisibility(View.GONE);
                bAction.setBackground(BG_CANCEL);
                bAction.setText("Cancel Request");
                bAction.setVisibility(View.VISIBLE);
                actionContainer.setVisibility(View.VISIBLE);
                break;
            case R.string.sent:
                bDeclineRequest.setVisibility(View.GONE);
                bAction.setBackground(BG_SEND);
                bAction.setText("Send Request");
                bAction.setVisibility(View.VISIBLE);
                actionContainer.setVisibility(View.VISIBLE);
                break;
            case R.string.accept:
                bDeclineRequest.setVisibility(View.VISIBLE);
                bAction.setBackground(BG_ACCEPT);
                bAction.setText("Accept Request");
                bAction.setVisibility(View.VISIBLE);
                actionContainer.setVisibility(View.VISIBLE);
                break;
            case R.string.accepted:
                bDeclineRequest.setVisibility(View.GONE);
                bAction.setBackground(BG_UNFRIEND);
                bAction.setText("UnFriend");
                bAction.setVisibility(View.VISIBLE);
                actionContainer.setVisibility(View.VISIBLE);
                break;
            case R.string.own_profile:
                bAction.setVisibility(View.GONE);
                bDeclineRequest.setVisibility(View.GONE);
                actionContainer.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void findViews(View root) {
        profilepic = (ImageView) root.findViewById(R.id.profilepic);
        bAction = (Button) root.findViewById(R.id.bAction);
        actionContainer = (LinearLayout) root.findViewById(R.id.actionContainer);
        tvDisplayName = (TextView) root.findViewById(R.id.tvDisplayName);
        tvStatus = (TextView) root.findViewById(R.id.tvStatus);
        tvTotalFriends = (TextView) root.findViewById(R.id.tvTotalFriends);
        progressBar = (ProgressBar) root.findViewById(R.id.progressBar);
        bAction = (Button) root.findViewById(R.id.bAction);
        bDeclineRequest = (Button) root.findViewById(R.id.bDeclineRequest);

        bAction.setOnClickListener(this);
        bDeclineRequest.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        bAction.setEnabled(false);
        switch (v.getId()) {
            case R.id.bAction:
                if (friendshipStatus == NOT_FRIENDS) {
                    Log.d(TAG, "onClick: NOT_FRIENDS");

                    String notificationId = notifications.child(userId).push().getKey();
                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", currentUserId);
                    notificationData.put("type", "request");

                    Map requestMap = new HashMap();
                    requestMap.put("FriendRequests/" + currentUserId + "/" + userId + "/requestType", "sent");
                    requestMap.put("FriendRequests/" + userId + "/" + currentUserId + "/requestType", "received");
                    requestMap.put("Notifications/" + userId + "/" + notificationId, notificationData);

                    rootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            bAction.setEnabled(true);
                            Toast.makeText(getActivity(), "Request Sent Successfully!!!", Toast.LENGTH_SHORT).show();
                            friendshipStatus = REQUEST_SENT;
                            updateAction(R.string.cancel);
                        }
                    });

                } else if (friendshipStatus == REQUEST_SENT) {
                    Log.d(TAG, "onClick: REQUEST_SENT");

                    Map requestMap = new HashMap();
                    requestMap.put("FriendRequests/" + currentUserId + "/" + userId + "/requestType", null);
                    requestMap.put("FriendRequests/" + userId + "/" + currentUserId + "/requestType", null);

                    rootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            bAction.setEnabled(true);
                            Toast.makeText(getActivity(), "Friend Request Cancelled successfully", Toast.LENGTH_SHORT).show();
                            friendshipStatus = NOT_FRIENDS;
                            updateAction(R.string.sent);
                        }
                    });

                } else if (friendshipStatus == REQUEST_RECEIVED) {
                    Log.d(TAG, "onClick: REQUEST_RECEIVED");
                    final String date = DateFormat.getDateTimeInstance().format(new Date());

                    Map requestMap = new HashMap();
                    requestMap.put("Friends/" + currentUserId + "/" + userId + "/sinceDate", date);
                    requestMap.put("Friends/" + userId + "/" + currentUserId + "/sinceDate", date);
                    requestMap.put("FriendRequests/" + currentUserId + "/" + userId + "/requestType", null);
                    requestMap.put("FriendRequests/" + userId + "/" + currentUserId + "/requestType", null);

                    rootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            bAction.setEnabled(true);
                            friendshipStatus = FRIENDS;
                            updateAction(R.string.accepted);
                            Toast.makeText(getActivity(), "You are now friends with " + tvDisplayName.getText().toString()
                                    + "!!!", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else if (friendshipStatus == FRIENDS) {
                    // Unfriend the friend
                    Log.d(TAG, "onClick: FRIENDS");

                    Map requestMap = new HashMap();
                    requestMap.put("Friends/" + currentUserId + "/" + userId, null);
                    requestMap.put("Friends/" + userId + "/" + currentUserId, null);

                    rootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            bAction.setEnabled(true);
                            friendshipStatus = NOT_FRIENDS;
                            Toast.makeText(getActivity(), "You are no more friends with "
                                    + tvDisplayName.getText().toString() + "!!!", Toast.LENGTH_SHORT).show();
                            updateAction(R.string.sent);
                        }
                    });
                }
                break;
            case R.id.bDeclineRequest:

                Map requestMap = new HashMap();
                requestMap.put("FriendRequests/" + currentUserId + "/" + userId + "/requestType", null);
                requestMap.put("FriendRequests/" + userId + "/" + currentUserId + "/requestType", null);

                rootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        bAction.setEnabled(true);
                        friendshipStatus = NOT_FRIENDS;
                        updateAction(R.string.sent);
                        Toast.makeText(getActivity(), "Friend Request Declined !!!", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }
    }

}
