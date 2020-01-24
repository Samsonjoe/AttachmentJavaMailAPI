package com.wiz.attachmentjavamailapi;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    //multiple Images Pick
    int PICK_IMAGE_MULTIPLE = 1;
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;

    String uploadFileName;
    private static final String KEY_EMPTY = "";
    ProgressDialog please_wait;
    EditText editTextshowFilePickedPath;
    String FilePickedPathString;
    Uri URI = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(checkPermissionArray(Permissions.PERMISSIONS)){
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        }else{
            verifyPermissions(Permissions.PERMISSIONS);
            Toast.makeText(this, "Please check permissions", Toast.LENGTH_SHORT).show();
        }

        editTextshowFilePickedPath = findViewById(R.id.EdittextClaimFilePath);
        please_wait  = new ProgressDialog(MainActivity.this);

        Button btnChooseClaimFile = findViewById(R.id.buttonChooseFileClaim);
        btnChooseClaimFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,1);
                /*Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"), PICK_IMAGE_MULTIPLE);*/
            }
        });


    Button btn_send = (Button)findViewById(R.id.btn_sendMail);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Retrieve the data entered in the edit texts
                FilePickedPathString = editTextshowFilePickedPath.getText().toString().trim();



                FilePickedPathString = editTextshowFilePickedPath.getText().toString().trim();
                if (validateInputs()) {

                    please_wait.show();
                    // new Thread();
                    //start

                    editTextshowFilePickedPath.getText().clear();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                MailSender sender = new MailSender("your_email",
                                        "your_password");
                                sender.sendMail("ATTACHMENT subject", "This is the test body content",uploadFileName,
                                        "your_email", "email_of person_being emailed");

                                Toast.makeText(MainActivity.this, "SUCCESS", Toast.LENGTH_SHORT).show();
                                please_wait.hide();
                            } catch (Exception e) {
                                Log.e("SendMail", e.getMessage(), e);
                                Toast.makeText(MainActivity.this, "FAILED", Toast.LENGTH_SHORT).show();
                                please_wait.hide();
                            }
                        }

                    }).start();
                    //end
                }
            }
        });


    }

    /**
     * Check an array of permissions
     * @param permissions
     * @return
     */

    public boolean checkPermissionArray(String[] permissions) {
        Log.d(TAG, "checkPermissionArray: checking permissions array.");

        for (int i = 0; i<permissions.length; i++){
            String check = permissions[i];
            if(!checkPermissions(check)){
                return false;
            }
        }
        return true;
    }

    /**
     * Check a single permission it has been verified
     * @param permission
     * @return
     */
    public boolean checkPermissions(String permission) {
        Log.d(TAG, "checkPermissions: checking permission " + permission);

        int permissionRequest = ActivityCompat.checkSelfPermission(MainActivity.this, permission);

        if(permissionRequest != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "checkPermissions: \n Permission wa not granted for: " + permission);
            return false;
        }
        else{
            Log.d(TAG, "checkPermissions: \n permission was granted for: " + permission);
            return true;
        }
    }


/***
     * verify all the permissions passed to the array
     * @param permissions
     */
    private void verifyPermissions(String[] permissions) {
        Log.d(TAG, "verifyPermissions: verifying permissions.");

        ActivityCompat.requestPermissions(
                MainActivity.this,
                permissions,
                VERIFY_PERMISSIONS_REQUEST
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub


        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            Uri selectedImmage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImmage, filePathColumn, null, null, null);
            // columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            uploadFileName = cursor.getString(column_index);
            // Log.e("Attachment Path:",uploadFileName);

            URI = Uri.parse("file://" + uploadFileName);
            cursor.close();


            //uploadFileName =  data.getData().getPath();
            // uploadFilePath = data.getData().getPath();


            editTextshowFilePickedPath.setText(uploadFileName);
            Toast.makeText(MainActivity.this, uploadFileName, Toast.LENGTH_LONG).show();

        }
    }

    /**
     * Validates inputs and shows error if any
     * @return
     */
    private boolean validateInputs() {


        if(KEY_EMPTY.equals(FilePickedPathString)){
            editTextshowFilePickedPath.requestFocus();
            Toast.makeText(MainActivity.this, "Kindly attach a document file", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

}
