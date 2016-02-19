package com.example.denniskim.ribbit2.Adapter;

import android.content.Context;
import android.media.Image;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.denniskim.ribbit2.R;
import com.example.denniskim.ribbit2.Util.MD5Util;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;

public class UserAdapter extends ArrayAdapter<ParseUser> {

    protected Context mContext;
    protected List<ParseUser> mUsers;

    public static final String TAG = UserAdapter.class.getSimpleName();

    public UserAdapter(Context context, List<ParseUser> users){
        super(context, R.layout.message_item,users);
        mContext = context;
        mUsers = users;
    }

    // want to inflate the View and return it to the list
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        //need this check so you can create it when its not null and when there is something there
        // we want to just 'recycle' or change the data so we can save memory instead of creating
        // everything at once
        if(convertView==null) {
            //LayoutInflator takes an XML file and makes it into a layout
            convertView = LayoutInflater.from(mContext).inflate(R.layout.user_item, null);
            holder = new ViewHolder();
            // need to extend from convertView because findView is an activity method.
            // b/c we want to get the layout and were not attached to activity yet
            holder.userImageView = (ImageView) convertView.findViewById(R.id.userImageView);
            holder.nameLabel = (TextView) convertView.findViewById(R.id.nameLabel);
            holder.checkImageView= (ImageView)convertView.findViewById(R.id.checkImageView);
            convertView.setTag(holder);
        }
        else{
            // this recycles the data so we can be more efficient and gets us the tag we already created
            holder = (ViewHolder)convertView.getTag();
        }

        ParseUser user = mUsers.get(position);
        /*
            this code below is using the implementation of gravatar  to change the users email to
            hex and grab the image
         */
        String email= user.getEmail().toLowerCase();
        if(email.equals(""))
        {
            holder.userImageView.setImageResource(R.drawable.avatar_empty);
        }

        else{
            String hash = MD5Util.md5Hex(email);
            // this will dynamically set the image size to gravatar which is the s=204
            // the &d=404 is when there is no image and we set a default image
            String gravatarUrl = "http://www.gravatar.com/avatar/" + hash + "?s=204&d=404";
            Picasso.with(mContext)
                    .load(gravatarUrl)
                    .placeholder(R.drawable.avatar_empty)
                    .into(holder.userImageView);
        }

        Date createdAt = user.getCreatedAt();
        // to get current time
        long now = new Date().getTime();
        // first paramter = time of the message
        // second parameter = the current time
        // third - resolution - minimum seconds, hours
        String convertedDate = DateUtils.getRelativeTimeSpanString(createdAt.getTime(),
                                now, DateUtils.SECOND_IN_MILLIS).toString();


        holder.nameLabel.setText(user.getUsername());

        GridView gridView = (GridView)parent;
        if(gridView.isItemChecked(position))
        {
            holder.checkImageView.setVisibility(View.VISIBLE);
        }

        else{
            holder.checkImageView.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    private static class ViewHolder{
        ImageView userImageView;
        ImageView checkImageView;
        TextView nameLabel;

    }

    public void refill(List<ParseUser> users)
    {
        mUsers.clear();
        mUsers.addAll(users);
        notifyDataSetChanged();
    }
}
