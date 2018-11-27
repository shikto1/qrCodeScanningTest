package walletmix.com.walletmixpayment.ui;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.Result;
import org.json.JSONException;
import org.json.JSONObject;
import butterknife.BindView;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import walletmix.com.walletmixpayment.R;
import walletmix.com.walletmixpayment.base.BaseActivity;
import walletmix.com.walletmixpayment.data.network.apiResponses.GetMerchantCredentialApiService;
import walletmix.com.walletmixpayment.data.network.apiServices.GetMerchantCredentialResponse;
import walletmix.com.walletmixpayment.data.pref.Key;
import walletmix.com.walletmixpayment.data.network.utils.NetworkUtils;
import walletmix.com.walletmixpayment.data.network.utils.ServiceGenerator;


public class QRCodeScannerActivity extends BaseActivity implements ZXingScannerView.ResultHandler {


    Call<GetMerchantCredentialResponse> getMerchantCredentialResponseCall;

    @BindView(R.id.zxing_qr_code_scanner_view)
    ZXingScannerView qrCodeScannerView;

    @BindView(R.id.scanner_text)
    TextView scannerText;

    @Override
    protected int getContentView() {
        return R.layout.activity_qrcode_scanner;
    }

    @Override
    protected void onViewReady(Intent getIntent, Bundle savedInstanceSated) {

        assert mActionBar != null;
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);

        qrCodeScannerView.startCamera();
        qrCodeScannerView.setResultHandler(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!qrCodeScannerView.isActivated()) {
            qrCodeScannerView.startCamera();
            qrCodeScannerView.setResultHandler(this);
        }
        scannerText.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scannerText.setVisibility(View.GONE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        qrCodeScannerView.stopCamera();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void handleResult(Result result) {
        String merchantId = result.getText();
        if (NetworkUtils.isNetworkAvailable(this)) {
            loadMerchantCredentials(merchantId);
        } else {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
        }

    }


    private void loadMerchantCredentials(final String merchantId) {
        try {
            final String wmxId = new String(Base64.decode(merchantId, Base64.DEFAULT));
            if (!wmxId.toLowerCase().startsWith("wmx")) {
                Toast.makeText(this, "QR Code is not valid.", Toast.LENGTH_SHORT).show();
                return;
            }
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            GetMerchantCredentialApiService merchantCredentialApiService = ServiceGenerator.createService(this, GetMerchantCredentialApiService.class);
            getMerchantCredentialResponseCall = merchantCredentialApiService.getMerchantCredentials(merchantId);
            getMerchantCredentialResponseCall.enqueue(new Callback<GetMerchantCredentialResponse>() {
                @Override
                public void onResponse(@NonNull Call<GetMerchantCredentialResponse> call, @NonNull Response<GetMerchantCredentialResponse> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful() && response.body() != null) {
                        GetMerchantCredentialResponse result = response.body();
                        assert result != null;
                        String enCodedCredentials = result.credentials;
                        String afterDecodedCredential = new String(Base64.decode(enCodedCredentials, Base64.DEFAULT));
                        try {
                            JSONObject credentialsObject = new JSONObject(afterDecodedCredential);
                            String userName = credentialsObject.optString("username");
                            String password = credentialsObject.optString("password");
                            String appkey = credentialsObject.optString("app_key");
                            String appName = credentialsObject.optString("app_name");
                            Intent initIntent = new Intent(QRCodeScannerActivity.this, InitPaymentActivity.class);

                            initIntent.putExtra(Key.wmx_id.name(), wmxId);
                            initIntent.putExtra(Key.wmx_username.name(), userName);
                            initIntent.putExtra(Key.wmx_password.name(), password);
                            initIntent.putExtra(Key.wmx_api_key.name(), appkey);
                            initIntent.putExtra(Key.app_name.name(), appName);
                            initIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(initIntent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<GetMerchantCredentialResponse> call, @NonNull Throwable t) {
                    progressDialog.dismiss();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "QR Code is not valid.", Toast.LENGTH_SHORT).show();
        }

    }
}
