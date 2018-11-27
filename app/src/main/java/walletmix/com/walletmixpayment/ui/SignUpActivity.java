package walletmix.com.walletmixpayment.ui;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import butterknife.BindView;
import butterknife.OnClick;
import walletmix.com.walletmixpayment.R;
import walletmix.com.walletmixpayment.base.BaseActivity;
import walletmix.com.walletmixpayment.data.firebase.User;
import walletmix.com.walletmixpayment.data.pref.Key;
import walletmix.com.walletmixpayment.data.pref.SessionManager;
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
                String password = etPassword.getText().toString();
                final User newUser = new User(userFullName, email, phoneNumber);
                if(inputIsValid(newUser)){
                    logUtils.logV(userFullName+"'\n"+ email + "\n"+ phoneNumber+ "\n"+ password);
                    showProgressDialog("Signing up...");
                    mFireBaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                String userUiId = task.getResult().getUser().getUid();
                                writeNewUser(userUiId, newUser);
                            }else{
                                alertServices.showToast("Email already taken");
                                hideProgressDialog();
                            }
                        }
                    });
                }
                break;

            case R.id.sign_up_with_google_button:
                break;
        }
    }

    private boolean inputIsValid(User newUser) {
        boolean result = false;
        if(newUser.getUserFullName().isEmpty()){
            alertServices.showToast("Name can not be empty");
        }else if (newUser.getEmail().isEmpty()){
            alertServices.showToast("Email can not be empty.");
        }else if (ValidationUtils.isEmailValid(newUser.getEmail())){
            alertServices.showToast("Email must be valid");
        }else if (newUser.getPhoneNumber().isEmpty()){
            alertServices.showToast("Phone number can not be empty");
        }else if (etPassword.getText().toString().isEmpty()){
            alertServices.showToast("Password can not be empty.");
        }else {
            result = true;
        }
        return result;
    }

    private void writeNewUser(final String userId, final User user) {
        if (mDatabase != null)
            mDatabase.child(getString(R.string.app_name)).child("users").child(userId).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        sessionManager.putString(Key.userFullName.name(), user.getUserFullName());
                        sessionManager.putString(Key.userEmail.name(), user.getEmail());
                        sessionManager.putString(Key.userPhone.name(), user.getPhoneNumber());
                        hideProgressDialog();
                        mNavigator.navigateToHomeByFinishingall(SignUpActivity.this);
                        finish();
                    }
                }
            });
    }

}
