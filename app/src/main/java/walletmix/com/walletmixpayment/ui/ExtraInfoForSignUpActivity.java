package walletmix.com.walletmixpayment.ui;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.EditText;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import butterknife.BindView;
import butterknife.OnClick;
import walletmix.com.walletmixpayment.R;
import walletmix.com.walletmixpayment.base.BaseActivity;
import walletmix.com.walletmixpayment.data.firebase.User;
import walletmix.com.walletmixpayment.data.pref.Key;

public class ExtraInfoForSignUpActivity extends BaseActivity {

    @BindView(R.id.et_phone_number)
    EditText etPhoneNumber;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    String fullName, email, userUiId;

    @Override
    protected int getContentView() {
        return R.layout.activity_extra_info_for_sign_up;
    }

    @Override
    protected void onViewReady(Intent getIntent, Bundle savedInstanceSated) {
        firebaseAuth = FirebaseAuth.getInstance();
        if(getIntent != null){
            fullName = getIntent.getStringExtra(Key.userFullName.name());
            email = getIntent.getStringExtra(Key.userEmail.name());
            userUiId = getIntent.getStringExtra(Key.current_user_uiId.name());
        }
    }

    @OnClick({R.id.sign_up_done_button})
    void onDoneButtonClicked(){
        final String phoneNumber = etPhoneNumber.getText().toString().trim();
        if(phoneNumber.isEmpty()){
            alertServices.showToast("Phone number can not be empty.");
        }else{
            User newUser = new User(fullName,email,phoneNumber);
            databaseReference = FirebaseDatabase.getInstance().getReference().child(getString(R.string.app_name)).child("users").child(userUiId);
            if(networkUtils.isNetworkAvailable()){
                showProgressDialog("Please wait...");
                databaseReference.setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        hideProgressDialog();
                        if(task.isSuccessful()){
                            mSessionManager.putString(Key.userFullName.name(), fullName);
                            mSessionManager.putString(Key.userEmail.name(), email);
                            mSessionManager.putString(Key.userPhone.name(), phoneNumber);
                            mNavigator.navigateToHomeByFinishingall(ExtraInfoForSignUpActivity.this);
                        }else{
                            alertServices.showToast("Please try again");
                        }
                    }
                });

            }else{
                showInternetAlertDialog();
            }
        }
    }
}
