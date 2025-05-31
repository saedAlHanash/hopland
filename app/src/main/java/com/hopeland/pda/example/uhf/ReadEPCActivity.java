package com.hopeland.pda.example.uhf;

import static com.util.BaseActivity.MSG_USER_BEG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.hopeland.pda.example.PublicData;
import com.hopeland.pda.example.R;
import com.pda.rfid.EPCModel;
import com.pda.rfid.IAsynchronousMessage;
import com.pda.rfid.uhf.UHF;
import com.pda.rfid.uhf.UHFReader;
import com.port.Adapt;
import com.util.Helper.Helper_ThreadPool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author RFID_C Read Tag
 */
@SuppressLint("HandlerLeak")
public class ReadEPCActivity extends Activity implements
        IAsynchronousMessage {
    public UHF CLReader = UHFReader.getUHFInstance();
    static Boolean _UHFSTATE = false;
    static int _NowAntennaNo = 1;
    static int _UpDataTime = 0;


    private ListView listView = null;
    private SimpleAdapter sa = null;
    private Button btn_Read = null;
    private TextView tv_TitleTagID = null;
    private TextView tv_TitleTagCount = null;


    private Spinner sp_ReadType = null;
    private static boolean isStartPingPong = false;
    private boolean isKeyDown = false;
    private int keyDownCount = 0;

    private static int _ReadType = 0;
    private final HashMap<String, EPCModel> hmList = new HashMap<>();
    private final Object hmList_Lock = new Object();
    private Boolean IsFlushList = true;
    private final Object beep_Lock = new Object();
    ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_SYSTEM,
            ToneGenerator.MAX_VOLUME);

    private static final boolean isPowerLowShow = false;

    private final int MSG_RESULT_READ = MSG_USER_BEG + 1;
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_RESULT_READ) {
                ShowList();
            }
        }
    };
    private IAsynchronousMessage log = null;

    public void UHF_Dispose() {
        if (_UHFSTATE) {
            UHFReader._Config.CloseConnect();
            _UHFSTATE = false;
        }
    }

    @SuppressLint("UseSparseArrays")
    @SuppressWarnings("serial")
    protected void UHF_GetReaderProperty() {

        String propertyStr = UHFReader._Config.GetReaderProperty();

        String[] propertyArr = propertyStr.split("\\|");
        var hm_Power = new HashMap<Integer, Integer>() {
            {
                put(1, 1);
                put(2, 3);
                put(3, 7);
                put(4, 15);
            }
        };
        if (propertyArr.length > 3) {
            try {
                var x = hm_Power.get(Integer.parseInt(propertyArr[2]));
                if (x != null) _NowAntennaNo = x;
            } catch (Exception ex) {
                Log.d("Debug", "Get Reader Property failure and conversion failed!");
            }
        } else {
            Log.d("Debug", "Get Reader Property failure");
        }
    }

    public Boolean UHF_Init(IAsynchronousMessage log) {
        var rt = false;
        try {
            if (!_UHFSTATE) {
                boolean ret = UHFReader.getUHFInstance().OpenConnect(log);
                if (ret) {
                    rt = true;
                    _UHFSTATE = true;
                }

                Thread.sleep(500);
            } else {
                rt = true;
            }
        } catch (Exception ex) {
            Log.d("debug", "On the UHF electric abnormal:" + ex.getMessage());
        }
        return rt;
    }

    protected void UHF_SetTagUpdateParam() {


        var searchRT = UHFReader._Config.GetTagUpdateParam();
        var arrRT = searchRT.split("\\|");
        if (arrRT.length >= 2) {
            int nowUpDataTime = Integer.parseInt(arrRT[0]);
            Log.d("Debug", "Check the label to upload time:" + nowUpDataTime);
            if (_UpDataTime != nowUpDataTime) {

                UHFReader._Config.SetTagUpdateParam(_UpDataTime, 0);
                Log.d("Debug", "Sets the label upload time...");
            }
        } else {
            Log.d("Debug", "Query tags while uploading failure...");
        }
    }


    public void Read(View v) {
        Button btnRead = (Button) v;
        String controlText = btnRead.getText().toString();
        if (controlText.equals(getString(R.string.btn_read))) {
            PingPong_Read();
            btnRead.setText(getString(R.string.btn_read_stop));
            sp_ReadType.setEnabled(false);
        } else {
            Pingpong_Stop();
            btnRead.setText(getString(R.string.btn_read));
            sp_ReadType.setEnabled(true);
        }
    }


    public void Clear(View v) {
        hmList.clear();
        ShowList();
    }


    public void Back(View v) {
        if (btn_Read.getText().toString()
                .equals(getString(R.string.btn_read_stop))) {

            return;
        }
        ReadEPCActivity.this.finish();
    }


    protected void Init() {
        log = this;
        if (!UHF_Init(log)) {

        } else {
            try {
                UHF_GetReaderProperty();
                Thread.sleep(20);
                CLReader.Stop();
                Thread.sleep(20);
                UHF_SetTagUpdateParam();
            } catch (Exception ignored) {
            }


            Refush();

            listView = this.findViewById(R.id.lv_Main);
            tv_TitleTagID = findViewById(R.id.tv_TitleTagID);
            tv_TitleTagCount = findViewById(R.id.tv_TitleTagCount);

            btn_Read = findViewById(R.id.btn_Read);
            btn_Read.setText(getString(R.string.btn_read));
            sp_ReadType = findViewById(R.id.sp_ReadType);


            sp_ReadType
                    .setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onItemSelected(AdapterView<?> arg0,
                                                   View arg1, int arg2, long arg3) {
                            if (isStartPingPong)
                                return;
                            int selectItem = sp_ReadType
                                    .getSelectedItemPosition();
                            if (PublicData._IsCommand6Cor6B.equals("6C")) {
                                tv_TitleTagCount.setText("Count");
                                if (selectItem == 0) {
                                    _ReadType = 0;
                                    tv_TitleTagID.setText("EPC");
                                } else if (selectItem == 1) {
                                    _ReadType = 1;
                                    tv_TitleTagID.setText("TID");
                                } else if (selectItem == 2) {
                                    _ReadType = 2;
                                    tv_TitleTagID.setText("UserData");
                                }
                            } else {
                                tv_TitleTagCount.setText("Count");
                                if (selectItem == 0) {
                                    _ReadType = 0;
                                    tv_TitleTagID.setText("EPC");
                                } else if (selectItem == 1) {
                                    _ReadType = 1;
                                    tv_TitleTagID.setText("TID");
                                } else if (selectItem == 2) {
                                    _ReadType = 2;
                                    tv_TitleTagID.setText("UserData");
                                }
                            }

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> arg0) {

                        }
                    });

        }
    }


    protected void Refush() {
        IsFlushList = true;

        Helper_ThreadPool.ThreadPool_StartSingle(() -> {
            while (IsFlushList) {
                try {
                    handler.sendMessage(handler.obtainMessage(MSG_RESULT_READ, null));
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
        });

        Helper_ThreadPool.ThreadPool_StartSingle(() -> {
            while (IsFlushList) {
                synchronized (beep_Lock) {
                    try {
                        beep_Lock.wait();
                    } catch (InterruptedException ignored) {
                    }
                }
                if (IsFlushList) {
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);
                }

            }
        });
    }


    protected void Dispose() {
        isStartPingPong = false;
        IsFlushList = false;
        synchronized (beep_Lock) {
            beep_Lock.notifyAll();
        }
        UHF_Dispose();
    }


    protected void ShowList() {
        if (!isStartPingPong)
            return;

        List<Map<String, String>> c = GetData();

        sa = new SimpleAdapter(this, c, R.layout.epclist_item, new String[]{"EPC", "ReadCount"}, new int[]{
                R.id.EPCList_TagID, R.id.EPCList_ReadCount});
        listView.setAdapter(sa);
        listView.invalidate();
    }


    @SuppressWarnings({"rawtypes", "unused"})
    protected List<Map<String, String>> GetData() {
        List<Map<String, String>> rt = new ArrayList<>();
        synchronized (hmList_Lock) {
            for (var stringEPCModelEntry : hmList.entrySet()) {

                var val = (EPCModel) ((Map.Entry) stringEPCModelEntry).getValue();
                var map = new HashMap<String, String>();
                if (_ReadType == 0) {
                    map.put("EPC", val._EPC);
                } else if (_ReadType == 1) {
                    map.put("EPC", val._TID);
                } else {
                    map.put("EPC", val._UserData);
                }
                var rc = val._TotalCount;
                map.put("ReadCount", Long.toString(rc));
                rt.add(map);
            }
        }
        return rt;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.setContentView(R.layout.uhf_read);

    }


    @Override
    protected void onPause() {
        super.onPause();
        Dispose();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Init();
    }

    @Override
    public void onBackPressed() {
        Back(null);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("PDADemo", "onKeyDown keyCode = " + keyCode);
        if ((Adapt.DEVICE_TYPE_HY820 == Adapt.getDeviceType() && (keyCode == KeyEvent.KEYCODE_F9 /* RFID扳机*/ || keyCode == 285  /* 左快捷*/ || keyCode == 286  /* 右快捷*/))
                || ((Adapt.getSN().startsWith("K3")) && (keyCode == KeyEvent.KEYCODE_F1 || keyCode == KeyEvent.KEYCODE_F5))
                || ((Adapt.getSN().startsWith("K6")) && (keyCode == KeyEvent.KEYCODE_F1 || keyCode == KeyEvent.KEYCODE_F5))) {

            btn_Read.setText(getString(R.string.btn_read_stop));
            sp_ReadType.setEnabled(false);
            btn_Read.setClickable(false);
            if (!isKeyDown) {
                isKeyDown = true;
                if (!isStartPingPong) {
                    Clear(null);
                    Pingpong_Stop();
                    isStartPingPong = true;
                    if (PublicData._IsCommand6Cor6B.equals("6C")) {
                        GetEPC_6C();
                    } else {
                        CLReader.Get6B(_NowAntennaNo + "|1" + "|1" + "|"
                                + "1,000F");
                    }
                }
            } else {
                if (keyDownCount < 10000)
                    keyDownCount++;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d("PDADemo", "onKeyUp keyCode = " + keyCode);
        if ((Adapt.DEVICE_TYPE_HY820 == Adapt.getDeviceType() && (keyCode == KeyEvent.KEYCODE_F9 /* RFID扳机*/ || keyCode == 285  /* 左快捷*/ || keyCode == 286  /* 右快捷*/))
                || ((Adapt.getSN().startsWith("K3")) && (keyCode == KeyEvent.KEYCODE_F1 || keyCode == KeyEvent.KEYCODE_F5))
                || ((Adapt.getSN().startsWith("K6")) && (keyCode == KeyEvent.KEYCODE_F1 || keyCode == KeyEvent.KEYCODE_F5))) {

            CLReader.Stop();
            isStartPingPong = false;
            keyDownCount = 0;
            isKeyDown = false;
            btn_Read.setText(getString(R.string.btn_read));
            sp_ReadType.setEnabled(true);
            btn_Read.setClickable(true);
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void OutPutEPC(EPCModel model) {
        if (!isStartPingPong)
            return;
        try {
            synchronized (hmList_Lock) {
                if (hmList.containsKey(model._EPC + model._TID)) {
                    EPCModel tModel = hmList.get(model._EPC + model._TID);
                    if (tModel != null) {
                        tModel._TotalCount++;
                        model._TotalCount = tModel._TotalCount;
                    }
                    hmList.remove(model._EPC + model._TID);
                    hmList.put(model._EPC + model._TID, model);
                } else {
                    hmList.put(model._EPC + model._TID, model);
                }
            }
            synchronized (beep_Lock) {
                beep_Lock.notify();
            }
        } catch (Exception ex) {
            Log.d("Debug", "Tags output exceptions:" + ex.getMessage());
        }

    }


    public void PingPong_Read() {
        if (isStartPingPong)
            return;
        isStartPingPong = true;
        Clear(null);
        Helper_ThreadPool.ThreadPool_StartSingle(() -> {
            try {
                if (!isPowerLowShow) {
                    if (PublicData._IsCommand6Cor6B.equals("6C")) {
                        GetEPC_6C();
                    } else {
                        CLReader.Get6B(_NowAntennaNo + "|1" + "|1" + "|" + "1,000F");
                    }
                }
            } catch (Exception ignored) {

            }
        });
    }


    public void Pingpong_Stop() {
        isStartPingPong = false;
        CLReader.Stop();
    }


    private void GetEPC_6C() {

        String text = tv_TitleTagID.getText().toString();
        switch (text) {
            case "EPC" -> UHFReader._Tag6C.GetEPC(_NowAntennaNo, 1);
            case "TID" -> UHFReader._Tag6C.GetEPC_TID(_NowAntennaNo, 1);
            case "UserData" -> UHFReader._Tag6C.GetEPC_TID_UserData(_NowAntennaNo, 1, 0, 6);
        }

    }

}
