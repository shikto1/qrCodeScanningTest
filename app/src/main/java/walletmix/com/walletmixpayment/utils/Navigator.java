package walletmix.com.walletmixpayment.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import walletmix.com.walletmixpayment.ui.MainActivity;
import walletmix.com.walletmixpayment.R;
import walletmix.com.walletmixpayment.ui.LoginActivity;


public class Navigator {


    public  void navigateToHomeByFinishingall(Context from) {
        Intent i = new Intent(from, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        from.startActivity(i);
        ((AppCompatActivity)from).overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

   // public void navigateToForgotPass(Context from){navigateTo(from,ForgotPasswordActivity.class);}
    public void navigateToHome(Context from){
        navigateTo(from, MainActivity.class);
    }

    public <T> void navigateTo(Context context, Class<T> target){
        Intent intent = new Intent(context,target);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
        ((AppCompatActivity)context).overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    public <T> void navigateToActivityWithValue(Context context, Class<T> target, String key, String value){
        Intent intent = new Intent(context,target);
        intent.putExtra(key,value);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
        ((AppCompatActivity)context).overridePendingTransition(R.anim.slide_from_right,R.anim.slide_to_left);
    }


}
