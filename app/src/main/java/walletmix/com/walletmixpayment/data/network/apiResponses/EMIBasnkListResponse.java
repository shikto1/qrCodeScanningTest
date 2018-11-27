package walletmix.com.walletmixpayment.data.network.apiResponses;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.HashMap;

public class EMIBasnkListResponse {

    @SerializedName("emiBankList")
    @Expose
    public HashMap<String, String> emiBankList;

}
