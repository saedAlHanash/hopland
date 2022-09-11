package com.hopeland.pda.example.SAED.UI.Activities;

import android.Manifest;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import com.hopeland.pda.example.R;
import com.hopeland.pda.example.SAED.AppConfig.FC;
import com.hopeland.pda.example.SAED.Helpers.View.FTH;
import com.hopeland.pda.example.SAED.UI.Fragments.Auth.AuthFragment;


public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_PHONE_STATE}, 10);
        FTH.replaceFadFragment(FC.AUTH_C, this, new AuthFragment());
    }

}