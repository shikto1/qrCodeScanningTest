package walletmix.com.walletmixpayment.ui;
import android.content.Intent;
import android.os.Bundle;
import org.json.JSONException;
import org.json.JSONObject;
import walletmix.com.walletmixpayment.R;
import walletmix.com.walletmixpayment.base.BaseActivity;
import walletmix.com.walletmixpayment.utils.AlertServices;
import walletmix.com.walletmixpayment.utils.Navigator;
public class GatewayCallbackActivity extends BaseActivity {

    String responseTxnStatus,responseWmxId;

    @Override
    protected int getContentView() {
        return R.layout.activity_gateway_response;
    }

    @Override
    protected void onViewReady(Intent getIntent, Bundle savedInstanceSated) {
        String response = getIntent().getStringExtra("response");
        JSONObject jsonObject;
        if (response.equals("false")) {
            alert("Transaction was incomplete. Please try again to complete your transaction.");
        } else {
            try {
                jsonObject = new JSONObject(response);
                responseWmxId = jsonObject.getString("wmx_id");
                responseTxnStatus = jsonObject.getString("txn_status");
                switch (responseTxnStatus) {
                    case "1000":
                        alert("Transaction Success");
                        break;
                    case "1001":
                        alert("Transaction Rejected");
                        break;
                    case "1009":
                        alert("Transaction Cancelled");
                        break;

                }
            } catch (JSONException e) {

                e.printStackTrace();
            }
        }
    }

    private void alert(String msg) {
        alertServices.showAlertForConfirmation(this, null, msg, null, "Back to Home", new AlertServices.AlertListener() {
            @Override
            public void onNegativeBtnClicked() {

            }

            @Override
            public void onPositiveBtnClicked() {
                mNavigator.navigateToHomeByFinishingall(GatewayCallbackActivity.this);

            }
        });
    }
}



