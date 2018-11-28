package walletmix.com.walletmixpayment.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.facebook.login.Login;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.fabric.sdk.android.Fabric;
import walletmix.com.walletmixpayment.R;
import walletmix.com.walletmixpayment.data.firebase.User;
import walletmix.com.walletmixpayment.data.pref.Key;
import walletmix.com.walletmixpayment.data.pref.SessionManager;
import walletmix.com.walletmixpayment.utils.AlertServices;
import walletmix.com.walletmixpayment.utils.Navigator;
import walletmix.com.walletmixpayment.utils.NetworkUtils;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    Button loginWithGmailBtn, signUpBtn, loginButton;

    // CallbackManager callbackManager;
    FirebaseAuth mFireBaseAuth;
    GoogleSignInClient googleSignInClient;
    NetworkUtils networkUtils;
    EditText etEmail, etPassword;
    Navigator navigator;
    SessionManager sessionManager;
    FirebaseUser firebaseUser;
    AlertServices alertServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        mFireBaseAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);
        if (sessionManager.getBoolean(Key.allowed_to_go_home.name(), false)) {
            Intent mainIntent = new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(mainIntent);
            finish();
        }
        setContentView(R.layout.activity_login);
        init();
    }

    private void init() {
        networkUtils = new NetworkUtils(this);
        alertServices = new AlertServices(this);
        navigator = new Navigator();


        loginWithGmailBtn = findViewById(R.id.login_button_with_gmail);
        signUpBtn = findViewById(R.id.sign_up_button);
        loginButton = findViewById(R.id.login_button);
        etEmail = findViewById(R.id.etEmailAddress);
        etPassword = findViewById(R.id.etPassword);


        loginWithGmailBtn.setOnClickListener(this);
        signUpBtn.setOnClickListener(this);
        loginButton.setOnClickListener(this);


//        callbackManager = CallbackManager.Factory.create();
//        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
//            @Override
//            public void onSuccess(LoginResult loginResult) {
//                handleFacebookAccessToken(loginResult.getAccessToken());
//                getUserDetails(loginResult);
//            }
//
//            @Override
//            public void onCancel() {
//
//            }
//
//            @Override
//            public void onError(FacebookException error) {
//
//            }
//        });

        // Google Sign in account
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, signInOptions);
        googleSignInClient.revokeAccess();


    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
//            case R.id.login_button_with_facebook:
//                AccessToken currentToken = AccessToken.getCurrentAccessToken();
//                boolean isLoggedIn = currentToken!= null && !currentToken.isExpired();
//                if(isLoggedIn)
//                    LoginManager.getInstance().logOut();
//                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
//                break;
            case R.id.login_button_with_gmail:
                if(networkUtils.isNetworkAvailable()){
                    if (mFireBaseAuth != null) {
                        Intent signInIntent = googleSignInClient.getSignInIntent();
                        startActivityForResult(signInIntent, 13);
                    }
                }else{
                    alertServices.showToast("No Internet Connection.");
                }
                break;
            case R.id.sign_up_button:
                Intent signUpIntent = new Intent(this, SignUpActivity.class);
                signUpIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(signUpIntent);
                break;
            case R.id.login_button:
                if (networkUtils.isNetworkAvailable()) {
                    final String email = etEmail.getText().toString();
                    final String password = etPassword.getText().toString();
                    if (email.isEmpty()) {
                        alertServices.showToast("Email can not be empty.");
                    } else if (password.isEmpty()) {
                        alertServices.showToast("Password can not be empty.");
                    } else {
                        final ProgressDialog progressDialog = new ProgressDialog(this);
                        progressDialog.setCancelable(false);
                        progressDialog.setMessage("Signing in...");
                        progressDialog.show();
                        mFireBaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    firebaseUser = task.getResult().getUser();
                                    if (!firebaseUser.isEmailVerified()) {
                                        progressDialog.dismiss();
                                        firebaseUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                alertServices.showAlertForConfirmation(LoginActivity.this, "Email Verification Required",
                                                        "A verification mail has been sent to " + email + ". Please verify your email address and sign in.",
                                                        null, "Okay", new AlertServices.AlertListener() {
                                                            @Override
                                                            public void onNegativeBtnClicked() {

                                                            }

                                                            @Override
                                                            public void onPositiveBtnClicked() {

                                                            }
                                                        });

                                            }
                                        });
                                    } else {
                                        DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.app_name)).child("users");
                                        mDatabaseRef.child(task.getResult().getUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                progressDialog.dismiss();
                                                if (dataSnapshot != null) {
                                                    User user = dataSnapshot.getValue(User.class);
                                                    assert user != null;
                                                    sessionManager.putString(Key.userFullName.name(), user.getUserFullName());
                                                    sessionManager.putString(Key.userEmail.name(), user.getEmail());
                                                    sessionManager.putString(Key.userPhone.name(), user.getPhoneNumber());
                                                    sessionManager.putBoolean(Key.allowed_to_go_home.name(), true);
                                                    navigator.navigateToHome(LoginActivity.this);
                                                    finish();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                progressDialog.dismiss();
                                            }
                                        });
                                    }
                                } else {
                                    progressDialog.dismiss();
                                    String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                    switch (errorCode) {
                                        case "ERROR_INVALID_EMAIL":
                                            Toast.makeText(LoginActivity.this, "The email address is not valid.", Toast.LENGTH_LONG).show();
                                            break;

                                        case "ERROR_WRONG_PASSWORD":
                                            Toast.makeText(LoginActivity.this, "The password is invalid.", Toast.LENGTH_LONG).show();
                                            break;

                                        case "ERROR_USER_DISABLED":
                                            Toast.makeText(LoginActivity.this, "The user account has been disabled by an administrator.", Toast.LENGTH_LONG).show();
                                            break;

                                        case "ERROR_USER_TOKEN_EXPIRED":
                                            Toast.makeText(LoginActivity.this, "The user\\'s credential is no longer valid. The user must sign in again.", Toast.LENGTH_LONG).show();
                                            break;

                                        case "ERROR_USER_NOT_FOUND":
                                            Toast.makeText(LoginActivity.this, "User not found", Toast.LENGTH_LONG).show();
                                            break;

                                        case "ERROR_INVALID_USER_TOKEN":
                                            Toast.makeText(LoginActivity.this, "The user\\'s credential is no longer valid. The user must sign in again.", Toast.LENGTH_LONG).show();
                                            break;
                                    }
                                }
                            }
                        });
                    }
                } else {
                    alertServices.showToast("No Internet Connection.");
                }
                break;
        }
    }

    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


