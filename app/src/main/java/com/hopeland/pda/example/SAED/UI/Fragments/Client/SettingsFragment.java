package com.hopeland.pda.example.SAED.UI.Fragments.Client;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.hopeland.pda.example.R;
import com.hopeland.pda.example.SAED.AppConfig.SharedPreference;
import com.hopeland.pda.example.uhf.UHFBaseActivity;
import com.pda.rfid.uhf.UHFReader;

import java.util.Hashtable;


public class SettingsFragment extends Fragment {

    EditText ip;
    EditText port;
    Button save;
    Spinner spinner;
    View view;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings1, container, false);

        initViews();

        initIpEditText();

        initIpAnPort();

        listeners();

        GetPower();

        return view;
    }

    private void initViews() {
        ip = view.findViewById(R.id.ip);
        port = view.findViewById(R.id.port);
        save = view.findViewById(R.id.save);
        spinner = view.findViewById(R.id.spinner);
    }

    void listeners() {
        save.setOnClickListener(saveListener);
    }


    private final View.OnClickListener saveListener = v -> {
        saveInFile();
        savePower();
    };

    private void savePower() {
        int param = spinner.getSelectedItemPosition();

        if (UHFReader._Config.SetFrequency(param) == 0)
            Button_SetPower(); // 频率功率一起改
        else
            Toast.makeText(requireActivity(), getString(R.string.str_faild), Toast.LENGTH_SHORT).show();

    }

    // Query current power
    protected boolean GetPower() {
        boolean rt = false;
        int iPower = UHFReader._Config.GetANTPowerParam();
        if (iPower != -1)
            rt = true;

        try {
            spinner.setSelection(iPower);
        } catch (Exception ex) //set the power to 25
        {
            int antCount = 1;
            UHFReader._Config.SetANTPowerParam(antCount, 25);
            spinner.setSelection(25);
        }
        return rt;
    }

    // SerPower
    public void Button_SetPower() {

        int antCount = 1;
        int rt = UHFReader._Config.SetANTPowerParam(antCount, spinner.getSelectedItemPosition());

        if (rt == 0)
            Toast.makeText(requireActivity(), getString(R.string.str_success), Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(requireActivity(), getString(R.string.str_faild), Toast.LENGTH_SHORT).show();


    }

    //saed :
    void initIpEditText() {
        InputFilter[] filters = new InputFilter[1];
        filters[0] = (source, start, end, dest, dstart, dend) -> {
            if (end > start) {
                String destTxt = dest.toString();
                String resultingTxt = destTxt.substring(0, dstart)
                        + source.subSequence(start, end)
                        + destTxt.substring(dend);
                if (!resultingTxt
                        .matches("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
                    return "";
                } else {
                    String[] splits = resultingTxt.split("\\.");
                    for (String split : splits) {
                        if (Integer.parseInt(split) > 255) {
                            return "";
                        }
                    }
                }
            }
            return null;
        };

        ip.setFilters(filters);
    }

    void initIpAnPort() {

        int mPort = SharedPreference.getPort();
        String mIp = SharedPreference.getIp();

        if (!mIp.isEmpty())
            this.ip.setText(mIp);

        if (mPort != 0)
            this.port.setText(String.valueOf(mPort));
    }

    void saveInFile() {
        String mIp = ip.getText().toString();
        int mPort = Integer.parseInt(port.getText().toString());

        if (mIp.isEmpty()) {
            ip.setError(getString(R.string.requierd));
            return;
        }
        if (mPort == 0) {
            port.setError(getString(R.string.requierd));
            return;
        }

        SharedPreference.saveIp(mIp);
        SharedPreference.savePort(mPort);

        Toast.makeText(requireActivity(), getResources().getString(R.string.done), Toast.LENGTH_SHORT).show();

        requireActivity().onBackPressed();
    }
}