package walletmix.com.walletmixpayment.data.network.utils;


import android.content.Context;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import walletmix.com.walletmixpayment.BuildConfig;

class RetrofitApiClient {

    private static Retrofit retrofit = null;

    static Retrofit getRetrofit(final Context context) {

        if (retrofit == null) {

            //HttpClient Builder....
            OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
            okHttpClientBuilder.connectTimeout(20, TimeUnit.SECONDS);
            okHttpClientBuilder.readTimeout(20, TimeUnit.SECONDS);
            okHttpClientBuilder.writeTimeout(20, TimeUnit.SECONDS);

            okHttpClientBuilder.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(@NonNull Chain chain) throws IOException {
                    if(!NetworkUtils.isNetworkAvailable(context)){
                        throw new NoConnectionException();
                    }else{
                        Request.Builder requestBuilder = chain.request().newBuilder();
                        requestBuilder.addHeader("Accept",Headers.ACCEPT);

                        return chain.proceed(requestBuilder.build());
                    }
                }
            });

            // Adding Logging Interceptor in debug mode to see details of request....
            if(BuildConfig.DEBUG){
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                okHttpClientBuilder.addInterceptor(logging);
            }

            // Building Retrofit...
            retrofit = new Retrofit.Builder()
                    .baseUrl(API.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClientBuilder.build())
                    .build();
        }
        return retrofit;
    }

}
