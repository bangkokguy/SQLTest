package android.development.bangkokguy.sqltest;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

public class CollectBaroData extends Service {

    static final boolean DEBUG = true;
    static final String TAG = "CollectBaroData";
    static final String TABLE = "baroData";

    SQLiteDatabase DB;
    SensorManager mSensorManager;
    Sensor baro;
    Context context;

    long insertRow (String timeStamp, String baroValue) {

        ContentValues insertValues = new ContentValues();
        insertValues.put("Date", timeStamp);
        insertValues.put("Value", baroValue);
        return DB.insert(TABLE, null, insertValues);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = getApplicationContext();

        DB = openOrCreateDatabase(TABLE, MODE_PRIVATE, null);
        DB.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE+"(Date VARCHAR,Value VARCHAR);");

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        baro = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if(baro == null){
            Log.d(TAG,"Failure! No barometer.");
        }
        else {
            Log.d(TAG,"Success! There's a barometer.");
            mSensorManager.registerListener(sensorEventListener, baro, SensorManager.SENSOR_DELAY_NORMAL);
        }

        return START_STICKY;
    }

    SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            Log.d(TAG, "Insert result="
                    +
                    insertRow(
                        Long.toString(event.timestamp),
                        Long.toString((long)event.values[0])));
            mSensorManager.unregisterListener(this);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
