package walletmix.com.walletmixpayment.utils;
import android.util.Log;

import walletmix.com.walletmixpayment.BuildConfig;

public class LogUtils{

    public static String TAG = "SHISHIR_13";

    public LogUtils(){}

    public  void logD( String message) {
        logD(TAG,message);
    }

    public  void logD(String tag ,  String message) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message);
        }
    }

    public  <T> void logD(Class<T> className, String message) {
        logD(className.getSimpleName(), message);

    }

    public  void logV( String message) {
        logV(TAG, message);
    }


    public  void logV(String tag ,  String message) {
        if (BuildConfig.DEBUG) {
            Log.v(tag, message);
        }
    }

    public  <T> void logV(Class<T> className, String message) {
        logV(className.getSimpleName(),message);
    }

}
