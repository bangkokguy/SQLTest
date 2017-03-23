package android.development.bangkokguy.sqltest;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;

public class CollectBaroData extends Service {

    static final boolean DEBUG = true;
    static final String TAG = "CollectBaroData";
    static final String TABLE = "baroDB";

    SQLiteDatabase DB;
    long result;

    public CollectBaroData() {
        DB = openOrCreateDatabase(TABLE, MODE_PRIVATE, null);
        DB.execSQL("CREATE TABLE IF NOT EXISTS BaroData(Date VARCHAR,Value VARCHAR);");
    }

    int insertRow () {
        DB.execSQL("INSERT INTO BaroData VALUES('admin','admin');");
        ContentValues insertValues = new ContentValues();
        insertValues.put("Date", "date");
        insertValues.put("Value", "500");
        result = DB.insert(TABLE, null, insertValues);
        if (DEBUG) Log.d(TAG, "Insert result="+Long.toString(result));
        return 0;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
