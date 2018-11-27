package walletmix.com.walletmixpayment.base;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import walletmix.com.walletmixpayment.R;
import walletmix.com.walletmixpayment.data.pref.SessionManager;
import walletmix.com.walletmixpayment.utils.AlertServices;
import walletmix.com.walletmixpayment.utils.LogUtils;
import walletmix.com.walletmixpayment.utils.Navigator;
import walletmix.com.walletmixpayment.utils.NetworkUtils;


public abstract class BaseActivity extends AppCompatActivity{

    @Nullable
    protected Unbinder viewUnbinder = null;
    protected ActionBar mActionBar;

    protected ProgressDialog mProgressDialog = null;
    protected Navigator mNavigator;

    protected SessionManager mSessionManager;


    protected LogUtils logUtils;

    protected AlertServices alertServices;

    protected NetworkUtils networkUtils;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
        viewUnbinder = ButterKnife.bind(this);
        mActionBar = getSupportActionBar();
        mSessionManager = new SessionManager(this);
        mNavigator = new Navigator();
        logUtils = new LogUtils();
        alertServices = new AlertServices(this);
        networkUtils = new NetworkUtils(this);
        if(mActionBar != null){
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setDisplayShowHomeEnabled(true);
        }
        onViewReady(getIntent(), savedInstanceState);
    }

    protected abstract int getContentView();
    protected abstract void onViewReady(Intent getIntent, Bundle savedInstanceSated);

    protected void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }
    protected void showToast(String message) {
        alertServices.showToast(this, message);
    }


    protected void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

  public void showInternetAlertDialog(){
        showToast("No internet connection.");
  }
    @Override
    public void onDestroy() {
        if (viewUnbinder != null) {
            viewUnbinder.unbind();
        }
        super.onDestroy();
    }
}
