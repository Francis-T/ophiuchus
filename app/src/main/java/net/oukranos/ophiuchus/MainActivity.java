package net.oukranos.ophiuchus;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.internal.http.multipart.FilePart;
import com.android.internal.http.multipart.MultipartEntity;
import com.android.internal.http.multipart.Part;
import com.android.internal.http.multipart.StringPart;

import net.oukranos.ophiuchus.types.HealthDb;
import net.oukranos.ophiuchus.utils.AppUtils;
import net.oukranos.ophiuchus.utils.HealthDbHelper;
import net.oukranos.ophiuchus.utils.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private UploadRecordsTask _currentUploadTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnNewRecord = (Button) findViewById(R.id.btn_new_record);
        btnNewRecord.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, NewRecordActivity.class);
                        startActivity(intent);
                    }
                }
        );

        Button btnUploadData = (Button) findViewById(R.id.btn_server_upload);
        btnUploadData.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showConfirmUploadDialog();
                        return;
                    }
                }
        );

        Button btnBrowse = (Button) findViewById(R.id.btn_browse_records);
        btnBrowse.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, DataBrowserActivity.class);
                        startActivity(intent);
                        return;
                    }
                }
        );

        Button btnSetServer = (Button) findViewById(R.id.btn_set_remote_server);
        btnSetServer.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showSetRemoteServerDialog();
                        return;
                    }
                }
        );

        OphiuchusApp app = AppUtils.getApplication(this);
        String uploadUrl = app.getUploadServerUrl();
        if (uploadUrl.equals("")) {
            showSetRemoteServerDialog();
        }

        return;
    }

    @Override
    protected void onResume() {
        super.onResume();

        OphiuchusApp app = AppUtils.getApplication(this);
        String mqFilePath = app.getMedicalQueryFilePath();
        if (mqFilePath.equals("")) {
            updateButtonToDownloadMedicalQueryFile();
        } else {
            updateButtonToSetMedicalQueryFile();
        }

        return;
    }

    private void updateButtonToDownloadMedicalQueryFile() {
        Button btnSetMedicalQueryFile = (Button) findViewById(R.id.btn_set_medical_query_file);
        btnSetMedicalQueryFile.setText("Download Medical Query File");
        btnSetMedicalQueryFile.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new DownloadQueryFileTask().execute();
                        return;
                    }
                }
        );

        return;
    }

    private void updateButtonToSetMedicalQueryFile() {
        Button btnSetMedicalQueryFile = (Button) findViewById(R.id.btn_set_medical_query_file);
        btnSetMedicalQueryFile.setText("Set Medical Query File Path");
        btnSetMedicalQueryFile.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showSetMedicalQueryFilePathDialog();
                        return;
                    }
                }
        );
        btnSetMedicalQueryFile.setOnLongClickListener(
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        new DownloadQueryFileTask().execute();
                        return true;
                    }
                }
        );

        return;
    }

    private void showConfirmUploadDialog() {
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);

        dlgBuilder.setTitle("Upload to Server")
                .setMessage("This will upload the contents of the local database to the remote server. Do you wish to proceed?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (_currentUploadTask != null) {
                            display("A previous upload task is still ongoing!");
                            dialog.cancel();
                            return;
                        }

                        _currentUploadTask = new UploadRecordsTask();
                        _currentUploadTask.execute();

                        display("Server upload started");
                        dialog.cancel();

                        /* TODO Start Progress Dialog instead */

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

    private void showSetMedicalQueryFilePathDialog() {
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

                        OphiuchusApp app = AppUtils.getApplication(MainActivity.this);
                        app.setMedicalQueryFilePath(pathStr);

                        if (pathStr.equals("")) {
                            updateButtonToDownloadMedicalQueryFile();
                        }

                        display("File path set to " + pathStr);
                        dialog.cancel();

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

    private void showSetRemoteServerDialog() {
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);

        final EditText edtInput = new EditText(this);
        OphiuchusApp app = AppUtils.getApplication(this);
        edtInput.setText(app.getUploadServerUrl());

        dlgBuilder.setTitle("Set Remote Server")
                .setMessage(
                        "Enter the URL of the remote server you want to use for data uploads. "
                )
                .setView(edtInput)
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String urlStr = edtInput.getText().toString();

                        OphiuchusApp app = AppUtils.getApplication(MainActivity.this);
                        app.setUploadServerUrl(urlStr);

                        display("Server URL set to " + urlStr);
                        dialog.cancel();

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

    private class DownloadQueryFileTask extends AsyncTask<Void, String, Void> {
        private final HttpClient _httpClient = new DefaultHttpClient();
        private String _downloadUrl = "";
        private static final String DOWNLOAD_URL_SUFFIX = "/download/queries/medical";

        @Override
        protected Void doInBackground(Void... params) {
            OphiuchusApp app = AppUtils.getApplication(MainActivity.this);
            _downloadUrl = app.getUploadServerUrl();

            if (_downloadUrl.equals("")) {
                publishProgress("Remote server url not yet set!");
                return null;
            }
            _downloadUrl +=  DOWNLOAD_URL_SUFFIX;

            publishProgress("Attempting to download query file from " + _downloadUrl);
            try {
                HttpGet request = new HttpGet(_downloadUrl);
                HttpResponse response = _httpClient.execute(request);

                String storagePath = AppUtils.getStoragePath();
                AppUtils.prepareDirectory(storagePath);
                AppUtils.saveToFile(storagePath, "queries.json", false,
                        EntityUtils.toByteArray(response.getEntity()));

                /* Automagically set our medical query file path afterwards */
                app.setMedicalQueryFilePath(storagePath + "queries.json");
            } catch (Exception e) {
                Logger.err("Exception occurred: " + e.getMessage());
                publishProgress("Failed to download medical query file");
                return null;
            }
            publishProgress("Finished downloading medical query file");

            return null;
        }

        @Override
        protected void onProgressUpdate(String... msg) {
            display(msg[0]);
            return;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateButtonToSetMedicalQueryFile();
            return;
        }
    }

    private class UploadRecordsTask extends AsyncTask<Void, String, Void> {
        private final HttpClient _httpClient = new DefaultHttpClient();
        private String _uploadUrl = "";
        private static final String UPLOAD_URL_SUFFIX = "/upload";

        @Override
        protected Void doInBackground(Void... params) {
            OphiuchusApp app = AppUtils.getApplication(MainActivity.this);
            _uploadUrl = app.getUploadServerUrl();

            if (_uploadUrl.equals("")) {
                publishProgress("Remote server url not yet set!");
                return null;
            }
            _uploadUrl +=  UPLOAD_URL_SUFFIX;

            HealthDb hdb = HealthDb.getInstance(MainActivity.this);

            int iTotalUnsent = hdb.countUnsentRecords();
            if (iTotalUnsent <= 0) {
                publishProgress("No records to send");
                return null;
            }

            int iUploadCounter = 1;

            Cursor c = hdb.getUnsentHealthDataRecordCursor();
            c.moveToFirst();
            while (true) {
                publishProgress("Processing record #" + iUploadCounter + " of " + iTotalUnsent);

                if (uploadRecord(c)) {
                    /* If record upload was successful */

                    /* Get the ID of the successfully uploaded record */
                    int iIdCol = c.getColumnIndex(HealthDbHelper.C_ID);
                    int iId = c.getInt(iIdCol);

                    /* Modify it's status in the database to "Sent" */
                    hdb.setHealthRecordStatus(iId, "Sent");
                }

                iUploadCounter++;

                if (!c.moveToNext()) {
                    break;
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... msg) {
            display(msg[0]);
            return;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            _currentUploadTask = null;
            display("Server uploads finished!");
            super.onPostExecute(aVoid);
        }

        private boolean uploadRecord(Cursor c) {

            int iColContent = c.getColumnIndex(HealthDbHelper.C_CONTENT);
            int iColFilename = c.getColumnIndex(HealthDbHelper.C_AUDIO_FILE);

            String deviceId = AppUtils.getDeviceId(MainActivity.this);
            String content = c.getString(iColContent);
            String filename = c.getString(iColFilename) + ".amr";
            File audioFile = new File(AppUtils.getStoragePath(), filename);
            if (!audioFile.exists()) {
                Logger.err("Audio file does not exist: " +filename);
                return false;
            }

            HttpPost httpPost = new HttpPost(_uploadUrl);

            StringPart deviceIdPart = new StringPart("device_id", deviceId);
            StringPart contentPart = new StringPart("content", content);
            FilePart audioFilePart;
            try {
                audioFilePart = new FilePart("filename", audioFile);
            } catch (Exception e) {
                Logger.err("Exception occurred for " + filename + ": " + e.getMessage());
                return false;
            }

            Part parts[] = { deviceIdPart, contentPart, audioFilePart };
            HttpEntity multipartEntity = new MultipartEntity(parts);

            httpPost.setEntity(multipartEntity);

            HttpResponse httpResp;
            try {
                httpResp = _httpClient.execute(httpPost);
            } catch (Exception e) {
                Logger.err("Exception occurred during HTTP POST attempt: " + e.getMessage());
                return false;
            }

            int iStatusCode = httpResp.getStatusLine().getStatusCode();
            if (iStatusCode >= 300) {
                Logger.err("HTTP POST failed: " + iStatusCode);
                return false;
            }

            return true;
        }

    }

    private void display(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        return;
    }
}
