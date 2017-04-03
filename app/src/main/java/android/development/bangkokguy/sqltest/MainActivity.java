package android.development.bangkokguy.sqltest;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.sql.Timestamp;

/**
 * TODO: Select last n days, weeks, months
 * TODO:    Date picker?
 * TODO: Show data between time intervals
 * TODO:    Date picker?
 * TODO: Create settings activity->
 * TODO:    default time interval
 * TODO:    delete records older than
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    static final boolean DEBUG = true;
    static final String TAG = "MainActivity";
    static final String TABLE = "baroData";

    static final String DEFAULT_ZOOM = "1";

    SQLiteDatabase DB;
    Button mainResetDB, mainRefreshView, inc_step, reset_step, dec_step;
    SimpleCursorAdapter dataAdapter;
    SensorManager mSensorManager;
    Sensor baro;
    ListView lvMain;
    GraphView graph;
    LineGraphSeries<DataPoint> series;
    String zoom = DEFAULT_ZOOM;

    String where() {
        //return " where (_Id % " + zoom + ") = 0";
        long ts = (System.currentTimeMillis()-24*1000*Integer.parseInt(zoom)*3600);
        return " where (Date > \'" + Long.toString(ts) + "\')";
        //return " where (Date > \'2017-03-27\') and (Date < \'2017-03-28\')";
    }

    void dropTable(){
        DB.execSQL("DROP TABLE " + TABLE);
    }
    void createTable() {
        DB.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "Date VARCHAR," +
                "Value VARCHAR," +
                "Elapsed VARCHAR);");
    }
    void recreateTable(){
        dropTable();
        createTable();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mainResetDB = (Button) findViewById(R.id.mainResetDB);
        mainResetDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recreateTable();
            }
        });

        mainRefreshView = (Button) findViewById(R.id.mainRefreshView);
        mainRefreshView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataAdapter.swapCursor(DB.rawQuery("SELECT * FROM " + TABLE + where(), null));
                graphInit();
            }
        });

        dec_step = (Button) findViewById(R.id.dec_step);
        dec_step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = Integer.parseInt(reset_step.getText().toString());
                if (--i < 1) i = 1;
                reset_step.setText(Integer.toString(i));
            }
        });

        reset_step = (Button) findViewById(R.id.reset_step);
        reset_step.setText(zoom);
        reset_step.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                zoom = reset_step.getText().toString();
                if(DEBUG)Log.d(TAG, "reset_step:onLayoutChange:"+where());
            }
        });
        reset_step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset_step.setText(DEFAULT_ZOOM);
            }
        });

        inc_step = (Button) findViewById(R.id.inc_step);
        inc_step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = Integer.parseInt(reset_step.getText().toString());
                reset_step.setText(Integer.toString(++i));
            }
        });


        // Find ListView to populate
        lvMain = (ListView) findViewById(R.id.lvMain);

        init();
        sensorInit();
        graphInit();
        alarmInit();
    }

    void alarmInit() {
        AlarmManager alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        this,
                        0,
                        new Intent(this, AlarmReceiver.class),
                        PendingIntent.FLAG_CANCEL_CURRENT);

        // Use inexact repeating which is easier on battery (system can phase events and not wake at exact times)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "setAndAllowWhileIdle");
            long triggerTime = SystemClock.elapsedRealtime() + 300000;
            alarmMgr.setAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    //0,
                    triggerTime, //INTERVAL_FIFTEEN_MINUTES,
                    pendingIntent);
        } else {
            alarmMgr.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    0,
                    300, //INTERVAL_FIFTEEN_MINUTES,
                    pendingIntent);
        }
    }

    DataPoint [] data() {
        DataPoint [] d = null;

        Cursor c = DB.rawQuery("SELECT  * FROM "+TABLE + where(), null);

        Log.d(TAG, "data(): where->"+where()+" count()->"+Integer.toString(c.getCount()));

        if(c.moveToFirst()) {
            d = new DataPoint[c.getCount()];

            int i = 0;
            do {
                int id = c.getInt(0);
                String title = c.getString(1);
                String value = c.getString(2);
                d[i++] = new DataPoint(Long.parseLong(title), Long.parseLong(value));

                if(DEBUG)Log.d(TAG, "_Id="+Integer.toString(id)+" Timestamp="+title);

            } while (c.moveToNext());
        }

        c.close();
        if(d==null) {
            d = new DataPoint[1];
            d[0] = new DataPoint(1,1);
        }

        return d;
    }

    DataPoint [] d;
    void graphInit() {
        d = data();
        graph = (GraphView) findViewById(R.id.graph);
        graph.removeAllSeries();

        if(d!=null){
            Log.d(TAG, "Data length="+Integer.toString(d.length));
            series = new LineGraphSeries<>(d);

            // styling series
            series.setTitle("Baro data");
            series.setColor(Color.GREEN);
            series.setDrawDataPoints(true);
            series.setDataPointsRadius(10);
            series.setThickness(8);

            graph.getViewport().setMinY(100000);
            graph.getViewport().setMaxY(102000);
            graph.getViewport().setYAxisBoundsManual(true);


            //graph.getSecondScale().setMinY(940);
            //graph.getSecondScale().setMaxY(1024);
            graph.addSeries(series);
            graph.setOnClickListener(new GraphView.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG,"onClick.graph");
                    d = data();
                    graph.removeAllSeries();
                    series = new LineGraphSeries<>(d);
                    series.setTitle("Random Curve 1");
                    series.setColor(Color.GREEN);
                    series.setDrawDataPoints(true);
                    series.setDataPointsRadius(10);
                    series.setThickness(8);
                    graph.addSeries(series);
                }
            });
            graph.getViewport().setScrollable(true); // enables horizontal scrolling
            graph.getViewport().setScrollableY(false); // enables vertical scrolling
            graph.getViewport().setScalable(true); // enables horizontal zooming and scrolling
            graph.getViewport().setScalableY(false); // enables vertical zooming and scrolling
            StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
            staticLabelsFormatter.setHorizontalLabels(new String[] {"old", "middle", "new"});
            //staticLabelsFormatter.setVerticalLabels(new String[] {"low", "middle", "high"});
            graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);

        }
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
        //mainResetDB.setText(Float.toString(event.values[0]*100));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        DB = openOrCreateDatabase(TABLE, MODE_PRIVATE, null);
        mSensorManager.registerListener(this, baro, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        DB.close();
    }

    void init () {
        DB = openOrCreateDatabase(TABLE, MODE_PRIVATE, null);
        Log.d(TAG, DB.getPath());

        //dropTable(); /*!!!!!!*/
        createTable();

        String[] columns = new String[] {
                "_id",
                "Date",
                "Value",
                "Elapsed"
        };

        // the XML defined views which the data will be bound to
        int[] to = new int[] {
                R.id.tvId,
                R.id.tvDate,
                R.id.tvValue,
                R.id.tvElapsed
        };

        // create the adapter using the cursor pointing to the desired data
        //as well as the layout information
        dataAdapter = new SimpleCursorAdapter(
                this,
                R.layout.template,
                DB.rawQuery("SELECT  * FROM "+TABLE + where(), null), //cursor,
                columns,
                to,
                0);

        if (lvMain != null) {
            lvMain.setAdapter(dataAdapter);
        }
    }
}
