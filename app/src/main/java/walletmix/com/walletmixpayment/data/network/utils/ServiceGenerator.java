package walletmix.com.walletmixpayment.data.network.utils;

import android.content.Context;

public class ServiceGenerator {

    public static  <T> T createService(Context context, Class<T> service) {
        return RetrofitApiClient.getRetrofit(context).create(service);
    }

}
