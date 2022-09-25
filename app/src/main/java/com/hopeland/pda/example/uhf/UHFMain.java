package com.hopeland.pda.example.uhf;

import com.hopeland.pda.example.R;
import com.hopeland.pda.example.SAED.AppConfig.SharedPreference;
import com.pda.rfid.EPCModel;
import com.pda.rfid.IAsynchronousMessage;
import com.port.Adapt;

import android.content.Intent;
import android.os.*;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

/**
 * @author RFID_C UHF
 */
public class UHFMain extends UHFBaseActivity implements IAsynchronousMessage {

    private final int MSG_ENTER_READ = MSG_USER_BEG + 1;
    private final int MSG_ENTER_READ_MATCH = MSG_USER_BEG + 2;
    private final int MSG_ENTER_WRITE = MSG_USER_BEG + 3;
    private final int MSG_ENTER_CONFIG = MSG_USER_BEG + 4;
    private final int MSG_SHOW_UHF_VER = MSG_USER_BEG + 5;
    //private final int MSG_ENTER_LOCK = MSG_USER_BEG + 6;

    EditText ip;
    EditText port;

    @Override
    protected void msgProcess(Message msg) {
        Intent intent;
        switch (msg.what) {
            case MSG_ENTER_READ:
                intent = new Intent();
                intent.setClass(UHFMain.this, ClientActivity.class);
                startActivity(intent);
                break;
            case MSG_ENTER_READ_MATCH:
                intent = new Intent();
                intent.setClass(UHFMain.this, ReadEPCmatchingActivity.class);
                startActivity(intent);
                break;
            case MSG_ENTER_WRITE:
                intent = new Intent();
                intent.setClass(UHFMain.this, WriteEPCActivity.class);
                startActivity(intent);
                break;
            case MSG_ENTER_CONFIG:
                intent = new Intent();
                intent.setClass(UHFMain.this, ConfigActivity.class);
                startActivity(intent);
                break;
            case MSG_SHOW_UHF_VER:
                if (msg.obj != null)
                    showTip((String) msg.obj);
                break;
            default:
                break;
        }
        super.msgProcess(msg);
    }

    // Read
    public void OpenReadActivity(View v) {
        if (isFastClick()) {
            return;
        }

        new Thread() {
            @Override
            public void run() {
                sendMessage(MSG_SHOW_WAIT, getString(R.string.str_loading));
                sendMessage(MSG_ENTER_READ, null);
            }
        }.start();
    }

    // Read
    public void OpenReadMatchingActivity(View v) {
        if (isFastClick()) {
            return;
        }

        new Thread(() -> {
            sendMessage(MSG_SHOW_WAIT, getString(R.string.str_loading));
            sendMessage(MSG_ENTER_READ_MATCH, null);
        }).start();
    }

    // Write
    public void OpenWriteActivity(View v) {
        if (isFastClick()) {
            return;
        }
        new Thread() {
            @Override
            public void run() {
                sendMessage(MSG_SHOW_WAIT, getString(R.string.str_loading));
                sendMessage(MSG_ENTER_WRITE, null);
            }

            ;
        }.start();
    }

    // Config
    public void OpenConfigActivity(View v) {
        if (isFastClick()) {
            return;
        }

        new Thread() {
            @Override
            public void run() {
                sendMessage(MSG_SHOW_WAIT, getString(R.string.str_loading));
                sendMessage(MSG_ENTER_CONFIG, null);
            }

            ;
        }.start();
    }

    // Version
    public void GetVersion(View v) {
        if (isFastClick()) {
            return;
        }

        new Thread() {
            @Override
            public void run() {
                sendMessage(MSG_SHOW_WAIT,
                        getString(R.string.str_please_waitting));
                String vStr = "- BaseBand: ";
                if (UHF_Init(UHFMain.this)) {
                    vStr += CLReader.GetReaderBaseBandSoftVersion();
                } else {
                    vStr += "null";
                }
                UHF_Dispose();

                if (Adapt.getPropertiesInstance().support("backupPower")) {
                    vStr += "\nAP: " + Adapt.getPowermanagerInstance().getBackupPowerSOC();
                }
                sendMessage(MSG_SHOW_UHF_VER, vStr);
                sendMessage(MSG_HIDE_WAIT, null);
            }

            ;
        }.start();
    }

    // Back
    public void BackIndex(View v) {
        finish();
    }

    //save ipConfig
    public void ipConfig(View view) {
        saveInFile();
    }

    //region saed:

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

        SharedPreference.getInstance(this);

        SharedPreference.saveIp(mIp);
        SharedPreference.savePort(mPort);

        Toast.makeText(this, getResources().getString(R.string.done), Toast.LENGTH_SHORT).show();
    }

    void initIpAnPort() {

        int mPort = SharedPreference.getPort();
        String mIp = SharedPreference.getIp();

        if (!mIp.isEmpty())
            this.ip.setText(mIp);

        if (mPort != 0)
            this.port.setText(String.valueOf(mPort));
    }

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.uhf_main);
        SharedPreference.getInstance(this);

        //region saed:
        ip = findViewById(R.id.ip);
        port = findViewById(R.id.port);

        initIpAnPort();
        initIpEditText();

        //endregion

        try {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } catch (Exception ex) {
            Log.d("Debug", "The initialization of abnormal:" + ex.getMessage());
        }

        showCustomBar(getString(R.string.btn_MainMenu_UHF),
                getString(R.string.str_back), null,
                R.drawable.left, 0,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BackIndex(v);
                    }
                },
                null
        );
    }

    @Override
    protected void onDestroy() {
        // destroy
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sendMessage(MSG_HIDE_WAIT, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void OutPutEPC(EPCModel model) {
    }


}
