package aenadon.viruscomplete;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import java.io.File;

public class ScanFileSend extends AppCompatActivity {

    EditText selectFileBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_file_send);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        selectFileBox = (EditText) findViewById(R.id.selectFileBox);

    }

    public void pickFile(View view) { // On SDK >= 23, we need to politely ask for permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) { // If >Lollipop AND permission not granted...
            askPolitelyForPermission();
        } else {
            chooseFile();
        }
    }

    public void testChecksum(View view) {
        String fileToCheck = selectFileBox.getText().toString();
        if (fileToCheck.isEmpty()) {
            Toast.makeText(ScanFileSend.this, getString(R.string.error_select_file), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(ScanFileSend.this, C.getMD5(fileToCheck), Toast.LENGTH_LONG).show();
        }
    }

    private void askPolitelyForPermission() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.permission_request_title))
                .setMessage(getString(R.string.permission_request_message)) // explain why we need permission and then request it
                .setPositiveButton(getString(R.string.permission_request_button), new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(ScanFileSend.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, C.REQUEST_FILE_ACCESS);
                    }
                })
                .show();
    }

    // SDK >= 23: After permission was granted/denied, this is called
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        // https://developer.android.com/training/permissions/requesting.html
        switch (requestCode) {
            case C.REQUEST_FILE_ACCESS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, getString(R.string.permission_granted_thanks), Toast.LENGTH_SHORT).show();
                    chooseFile();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.permission_denied_title))
                            .setMessage(getString(R.string.permission_denied_message))
                            .setPositiveButton(getString(R.string.try_again), null)
                            .show();
                }
            }
        }
    }

    private void chooseFile() {
        // file picker library
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(System.getenv("ANDROID_STORAGE")); // the storage directory shows a selection of all storages attached (int/ext SD, USB-OTG)

        FilePickerDialog filePicker = new FilePickerDialog(this, properties);
        filePicker.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                if (files.length != 0) {
                    selectFileBox.setText(files[0]); // show the selected file to the user
                }
            }
        });
        filePicker.show();

    }
}
