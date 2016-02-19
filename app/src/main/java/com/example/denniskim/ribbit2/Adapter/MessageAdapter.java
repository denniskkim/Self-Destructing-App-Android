package com.example.denniskim.ribbit2.Adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.denniskim.ribbit2.Util.ParseConstants;
import com.example.denniskim.ribbit2.R;
import com.parse.ParseObject;

import java.util.Date;
import java.util.List;
/*
 * Creating a custom adapter for the inbox fragment
 */
public class MessageAdapter extends ArrayAdapter<ParseObject> {

    protected Context mContext;
    protected List<ParseObject> mMessages;

    public static final String TAG = MessageAdapter.class.getSimpleName();

    public MessageAdapter(Context context, List<ParseObject> messages){
        super(context, R.layout.message_item,messages);
        mContext = context;
        mMessages = messages;
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.message_item, null);
            holder = new ViewHolder();
            // need to extend from convertView because findView is an activity method.
            // b/c we want to get the layout and were not attached to activity yet
            holder.iconImageView = (ImageView) convertView.findViewById(R.id.messageIcon);
            holder.nameLabel = (TextView) convertView.findViewById(R.id.senderLabel);
            holder.timeLabel=(TextView)convertView.findViewById(R.id.timeLabel);
            convertView.setTag(holder);
        }
        else{
            // this recycles the data so we can be more efficient and gets us the tag we already created
            holder = (ViewHolder)convertView.getTag();
        }

        ParseObject message = mMessages.get(position);

        Date createdAt = message.getCreatedAt();
        // to get current time
        long now = new Date().getTime();
        // first paramter = time of the message
        // second parameter = the current time
        // third - resolution - minimum seconds, hours
        String convertedDate = DateUtils.getRelativeTimeSpanString(createdAt.getTime()
                                ,now, DateUtils.SECOND_IN_MILLIS).toString();

        holder.timeLabel.setText(convertedDate);


       // Log.d(TAG,"this is what get string gets: "+ message.getString(ParseConstants.KEY_FILE_TYPE));
        if(message.getString(ParseConstants.KEY_FILE_TYPE).equals(ParseConstants.TYPE_IMAGE)) {
            holder.iconImageView.setImageResource(R.drawable.ic_picture);
        }
        else{ //if its a video
           holder.iconImageView.setImageResource(R.drawable.ic_video);
        }

        holder.nameLabel.setText(message.getString(ParseConstants.KEY_SENDER_NAME));
        return convertView;
    }

    private static class ViewHolder{
        ImageView iconImageView;
        TextView nameLabel;
        TextView timeLabel;
    }

    public void refill(List<ParseObject> messages)
    {
        mMessages.clear();
        mMessages.addAll(messages);
        notifyDataSetChanged();
    }
}
