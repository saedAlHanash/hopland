package com.hopeland.pda.example.SAED.UI.Fragments.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.hopeland.pda.example.ItemMainActivity;
import com.hopeland.pda.example.R;


public class LoginFragment extends Fragment {

    Button login;
    TextView textView2;
    EditText username;
    EditText password;

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_login, container, false);

        initViews();
        listeners();

        return view;
    }

    void initViews() {
        login = view.findViewById(R.id.login1);
        textView2 = view.findViewById(R.id.textView2);
        username = view.findViewById(R.id.user_name);
        password = view.findViewById(R.id.password);
    }

    void listeners() {
        login.setOnClickListener(loginListener);
    }

    final View.OnClickListener loginListener = view -> {
        if (checkFields()) {
            String u = username.getText().toString();
            String p = password.getText().toString();
            if (u.equals("admin") && p.equals("admin"))
                startEntryActivity();
            else
                Toast.makeText(requireContext(), getResources()
                        .getString(R.string.wrong_user_or_Pass), Toast.LENGTH_SHORT).show();

        }
    };

    void startEntryActivity() {
        Intent intent = new Intent(requireActivity(), ItemMainActivity.class);
        intent.putExtra("key", "admin");
        startActivity(intent);
    }

    /**
     * checking if username or password not empty
     */

    boolean checkFields() {
        if (username.getText().length() == 0) {
            username.setError(getString(R.string.empty_feild));
            return false;
        }
        if (password.getText().length() == 0) {
            password.setError(getString(R.string.empty_feild));
            return false;
        }
        return true;
    }

}