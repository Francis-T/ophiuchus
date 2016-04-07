package net.oukranos.ophiuchus;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CursorAdapter;
import android.widget.ListView;

import net.oukranos.ophiuchus.types.HealthDb;
import net.oukranos.ophiuchus.types.HealthDbRecordsAdapter;

import java.util.List;

public class DataBrowserActivity extends AppCompatActivity {

    private HealthDbRecordsAdapter _adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_browser);

        /* Prepare the adapter */
        HealthDb hdb = HealthDb.getInstance(this);
        Cursor c = hdb.getHealthDataRecordCursor();
        _adapter = new HealthDbRecordsAdapter(this, c, false);

        /* Bind the adapter to a list view */
        ListView list = (ListView) findViewById(R.id.list_stored_data);
        list.setAdapter(_adapter);

        return;
    }
}