//    protected void getUserDetails(LoginResult loginResult) {
//        GraphRequest data_request = GraphRequest.newMeRequest(
//                loginResult.getAccessToken(),
//                new GraphRequest.GraphJSONObjectCallback() {
//                    @Override
//                    public void onCompleted(
//                            JSONObject json_object,
//                            GraphResponse response) {
//                        try {
//                            String user_name = json_object.get("name").toString();
//                            JSONObject profile_pic_data = new JSONObject(json_object.get("picture").toString());
//                            showToast(user_name);
////                            startActivity(new Intent(LoginActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
////                            finish();
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//
//                    }
//
//                });
//        Bundle permission_param = new Bundle();
//        permission_param.putString("fields", "id,name, picture.width(120).height(120)");
//                data_request.setParameters(permission_param);
//        data_request.executeAsync();
//
//    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // callbackManager.onActivityResult(requestCode, resultCode, data);// Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 13) {

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    signInWithGoogle(account);
                }
            } catch (ApiException ignored) {
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void signInWithGoogle(final GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Signing in...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        mFireBaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();
                            final String userUiId = user.getUid();
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(getString(R.string.app_name)).child("users").child(userUiId);
                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    progressDialog.dismiss();
                                    if (dataSnapshot.exists()) {
                                        User user = dataSnapshot.getValue(User.class);
                                        assert user != null;
                                        sessionManager.putString(Key.userFullName.name(), user.getUserFullName());
                                        sessionManager.putString(Key.userEmail.name(), user.getEmail());
                                        sessionManager.putString(Key.userPhone.name(), user.getPhoneNumber());
                                        sessionManager.putBoolean(Key.allowed_to_go_home.name(), true);
                                        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(mainIntent);
                                        finish();
                                    } else {
                                        Intent extraInfoIntent = new Intent(LoginActivity.this, ExtraInfoForSignUpActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        extraInfoIntent.putExtra(Key.userFullName.name(), acct.getDisplayName());
                                        extraInfoIntent.putExtra(Key.userEmail.name(), acct.getEmail());
                                        extraInfoIntent.putExtra(Key.current_user_uiId.name(), userUiId);
                                        startActivity(extraInfoIntent);
                                        finish();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    progressDialog.dismiss();
                                }
                            });

                        } else {
                            progressDialog.dismiss();
                            showToast("Failed");
                        }

                    }
                });
    }

//    private void handleFacebookAccessToken(AccessToken token) {
//        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
//        mFireBaseAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                            startActivity(mainIntent);
//                            finish();
//                        }
//                    }
//                });
//    }

//    private void updateUI(FirebaseUser user){
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//        if(account != null){
//            String name = account.getDisplayName();
//            String email = account.getEmail();
//            showToast("Name :"+ name + "\nEmail: "+ email);
//        }
//    }
}
