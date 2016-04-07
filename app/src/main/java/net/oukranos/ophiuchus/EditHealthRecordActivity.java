package net.oukranos.ophiuchus;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.Toast;

import net.oukranos.ophiuchus.types.AnswerableControlAdapter;
import net.oukranos.ophiuchus.types.AnswerableControlData;
import net.oukranos.ophiuchus.types.HealthDb;
import net.oukranos.ophiuchus.utils.AppUtils;
import net.oukranos.ophiuchus.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EditHealthRecordActivity extends AppCompatActivity {
    private AnswerableControlAdapter _adapter = null;
    private ExpandableListView _list = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_health_record);

        /* Get our next HealthRecordId */
        HealthDb healthDb = HealthDb.getInstance(this);
        String recIdStr = String.valueOf(healthDb.getNextRecordId());

        /* TODO All data assignment should be pulled from elsewhere, not hardcoded */
        OphiuchusApp app = AppUtils.getApplication(this);
        if (app.getMedicalQueryFilePath().equals("")) {
            showSetMedicalQueryFilePathDialog(recIdStr);
        } else {
            if (!setupListAdapter(recIdStr)) {
                finish();
                return;
            }
            /* TODO If we already have data cached from before, then now is the time to give
               them back to the AnswerableControlData objects */
        }


        ImageButton btnSave = (ImageButton) findViewById(R.id.btn_save);
        btnSave.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveData();
                        return;
                    }
                }
        );

        ImageButton btnCancel = (ImageButton) findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }
        );

        return;
    }

    @Override
    public void onBackPressed() {
        if (_adapter == null) {
            super.onBackPressed();
            return;
        }

        if (_adapter.isDataChanged()) {
            startSaveRecordDialog();
        } else {
            super.onBackPressed();
        }

        return;
    }

    private boolean setupListAdapter(String recIdStr) {
        /* Pull our medical data queries from a file */
        List<String> groupList = new ArrayList<>();
        List<AnswerableControlData> dataList = new ArrayList<>();
        if (!loadMedicalQueries(groupList, dataList)) {
            return false;
        }

        /* Prepare the List Adapter */
        _adapter = new AnswerableControlAdapter(this, recIdStr, dataList, groupList);
        _list = (ExpandableListView) findViewById(R.id.list_queries);
        _list.setOnGroupExpandListener(
                new ExpandableListView.OnGroupExpandListener() {
                    @Override
                    public void onGroupExpand(int groupPosition) {
                        for (int iIdx = 0; iIdx < _list.getCount(); iIdx++) {
                            if (iIdx != groupPosition) {
                                _list.collapseGroup(iIdx);
                            }
                        }
                        return;
                    }
                }
        );
        _list.setAdapter(_adapter);

        /* Expand the first group */
        _list.expandGroup(0);

        return true;
    }

    private void saveData() {
        if (_adapter == null) {
            return;
        }

        String healthRec = _adapter.getJsonString();
        OphiuchusApp app = AppUtils.getApplication(EditHealthRecordActivity.this);
        app.cacheHealthRecord(healthRec);

        String storagePath = AppUtils.getStoragePath() + "health_data/";
        AppUtils.prepareDirectory(storagePath);
        AppUtils.saveToFile(storagePath, "health.txt", false, healthRec.getBytes());
        return;
    }

    private void startSaveRecordDialog() {
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);

        dlgBuilder.setTitle("Warning")
                .setMessage("Unsaved health record data will be lost when you leave this screen. " +
                            "Would you like to save the data?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveData();
                        finish();
                        return;
                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        return;
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                        return;
                    }
                });

        dlgBuilder.create().show();

        return;
    }

    private boolean loadMedicalQueries(List<String> groupList, List<AnswerableControlData> dataList) {
        OphiuchusApp app = AppUtils.getApplication(this);
        String mqFilePath = app.getMedicalQueryFilePath();
        if (mqFilePath.equals("")) {
            display("Could not find file containing medical queries");
            return false;
        }

        String contents = AppUtils.getFileContentString(mqFilePath);
        if (contents == null) {
            display("Failed to read medical query file: " + mqFilePath);
            return false;
        }

        Logger.info("Contents: \n>" + contents);

        try {
            JSONObject queriesObj = new JSONObject(contents);

            JSONArray groupArr = queriesObj.getJSONArray("groups");
            for (int iIdx = 0; iIdx < groupArr.length(); iIdx++) {
                groupList.add(groupArr.getString(iIdx));
            }

            JSONArray controlsArr = queriesObj.getJSONArray("controls");
            for (int iIdx = 0; iIdx < controlsArr.length(); iIdx++) {
                JSONObject controlObj = controlsArr.getJSONObject(iIdx);

                String name = controlObj.getString("name");
                String label = controlObj.getString("label");
                String controlType = controlObj.getString("control_type");
                String dataType = controlObj.getString("data_type");
                String group = controlObj.getString("group");

                String restrictions = controlObj.optString("restrictions");
                String suffix = controlObj.optString("suffix");
                String value = controlObj.optString("value");

                AnswerableControlData controlData =
                        new AnswerableControlData(name, label);
                controlData.setControlType(controlType);
                controlData.setDataType(dataType);
                controlData.setGroup(group);
                controlData.setRestrictions(restrictions);
                controlData.setSuffix(suffix);
                controlData.setValue(value);

                dataList.add(controlData);
            }
        } catch (JSONException e) {
            Logger.err("JSONException occurred while trying to read medical query file: " +
                    mqFilePath);
            e.printStackTrace();
            display("Failed to load medical query file: " + mqFilePath);
            return false;
        }

        return true;
    }

    private void showSetMedicalQueryFilePathDialog(final String recIdStr) {
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);

        final EditText edtInput = new EditText(this);
        OphiuchusApp app = AppUtils.getApplication(this);
        edtInput.setText(app.getMedicalQueryFilePath());

        dlgBuilder.setTitle("Set Medical Query File")
                .setMessage(
                        "Enter the file path to the medical query file on this device."
                )
                .setView(edtInput)
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String pathStr = edtInput.getText().toString();

                        OphiuchusApp app = AppUtils.getApplication(EditHealthRecordActivity.this);
                        app.setMedicalQueryFilePath(pathStr);

                        display("File path set to " + pathStr);

                        /* Re-attempt to setup our list adapter */
                        if (!setupListAdapter(recIdStr)) {
                            display("Invalid medical query file: " + pathStr);
                        } else {
                            dialog.cancel();
                        }

                        return;
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }
        );

        /* Show the dialog box */
        dlgBuilder.create().show();

        return;
    }

    private void display(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        return;
    }
}
