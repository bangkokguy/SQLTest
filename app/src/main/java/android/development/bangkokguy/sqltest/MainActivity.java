package android.development.bangkokguy.sqltest;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.renderscript.Sampler;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.List;

import static android.app.AlarmManager.INTERVAL_FIFTEEN_MINUTES;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    static final boolean DEBUG = true;
    static final String TAG = "CollectBaroData";
    static final String TABLE = "baroData";

    SQLiteDatabase DB;
    Button mainResetDB, mainRefreshView;
    SimpleCursorAdapter dataAdapter;
    SensorManager mSensorManager;
    Sensor baro;
    ListView lvMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mainResetDB = (Button) findViewById(R.id.mainResetDB);
        mainResetDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DB.execSQL("DROP TABLE " + TABLE);
                DB.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE +
                        "(_id INTEGER PRIMARY KEY AUTOINCREMENT,Date VARCHAR,Value VARCHAR);");
            }
        });

        mainRefreshView = (Button) findViewById(R.id.mainRefreshView);
        mainRefreshView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataAdapter.swapCursor(DB.rawQuery("SELECT  * FROM "+TABLE, null));
                graphInit();
            }
        });

        // Find ListView to populate
        lvMain = (ListView) findViewById(R.id.lvMain);

        init();
        sensorInit();
        graphInit();

        AlarmManager alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        this,
                        0,
                        new Intent(this, AlarmReceiver.class),
                        PendingIntent.FLAG_CANCEL_CURRENT);

        // Use inexact repeating which is easier on battery (system can phase events and not wake at exact times)
        alarmMgr.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                0,
                600, //INTERVAL_FIFTEEN_MINUTES,
                pendingIntent);
    }

    void graphInit() {
        GraphView graph = (GraphView) findViewById(R.id.graph);

        Cursor c = DB.rawQuery("SELECT  * FROM "+TABLE, null);
        c.moveToFirst();

        DataPoint [] data = new DataPoint[c.getCount()-1];
        //List<DataPoint> list = new ArrayList<>();

        int i = 0;
        while (c.moveToNext()) {
            int id = c.getInt(0);
            Log.d(TAG, "_Id = "+Integer.toString(id));

            String title = c.getString(1);
            Log.d(TAG, "Timestamp = "+title);

            String value = c.getString(2);

            data[i] = new DataPoint(i++, Integer.parseInt(value));
            //list.add(new DataPoint(i++, Integer.parseInt(value)));
        }

        /*LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3)
        });*/
        //LineGraphSeries<DataPoint> series = new LineGraphSeries<>((DataPoint[]) list.toArray());
        Log.d(TAG, "Data length="+Integer.toString(data.length));
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(data);
        graph.addSeries(series);

        c.close();
    }

    void sensorInit(){
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        baro = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if(baro == null){
            Log.d(TAG,"Failure! No barometer.");
            mainResetDB.setText("No baro");
        }
        else {
            Log.d(TAG,"Success! There's a barometer.");
            Log.d(TAG, "baro.toString="+baro.toString());
            mainResetDB.setText("baro ok");
            mSensorManager.registerListener(this, baro, SensorManager.SENSOR_DELAY_UI);

        }
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        mainResetDB.setText(Long.toString(event.timestamp));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, baro, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    void init () {
        DB = openOrCreateDatabase(TABLE, MODE_PRIVATE, null);
        Log.d(TAG, DB.getPath());
        DB.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT,Date VARCHAR,Value VARCHAR);");

        String[] columns = new String[] {
                "_id",
                "Date",
                "Value"
        };

        // the XML defined views which the data will be bound to
        int[] to = new int[] {
                R.id.tvId,
                R.id.tvDate,
                R.id.tvValue,
        };

        // create the adapter using the cursor pointing to the desired data
        //as well as the layout information
        dataAdapter = new SimpleCursorAdapter(
                this,
                R.layout.template,
                DB.rawQuery("SELECT  * FROM "+TABLE, null), //cursor,
                columns,
                to,
                0);

        if (lvMain != null) {
            lvMain.setAdapter(dataAdapter);
        }
    }
}
