package com.chat.bridge;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

import java.text.SimpleDateFormat;
import java.util.Date;
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
                .inflate(R.layout.message_sender_item_layout, parent, false);
        return new MessageViewHolder(view);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageBody;
        public CircularImageView senderProfileImage;
        public LinearLayout messageBodyContainer;
        public ImageView ivSeen;
        public TextView tvTimestamp;

        public MessageViewHolder(View itemView) {
            super(itemView);

            messageBodyContainer = (LinearLayout) itemView.findViewById(R.id.messageBodyContainer);
            messageBody = (TextView) itemView.findViewById(R.id.messageBody);
            senderProfileImage = (CircularImageView) itemView.findViewById(R.id.senderProfileImage);
            ivSeen = (ImageView) itemView.findViewById(R.id.ivSeen);
            tvTimestamp = (TextView) itemView.findViewById(R.id.tvTimestamp);
        }
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        mAuth = FirebaseAuth.getInstance();
        final String currentUserId = mAuth.getCurrentUser().getUid();
        Messages msg = messagesList.get(position);

        boolean seen = Boolean.parseBoolean(msg.getSeen());


        if (seen) {
            holder.ivSeen.setImageResource(R.mipmap.ic_seen);
        } else {
            holder.ivSeen.setImageResource(R.mipmap.ic_not_seen);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
        String localTime = sdf.format(new Date(msg.getTime()));
        holder.tvTimestamp.setText(localTime);

        final String fromUserId = msg.getFrom();
        if (fromUserId != null) {
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
                holder.ivSeen.setVisibility(View.VISIBLE);
                holder.messageBodyContainer.setBackgroundResource(R.drawable.bg_receiver_messsage_text);
                holder.senderProfileImage.setVisibility(View.GONE);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ALIGN_PARENT_END);
                params.setMargins(200, 20, 20, 20);
                holder.messageBodyContainer.setLayoutParams(params);
            } else {
                holder.ivSeen.setVisibility(View.GONE);
                holder.messageBodyContainer.setBackgroundResource(R.drawable.bg_sender_messsage_text);
                holder.senderProfileImage.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.END_OF, R.id.senderProfileImage);
                params.addRule(RelativeLayout.CENTER_VERTICAL);
                params.setMargins(20, 0, 200, 0);
                holder.messageBodyContainer.setLayoutParams(params);
            }
            holder.messageBody.setText(msg.getMessage());
        }
        if (holder.messageBody.getText().toString().equals("XEADSFTRD23")) {
            holder.messageBodyContainer.setVisibility(View.GONE);
            holder.senderProfileImage.setVisibility(View.GONE);
        } else {
            holder.messageBodyContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }


}
