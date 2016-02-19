package com.example.denniskim.ribbit2.UI;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.denniskim.ribbit2.Adapter.UserAdapter;
import com.example.denniskim.ribbit2.Util.FileHelper;
import com.example.denniskim.ribbit2.Util.ParseConstants;
import com.example.denniskim.ribbit2.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;



public class RecipientsActivity extends Activity {

    public static final String TAG = RecipientsActivity.class.getSimpleName();

    protected List<ParseUser> mFriends; // to store the parse list after we get the list from the fetcher(findinBackground)
    protected ParseRelation<ParseUser> mFriendsRelation; // user relation variable
    protected ParseUser mCurrentUser;
    protected MenuItem mSendButton;
    protected Uri mMediaUri;
    protected String mFileType;
    protected GridView mGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.user_grid);
        setupActionBar();
        mGridView = (GridView)findViewById(R.id.friendsGrid);
        // this allows us to check the designated item on the list
        mGridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mGridView.setOnItemClickListener(mOnClickListener);

        TextView emptyTextView = (TextView)findViewById(android.R.id.empty);
        mGridView.setEmptyView(emptyTextView);

        mMediaUri = getIntent().getData();
        // this gets the extra data that was initialized in main activity to get the file type
        // getting the extra with the key that was given in main activity as well
        mFileType = getIntent().getExtras().getString(ParseConstants.KEY_FILE_TYPE);
    }

    @Override
    public void onResume() {
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser(); // gets the current user of the app
        //access/create a relation value for a key.
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_RELATION);
        //this gets the list of friends
        ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
        query.addAscendingOrder(ParseConstants.KEY_USERNAME);
        //need getActivity because this class doesnt extend activity. so obtain it by getting parents Activity class
        setProgressBarIndeterminateVisibility(true);
        // using mFriends as the data source
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {
                setProgressBarIndeterminateVisibility(false);
                if (e == null) {
                    mFriends = list;
                    String[] username = new String[mFriends.size()];
                    int i = 0;
                    // for every ParseUser user in mList populate the username array
                    // for each works nicely with generics
                    // this is populating the mList list into the ParseUser user object
                    for (ParseUser user : mFriends) {
                        //
                        username[i] = user.getUsername();
                        i++;

                    }
               /*
                  An adapater is something that knows about the list items and how to
                         represent them and draw each list item on the screen
                  first parameter: context: need to do it this way cause this class doesnt
                                  extend the activity or context
                  second parameter: defines the layout of the list that defines how the list
                               item appears... this makes a check list
                  third parameter: data to create view for the list item
                */
                    if (mGridView.getAdapter() == null) {
                        UserAdapter adapter = new UserAdapter(RecipientsActivity.this, mFriends);
                        mGridView.setAdapter(adapter);
                    } else { // want to refill it/ recycle it
                        ((UserAdapter) mGridView.getAdapter()).refill(mFriends);
                    }
                } else {
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
                    // this will get the appropriate exception message
                    builder.setMessage(e.getMessage());
                    builder.setTitle(R.string.error_title);
                    // type of button to be invoked when the user sees the dialog and wants it to go away
                    // setting the listener(2nd parameter) to null means that you dont want the button to do anything
                    builder.setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

            }
        });


    }//onResume end

    private void setupActionBar() {

        getActionBar().setDisplayHomeAsUpEnabled(true);

    }
    //creates the menu, which is the send button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recipients, menu);

        //get the menu item object so we can make it visible
        mSendButton = menu.getItem(0);
        return true;
    }

    // where the button implementation is at. what you do if you press the send button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send) {
            //1.create parse object
            ParseObject message = createMessage();
            if(message ==null)//error
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("There was an error with the file selected. Please select another message")
                        .setTitle("We're Sorry")
                        .setPositiveButton(android.R.string.ok,null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            // 2.send code to backend
            else{ // success
                send(message);
                // finishes the current activity and will go back to the previous activity(parent activity)
                finish();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    protected ParseObject createMessage()
    {
        ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
        // putting the data into the backend cloud with specified id we create to label them
        message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
        message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
        message.put(ParseConstants.KEY_RECEPIENT_IDS, getRecipientIds());
        message.put(ParseConstants.KEY_FILE_TYPE, mFileType);

        byte[] fileBytes = FileHelper.getByteArrayFromFile(this, mMediaUri);
        if(fileBytes == null)
        {
            return null;
        }
        else{
            if(mFileType.equals(ParseConstants.TYPE_IMAGE))
            {
                // to compress the image
                fileBytes= FileHelper.reduceImageForUpload(fileBytes);
            }

            String fileName = FileHelper.getFileName(this,mMediaUri,mFileType);
            ParseFile file = new ParseFile(fileName,fileBytes);
            message.put(ParseConstants.KEY_FILE, file);

            return message;
        }
    }

    protected ArrayList<String> getRecipientIds(){
        ArrayList<String> recipientIds = new ArrayList<String>();
        for(int i = 0; i<mGridView.getCount();i++)
        {
            if(mGridView.isItemChecked(i)) // will mean that person is a recipient
            {
                recipientIds.add(mFriends.get(i).getObjectId());
            }
        }
        return recipientIds;
    }

    protected void send(ParseObject message)
    {
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e==null) // success
                {
                    Toast.makeText(RecipientsActivity.this, "Message Sent!", Toast.LENGTH_LONG).show();
                    sendPushNotifications();
                }

                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
                    builder.setMessage("There was an error sending your message")
                            .setTitle("We're Sorry")
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
            }
        });
    }

    protected AdapterView.OnItemClickListener mOnClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            if(mGridView.getCheckedItemCount() > 0) {
                mSendButton.setVisible(true);
            }
            else{
                mSendButton.setVisible(false);
            }

            ImageView checkImageView = (ImageView)view.findViewById(R.id.checkImageView);

            if(mGridView.isItemChecked(position)) // add a friend
            {
                //add the recipient
                checkImageView.setVisibility(View.VISIBLE);
            }

            else{//remove friend locally and in the backend

                checkImageView.setVisibility(View.INVISIBLE);
            }

        }
    };

    protected void sendPushNotifications()
    {
        ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
        // check the user id to see if its contained in the recipients id
        query.whereContainedIn(ParseConstants.KEY_USER_ID, getRecipientIds());

        //send push notification
        ParsePush push = new ParsePush();
        push.setQuery(query);
        push.setMessage(getString(R.string.push_message, ParseUser.getCurrentUser().getUsername()));
        push.sendInBackground();
    }
}