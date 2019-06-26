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
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.github.felixgail.gplaymusic.util.TokenProvider;

import java.io.IOException;

import svarzee.gps.gpsoauth.AuthToken;
import svarzee.gps.gpsoauth.Gpsoauth;

public class LoginActivity extends AppCompatActivity {

    private static final Uri sUri = Uri.parse("content://com.google.android.gsf.gservices");

    private CheckBox checkBoxPass;
    private CheckBox checkBoxAid;
    private EditText editTextUser;
    private EditText editTextPass;
    private EditText editTextAndroid;
    private Button button;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_login);

        checkBoxPass = findViewById(R.id.pass_check);
        checkBoxAid = findViewById(R.id.id_check);
        editTextUser = findViewById(R.id.user);
        editTextPass = findViewById(R.id.pass);
        editTextAndroid = findViewById(R.id.android_id);
        button = findViewById(R.id.login_bt);

        editTextUser.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        checkBoxPass.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            int selectionStart = editTextPass.getSelectionStart();
            if (isChecked)
                editTextPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            else
                editTextPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
            editTextPass.setSelection(selectionStart);
        });

        checkBoxAid.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (isChecked)
                editTextAndroid.setVisibility(View.VISIBLE);
            else
                editTextAndroid.setVisibility(View.GONE);
        });


        button.setOnClickListener((View v) -> {
            if (checkBoxAid.isChecked()) {
                if (!editTextUser.getText().toString().equals("") && !editTextPass.getText().toString().equals("") && !editTextAndroid.getText().toString().equals("")) {

                    InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    im.hideSoftInputFromWindow(editTextPass.getWindowToken(), 0);

                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    ViewGroup vView = (ViewGroup) getLayoutInflater().inflate(R.layout.ad_loading, null);
                    builder.setView(vView);
                    builder.setCancelable(false);
                    final AlertDialog alert = builder.create();
                    alert.show();

                    TokenGen tokenGen = new TokenGen(editTextUser.getText().toString(), editTextPass.getText().toString(), editTextAndroid.getText().toString(), alert);
                    tokenGen.execute();

                } else {
                    (new AlertDialog.Builder(LoginActivity.this)).setMessage(getString(R.string.login_alert_fill_in)).setPositiveButton("OK", null).create().show();
                }

            } else {
                if (!editTextUser.getText().toString().equals("") && !editTextPass.getText().toString().equals("")) {

                    InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    im.hideSoftInputFromWindow(editTextPass.getWindowToken(), 0);

                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    ViewGroup vView = (ViewGroup) getLayoutInflater().inflate(R.layout.ad_loading, null);
                    builder.setView(vView);
                    builder.setCancelable(false);
                    final AlertDialog alert = builder.create();
                    alert.show();

                    TokenGen tokenGen = new TokenGen(editTextUser.getText().toString(), editTextPass.getText().toString(), getGSFID(getApplicationContext()), alert);
                    tokenGen.execute();

                } else {
                    (new AlertDialog.Builder(LoginActivity.this)).setMessage(getString(R.string.login_alert_fill_in)).setPositiveButton("OK", null).create().show();
                }
            }

        });
    }

    private void loginSuccessful(String stringRet) {
        Intent returnData = new Intent();
        returnData.putExtra(getString(R.string.token), stringRet);
        setResult(HomeActivity.LOGIN_ACTIVITY);
        setResult(RESULT_OK, returnData);
        finish();
    }

    private void loginError() {
        (new AlertDialog.Builder(LoginActivity.this)).setMessage(getString(R.string.login_alert_error)).setPositiveButton(getString(R.string.box_ok), (DialogInterface dialog, int which) -> {
            editTextPass.setText("");
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }).create().show();
    }

    @Override
    public void onBackPressed() {
        (new AlertDialog.Builder(LoginActivity.this)).setMessage(getString(R.string.ask_exit)).setPositiveButton(getString(R.string.box_exit), (DialogInterface dialog, int which) -> {
            setResult(RESULT_CANCELED);
            finish();
        }).setNegativeButton(getString(R.string.box_cancel), null).create().show();
    }

    private class TokenGen extends AsyncTask<Void, Void, Void> {

        private AuthToken authToken = null;

        private String USERNAME;
        private String PASSWORD;
        private String ANDROIDID;

        AlertDialog alertDialog;

        TokenGen(String username, String password, String androidID, AlertDialog alertDialog) {
            super();
            this.USERNAME = username;
            this.PASSWORD = password;
            this.ANDROIDID = androidID;
            this.alertDialog = alertDialog;

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
            alertDialog.cancel();
            if (authToken != null)
                loginSuccessful(authToken.getToken());
            else
                loginError();
        }

    }

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
