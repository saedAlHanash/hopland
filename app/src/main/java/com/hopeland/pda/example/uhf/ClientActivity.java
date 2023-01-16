package com.hopeland.pda.example.uhf;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hopeland.pda.example.PublicData;
import com.hopeland.pda.example.R;
import com.hopeland.pda.example.SAED.AppConfig.FC;
import com.hopeland.pda.example.SAED.AppConfig.FN;
import com.hopeland.pda.example.SAED.AppConfig.SharedPreference;
import com.hopeland.pda.example.SAED.Helpers.Converters.GzipConverter;
import com.hopeland.pda.example.SAED.Helpers.Images.ConverterImage;
import com.hopeland.pda.example.SAED.Helpers.NoteMessage;
import com.hopeland.pda.example.SAED.Helpers.View.FTH;
import com.hopeland.pda.example.SAED.Network.SaedSocket;
import com.hopeland.pda.example.SAED.UI.Fragments.Client.ClientFragment;
import com.hopeland.pda.example.SAED.UI.Fragments.Client.Prosess.SmartScanFragment;
import com.hopeland.pda.example.SAED.ViewModels.All;
import com.hopeland.pda.example.SAED.ViewModels.MyViewModel;
import com.hopeland.pda.example.SAED.ViewModels.Product;
import com.pda.rfid.uhf.UHFReader;
import com.pda.rfid.EPCModel;
import com.pda.rfid.IAsynchronousMessage;
import com.port.Adapt;
import com.util.Helper.*;

import android.annotation.SuppressLint;
import android.media.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;

/**
 * @author RFID_C Read Tag
 */
public class ClientActivity extends UHFBaseActivity implements
        IAsynchronousMessage {

    //region  OLD

    static boolean isStartPingPong = false; //Whether to start reading tag
    boolean isKeyDown = false; // Whether the board is pressed
    boolean isLongKeyDown = false; // Whether the board is in long press state
    int keyDownCount = 0; // Number of times the board is pressed

    final Object hmList_Lock = new Object();
    Boolean IsFlushList = true; //Whether to refresh the list
    final Object beep_Lock = new Object();
    ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_SYSTEM,
            ToneGenerator.MAX_VOLUME);

    //endregion

    //region GLOBAL

    //region Views

    FragmentContainerView clientContainer;
    TextView notConnectTv;

    //endregion

    //region connection config
    /**
     * socket ip address
     */
    public String ip;
    /**
     * socket port
     */
    public int port;
    URI uri;

    //endregion

    //region sockets
    WebSocketClient webSocketClient;
    public SaedSocket socket;
    //endregion

    public MyViewModel myViewModel;
    Gson gson = new Gson();

    public OnReadTag onReadTag;

    //endregion

    final String TAG = "SAED_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 创建
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        SharedPreference.getInstance(this);

        myViewModel = ViewModelProviders.of(this).get(MyViewModel.class);

        startClientFragment();

        ip = SharedPreference.getIp().replaceAll("\\s+", "");
        port = SharedPreference.getPort();

        if (ip == null || ip.isEmpty())
            return;

        initSocket(ip, port);

        initView();

