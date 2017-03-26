package android.development.bangkokguy.sqltest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    static final boolean DEBUG = true;
    static final String TAG = "BroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(DEBUG)Log.d(TAG, "onReceive");
        context.startService(new Intent(context, CollectBaroData.class));
    }

}
