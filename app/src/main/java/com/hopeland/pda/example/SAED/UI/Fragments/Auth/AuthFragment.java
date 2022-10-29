package com.hopeland.pda.example.SAED.UI.Fragments.Auth;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

import java.util.ArrayList;
import java.util.Locale;


public class AuthFragment extends Fragment {

    Button login;
    Button guest;
    Spinner languageSpinner;
    TextView languageIcon;
    ArrayAdapter<String> adapter;

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_auth, container, false);

        initView();

        initSpinner();

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

        if (SharedPreference.getLanguage().equals("ar"))
            setLanguage("ar");
        else
            setLanguage("en");
    }

    void listeners() {

        login.setOnClickListener(adminListener);
        guest.setOnClickListener(guestListener);

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {

                    if (SharedPreference.getLanguage().equals("ar"))
                        return;

                    SharedPreference.saveLanguage("ar");
                    initLanguage();

                    return;
                }

                if (position == 1) {

                    if (SharedPreference.getLanguage().equals("en"))
                        return;

                    SharedPreference.saveLanguage("en");
                    initLanguage();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        languageIcon.setOnClickListener(view1 -> {
            languageSpinner.performClick();
        });
    }

    private void initSpinner() {
        ArrayList<String> language = new ArrayList<>();
        language.add("العربية");
        language.add("english");

        adapter = new ArrayAdapter<>(requireActivity(),
                R.layout.item_spinner, R.id.textView, language);
        adapter.setDropDownViewResource(R.layout.item_spinner_drop);

        languageSpinner.setAdapter(adapter);

        if (SharedPreference.getLanguage().equals("ar"))
            languageSpinner.setSelection(0);
        else
            languageSpinner.setSelection(1);
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