//        // Set base band auto mode, q=1, session=1, flag = 0 flagA
//      int x=  UHFReader._Config.SetEPCBaseBandParam(255, 4, 0, 2);
//        // set ant 1 power to 20dBm
//      int y=  UHFReader._Config.SetANTPowerParam(1, 25);
//
//        Log.d(TAG, "onCreate: " +x +" "+ y);
////        UHFReader._Config.SetANTPowerParam(1,28);
//
//        UHFReader._Config.SetEPCBaseBandParam(255,0,1,0);

    }

    void initView() {
        clientContainer = findViewById(R.id.client_container);
        notConnectTv = findViewById(R.id.not_connect);
//        rssis[0] = 60;
//        rssis[1] = 70;
//        rssis[2] = 90;
//        rssis[3] = 120;
//        rssis[4] = 50;
//        rssis[5] = 80;
//        rssis[6] = 100;
//        rssis[7] = 65;
//        rssis[8] = 85;
//        rssis[9] = 95;
    }

    //region getData

    public void getAll() {
        myViewModel.getAll(socket);
    }


    //endregion

    //region observers
    final Observer<Boolean> observer = isConnect -> {

        if (isConnect == null)
            return;
        if (isConnect)
            notConnectTv.setVisibility(View.GONE);
        else
            notConnectTv.setVisibility(View.VISIBLE);
    };

    //endregion

    //region socket

    /**
     * start connection with socket
     *
     * @param mIp   socket ip address
     * @param mPort socket port
     */
    public void initSocket(String mIp, int mPort) {

        initUri(mIp, mPort);

        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake h) {
                Log.d(TAG, "onOpen: ");

                getAll();

                handler.sendEmptyMessage(201);
            }

            @Override
            public void onMessage(String message) {

                Log.d(TAG, "onMessage: message " + message);

                if (message.trim().equals("401")) {
                    handler.sendEmptyMessage(401);
                    return;
                }

                if (message.trim().equals("404"))
                    myViewModel.productLiveData.postValue(new Pair<>(null, false));

                if (message.trim().equals("200"))
                    handler.sendEmptyMessage(1000);

                if (message.charAt(0) == '[') {

                    ArrayList<All> alls = gson.fromJson(message,
                            new TypeToken<ArrayList<All>>() {
                            }.getType());

                    myViewModel.allLiveData.postValue(alls);

                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.d(TAG, "onClose: ");
                handler.sendEmptyMessage(202);
            }

            @Override
            public void onError(Exception ex) {
                Log.e(TAG, "onError: ", ex);
//                handler.sendEmptyMessage(500);
            }

            @Override
            public void onMessage(ByteBuffer bytes) {
                Log.d(TAG, "onMessage: have message ");
                byte[] bytes1 = bytes.array();
                String message = null;

                try {
                    message = GzipConverter.decompress(bytes1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (message == null) {
                    handler.sendEmptyMessage(500);
                    return;
                }

                if (message.trim().equals("404")) {
                    myViewModel.productLiveData.postValue(new Pair<>(null, false));
                    handler.sendEmptyMessage(404);
                    return;
                }

                if (message.trim().equals("401")) {
                    handler.sendEmptyMessage(401);
                    return;
                }

                if (message.trim().equals("200")) {
                    handler.sendEmptyMessage(1000);
                    return;
                }

                if (message.charAt(0) == '[') {

                    ArrayList<All> alls = gson.fromJson(message,
                            new TypeToken<ArrayList<All>>() {
                            }.getType());

                    myViewModel.allLiveData.postValue(alls);

                    return;
                }

                Product product = gson.fromJson(message, Product.class);
                product.bitmap = ConverterImage.convertBase64ToBitmap(product.im);
                product.bitmap = ConverterImage.getResizedBitmap(product.bitmap, 300);
                myViewModel.productLiveData.postValue(new Pair<>(product, true));

                handler.sendEmptyMessage(200);
            }
        };

        socket = new SaedSocket(webSocketClient);
    }

    void initUri(String mIp, int mPort) {
        try {
            uri = new URI("ws://" + mIp + ":" + mPort);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            //اوصليلي الجهاز
        }
    }

    //endregion

    //region handler

    final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 404:
                    Toast.makeText(ClientActivity.this,
                            getString(R.string.not_found), Toast.LENGTH_SHORT).show();
                    break;

                case 500:
                    Toast.makeText(ClientActivity.this,
                            getString(R.string.have_error), Toast.LENGTH_SHORT).show();
                    break;

                case 200:

                    break;

                case 201:
                    myViewModel.connectLiveData.postValue(true);
                    break;

                case 202:
                    handler.postDelayed(() -> new Thread(() -> {
                        if (!socket.isClosed) {
                            webSocketClient.reconnect();
                            Log.d(TAG, "handleMessage: reconnect");
                        }
                    }).start(), 5000);

                    myViewModel.connectLiveData.postValue(false);
                    break;

                case 401:
                    myViewModel.sendReportLiveData.setValue(false);
                    break;

                case 1000:
                    myViewModel.sendReportLiveData.setValue(true);

            }
        }
    };

    //endregion

    //region process

    int x;
    byte rssis = 1;

    byte s = 1;
    boolean d = true;

    // Start reading tag
    public void read() {

        if (onReadTag == null)
            return;

//        for (int i = 0; i < 10; i++) {
//            onReadTag.onRead("saed"+i, s);
//            Log.d(TAG, "read: " + i);
//        }
//
//        if (d)
//            return;

//        x += 1;
//
//        if (x == 2)
//            onReadTag.onRead("00242540" + x, rssis);
//        else
//            onReadTag.onRead("000" + x, rssis);
//
//        if (d)
//            return;

        if (isStartPingPong)
            return;

        isStartPingPong = true;

        Toast.makeText(this, getString(R.string.start_reading), Toast.LENGTH_SHORT).show();

        Helper_ThreadPool.ThreadPool_StartSingle(() -> {
            try {
                GetEPC_6C();
            } catch (Exception ignored) {
            }
        });
    }

    public void readSmart(String epc) {

        if (onReadTag == null)
            return;

        if (isStartPingPong)
            return;

        isStartPingPong = true;

        Helper_ThreadPool.ThreadPool_StartSingle(() -> {
            try {
                GetEPC_6CSmart(epc);
            } catch (Exception ignored) {
            }
        });
    }


    // Stop reading tag
    public void stop() {

        if (!isStartPingPong)
            return;

        Toast.makeText(this, getString(R.string.stop_reading), Toast.LENGTH_SHORT).show();
        isStartPingPong = false;
        CLReader.Stop();
    }


    //init
    protected void Init() {
        if (!UHF_Init(this)) { // 打开模块电源失败
            showMsg(getString(R.string.uhf_low_power_info),
                    (arg0, arg1) -> ClientActivity.this.finish());
        } else {
            try {

                super.UHF_GetReaderProperty(); // 获得读写器的能力
                Thread.sleep(20);
                CLReader.Stop(); // 停止指令
                Thread.sleep(20);
                super.UHF_SetTagUpdateParam(); // 设置标签重复上传时间为20ms

            } catch (Exception ignored) {
            }

            //刷新线程
            initSound();
        }
    }

    //Refush
    protected void initSound() {

        IsFlushList = true;
//        // thread
//        Helper_ThreadPool.ThreadPool_StartSingle(() -> {
//            while (IsFlushList) {
//                try {
//                    sendMessage(MSG_RESULT_READ, null);
//                    Thread.sleep(1000); // Refresh every second
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });

        // The buzzer sounds
        Helper_ThreadPool.ThreadPool_StartSingle(() -> {
            while (IsFlushList) {

                synchronized (beep_Lock) {
                    try {
                        beep_Lock.wait();
                    } catch (InterruptedException ignored) {
                    }
                }

                if (IsFlushList)
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);
            }
        });
    }

    // Dispose
    protected void Dispose() {
        isStartPingPong = false;
        IsFlushList = false;
        synchronized (beep_Lock) {
            beep_Lock.notifyAll();
        }

        UHF_Dispose();
    }

    //endregion

    //region handler process

    final Handler handler1 = new Handler(Looper.getMainLooper()) {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            if (onReadTag == null)
                return;


            switch (msg.what) {

                case 1:
                    String epc = msg.getData().getString("epc");
                    byte rssi = msg.getData().getByte("rssi");


                    if (epc == null || epc.length() == 0)
                        return;

                    Log.d(TAG, "handleMessage:   " + epc);

                    onReadTag.onRead(epc, rssi);

                    break;

                case 1980:
                    break;
            }
        }
    };

    //endregion

    void startClientFragment() {
        FTH.replaceFadFragment(FC.CLIENT_C,
                this, new ClientFragment(), FN.CLIENT_FN);
    }

    void removeObservers() {
        myViewModel.connectLiveData.removeObserver(observer);
    }


    @Override
    protected void onDestroy() {
        if (socket != null)
            socket.close();

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        removeObservers();
        stop();

        Dispose();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (webSocketClient != null && webSocketClient.isClosed())
            webSocketClient.reconnect();

        myViewModel.connectLiveData.observeForever(observer);

        Init();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //log.d("PDADemo", "onKeyDown keyCode = " + keyCode);
        if ((Adapt.DEVICE_TYPE_HY820 == Adapt.getDeviceType() && (keyCode == KeyEvent.KEYCODE_F9 /* RFID扳机*/ || keyCode == 285  /* 左快捷*/ || keyCode == 286  /* 右快捷*/))
                || ((Adapt.getSN().startsWith("K3")) && (keyCode == KeyEvent.KEYCODE_F1 || keyCode == KeyEvent.KEYCODE_F5))
                || ((Adapt.getSN().startsWith("K6")) && (keyCode == KeyEvent.KEYCODE_F1 || keyCode == KeyEvent.KEYCODE_F5))) { // 按下扳机

//            btn_Read.setText(getString(R.string.btn_read_stop));
//            sp_ReadType.setEnabled(false);
//            btn_Read.setClickable(false);

            if (!isKeyDown) {
                isKeyDown = true;
                if (!isStartPingPong) {
                    stop(); // stop
                    isStartPingPong = true;
                    String rt;
                    if (PublicData._IsCommand6Cor6B.equals("6C")) {// 6C
                        if (FTH.getCurrentFragmentName(this).equals(FN.SMART_SCAN_FN)) {
                            Fragment fragment = FTH.getFragmentByName(this, FN.SMART_SCAN_FN);
                            if (fragment != null) {
                                String epc = ((SmartScanFragment) fragment).epc.getText().toString();
                                GetEPC_6CSmart(epc);
                            } else
                                NoteMessage.showSnackBar(this, getString(R.string.screen_button));

                        } else {
                            isStartPingPong = false;
                            read();
                        }

                    } else {// 6B
                        rt = CLReader.Get6B(_NowAntennaNo + "|1" + "|1" + "|"
                                + "1,000F");
                    }
                }
            } else {
                if (keyDownCount < 10000)
                    keyDownCount++;
            }
            if (keyDownCount > 100) {
                isLongKeyDown = true;
            }
            if (isLongKeyDown) { // long down

            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //log.d("PDADemo", "onKeyUp keyCode = " + keyCode);
        if ((Adapt.DEVICE_TYPE_HY820 == Adapt.getDeviceType() && (keyCode == KeyEvent.KEYCODE_F9 /* RFID扳机*/ || keyCode == 285  /* 左快捷*/ || keyCode == 286  /* 右快捷*/))
                || ((Adapt.getSN().startsWith("K3")) && (keyCode == KeyEvent.KEYCODE_F1 || keyCode == KeyEvent.KEYCODE_F5))
                || ((Adapt.getSN().startsWith("K6")) && (keyCode == KeyEvent.KEYCODE_F1 || keyCode == KeyEvent.KEYCODE_F5))) { // 放开扳机

            stop();

            keyDownCount = 0;
            isKeyDown = false;
            isLongKeyDown = false;
//            btn_Read.setText(getString(R.string.btn_read));
//            sp_ReadType.setEnabled(true);
//            btn_Read.setClickable(true);
        }
        return super.onKeyUp(keyCode, event);
    }

    Bundle bundle = new Bundle();

    @Override
    public void OutPutEPC(EPCModel model) {
        if (!isStartPingPong)
            return;

        try {

            synchronized (hmList_Lock) {
                bundle.putString("epc", model._EPC);
                bundle.putByte("rssi", model._RSSI);
                Message message = new Message();
                message.what = 1;
                message.setData(bundle);

                handler1.sendMessage(message);

            }
            synchronized (beep_Lock) {
                beep_Lock.notify();
            }

        } catch (Exception ex) {
            //log.d("Debug", "Tags output exceptions:" + ex.getMessage());
        }

    }

    //6C,Read tag
    int GetEPC_6C() {

        Log.d(TAG, "GetEPC_6C: ");
        int ret;
//        ret = UHFReader._Tag6C.GetEPC_TID(1, 1);
        ret = UHFReader._Tag6C.GetEPC(_NowAntennaNo, 1);
//        ret = UHFReader._Tag6C.GetEPC_MatchEPC(_NowAntennaNo, 1,"000000000000000000000023");

        return ret;
    }

    int GetEPC_6CSmart(String epc) {

        int ret;

        ret = UHFReader._Tag6C.GetEPC_MatchEPC(_NowAntennaNo, 1, epc);

        return ret;
    }

    public interface OnReadTag {
        void onRead(@NotNull String epc, byte rssi);
    }
}
