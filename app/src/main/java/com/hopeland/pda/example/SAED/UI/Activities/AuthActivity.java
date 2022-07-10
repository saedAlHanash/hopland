package com.hopeland.pda.example.SAED.UI.Activities;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.hopeland.pda.example.AppConfig.FC;
import com.hopeland.pda.example.Helpers.View.FTH;
import com.hopeland.pda.example.R;
import com.hopeland.pda.example.SAED.UI.Fragments.Auth.AuthFragment;

import butterknife.ButterKnife;

public class AuthActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);

        FTH.replaceFadFragment(FC.AUTH_C, this, new AuthFragment());
    }

}


//    void setLanguage(String lang) {
//        Resources resources = this.getResources();
//        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
//        Configuration config = resources.getConfiguration();
//        Locale locale = new Locale(lang.toLowerCase());
//        Locale.setDefault(locale);
//        config.setLocale(locale);
//        resources.updateConfiguration(config, displayMetrics);
//    }