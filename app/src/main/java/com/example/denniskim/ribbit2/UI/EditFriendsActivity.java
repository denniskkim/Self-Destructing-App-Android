package com.example.denniskim.ribbit2.UI;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.denniskim.ribbit2.Adapter.UserAdapter;
import com.example.denniskim.ribbit2.Util.ParseConstants;
import com.example.denniskim.ribbit2.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;


public class EditFriendsActivity extends Activity {

    public static final String TAG = EditFriendsActivity.class.getSimpleName();
    protected List<ParseUser> mUsers; // to store the parse list after we get the list from the fetcher(findinBackground)
    protected ParseRelation<ParseUser> mFriendsRelation; // user relation variable
    protected ParseUser mCurrentUser;
    protected GridView mGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this method is used to request a few extended window features that have to do with the window our app is running
        //this must be called before setContentView, also used to use the progress bar
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.user_grid);

        mGridView = (GridView)findViewById(R.id.friendsGrid);

        // this is to change the mode of the checkbox list
        mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);
        mGridView.setOnItemClickListener(mOnItemClickListener);

        TextView emptyTextView = (TextView)findViewById(android.R.id.empty);
        mGridView.setEmptyView(emptyTextView);
    }

    /* whenever the activity is displayed. this resume method is called */
    @Override
    protected void onResume() {
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser(); // gets the current user of the app
        // this gets the relation of the user.
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_RELATION);


        setProgressBarIndeterminateVisibility(true);
        //The ParseUser is a local representation of user data that can be saved and retrieved from the Parse cloud.
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        //we can assort the data field ascending or descending.
        // the parameter is the key of the api. so it will order the username
        query.orderByAscending(ParseConstants.KEY_USERNAME);
        //setting limits so that if there is a huge database, it wont take long
        query.setLimit(1000);
        //fetch query objects
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {
                setProgressBarIndeterminateVisibility(false);
                if (e == null) { //then success. have users to display
                    mUsers = list;
                    //initializing the array of username that will be used to display them
                    String[] username = new String[mUsers.size()];
                        int i = 0;
                        // for every ParseUser user in mList populate the username array
                        // for each works nicely with generics
                        // this is populating the mList list into the ParseUser user object
                        for (ParseUser user : mUsers) {
                            //
                            username[i] = user.getUsername();
                            i++;
                    }
                    /*
                        An adapater is something that knows about the list items and how to represent them
                        and draw each list item on the screen
                        first parameter: context
                        second parameter: defines the layout of the list that defines how the list item appears
                        third parameter: data to create view for the list item
                     */
                    if(mGridView.getAdapter() == null) {
                        UserAdapter adapter = new UserAdapter(EditFriendsActivity.this, mUsers);
                        mGridView.setAdapter(adapter);
                    }
                    else{ // want to refill it/ recycle it
                        ((UserAdapter)mGridView.getAdapter()).refill(mUsers);
                    }

                    addFriendCheckMarks();
                } else { //failure
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditFriendsActivity.this);
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
    }

    // to keep the check marks to see what friends you already added
    private void addFriendCheckMarks(){
        // retrieves the query that is associated with this parse relation
        // ALL OF THESE annoynomous inner methods are asynchronous tasks
        mFriendsRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {
                if(e==null) { //succeded and the list was returned and look for a match
                    // to look for a match, well just loop through the list
                    for(int i=0; i<mUsers.size(); i++)
                    {
                        ParseUser user = mUsers.get(i);

                        for(ParseUser friend: list)
                        {
                            if(friend.getObjectId().equals(user.getObjectId()))
                            {
                                mGridView.setItemChecked(i, true);
                            }
                        }
                    }
                }
                else{
                    Log.e(TAG,e.getMessage());
                }
            }
        });
    }

    // this method deals with what happens when the user is checked or not. and saves the information
    // to the backend of parse using the API
    protected AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            ImageView checkImageView = (ImageView)view.findViewById(R.id.checkImageView);

            if(mGridView.isItemChecked(position)) // add a friend
            {
                // add friends according to what has been tapped on from the list of friends
                mFriendsRelation.add(mUsers.get(position));
                checkImageView.setVisibility(View.VISIBLE);
            }

            else{//remove friend locally and in the backend
                mFriendsRelation.remove((mUsers.get(position)));
                checkImageView.setVisibility(View.INVISIBLE);
            }
            // this will be called for both if a friend is checked or not.
            // this code updates the backend of the code. in the parse data cloud
            // on the backend it will create a list of relations for the user.
            // DUPLICATES are handled by parse because it creates a unique key for each relation
            mCurrentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    // dont need to do anything if it was successful
                    if(e!=null){// failed
                        Log.e(TAG,e.getMessage());
                    }
                }
            });

        }
    };
}
