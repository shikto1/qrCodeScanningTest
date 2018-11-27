package walletmix.com.walletmixpayment.data.network.apiServices;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import walletmix.com.walletmixpayment.data.network.utils.API;

public interface GetMerchantCredentialApiService {

    @FormUrlEncoded
    @POST(API.GET_MERCHANT_CREDENTIAL)
    Call<GetMerchantCredentialResponse> getMerchantCredentials(@Field("wmx_id") String wmxId);
}
