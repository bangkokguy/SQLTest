package android.development.bangkokguy.sqltest;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import java.sql.Timestamp;

/**
 * TODO: Reorganize the baro data file->
 * TODO:    delete records older than specified date->
 * TODO:    after deletion copy into new table (eliminate deleted records)
 * DONE: Store the time elapsed between to samples->
 * DONE:    new field in database
 */

public class CollectBaroData extends Service {

    static final boolean DEBUG = true;
    static final String TAG = "CollectBaroData";
    static final String TABLE = "baroData";

    SQLiteDatabase DB;
    SensorManager mSensorManager;
    Sensor baro;
    Context context;

    void createTable() {
        DB.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "Date VARCHAR," +
                "Value VARCHAR," +
                "Elapsed VARCHAR);");
    }
    long insertRow (String timeStamp, String baroValue, String elapsedTime) {

        ContentValues insertValues = new ContentValues();
        insertValues.put("Date", timeStamp);
        insertValues.put("Value", baroValue);
        insertValues.put("Elapsed", elapsedTime);
        return DB.insert(TABLE, null, insertValues);
    }

    private PowerManager.WakeLock wl;
    AlarmManager alarmMgr;
    PendingIntent pendingIntent;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DoNjfdhotDimScreen");//End of onCreate
        wl.acquire();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            pendingIntent =
                    PendingIntent.getBroadcast(
                            this,
                            0,
                            new Intent(this, AlarmReceiver.class),
                            PendingIntent.FLAG_CANCEL_CURRENT);
        }

        context = getApplicationContext();

        DB = openOrCreateDatabase(TABLE, MODE_PRIVATE, null);
        createTable();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        baro = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if(baro == null){
            Log.e(TAG,"Failure! No barometer.");
        }
        else {
            if(DEBUG)Log.d(TAG,"Success! There's a barometer.");
            mSensorManager.registerListener(sensorEventListener, baro, SensorManager.SENSOR_DELAY_NORMAL);
        }

        return START_STICKY;
    }

    float s=0;
    Timestamp ts = null;
    long oldTime = System.currentTimeMillis();
    long newTime = 0;
    long elapsed = 0;

    SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            mSensorManager.unregisterListener(this);

            s = event.values[0]*100;
            newTime = System.currentTimeMillis();
            ts = new Timestamp(newTime);
            elapsed = (newTime - oldTime) / 1000;
            oldTime = newTime;

            Log.d(TAG, "Insert result="
                    +
                    insertRow(
                        //ts.toString(),
                        Long.toString(newTime),
                        Long.toString((long)s),
                        Long.toString(elapsed)
                    )
            );


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.d(TAG, "setAndAllowWhileIdle");
                long triggerTime = SystemClock.elapsedRealtime() + 300000;
                alarmMgr.setAndAllowWhileIdle(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        //0,
                        triggerTime, //INTERVAL_FIFTEEN_MINUTES,
                        pendingIntent);
            }

            wl.release();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
