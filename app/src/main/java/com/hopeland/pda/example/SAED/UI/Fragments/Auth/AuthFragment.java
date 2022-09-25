package com.hopeland.pda.example.SAED.UI.Fragments.Auth;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.hopeland.pda.example.R;
import com.hopeland.pda.example.SAED.AppConfig.FC;
import com.hopeland.pda.example.SAED.AppConfig.FN;
import com.hopeland.pda.example.SAED.AppConfig.SharedPreference;
import com.hopeland.pda.example.SAED.Helpers.View.FTH;
import com.hopeland.pda.example.SAED.UI.Activities.AuthActivity;
import com.hopeland.pda.example.uhf.ClientActivity;

import java.util.Locale;


public class AuthFragment extends Fragment {

    Button login;
    Button guest;
    Spinner languageSpinner;
    TextView languageIcon;

    int mSpinnerCheck = 0;

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_auth, container, false);

        initView();

        listeners();

        return view;
    }

    private void initView() {
        login = view.findViewById(R.id.login);
        guest = view.findViewById(R.id.guest);
        languageSpinner = view.findViewById(R.id.language);
        languageIcon = view.findViewById(R.id.textView5);
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

//        languageSpinner.setOnItemSelectedListener(languageListener);
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

//    private final AdapterView.OnItemSelectedListener languageListener =
//            new AdapterView.OnItemSelectedListener() {
//        @Override
//        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//            switch (i) {
//
//                case 0:
//                    ((TextView) view).setTextColor(getResources().getColor(R.color.gray));
//                    break;
//
//                case 1: {
//                    setLanguage("ar");
//                    break;
//                }
//
//                case 2: {
//                    setLanguage("en");
//                    break;
//                }
//            }
//        }
//
//        @Override
//        public void onNothingSelected(AdapterView<?> adapterView) {
//
//        }
//    };

    void startClientActivity() {
        Intent intent = new Intent(requireActivity(), ClientActivity.class);
        requireActivity().startActivity(intent);
        requireActivity().finish();
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