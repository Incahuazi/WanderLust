package Sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Erik.Rans on 27/09/2017.
 */

public class NetworkStateChecker extends BroadcastReceiver {

    private static final String TAG = "NetworkStateChecker";

    @Override
    public void onReceive(Context context, Intent intent) {
        tryToSync(context);
    }

    public void tryToSync(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE){
                Log.d(TAG, "onReceive: network detected");

                Intent newIntent = new Intent(context, SyncService.class);
                context.startService(newIntent);
            }
        }
    }
}
