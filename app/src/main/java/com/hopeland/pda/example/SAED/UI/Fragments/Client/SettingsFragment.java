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

import java.util.Hashtable;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.hopeland.pda.example.AppConfig.SharedPreference.getIp;
import static com.hopeland.pda.example.AppConfig.SharedPreference.getPort;
import static com.hopeland.pda.example.AppConfig.SharedPreference.saveIp;
import static com.hopeland.pda.example.AppConfig.SharedPreference.savePort;

public class SettingsFragment extends Fragment {

    @BindView(R.id.ip)
    EditText ip;
    @BindView(R.id.port)
    EditText port;
    @BindView(R.id.save)
    Button save;

    View view;

    private final Hashtable<Integer, Integer> mAntMap = new Hashtable<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);

        initIpEditText();

        initIpAnPort();

        listeners();

        return view;
    }

    void listeners() {
        save.setOnClickListener(saveListener);
    }


    private final View.OnClickListener saveListener = v -> {
        saveInFile();
    };




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

        int mPort = getPort();
        String mIp = getIp();

        if (!mIp.isEmpty())
            this.ip.setText(mIp);

        if (mPort != 0)
            this.port.setText(String.valueOf(mPort));
    }

    void saveInFile() {
        String mIp = ip.getText().toString();
        int mPort = Integer.parseInt(port.getText().toString());

        if (mIp.isEmpty()) {
            ip.setError("required");
            return;
        }
        if (mPort == 0) {
            port.setError("required");
            return;
        }

        saveIp(mIp);
        savePort(mPort);

        Toast.makeText(requireActivity(), getResources().getString(R.string.done), Toast.LENGTH_SHORT).show();

        requireActivity().onBackPressed();
    }
}