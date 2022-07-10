package com.hopeland.pda.example.SAED.UI.Activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.FrameLayout;

import com.hopeland.pda.example.AppConfig.FC;
import com.hopeland.pda.example.Helpers.View.FTH;
import com.hopeland.pda.example.R;
import com.hopeland.pda.example.SAED.UI.Fragments.Client.ClientFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ClientActivity extends AppCompatActivity {


    @BindView(R.id.client_container)
    FrameLayout clientContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        ButterKnife.bind(this);

        startClientFragment();

    }

    void startClientFragment() {
        FTH.replaceFadFragment(FC.CLIENT_C, this, new ClientFragment());
    }

}