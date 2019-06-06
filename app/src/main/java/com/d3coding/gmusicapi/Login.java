package com.d3coding.gmusicapi;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.github.felixgail.gplaymusic.util.TokenProvider;

import java.io.IOException;

import svarzee.gps.gpsoauth.AuthToken;
import svarzee.gps.gpsoauth.Gpsoauth;

public class Login extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        Toolbar mToolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);

        CheckBox checkBox = findViewById(R.id.id_check);
        EditText editTextUser = findViewById(R.id.user);
        EditText editTextPass = findViewById(R.id.pass);
        EditText editTextAndroid = findViewById(R.id.android_id);
        Button button = findViewById(R.id.login_bt);

        editTextUser.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editTextAndroid.setVisibility(View.VISIBLE);
                } else {
                    editTextAndroid.setVisibility(View.GONE);
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkBox.isChecked()) {
                    if (!editTextUser.getText().equals("") && !editTextPass.getText().equals("")) {

                        TokenGen tokenGen = new TokenGen(editTextUser.getText().toString(), editTextPass.getText().toString(), getGSFID(getApplicationContext()));
                        tokenGen.execute();

                    } else {
                        (new AlertDialog.Builder(Login.this)).setMessage(getString(R.string.login_alert_fill_in)).setPositiveButton("OK", null).create().show();
                    }
                } else {
                    if (!editTextUser.getText().equals("") && !editTextPass.getText().equals("") && !editTextAndroid.getText().equals("")) {

                        TokenGen tokenGen = new TokenGen(editTextUser.getText().toString(), editTextPass.getText().toString(), editTextAndroid.getText().toString());
                        tokenGen.execute();

                    } else {
                        (new AlertDialog.Builder(Login.this)).setMessage(getString(R.string.login_alert_fill_in)).setPositiveButton("OK", null).create().show();
                    }
                }

            }
        });


    }

    private class TokenGen extends AsyncTask<Void, Void, Void> {

        private AuthToken authToken = null;

        private String USERNAME;
        private String PASSWORD;
        private String ANDROIDID;

        public TokenGen(String username, String password, String androidID) {
            super();
            this.USERNAME = username;
            this.PASSWORD = password;
            this.ANDROIDID = androidID;

        }

        protected Void doInBackground(Void... params) {
            try {
                authToken = TokenProvider.provideToken(USERNAME, PASSWORD, ANDROIDID);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Gpsoauth.TokenRequestFailed e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (authToken != null)
                myMethod(authToken.getToken());
            else
                myMethod2();
        }
    }

    private void myMethod(String stringRet) {
        Intent returnData = new Intent();
        returnData.putExtra(getString(R.string.token), stringRet);
        setResult(RESULT_OK, returnData);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        finish();
    }

    private static final int RESULT_ERROR = -4;

    private void myMethod2() {
        setResult(RESULT_ERROR);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        finish();
    }

    @Override
    public void onBackPressed() {
        (new AlertDialog.Builder(Login.this)).setMessage(getString(R.string.ask_exit)).setPositiveButton(getString(R.string.box_exit),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                }
        ).setNegativeButton(android.R.string.cancel, null).create().show();
    }

    private static final Uri sUri = Uri.parse("content://com.google.android.gsf.gservices");

    public static String getGSFID(Context context) {
        try {
            Cursor query = context.getContentResolver().query(sUri, null, null, new String[]{"android_id"}, null);
            if (query == null) {
                return "Not found";
            }
            if (!query.moveToFirst() || query.getColumnCount() < 2) {
                query.close();
                return "Not found";
            }
            final String toHexString = Long.toHexString(Long.parseLong(query.getString(1)));
            query.close();
            return toHexString.toUpperCase().trim();
        } catch (SecurityException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e2) {
            e2.printStackTrace();
            return null;
        }
    }


}
