package com.example.denniskim.ribbit2;

import android.app.Application;
import android.util.Log;

import com.example.denniskim.ribbit2.UI.MainActivity;
import com.example.denniskim.ribbit2.Util.ParseConstants;
import com.example.denniskim.ribbit2.Util.Receiver;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.PushService;
import com.parse.SaveCallback;

/**
 * Created by denniskim on 9/2/15.
 */

/* this class is created so that it can inherit from the Android Application class because to use
 Parse API we need an Application class. By extending the Application class and need to specify that
 were using this customized Application class in the manifest.
 When the android app starts , the entry point, or the very first class that is loaded is this
 Application class
 */
public class RibbitApplication extends Application {
    @Override
        public void onCreate() {
        // Enable Local Datastore. and setting application id and client key
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "IMmejZXem2dRFeyJuDNvwe1i5IesaITb6YJR4ij7", "lzhsvpfsKALjvZYTg5CZlW2j3MyKC4DV2wEKxxGY");


        ParseInstallation.getCurrentInstallation().saveInBackground();


        /*
        // this is to test if theres a receive and response with the API
        ParseObject testObject = new ParseObject("TestObject");
        testObject.put("foo", "bar");
        //saves the object in the background thread
        testObject.saveInBackground(); */
    }

    public static void updateParseInstallation(ParseUser user){
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put(ParseConstants.KEY_USER_ID,user.getObjectId());
        installation.saveInBackground();
    }


}
