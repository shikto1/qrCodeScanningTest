package walletmix.com.walletmixpayment.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

public class AlertServices {

    private Toast mToast;
    private AlertDialog mAlert = null;
    private AlertDialog alert = null;
    private Context context;





    public AlertServices(Context context) {
        this.context = context;
    }

    public void showToast(Context context, String message) {
        try {
            if (!mToast.getView().isShown()) {
                createToast(context, message);

            } else {
                mToast.setText(message);
            }
        } catch (Exception e) {
            createToast(context, message);
        }
    }

    public void showToast(String message) {
        showToast(context, message);
    }

    public void showToast(Context context, int resId) {
        showToast(context, context.getString(resId));
    }


    private void createToast(Context context, String message) {
        mToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        mToast.show();
    }

    public void showAlert(Context context, String title, String message) {
        if (mAlert != null && mAlert.isShowing()) {
            mAlert.cancel();
        }
        mAlert = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }).create();
        mAlert.show();
    }

    public void showAlertForConfirmation(Context context, String title, String message, String negativeBtn, String positiveBtn, final AlertListener alertListener) {
        if (alert != null && alert.isShowing()) {
            alert.cancel();
        }
        alert = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton(negativeBtn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertListener.onNegativeBtnClicked();
                    }
                })
                .setPositiveButton(positiveBtn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertListener.onPositiveBtnClicked();
                    }
                })
                .create();
        alert.show();
    }

    public interface AlertListener {

        void onNegativeBtnClicked();

        void onPositiveBtnClicked();
    }

}
