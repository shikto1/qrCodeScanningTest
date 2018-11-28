package walletmix.com.walletmixpayment.push;
import android.app.PendingIntent;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import walletmix.com.walletmixpayment.utils.NotificationUtils;


public class FcmMessagingService extends FirebaseMessagingService {

    private NotificationUtils notificationUtils;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationUtils = new NotificationUtils(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String title = remoteMessage.getData().get("title");
        String mes = remoteMessage.getData().get("message");
        String imageUrl = remoteMessage.getData().get("banner");
        final PendingIntent pendingIntent = null;
        notificationUtils.showPushNotification(title,mes,pendingIntent,imageUrl);
    }
}
