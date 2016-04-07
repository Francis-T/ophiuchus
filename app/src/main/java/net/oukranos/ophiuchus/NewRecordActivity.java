package net.oukranos.ophiuchus;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import net.oukranos.ophiuchus.types.HealthDb;
import net.oukranos.ophiuchus.utils.AppUtils;
import net.oukranos.ophiuchus.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NewRecordActivity extends AppCompatActivity {
    private List<String> _listHealthInfo;
    private ArrayAdapter<String> _adapter;
    private String _healthRecordId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_record);

        /* Get our next HealthRecordId */
        HealthDb healthDb = HealthDb.getInstance(this);
        _healthRecordId = String.valueOf(healthDb.getNextRecordId());

        Button btnEditHealthRecord = (Button) findViewById(R.id.btn_edit_record);
        btnEditHealthRecord.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(NewRecordActivity.this,
                                EditHealthRecordActivity.class);
                        startActivity(intent);
                    }
                }
        );

        Button btnAudioRecord = (Button) findViewById(R.id.btn_audio_record);
        btnAudioRecord.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(NewRecordActivity.this, AudioRecordActivity.class);
                        intent.putExtra("AUDIO_FILENAME", getAudioFilename());
                        startActivity(intent);
                    }
                }
        );

        Button btnDatabaseSave = (Button) findViewById(R.id.btn_database_save);
        btnDatabaseSave.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showConfirmDbSaveDialog();
                        return;
                    }
                }
        );

        _listHealthInfo = new ArrayList<String>();

        GridView gridInfo = (GridView) findViewById(R.id.grid_info);
        _adapter = new ArrayAdapter<String>(this, R.layout.grid_info_item, _listHealthInfo);
        gridInfo.setAdapter(_adapter);

        /* Set the visible health record id */
        TextView txvRecordId = (TextView) findViewById(R.id.txv_record_id);
        txvRecordId.setText("Health Record #" + _healthRecordId);

        return;
    }

    @Override
    protected void onResume() {
        super.onResume();

        OphiuchusApp app = AppUtils.getApplication(this);
        if (app == null) {
            Logger.err("Error: Could not get base application");
            return;
        }

        String healthRecordStr = app.retrieveCachedHealthRecord();
        if (healthRecordStr.equals("")) {
            return;
        }

        parseHealthRecordData(healthRecordStr, _listHealthInfo);
        _adapter.notifyDataSetChanged();

        return;
    }

    /* PRIVATE METHODS */
    private String getAudioFilename() {
        OphiuchusApp app = AppUtils.getApplication(this);
        String oldFilename = app.retrieveCachedAudioFilename();

        /* If the old filename isn't blank, then reuse it */
        if (!oldFilename.equals("")) {
            return oldFilename;
        }

        return "OPhData_" + AppUtils.getDeviceId(NewRecordActivity.this) + "_" + _healthRecordId;

    }

    private void parseHealthRecordData(String data, List<String> container) {
        if (container == null) {
            return;
        }

        /* Clear the container */
        container.clear();

        try {
            JSONObject jsonObject = new JSONObject(data);
            JSONArray detailInfo;
            String value;

            detailInfo = jsonObject.getJSONArray("general info");
            for (int iIdx = 0; iIdx < detailInfo.length(); iIdx++) {
                JSONObject arrObject = detailInfo.getJSONObject(iIdx);
                container.add(arrObject.getString("name").replace("_", " "));

                value = arrObject.getString("value") + " " + arrObject.getString("suffix");
                if (value.length() > 10) {
                    value = value.substring(0,10);
                }
                container.add(value);
            }

            detailInfo = jsonObject.getJSONArray("medical history");
            for (int iIdx = 0; iIdx < detailInfo.length(); iIdx++) {
                JSONObject arrObject = detailInfo.getJSONObject(iIdx);
                container.add(arrObject.getString("name").replace("_", " "));

                value = arrObject.getString("value") + " " + arrObject.getString("suffix");
                if (value.length() > 10) {
                    value = value.substring(0,10);
                }
                container.add(value);
            }

            detailInfo = jsonObject.getJSONArray("checkup details");
            for (int iIdx = 0; iIdx < detailInfo.length(); iIdx++) {
                JSONObject arrObject = detailInfo.getJSONObject(iIdx);
                container.add(arrObject.getString("name").replace("_", " "));

                value = arrObject.getString("value") + " " + arrObject.getString("suffix");
                if (value.length() > 10) {
                    value = value.substring(0,10);
                }
                container.add(value);
            }
        } catch (JSONException e) {
            Logger.err("JSONException Occurred: " + e.getMessage());
            return;
        } catch (Exception e) {
            Logger.err("Exception Occurred: " + e.getMessage());
            return;
        }
        return;
    }

    private void showConfirmDbSaveDialog() {
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);

        dlgBuilder.setTitle("Save Health Record")
                .setMessage("This will save the current health record to the database. Do you wish to proceed?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        OphiuchusApp app = AppUtils.getApplication(NewRecordActivity.this);
                        String content = app.retrieveCachedHealthRecord();
                        String filename = app.retrieveCachedAudioFilename();

                        if (content.equals("")) {
                            display("Health record info not found!");
                            dialog.cancel();
                            return;
                        }

                        if (filename.equals("")) {
                            display("Audio file not found!");
                            dialog.cancel();
                            return;
                        }

                        HealthDb hdb = HealthDb.getInstance(NewRecordActivity.this);
                        if (!hdb.storeHealthRecord(content, filename)) {
                            Logger.err("Failed to store health record in database");
                        }

                        /* Clear the previously cached record once it has been saved to the DB */
                        app.clearCachedHealthRecord();
                        app.clearCachedAudioFilename();

                        display("Health record saved to database!");
                        finish();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
        });

        /* Show the dialog box */
        dlgBuilder.create().show();


        return;
    }

    private void display(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        return;
    }
}
