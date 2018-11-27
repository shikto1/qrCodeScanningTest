package walletmix.com.walletmixpayment.data.network.apiResponses;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetMerchantCredentialResponse {
    @SerializedName("credentials")
    @Expose
    public String credentials;
}
