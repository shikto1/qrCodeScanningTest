package walletmix.com.walletmixpayment.data.network.apiServices;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;
import walletmix.com.walletmixpayment.data.network.apiResponses.EMIBasnkListResponse;

public interface EMIBankListApiService {

    @GET
    Call<EMIBasnkListResponse> getEmiBankList(@Url String emiBankUrl);
}
