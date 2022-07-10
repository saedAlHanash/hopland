package com.hopeland.pda.example.SAED.UI.Fragments.Auth;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.hopeland.pda.example.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginFragment extends Fragment {

    @BindView(R.id.login1)
    Button login;
    @BindView(R.id.textView2)
    TextView textView2;
    @BindView(R.id.user_name)
    TextInputEditText username;
    @BindView(R.id.password)
    TextInputEditText password;

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.bind(this, view);

        listeners();

        return view;
    }

    void listeners() {
        login.setOnClickListener(loginListener);
    }

    private final View.OnClickListener loginListener = view -> {
        if (checkFields()) {
            String u = username.getText().toString();
            String p = password.getText().toString();
            if (u.equals("admin") && p.equals("admin"))
                startEntryActivity();
            else
                Toast.makeText(requireContext(), getResources().getString(R.string.wrong_user_or_Pass), Toast.LENGTH_SHORT).show();

        }
    };

    void startEntryActivity() {
//        Intent intent = new Intent(requireActivity(), EntryActivity.class);
//        intent.putExtra("key", "admin");
//        startActivity(intent);
    }

    /**
     * checking if username or password not empty
     */

    boolean checkFields() {
        if (username.getText().length() == 0) {
            username.setError("حقل فارغ");
            return false;
        }
        if (password.getText().length() == 0) {
            password.setError("حقل فارغ");
            return false;
        }
        return true;
    }

}