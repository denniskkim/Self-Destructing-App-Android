package com.example.denniskim.ribbit2.UI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.example.denniskim.ribbit2.Util.ParseConstants;
import com.example.denniskim.ribbit2.R;
import com.example.denniskim.ribbit2.Adapter.SectionsPagerAdapter;
import com.parse.ParseUser;


public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    //to see what class the error is coming from
    public static final String TAG = MainActivity.class.getSimpleName();

    // to identify the call, like a tag
    public static final int TAKE_PHOTO_REQUEST = 0;
    public static final int TAKE_VIDEO_REQUEST = 1;
    public static final int PICK_PHOTO_REQUEST = 2;
    public static final int PICK_VIDEO_REQUEST = 3;

    public static final int MEDIA_TYPE_IMAGE=4;
    public static final int MEDIA_TYPE_VIDEO=5;

    // 1kb = 1024 byte , 1024kb = 1mb and we want 10mb
    public static final int FILE_SIZE_LIMIT = 1024*1024*10;

    protected Uri mMediaUri;


    // varaible for listener to react to the list of items that was clicked for the camera option
    protected DialogInterface.OnClickListener mDialogListener =
            new DialogInterface.OnClickListener() {
                @Override
                // second parameter is the index of the list of which item was clicked on
                public void onClick(DialogInterface dialogInterface, int i) {
                    switch(i){
                        case 0: // take picture
                            Intent takePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            // retrieves the path name
                             mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                            // if there is a problem with the external device
                            if(mMediaUri == null)
                            {
                                Toast.makeText(MainActivity.this, R.string.error_Uri,Toast.LENGTH_LONG);
                            }
                            takePhoto.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                            // the activity should exit and give us a result back, and the main activity should wait for one
                            // the second parameter is the tag so we know which call it was coming from
                            startActivityForResult(takePhoto, TAKE_PHOTO_REQUEST);
                            break;
                        case 1: // take video
                            Intent takeVideo = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                            mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
                            if(mMediaUri == null)
                            {
                                Toast.makeText(MainActivity.this,R.string.error_Uri,Toast.LENGTH_LONG);
                            }

                            takeVideo.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                            // limit video to 10 sec
                            takeVideo.putExtra(MediaStore.EXTRA_DURATION_LIMIT,10);
                            // diminish the quality because parse has a 10mb limit
                            takeVideo.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,0); //0 for lowest
                            startActivityForResult(takeVideo,TAKE_VIDEO_REQUEST);
                            break;
                        case 2: //choose picture
                            Intent choosePhoto = new Intent(Intent.ACTION_GET_CONTENT);
                            // to limit the type of data that youre retrieving in the intent
                            choosePhoto.setType("image/*");
                            startActivityForResult(choosePhoto, PICK_PHOTO_REQUEST);
                            break;
                        case 3: // choose video
                            Intent chooseVideo = new Intent(Intent.ACTION_GET_CONTENT);
                            // to limit the type of data that youre retrieving in the intent
                            chooseVideo.setType("video/*");
                            Toast.makeText(MainActivity.this,"The selected video must be less than 10MB",
                                            Toast.LENGTH_LONG).show();
                            startActivityForResult(chooseVideo, PICK_VIDEO_REQUEST);
                            break;

                    }

                }
            };

    // Create a file for saving an image or video
    private Uri getOutputMediaFileUri(int mediaTypeImage) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        if(isExternalStorageAvailable()) //get Uri and return it
        {
            String appName = MainActivity.this.getString(R.string.app_name);
            // 1. get external Storage directory. this will create a subdirectory for the app
            File mediaStorageDir = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    appName);
            //2.create a subdirectory
            if(!mediaStorageDir.exists())
            {
                //mkdir return boolean value.
                if(!mediaStorageDir.mkdirs()) // if it fails to create a directory, return null
                {
                    Log.e(TAG,"Failed to create directory");
                    return null;
                }
            }
            /*
             3.create a file name
             4.create the file
            */
            File mediaFile;
            Date now = new Date();
            // create a time stamp for the label of the pcitures, because it will create unique stamps
            // and so it doesnt override the other files
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.US).format(now);

            // to get full path. (file.separator) gives us the separator in the path name so we dont
            // have to explicitly do it ourselves
            String path = mediaStorageDir.getPath() + File.separator;
            // if its a photo
            if(mediaTypeImage == MEDIA_TYPE_IMAGE)
            {
                mediaFile = new File(path+ "IMG_" + timeStamp + ".jpg");
            }

            else if(mediaTypeImage == MEDIA_TYPE_VIDEO) // if its a video
            {
                mediaFile = new File(path + "VID_" + timeStamp + ".mp4");
            }

            else {
                return null;
            }

            Log.d(TAG, "File" + Uri.fromFile(mediaFile));
            // 5.return the file's URI
            return  Uri.fromFile(mediaFile);
        }

        else {
            return null;
        }
    }

    private boolean isExternalStorageAvailable()
    {
        // tells us what stat the external storage is in
        String state = Environment.getExternalStorageState();
        if(state.equals(Environment.MEDIA_MOUNTED)){ //external storage is avaiable
            return true;
        }

        return false;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // have to be in between these two methods
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            navigateToLogin();
        } else { // if theyre logged in
            Log.i(TAG, currentUser.getUsername());
        }

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                //when swiping it changes that viewpager
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        // setting the tabs title and getting the amount of tabs there are
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(actionBar.newTab()
                    .setIcon(mSectionsPagerAdapter.getIcon(i))
                    .setTabListener(this));
        }
    }

    /*
     to get the result of the picture/ videos we took
     when choosing a picture from the gallery, the uri data is returned by this method
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) // successful, add photo/video to gallery
        {
            // to obtain the data from when youre choosing a photo/video from the gallery
            if (requestCode == PICK_PHOTO_REQUEST || requestCode == PICK_VIDEO_REQUEST) {
                if (data == null) { // intent has no data
                    Toast.makeText(this, "Sorry there was an Error!", Toast.LENGTH_LONG).show();
                } else { // our intent has data
                    mMediaUri = data.getData();
                }

                if(requestCode == PICK_VIDEO_REQUEST)
                {
                    //make sure the file is less than 10MB
                    int fileSize=0;
                    InputStream inputStream = null;
                    // picking a file from a gallery gives is a content Uri(content provider)
                    try{
                        // the getContent method resolves the content to the actual file on the device
                        inputStream = getContentResolver().openInputStream(mMediaUri);
                        // retrieves the size of the file and is in one byte
                        fileSize = inputStream.available();
                    }
                    catch(FileNotFoundException e)
                    {
                        Toast.makeText(this, "There was a problem with the extracted file.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    catch(IOException e)
                    {
                        Toast.makeText(this, "There was a problem with the extracted file.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    finally{ // if everythings okay, this always gets executed even if it catches an exception
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                        }
                    }

                    if(fileSize >= FILE_SIZE_LIMIT)
                    {
                        Toast.makeText(this,"The selected file is too large! Pick another file.",
                                Toast.LENGTH_LONG).show();
                        return; // to exit out
                    }
                }
            }

            // adding photos/videos to the gallery
            else {
                // we want to notify the gallery that new files are available
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                //specifying the path of the file
                mediaScanIntent.setData(mMediaUri);
                //notifying the gallery
                sendBroadcast(mediaScanIntent);
            }

            // after selecting a photo/video from the gallery, this will take you to the recipients
            // list defined in the RecipientsACtivity class. where you can send the photo/video.
            Intent recipientsIntent = new Intent(this, RecipientsActivity.class);
            // gets the data of the selected photo/video
            recipientsIntent.setData(mMediaUri);

            String fileType;
            if(requestCode == PICK_PHOTO_REQUEST || requestCode == TAKE_PHOTO_REQUEST)
            { // then we can set it to be an image
                fileType = ParseConstants.TYPE_IMAGE;
            }
            else{ // a video
                fileType = ParseConstants.TYPE_VIDEO;
            }

            // add extra data to the intent so other activities can share this data
            // first parameter is the key
            recipientsIntent.putExtra(ParseConstants.KEY_FILE_TYPE,fileType);
            startActivity(recipientsIntent);
        }
        else if( resultCode != RESULT_CANCELED){ // failed meaning it wasnt canceled
            Toast.makeText(this, "Sorry, there was an Error!", Toast.LENGTH_LONG).show();
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        // these flags are added to make the login activity be the front of our app. so when you
        // press the back button, it goes to the home page of the phone
        // saying that login activity should be new task and the old task should be cleared
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // this creates the extenion of the options. the 3 dot thing to access edit friend, logout
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /*
    the extention menu (the three dots)
    the menu item is selected and passed on to the parameter of this method and take appropriate action
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id)
        {
            //checking if the user clicked on the logout option
            case R.id.action_logut:
                ParseUser.logOut();
                navigateToLogin();
                break;


            case R.id.action_edit_friends:
               Intent intent = new Intent(this, EditFriendsActivity.class);
               startActivity(intent);
               break;

            case R.id.action_camera:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setItems(R.array.camera_choices, mDialogListener);
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
        these three methods are needed when implementing the actionbar.tab class
     */
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

    }
}