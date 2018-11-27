package walletmix.com.walletmixpayment.ui;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.walletmix.walletmixopglibrary.WalletmixOnlinePaymentGateway;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import walletmix.com.walletmixpayment.R;
import walletmix.com.walletmixpayment.data.network.apiResponses.EMIBankListApiService;
import walletmix.com.walletmixpayment.data.network.apiServices.EMIBasnkListResponse;
import walletmix.com.walletmixpayment.data.pref.Key;
import walletmix.com.walletmixpayment.data.pref.SessionManager;
import walletmix.com.walletmixpayment.data.network.utils.ServiceGenerator;
import walletmix.com.walletmixpayment.utils.AlertServices;
import walletmix.com.walletmixpayment.utils.PermissionManager;

public class MainActivity extends AppCompatActivity {
    WalletmixOnlinePaymentGateway onlinePaymentGateway;
    TextView userFullNameTv, userEmailTv, userPhoneNumberTv;
    Button scanQRCodeBtn;
    AlertServices alertServices;
    PermissionManager permissionManager;
    String[] PERMISSION_CAMERA = {android.Manifest.permission.CAMERA};
    final int CAMERA_REQUEST_CODE = 123;



    String EMI_BANK_LIST_URL = "https://epay.walletmix.com/get-emi-bank-list";
    ArrayList<String> emiBankKeyList;
    ArrayList<String> emiBankNameList;
    SessionManager sessionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        loadBankList();

        scanQRCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                permissionManager.checkPermission(MainActivity.this, Manifest.permission.CAMERA, new PermissionManager.PermissionAskListener() {
                    @Override
                    public void onNeedPermission() {
                        ActivityCompat.requestPermissions(MainActivity.this, PERMISSION_CAMERA, CAMERA_REQUEST_CODE);
                    }

                    @Override
                    public void onPermissionPreviouslyDenied() {
                        alertServices.showAlertForConfirmation(MainActivity.this, getString(R.string.permission_required), getString(R.string.permission_camera_denied_one),
                                getString(R.string.button_not_now), getString(R.string.buttoin_continue), new AlertServices.AlertListener() {
                                    @Override
                                    public void onNegativeBtnClicked() {
                                    }

                                    @Override
                                    public void onPositiveBtnClicked() {
                                        ActivityCompat.requestPermissions(MainActivity.this, PERMISSION_CAMERA, CAMERA_REQUEST_CODE);
                                    }
                                });
                    }

                    @Override
                    public void onPermissionPreviouslyDeniedWithNeverAskAgain() {
                        alertServices.showAlertForConfirmation(MainActivity.this, getString(R.string.permission_required), getString(R.string.permission_camera_denied_two),
                                getString(R.string.button_not_now), getString(R.string.button_settings), new AlertServices.AlertListener() {
                                    @Override
                                    public void onNegativeBtnClicked() {
                                    }

                                    @Override
                                    public void onPositiveBtnClicked() {
                                        permissionManager.goToSettings(MainActivity.this);
                                    }
                                });
                    }

                    @Override
                    public void onPermissionGranted() {
                        goForScanningMerchantsQRCode();
                    }
                });
            }
        });

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    goForScanningMerchantsQRCode();
                } else {
                    alertServices.showAlertForConfirmation(this, getString(R.string.permission_required), getString(R.string.permission_camera_denied_one),
                            getString(R.string.button_not_now), getString(R.string.buttoin_continue), new AlertServices.AlertListener() {
                                @Override
                                public void onNegativeBtnClicked() {
                                }

                                @Override
                                public void onPositiveBtnClicked() {
                                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSION_CAMERA, CAMERA_REQUEST_CODE);
                                }
                            });
                }
                break;

        }
    }

    private void goForScanningMerchantsQRCode(){
        Intent qrCodeScannerIntent = new Intent(MainActivity.this, QRCodeScannerActivity.class);
        qrCodeScannerIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(qrCodeScannerIntent);
    }

    private void init() {
        onlinePaymentGateway = new WalletmixOnlinePaymentGateway(this);
        sessionManager = new SessionManager(this);
        permissionManager = new PermissionManager(sessionManager);

        scanQRCodeBtn = findViewById(R.id.scanButton);
        userFullNameTv = findViewById(R.id.user_full_name_tv);
        userEmailTv = findViewById(R.id.user_email_tv);
        userPhoneNumberTv = findViewById(R.id.user_phone_number_tv);

        alertServices = new AlertServices(this);

        userFullNameTv.setText(sessionManager.getString(Key.userFullName.name()));
        userEmailTv.setText(sessionManager.getString(Key.userEmail.name()));
        userPhoneNumberTv.setText(sessionManager.getString(Key.userPhone.name()));


    }

    private void loadBankList() {

        EMIBankListApiService emiBankListApiService = ServiceGenerator.createService(MainActivity.this, EMIBankListApiService.class);
        emiBankListApiService.getEmiBankList(EMI_BANK_LIST_URL).enqueue(new Callback<EMIBasnkListResponse>() {
            @Override
            public void onResponse(Call<EMIBasnkListResponse> call, Response<EMIBasnkListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    HashMap<String, String> emiBankList = response.body().emiBankList;
                    emiBankKeyList = new ArrayList<>();
                    emiBankNameList = new ArrayList<>();
                    if (!emiBankList.isEmpty()) {
                        for (Map.Entry entry : emiBankList.entrySet()) {
                            emiBankKeyList.add(entry.getKey().toString());
                            emiBankNameList.add(entry.getValue().toString());

                        }
                        emiBankNameList.add(0, "Select your EMI bank");
                        emiBankKeyList.add(0,"dummyText");
                        sessionManager.putStringArrayList(Key.emi_bank_key_list.name(), emiBankKeyList);
                        sessionManager.putStringArrayList(Key.emi_bank_name_list.name(), emiBankNameList);
                    }
                }
            }

            @Override
            public void onFailure(Call<EMIBasnkListResponse> call, Throwable t) {

            }
        });
    }

}
