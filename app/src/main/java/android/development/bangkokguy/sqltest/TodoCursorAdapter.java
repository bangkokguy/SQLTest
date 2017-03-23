package android.development.bangkokguy.sqltest;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by bangkokguy on 3/23/17.
 *
 */

class TodoCursorAdapter extends CursorAdapter {
    TodoCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.activity_main, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView tvDate = (TextView) view.findViewById(R.id.tvDate);
        TextView tvValue = (TextView) view.findViewById(R.id.tvValue);
        // Extract properties from cursor
        String date = cursor.getString(cursor.getColumnIndexOrThrow("Date"));
        String value = cursor.getString(cursor.getColumnIndexOrThrow("Value"));
        // Populate fields with extracted properties
        tvDate.setText(date);
        tvValue.setText(value);
    }
}