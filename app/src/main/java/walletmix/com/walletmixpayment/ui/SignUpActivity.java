package walletmix.com.walletmixpayment.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.OnClick;
import walletmix.com.walletmixpayment.R;
import walletmix.com.walletmixpayment.base.BaseActivity;
import walletmix.com.walletmixpayment.data.firebase.User;
import walletmix.com.walletmixpayment.data.pref.Key;
import walletmix.com.walletmixpayment.data.pref.SessionManager;
import walletmix.com.walletmixpayment.utils.AlertServices;
import walletmix.com.walletmixpayment.utils.ValidationUtils;

public class SignUpActivity extends BaseActivity {

    @BindView(R.id.et_full_name)
    EditText etFullName;

    @BindView(R.id.et_email)
    EditText etEmail;

    @BindView(R.id.et_password)
    EditText etPassword;

    @BindView(R.id.et_phone_number)
    EditText etPhoneNumber;

    private DatabaseReference mDatabase;
    private FirebaseAuth mFireBaseAuth;
    private SessionManager sessionManager;

    GoogleSignInClient googleSignInClient;

    @Override
    protected int getContentView() {
        return R.layout.activity_sign_up;
    }

    @Override
    protected void onViewReady(Intent getIntent, Bundle savedInstanceSated) {
        if (mActionBar != null) {
            this.setTitle("SIGN UP");
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setDisplayShowHomeEnabled(true);
        }
        init();

    }

    private void init() {
        sessionManager = new SessionManager(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFireBaseAuth = FirebaseAuth.getInstance();


        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, signInOptions);
        googleSignInClient.revokeAccess();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick({R.id.signUpBtn, R.id.sign_up_with_google_button})
    public void onSignUpButtonClicked(View view) {
        switch (view.getId()) {
            case R.id.signUpBtn:
                String userFullName = etFullName.getText().toString();
                String email = etEmail.getText().toString();
                String phoneNumber = etPhoneNumber.getText().toString();
                final String password = etPassword.getText().toString();
                final User newUser = new User(userFullName, email, phoneNumber);
                if (inputIsValid(newUser)) {
                    logUtils.logV(userFullName + "'\n" + email + "\n" + phoneNumber + "\n" + password);
                    showProgressDialog("Signing up...");
                    mFireBaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String userUiId = task.getResult().getUser().getUid();
                                writeNewUser(userUiId, newUser, task.getResult().getUser());
                            } else {
                                hideProgressDialog();
                                String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                switch (errorCode) {
                                    case "ERROR_INVALID_EMAIL":
                                        alertServices.showToast("The email address is not valid.");
                                        break;

                                    case "ERROR_EMAIL_ALREADY_IN_USE":
                                        alertServices.showToast("The email address is already used");
                                        ;
                                        break;

                                    case "ERROR_CREDENTIAL_ALREADY_IN_USE":
                                        alertServices.showToast("This credential is already associated with a different user account.");
                                        break;

                                    case "ERROR_WEAK_PASSWORD":
                                        alertServices.showToast("The given password is very weak");
                                        break;

                                }
                            }
                        }
                    });
                }
                break;

            case R.id.sign_up_with_google_button:
                if(networkUtils.isNetworkAvailable()){
                    if (mFireBaseAuth != null) {
                        Intent signInIntent = googleSignInClient.getSignInIntent();
                        startActivityForResult(signInIntent, 13);
                    }
                }else{
                    showInternetAlertDialog();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 13) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    if (networkUtils.isNetworkAvailable()) {
                        fireBaseAuthWithGoogle(account);
                    } else {
                        showInternetAlertDialog();
                    }
                }
            } catch (ApiException ignored) {
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void fireBaseAuthWithGoogle(final GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        showProgressDialog("Signing up...");
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
                                    hideProgressDialog();
                                    if(dataSnapshot.exists()){
                                        User user = dataSnapshot.getValue(User.class);
                                        assert  user != null;
                                        sessionManager.putString(Key.userFullName.name(), user.getUserFullName());
                                        sessionManager.putString(Key.userEmail.name(), user.getEmail());
                                        sessionManager.putString(Key.userPhone.name(), user.getPhoneNumber());
                                        sessionManager.putBoolean(Key.allowed_to_go_home.name(),true);
                                        mNavigator.navigateToHomeByFinishingall(SignUpActivity.this);
                                    }else{
                                        Intent extraInfoIntent = new Intent(SignUpActivity.this, ExtraInfoForSignUpActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        extraInfoIntent.putExtra(Key.userFullName.name(), acct.getDisplayName());
                                        extraInfoIntent.putExtra(Key.userEmail.name(),acct.getEmail());
                                        extraInfoIntent.putExtra(Key.current_user_uiId.name(), userUiId);
                                        startActivity(extraInfoIntent);
                                        finish();
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    hideProgressDialog();
                                }
                            });
                        } else {
                            alertServices.showToast("Failed to Sign up.");
                        }
                    }
                });
    }

    private boolean inputIsValid(User newUser) {
        boolean result = false;
        if (newUser.getUserFullName().isEmpty()) {
            alertServices.showToast("Name can not be empty.");
        } else if (newUser.getEmail().isEmpty()) {
            alertServices.showToast("Email can not be empty.");
        } else if (newUser.getPhoneNumber().isEmpty()) {
            alertServices.showToast("Phone number can not be empty.");
        } else if (etPassword.getText().toString().isEmpty()) {
            alertServices.showToast("Password can not be empty.");
        } else if (etPassword.getText().toString().trim().length() < 6) {
            alertServices.showToast("Password must be at least 6 digits.");
        } else {
            result = true;
        }
        return result;
    }

    private void writeNewUser(final String userId, final User user, final FirebaseUser firebaseUser) {
        if (mDatabase != null)
            mDatabase.child(getString(R.string.app_name)).child("users").child(userId).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                hideProgressDialog();
                                if(task.isSuccessful()){
                                    etFullName.getText().clear();
                                    etEmail.getText().clear();
                                    etPhoneNumber.getText().clear();
                                    etPassword.getText().clear();
                                    alertServices.showAlertForConfirmation(SignUpActivity.this, "Email Verification Required",
                                            "A verification mail has been sent to " + user.getEmail() + ". Please verify your email address and sign in.",
                                            null, "Okay", new AlertServices.AlertListener() {
                                                @Override
                                                public void onNegativeBtnClicked() {

                                                }

                                                @Override
                                                public void onPositiveBtnClicked() {
                                                    onBackPressed();
                                                }
                                            });
                                }
                            }
                        });
                    }
                }
            });
    }

}
