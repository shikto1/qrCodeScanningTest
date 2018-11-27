package walletmix.com.walletmixpayment.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import walletmix.com.walletmixpayment.data.pref.SessionManager;


public class PermissionManager {

    private SessionManager sessionManager;


    public PermissionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    private boolean shouldAskPermission() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
    }

    private boolean shouldAskPermission(Context context, String permission) {
        if (shouldAskPermission()) {
            int permissionResult = ActivityCompat.checkSelfPermission(context, permission);
            if (permissionResult != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }


    public void checkPermission(Context context, String permission, PermissionAskListener listener) {

        if (shouldAskPermission(context, permission)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((AppCompatActivity) context, permission)) {
                listener.onPermissionPreviouslyDenied();
            } else {
                if (sessionManager.isFirstTimeAskingPermission(permission)) {
                    sessionManager.firstTimeAskingPermission(permission, false);
                    listener.onNeedPermission();
                } else {

                    listener.onPermissionPreviouslyDeniedWithNeverAskAgain();
                }
            }
        } else {
            listener.onPermissionGranted();
        }
    }

    public void goToSettings(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.parse("package:" + context.getApplicationContext().getPackageName());
        intent.setData(uri);
        context.startActivity(intent);
    }



    public interface PermissionAskListener {

        void onNeedPermission();

        void onPermissionPreviouslyDenied();

        void onPermissionPreviouslyDeniedWithNeverAskAgain();

        void onPermissionGranted();
    }
}
