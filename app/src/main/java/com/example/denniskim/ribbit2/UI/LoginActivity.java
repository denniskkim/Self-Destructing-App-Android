package com.example.denniskim.ribbit2.UI;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.denniskim.ribbit2.R;
import com.example.denniskim.ribbit2.RibbitApplication;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;


public class LoginActivity extends ActionBarActivity {
    protected EditText mUsername;
    protected EditText mPassword;
    protected Button mLoginButton;
    //protected means that it can be seen by other subclasses of this class or other classes within the same pacakge
    protected TextView mSignUpTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //this method is used to request a few extended window features that have to do with the window our app is running
        //this must be called before setContentView
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_login);

        getActionBar().hide();


        mSignUpTextView = (TextView)findViewById(R.id.signUpLabel);
        //method for when you click on the textView.
        mSignUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // why you dont use 'this' for the first parameter,which represents Context,
                // is because of the annonymous inner method. so the context would be annoynomous inner type
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        mUsername = (EditText)findViewById(R.id.userLabel);
        mPassword = (EditText)findViewById(R.id.passwordLabel);
        mLoginButton = (Button)findViewById(R.id.loginButton);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // getText returns editable and this is what gets you the string so you need to convert to string
                String username = mUsername.getText().toString();
                String password = mPassword.getText().toString();

                //trim is to delete all the white spaces that user might accidently input
                username = username.trim();
                password = password.trim();


                //check if the fields are empty
                if(username.isEmpty() || password.isEmpty())
                {
                    // constructing the dialog object
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    // parameter is using the initalized string variable in strings.xml
                    builder.setMessage(R.string.login_error_message);
                    builder.setTitle(R.string.login_error_title);
                    // type of button to be invoked when the user sees the dialog and wants it to go away
                    // setting the listener(2nd parameter) to null means that you dont want the button to do anything
                    builder.setPositiveButton(android.R.string.ok,null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

                else{// verify to login the right account.
                    // user tries to login and we show progress bar
                    setProgressBarIndeterminateVisibility(true);
                    ParseUser.logInInBackground(username, password, new LogInCallback() {
                        @Override
                        //these parameters is what the parse.com sends back to the program.
                        // so if it was successful, user varaible would be initialized and variable will be null
                        public void done(ParseUser parseUser, ParseException e) {
                            // after the callback is called, get rid of indicator
                            setProgressBarIndeterminateVisibility(false);
                            if(e == null) //successful login
                            {
                                RibbitApplication.updateParseInstallation(parseUser);
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                            else // failed to login
                            {
                                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                // this will get the appropriate exception message
                                builder.setMessage(e.getMessage());
                                builder.setTitle(R.string.login_error_title);
                                // type of button to be invoked when the user sees the dialog and wants it to go away
                                // setting the listener(2nd parameter) to null means that you dont want the button to do anything
                                builder.setPositiveButton(android.R.string.ok,null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }
                    });


                }
            }
        });
    }


}
