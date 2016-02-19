package com.example.denniskim.ribbit2.UI;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.example.denniskim.ribbit2.R;
import com.example.denniskim.ribbit2.RibbitApplication;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;


public class SignUpActivity extends ActionBarActivity {

    protected EditText mUsername;
    protected EditText mEmail;
    protected EditText mPassword;
    protected Button mSignUp;
    protected Button mCancelButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //this method is used to request a few extended window features that have to do with the window our app is running
        //this must be called before setContentView, also used to use the progress bar
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_sign_up);
        getActionBar().hide();

        mUsername = (EditText)findViewById(R.id.userLabel);
        mEmail = (EditText)findViewById(R.id.emailLabel);
        mPassword = (EditText)findViewById(R.id.passwordLabel);
        mSignUp = (Button)findViewById(R.id.signUpButton);
        mCancelButton = (Button)findViewById(R.id.cancelButton);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // getText returns editable and this is what gets you the string so you need to convert to string
                String username = mUsername.getText().toString();
                String password = mPassword.getText().toString();
                String email = mEmail.getText().toString();

                //trim is to delete all the white spaces that user might accidently input
                username = username.trim();
                password = password.trim();
                email = email.trim();

                //check if the fields are empty
                if(username.isEmpty() || email.isEmpty() || password.isEmpty())
                {
                    // constructing the dialog object
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                    // parameter is using the initalized string variable in strings.xml
                    builder.setMessage(R.string.signup_error_message);
                    builder.setTitle(R.string.signup_error_title);
                    // type of button to be invoked when the user sees the dialog and wants it to go away
                    // setting the listener(2nd parameter) to null means that you dont want the button to do anything
                    builder.setPositiveButton(android.R.string.ok,null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

                else{// create the new user.
                    setProgressBarIndeterminateVisibility(true);
                    ParseUser newUser = new ParseUser();
                    newUser.setUsername(username);
                    newUser.setPassword(password);
                    newUser.setEmail(email);
                    // this will retrieve the data from the url and once it responses the done method is executed
                    newUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            setProgressBarIndeterminateVisibility(false);
                            if(e==null) // if the sign up was successful, meaning that there is no exception e
                            {
                                RibbitApplication.updateParseInstallation(ParseUser.getCurrentUser());
                                //again, using the annonymous inner class, so need to explicitly say which class we're in,instead of 'this'
                                Intent intent = new Intent(SignUpActivity.this,LoginActivity.class);
                                // so that when they press back, it doesnt go back to the sign up page
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }

                            else{ // sign up no successful
                                AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                                // this will get the appropriate exception message
                                builder.setMessage(e.getMessage());
                                builder.setTitle(R.string.signup_error_title);
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
