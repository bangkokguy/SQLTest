package android.development.bangkokguy.sqltest;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {

    static final boolean DEBUG = true;
    static final String TAG = "CollectBaroData";
    static final String TABLE = "baroData";

    SQLiteDatabase DB;
    long result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        //startService(new Intent(this, CollectBaroData.class));
        setContentView(R.layout.activity_main);
    }

    void init () {
        DB = openOrCreateDatabase(TABLE, MODE_PRIVATE, null);
        Log.d(TAG, DB.getPath());
        DB.execSQL("DROP TABLE " + TABLE);
        DB.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT,Date VARCHAR,Value VARCHAR);");

        ContentValues insertValues = new ContentValues();
        insertValues.put("Date", "date");
        insertValues.put("Value", "500");
        result = DB.insert(TABLE, null, insertValues);
        result = DB.insert(TABLE, null, insertValues);
        result = DB.insert(TABLE, null, insertValues);
        result = DB.insert(TABLE, null, insertValues);

        // Query for items from the database and get a cursor back
        Cursor todoCursor = DB.rawQuery("SELECT  * FROM "+TABLE, null);
        if(DEBUG)Log.d(TAG, Integer.toString(todoCursor.getCount()));

        // Find ListView to populate
        //ListView lvItems = (ListView) findViewById(R.id.lvItems);
        ListView lvItems = new ListView(this, null);

        // Setup cursor adapter using cursor from last step
        TodoCursorAdapter todoAdapter = new TodoCursorAdapter(this, todoCursor);
        todoAdapter.newView(this, todoCursor, lvItems);
        //todoAdapter.bindView(lvItems, this, todoCursor);

        // Attach cursor adapter to the ListView
        Log.d(TAG, todoAdapter.toString());

        //lvItems.setAdapter(todoAdapter);
    }
}
