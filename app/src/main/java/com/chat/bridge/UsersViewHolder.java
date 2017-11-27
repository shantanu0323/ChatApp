package com.chat.bridge;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

/**
 * Created by SHAAN on 15-07-17.
 */

public class UsersViewHolder extends RecyclerView.ViewHolder {
    View view;

    public UsersViewHolder(View itemView) {
        super(itemView);
        view = itemView;
    }

    public void setName(String name) {
        TextView tvName = (TextView) view.findViewById(R.id.tvName);
        tvName.setText(name);
    }

    public void setStatus(String status) {
        TextView tvStatus = (TextView) view.findViewById(R.id.tvStatus);
        tvStatus.setText(status);
    }

    public void setThumbnail(Context context, String thumbnail) {
        CircularImageView ivThumbnail = (CircularImageView) view.findViewById(R.id.thumbnail);
        Picasso.with(context).load(thumbnail).placeholder(R.drawable.default_image).into(ivThumbnail);
    }
}
