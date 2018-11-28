package walletmix.com.walletmixpayment.file;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import io.fabric.sdk.android.services.concurrency.AsyncTask;


public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

    private ImageDownloadListener downloadListener;
    private String imageUrl;

    public ImageDownloader(String imageUrl, ImageDownloadListener downloadListener){
        this.imageUrl = imageUrl;
        this.downloadListener = downloadListener;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        try {
            URL url = new URL(imageUrl);
            InputStream in;
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            in = connection.getInputStream();
            return BitmapFactory.decodeStream(in);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        downloadListener.onDownloadedImage(bitmap);
    }

    public interface ImageDownloadListener {

        void onDownloadedImage(Bitmap bitmap);

    }
}
