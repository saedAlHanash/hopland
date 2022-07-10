package com.hopeland.pda.example.SAED.UI.Fragments.Auth;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.hopeland.pda.example.AppConfig.FC;
import com.hopeland.pda.example.AppConfig.FC;
import com.hopeland.pda.example.AppConfig.FN;
import com.hopeland.pda.example.AppConfig.SharedPreference;
import com.hopeland.pda.example.Helpers.View.FTH;
import com.hopeland.pda.example.R;
import com.hopeland.pda.example.SAED.UI.Activities.AuthActivity;
import com.hopeland.pda.example.SAED.UI.Activities.ClientActivity;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AuthFragment extends Fragment {

    @BindView(R.id.login)
    Button login;
    @BindView(R.id.guest)
    Button guest;
    @BindView(R.id.language)
    Spinner languageSpinner;
    @BindView(R.id.textView5)
    TextView languageIcon;

    int mSpinnerCheck = 0;

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_auth, container, false);
        ButterKnife.bind(this, view);


        listeners();


        return view;
    }


    void initLanguage() {

        if (SharedPreference.getLanguage().equals("en"))
            setLanguage("ar");
        else
            setLanguage("en");
    }

    void listeners() {
        login.setOnClickListener(adminListener);
        guest.setOnClickListener(guestListener);
        languageSpinner.setOnItemSelectedListener(languageListener);

        languageIcon.setOnClickListener(view1 -> {
            languageSpinner.performClick();
        });
    }

    private final View.OnClickListener adminListener = view -> {
        startLoginFragment();
    };

    private final View.OnClickListener guestListener = view -> {
        startClientActivity();
    };


    private final AdapterView.OnItemSelectedListener languageListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            switch (i) {

                case 0:
                    ((TextView) view).setTextColor(getResources().getColor(R.color.gray));
                    break;

                case 1: {
                    setLanguage("ar");
                    break;
                }

                case 2: {
                    setLanguage("en");
                    break;
                }
            }


        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    void startClientActivity() {
        Intent intent = new Intent(requireActivity(), ClientActivity.class);
        startActivity(intent);
    }

    void startLoginFragment() {
        FTH.addToStakeFragment(FC.AUTH_C, requireActivity(), new LoginFragment(), FN.LOGIN_FN);
    }

    void setLanguage(String lang) {

        Locale locale = new Locale(lang);

        SharedPreference.saveLanguage(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        requireActivity().getResources().updateConfiguration(config,
                requireActivity().getResources().getDisplayMetrics());

        Intent refresh = new Intent(requireActivity(), AuthActivity.class);
        refresh.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(refresh);
        requireActivity().finish();
    }

}