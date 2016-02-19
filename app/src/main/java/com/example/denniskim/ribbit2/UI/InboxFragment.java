package com.example.denniskim.ribbit2.UI;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.denniskim.ribbit2.Adapter.MessageAdapter;
import com.example.denniskim.ribbit2.Util.ParseConstants;
import com.example.denniskim.ribbit2.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;



public class InboxFragment extends ListFragment{

    protected List<ParseObject> mMessages;
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    // this method is called when the Fragment is called for the first time, which is different than
    // activities in that it uses the onCreateView method instead of onCreate
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // similar to setContent view, creates view in code
        // second parameter -- where the fragment will be displayed. be the view pager from main activity
        // last parameter should be false whenever were adding a fragment to an activity.
        View rootView = inflater.inflate(R.layout.fragment_inbox, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefresh);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        mSwipeRefreshLayout.setColorScheme(R.color.swipeRefresh1,R.color.swipeRefresh2,R.color.swipeRefresh3,
                R.color.swipeRefresh4);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setProgressBarIndeterminateVisibility(true);
        retrieveMessages();
    }

    private void retrieveMessages() {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_MESSAGES);
        //goes through the entire query with the key of recepients id and looks for matches
        query.whereEqualTo(ParseConstants.KEY_RECEPIENT_IDS, ParseUser.getCurrentUser().getObjectId());
        query.orderByDescending(ParseConstants.KEY_TIME);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> messages, ParseException e) {
                getActivity().setProgressBarIndeterminateVisibility(false);
                if(mSwipeRefreshLayout.isRefreshing())// check if its refreshing
                {
                    mSwipeRefreshLayout.setRefreshing(false); // to stop the refreshing animation
                }
                if(e==null) //success, found messages
                {
                    // setting the messages of the list recieved from this done method
                    mMessages = messages;
                    String[] username = new String[mMessages.size()];
                    int i = 0;
                    // for every ParseUser user in mList populate the username array
                    // for each works nicely with generics
                    // this is populating the mList list into the ParseUser user object
                    for (ParseObject message: mMessages) {
                        //
                        username[i] = message.getString(ParseConstants.KEY_SENDER_NAME);
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
                   if(getListView().getAdapter() == null) {
                       MessageAdapter adapter = new MessageAdapter(getListView().getContext(),
                               mMessages);
                       //populate the list in the container
                       setListAdapter(adapter);
                   }

                    else{ // refill adapter with the correct position.
                       ((MessageAdapter) getListView().getAdapter()).refill(mMessages);
                   }
                }
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        ParseObject message = mMessages.get(position);
        String messageType = message.getString(ParseConstants.KEY_FILE_TYPE);

        ParseFile file = message.getParseFile(ParseConstants.KEY_FILE);
        Uri fileUri = Uri.parse((file.getUrl()));

        if(messageType.equals(ParseConstants.TYPE_IMAGE))
        {// view image
            Intent intent = new Intent(getActivity(), ViewImageActivity.class);
            intent.setData(fileUri);
            startActivity(intent);
        }
        else{// video video

            Intent intent = new Intent(Intent.ACTION_VIEW, fileUri);
            intent.setDataAndType(fileUri,"video/*");
            startActivity(intent);

        }

        //Delete the messages
        List<String> ids = message.getList(ParseConstants.KEY_RECEPIENT_IDS);

        if(ids.size() == 1) // last recipient - delete the whole thing
        {
           message.deleteInBackground();
        }

        else{ /// remove friends and saves
            ids.remove(ParseUser.getCurrentUser().getObjectId());
             ArrayList<String> idsToRemove = new ArrayList<String>();
            idsToRemove.add(ParseUser.getCurrentUser().getObjectId());

            message.removeAll(ParseConstants.KEY_RECEPIENT_IDS, idsToRemove);
            message.saveInBackground();

        }
    }

    protected SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            retrieveMessages();
        }
    };
}
