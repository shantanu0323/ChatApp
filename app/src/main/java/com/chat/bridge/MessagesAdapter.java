package com.chat.bridge;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Shaan on 02-12-2017.
 */

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
    private List<Messages> messagesList;
    private FirebaseAuth mAuth;
    private static final String TAG = "MessagesAdapter";
    private Context context;

    public MessagesAdapter(List<Messages> messagesList, Context context) {
        this.messagesList = messagesList;
        this.context = context;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_item_layout, parent, false);
        return new MessageViewHolder(view);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messsageBody;
        public CircularImageView senderProfileImage;

        public MessageViewHolder(View itemView) {
            super(itemView);

            messsageBody = (TextView) itemView.findViewById(R.id.messageBody);
            senderProfileImage = (CircularImageView) itemView.findViewById(R.id.senderProfileImage);
        }
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        mAuth = FirebaseAuth.getInstance();
        final String currentUserId = mAuth.getCurrentUser().getUid();
        Messages msg = messagesList.get(position);
        final String fromUserId = msg.getFrom();
        final CircularImageView ivThumbnail = holder.senderProfileImage;
        final String[] thumbnail = new String[1];
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                thumbnail[0] = dataSnapshot.child(fromUserId).child("thumbnail").getValue().toString();
                Picasso.with(context).load(thumbnail[0]).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_image).into(ivThumbnail, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Log.e(TAG, "onError: FAILED TO LOAD IMAGE");
                        Picasso.with(context).load(thumbnail[0]).placeholder(R.drawable.default_image).into(ivThumbnail);
                        }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if (fromUserId.equals(currentUserId)) {
            holder.messsageBody.setBackgroundColor(Color.WHITE);
            holder.messsageBody.setTextColor(Color.BLACK);

        } else {
            holder.messsageBody.setBackgroundResource(R.drawable.bg_messsage_text);
            holder.messsageBody.setTextColor(Color.WHITE);
        }
        holder.messsageBody.setText(msg.getMessage());
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }


}
