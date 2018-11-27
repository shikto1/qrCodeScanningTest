package walletmix.com.walletmixpayment.data.network.utils;

import java.io.IOException;

public class NoConnectionException extends IOException{

    @Override
    public String getMessage() {
        return  "No Internet Connection";
    }
}
