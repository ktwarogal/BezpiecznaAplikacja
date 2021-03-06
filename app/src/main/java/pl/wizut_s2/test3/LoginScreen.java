package pl.wizut_s2.test3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * A login screen that offers login via email/password.
 */
public class LoginScreen extends Activity implements LoaderCallbacks<Cursor>, PBAIClientInterface {

    static boolean IsInDebugLoginMode = false;
    final  Double CurrentVersion = 1.0;

    private static HashMap<String, Integer> mUnsuccessfullLogins;

    public void onListOfScannersUpdate(String s) {


        if(s.contains("Unable to resolve host")){
            Toast.makeText(getBaseContext(), "Internet is probably turned off.", Toast.LENGTH_LONG).show();
            return;
        }

        if(s.contains("https://"))
        {
            Toast.makeText(getBaseContext(), "IP is banned! Try again later.", Toast.LENGTH_LONG).show();
            return;
        }
        if(s.contains("Version:")) {
            String [] _VersionArray = s.split("\\:",-1);
            String _Version = _VersionArray[1];
            try{
                Double _VersionNumber = Double.parseDouble(_Version);
                if(_VersionNumber > CurrentVersion){
                    Toast.makeText(getBaseContext(), "There is a new version, download it from our site!.", Toast.LENGTH_LONG).show();
                }
            }
            catch(Exception _Ex) {
               //gonna catch'em a;;
            }
        }
        else{

            if (s.equals("")) {
                Boolean _FirstfailOnThisLogin = true;
                String _Login = mLoginView.getText().toString();
                Iterator it = mUnsuccessfullLogins.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, Integer> pair = (Map.Entry<String, Integer>) it.next();
                    if (pair.getKey().equals(_Login)) {
                        pair.setValue(pair.getValue() + 1);
                        _FirstfailOnThisLogin = false;
                        break;
                    }
                    //it.remove(); // avoids a ConcurrentModificationException
                }

                if (_FirstfailOnThisLogin) {
                    mUnsuccessfullLogins.put(_Login, 1);
                }

                mPasswordView.setError("Wrong credentials lol!!");
                LoginWebService = null;

            } else {
                LoginWebService = null;
                attemptLogin();
            }
        }
    }

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };

    private GetLoginService LoginWebService;
    private GetVersionService VersionWebService;
    public LoginScreen(){
        mUnsuccessfullLogins = new HashMap<String, Integer>();

        VersionWebService = new GetVersionService(this);
    }

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mLoginView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        setupActionBar();

        // Set up the login form.
        mLoginView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    //attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(IsInDebugLoginMode){
                    attemptLogin();
                    return;
                }


                Boolean _IsLoginLegalOnThisUsername = true;
                String _Login = mLoginView.getText().toString();
                Iterator it = mUnsuccessfullLogins.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, Integer> pair = (Map.Entry<String, Integer>)it.next();
                    if(pair.getKey().equals(_Login)){
                        if(pair.getValue()>= 5){
                            _IsLoginLegalOnThisUsername = false;
                            break;
                        }
                    }
                    //System.out.println(pair.getKey() + " = " + pair.getValue());

                    //it.remove(); // avoids a ConcurrentModificationException
                }

                if(_IsLoginLegalOnThisUsername) {
                    GetLoginSuccessFromServer();
                }
                else{
                    Toast.makeText(getBaseContext(), "Maximum number of incorrect tries exceeded for this login", Toast.LENGTH_LONG).show();
                }
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void GetLoginSuccessFromServer() {
        String _Login = mLoginView.getText().toString();
        String _Password = mPasswordView.getText().toString();
        //_Login = "snups@wp.pl";
        //_Password = "123456";

        if(LoginWebService == null) {
            LoginWebService = new GetLoginService(this);
        }
        LoginWebService.GetLoginResponse(_Login, _Password);

    }

    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {

    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mLoginView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String _Login = mLoginView.getText().toString();
        String _Password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // 1. Walidacja, czy dane są wpisane
        if(TextUtils.isEmpty(_Login) ) {
            mPasswordView.setError(getString(R.string.error_empty_login));
            focusView = mLoginView;
            cancel = true;
        }

        if(TextUtils.isEmpty(_Password) ) {
            mPasswordView.setError(getString(R.string.error_empty_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if(_Password.length() < 8 || _Password.length() > 50){
            mPasswordView.setError(getString(R.string.error_wrong_password_length));
            focusView = mPasswordView;
            cancel = true;
        }

        // Jeżeli jest ok do tej pory, to laczymy sie z webservice i sprawdzamy, czy jest ok
        // wysylamy SHA1 z login+sól+hasło
        if(!cancel) {
            String _Salt = "AplikacjaWizutowa!";
            String _Shortcut = null;
            try {
                _Shortcut = this.SHA1(_Login + _Salt + _Password);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }


            Context _Context = getApplicationContext();
            CharSequence _Text = _Shortcut;
            int _Duration = Toast.LENGTH_SHORT;

            //Toast toast = Toast.makeText(_Context, "Skrót: " + _Text, _Duration);
            //toast.show();

            if( CheckIfLoginCorrect()){
                Toast.makeText(getBaseContext(), "LOGIN CORRECT!", Toast.LENGTH_LONG).show();

            }
            else{
                mPasswordView.setError(getString(R.string.error_incorrect_verification));
                cancel = true;
            }


        }
/*
        // Check for a valid _Password, if the user entered one.
        if (!TextUtils.isEmpty(_Password) && !isPasswordValid(_Password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid _Login address.
        if (TextUtils.isEmpty(_Login)) {
            mLoginView.setError(getString(R.string.error_field_required));
            focusView = mLoginView;
            cancel = true;
        }
*/

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(_Login, _Password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean CheckIfLoginCorrect() {
        SystemClock.sleep(500);

        return true;
    }

    private String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    public  String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
    }


    private boolean isEmailValid(String email) {

        return true;

    }

    private boolean isPasswordValid(String password) {


        return true;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(LoginScreen.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mLoginView.setAdapter(adapter);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                Intent myIntent = new Intent(LoginScreen.this, MainActivity.class);
                myIntent.putExtra("key", "asdf"); //Optional parameters
                LoginScreen.this.startActivity(myIntent);
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_verification));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }



    }
}



