package com.sam07205.nav230131;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextInputLayout textInputAccount;
    TextInputLayout textInpPassword;

    TextWatcher mTextWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnLogin).setOnClickListener(this);
        findViewById(R.id.btnForgetPassword).setOnClickListener(this);

        textInputAccount = findViewById(R.id.inp_account);
        textInpPassword = findViewById(R.id.inp_password);

        EditText editPassword = textInpPassword.getEditText();
        assert editPassword != null;
        editPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                login();
                return false;
            }
        });

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnLogin) {
            login();
        } else if (v.getId() == R.id.btnForgetPassword) {

        }
    }

    void login() {
        String inpAccount = String.valueOf(Objects.requireNonNull(textInputAccount.getEditText()).getText());
        String inpPassword = String.valueOf(Objects.requireNonNull(textInpPassword.getEditText()).getText());
        textInputAccount.clearFocus();
        textInpPassword.clearFocus();
        if (inpAccount.equals("") || inpPassword.equals("")) {
            Toast.makeText(MainActivity.this, "輸入不得為空", Toast.LENGTH_SHORT).show();
            textInpPassword.getEditText().setText("");
            return;
        }

        Log.i("OUTP", String.format("%s %s", inpAccount, inpPassword));
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, ListActive.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("account", inpAccount);
        startActivity(intent);
    }


}