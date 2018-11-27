package walletmix.com.walletmixpayment.ui;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.walletmix.walletmixopglibrary.WalletmixOnlinePaymentGateway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.OnClick;
import walletmix.com.walletmixpayment.R;
import walletmix.com.walletmixpayment.base.BaseActivity;
import walletmix.com.walletmixpayment.data.pref.Key;
import walletmix.com.walletmixpayment.data.pref.SessionManager;

public class InitPaymentActivity extends BaseActivity {

    WalletmixOnlinePaymentGateway onlinePaymentGateway;

    boolean isForEmi = false;
    String emiPeriod = "", selectedEmiBank = "";
    ArrayList<String> emiBankKeyList;
    ArrayList<String> emiBankNameList;

    @BindView(R.id.et_amount)
    EditText etAmount;

    @BindView(R.id.et_shipping_address)
    EditText et_shipping_address;

    @BindView(R.id.emi_panel)
    LinearLayout emiPanel;

    @BindView(R.id.emi_checkbox)
    CheckBox emiCheckBox;

    @BindView(R.id.emi_bank_list_spinner)
    Spinner emiBankListSpinner;

    @BindView(R.id.emi_period_spinner)
    Spinner emiPeriodSpinner;


    String wmxId, wmxUserName, wmxPass, wmxAppKey, wmxAppName;
    SessionManager sessionManager;


    @Override
    protected int getContentView() {
        return R.layout.activity_initi_payment;
    }

    @Override
    protected void onViewReady(Intent getIntent, Bundle savedInstanceSated) {
        mActionBar.setDisplayShowHomeEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        if (getIntent != null) {
            wmxId = getIntent.getStringExtra(Key.wmx_id.name());
            wmxUserName = getIntent.getStringExtra(Key.wmx_username.name());
            wmxPass = getIntent.getStringExtra(Key.wmx_password.name());
            wmxAppKey = getIntent.getStringExtra(Key.wmx_api_key.name());
            wmxAppName = getIntent.getStringExtra(Key.app_name.name());

            mActionBar.setTitle("Merchant-"+ wmxAppName);
        }

        onlinePaymentGateway = new WalletmixOnlinePaymentGateway(this);
        sessionManager = new SessionManager(this);


        emiBankListSpinner = findViewById(R.id.emi_bank_list_spinner);
        emiPeriodSpinner = findViewById(R.id.emi_period_spinner);

        emiBankKeyList = sessionManager.getStringArrayList(Key.emi_bank_key_list.name());
        emiBankNameList = sessionManager.getStringArrayList(Key.emi_bank_name_list.name());

        ArrayAdapter emiBankListAdapter = new ArrayAdapter(this, R.layout.spinner_item_view, emiBankNameList);
        emiBankListSpinner.setAdapter(emiBankListAdapter);

        ArrayAdapter emiPeriodAdapter = ArrayAdapter.createFromResource(this,R.array.emi_periods,R.layout.spinner_item_view);
        emiPeriodSpinner.setAdapter(emiPeriodAdapter);

        emiPeriodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        emiPeriod = "3";
                        break;
                    case 1:
                        emiPeriod = "6";
                        break;
                    case 2:
                        emiPeriod = "9";
                        break;
                    case 3:
                        emiPeriod = "12";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        emiBankListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedEmiBank = emiBankKeyList.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        emiCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()

        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    emiPanel.setVisibility(View.VISIBLE);
                    isForEmi = true;
                } else {
                    isForEmi = false;
                    emiPanel.setVisibility(View.GONE);
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @OnClick({R.id.go_to_payment_gateway_btn})
    public void onGoToBtnClicked(View view) {
        switch (view.getId()) {
            case R.id.go_to_payment_gateway_btn:
                String amount = etAmount.getText().toString();
                if(amount.isEmpty()){
                    Toast.makeText(InitPaymentActivity.this,"Amount can not be empty",Toast.LENGTH_SHORT).show();
                }else{
                    if(networkUtils.isNetworkAvailable()){
                        goToPaymentGateWay(amount);
                    }else{
                        showInternetAlertDialog();
                    }
                }
                break;
        }
    }

    private void goToPaymentGateWay(String amount) {

        String merchantOrderId = randomString(12, true);
        String merchantRefId = merchantOrderId;
        String customerName = mSessionManager.getString(Key.userFullName.name(), "");//name;
        String customerPhone = mSessionManager.getString(Key.userPhone.name(),"");//phone;
        String customerEmail = mSessionManager.getString(Key.userEmail.name(),"");
        String customerAddress = ""/*"House:2/B, Road No.8,Khilkhet, Nikunjo-2,Dhaka-1229"*/;
        String customerCity = "";
        String customerCountry = "Bangladesh";
        String customerPostcode = ""/*"1229"*/;
        String callback_url = "https://epay.walletmix.com/check-payment";
        String currency = "BDT" /*"BDT"*/;

        String shippingName = "";
        String shippingAddress = et_shipping_address.getText().toString();
        String shippingCity = "";
        String shippingCountry = "";
        String shippingPostCode = "";

        String productDesc = "{1xAdd Fund[" + amount + "]=[" + amount + "]}+{shipping rate:0}-{discount amount:0}=" + amount;

        Map<String, String> extrajsonParam = new HashMap<>();


        PackageInfo pInfo = null;
        String version = "";
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (pInfo != null) {
            version = pInfo.versionName;
        }

        extrajsonParam.put("app_version", version);
        extrajsonParam.put("os", "android");
        extrajsonParam.put("app_name", getString(R.string.app_name));
        extrajsonParam.put("user_name", customerName);
        extrajsonParam.put("remarks", "");
        extrajsonParam.put("reference_id", merchantOrderId);
        if (isForEmi) {
            extrajsonParam.put("emi_period", emiPeriod);
            extrajsonParam.put("emi_selected_bank", selectedEmiBank);
        }


        String extra_json = new Gson().toJson(extrajsonParam);

        onlinePaymentGateway.setTransactionInformation(wmxId, wmxUserName, wmxPass, wmxAppKey, merchantOrderId/*"1000"*/,/*"s"+*/merchantRefId,
                customerName, customerPhone, customerEmail, customerAddress, customerCity, customerCountry, customerPostcode, productDesc, amount, currency,
                shippingName, shippingAddress, shippingCity, shippingCountry, shippingPostCode, wmxAppName, callback_url, extra_json);
        onlinePaymentGateway.startTransactions(false, GatewayCallbackActivity.class);
    }


    private static final String DATA_Mixed = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String DATA_number = "0123456789";

    private static Random RANDOM = new Random();

    private static String randomString(int len, boolean mixedData) {
        StringBuilder sb = new StringBuilder(len);
        if (mixedData) {
            for (int i = 0; i < len; i++) {
                sb.append(DATA_Mixed.charAt(RANDOM.nextInt(DATA_Mixed.length())));
            }
        } else {
            for (int i = 0; i < len; i++) {
                sb.append(DATA_number.charAt(RANDOM.nextInt(DATA_number.length())));
            }
        }
        return sb.toString();
    }


}
