package com.example.denniskim.ribbit2.UI;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import com.example.denniskim.ribbit2.Adapter.UserAdapter;
import com.example.denniskim.ribbit2.Util.ParseConstants;
import com.example.denniskim.ribbit2.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.List;

public class FriendsFragment extends Fragment {

    public static final String TAG = FriendsFragment.class.getSimpleName();

    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseUser mCurrentUser;
    protected List<ParseUser> mFriends;
    protected GridView mGridView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.user_grid,
                container, false);

        mGridView=(GridView) rootView.findViewById(R.id.friendsGrid);

        TextView emptyTextView = (TextView)rootView.findViewById(android.R.id.empty);
        mGridView.setEmptyView(emptyTextView);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_RELATION);

        getActivity().setProgressBarIndeterminateVisibility(true);

        ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
        query.addAscendingOrder(ParseConstants.KEY_USERNAME);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {
                getActivity().setProgressBarIndeterminateVisibility(false);

                if (e == null) {
                    mFriends = friends;

                    String[] usernames = new String[mFriends.size()];
                    int i = 0;
                    for(ParseUser user : mFriends) {
                        usernames[i] = user.getUsername();
                        i++;
                    }
                    if(mGridView.getAdapter() == null) {
                        UserAdapter adapter = new UserAdapter(getActivity(), mFriends);
                        mGridView.setAdapter(adapter);
                    }
                    else{ // want to refill it/ recycle it
                        ((UserAdapter)mGridView.getAdapter()).refill(mFriends);
                    }
                }
                else {
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(e.getMessage())
                            .setTitle(R.string.error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

}
/*
public class FriendsFragment extends ListFragment {
    public static final String TAG = FriendsFragment.class.getSimpleName();

    protected List<ParseUser> mFriends; // to store the parse list after we get the list from the fetcher(findinBackground)
    protected ParseRelation<ParseUser> mFriendsRelation; // user relation variable
    protected ParseUser mCurrentUser;

    // this method is called when the Fragment is called for the first time and creates the friend
    // data into the fragment container
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // similar to setContent view, creates view in code
        // second parameter -- where the fragment will be displayed. be the view pager from main activity
        // last parameter should be false whenever were adding a fragment to an activity.
        View rootView = inflater.inflate(R.layout.user_grid, container, false);
        return rootView;
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
        getActivity().setProgressBarIndeterminateVisibility(true);
        // using mFriends as the data source
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {
                getActivity().setProgressBarIndeterminateVisibility(false);
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
                               item appears
                  third parameter: data to create view for the list item
                */
                 /*   ArrayAdapter<String> adapter = new ArrayAdapter<String>(getListView().getContext(),
                            android.R.layout.simple_list_item_1,
                            username);
                    //populate the list in the container
                    setListAdapter(adapter);
                } else {
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(getListView().getContext());
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
}// FriendFragment end */

