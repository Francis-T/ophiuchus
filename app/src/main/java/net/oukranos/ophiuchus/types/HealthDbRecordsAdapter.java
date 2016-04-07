package net.oukranos.ophiuchus.types;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import net.oukranos.ophiuchus.R;
import net.oukranos.ophiuchus.utils.HealthDbHelper;
import net.oukranos.ophiuchus.utils.Logger;

/**
 * Created by francis on 3/26/16.
 */
public class HealthDbRecordsAdapter extends CursorAdapter {

    public HealthDbRecordsAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View targetView;
        try {
            int colId = cursor.getColumnIndex(HealthDbHelper.C_ID);
            int colStatus = cursor.getColumnIndex(HealthDbHelper.C_STATUS);
            int colContent = cursor.getColumnIndex(HealthDbHelper.C_CONTENT);
            int colAudioFile = cursor.getColumnIndex(HealthDbHelper.C_AUDIO_FILE);

            int iRecId = cursor.getInt(colId);
            String content = cursor.getString(colContent);
            String filename = cursor.getString(colAudioFile);
            String status = cursor.getString(colStatus);

            HealthRecord record = new HealthRecord(content);

            LayoutInflater inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            targetView = inflater.inflate(R.layout.list_database_item, parent, false);

            TextView txvId = (TextView) targetView.findViewById(R.id.txv_id);
            txvId.setText(String.valueOf(iRecId));

            TextView txvFileName = (TextView) targetView.findViewById(R.id.txv_file_name);
            txvFileName.setText(filename);

            TextView txvStatus = (TextView) targetView.findViewById(R.id.txv_sent_status);
            txvStatus.setText("Status: " + status);

            TextView txvContent = (TextView) targetView.findViewById(R.id.txv_content);
            txvContent.setText(record.toString());


        } catch (Exception e) {
            Logger.err("Exception occurred: " + e.getMessage());
            return new View(context);
        }

        return targetView;
    }

    @Override
    public void bindView(View targetView, Context context, Cursor cursor) {
        try {
            int colId = cursor.getColumnIndex(HealthDbHelper.C_ID);
            int colStatus = cursor.getColumnIndex(HealthDbHelper.C_STATUS);
            int colContent = cursor.getColumnIndex(HealthDbHelper.C_CONTENT);
            int colAudioFile = cursor.getColumnIndex(HealthDbHelper.C_AUDIO_FILE);

            int iRecId = cursor.getInt(colId);
            String content = cursor.getString(colContent);
            String filename = cursor.getString(colAudioFile);
            String status = cursor.getString(colStatus);

            HealthRecord record = new HealthRecord(content);

            TextView txvId = (TextView) targetView.findViewById(R.id.txv_id);
            txvId.setText(String.valueOf(iRecId));

            TextView txvFileName = (TextView) targetView.findViewById(R.id.txv_file_name);
            txvFileName.setText(filename);

            TextView txvStatus = (TextView) targetView.findViewById(R.id.txv_sent_status);
            txvStatus.setText("Status: " + status);

            TextView txvContent = (TextView) targetView.findViewById(R.id.txv_content);
            txvContent.setText(record.toString());

        } catch (Exception e) {
            Logger.err("Exception occurred: " + e.getMessage());
            return;
        }

        return;
    }
}
