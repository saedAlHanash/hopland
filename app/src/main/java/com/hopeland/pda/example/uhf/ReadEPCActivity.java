package com.hopeland.pda.example.uhf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hopeland.pda.example.Adapters.AdapterEpc;
import com.hopeland.pda.example.AppConfig.SharedPreference;
import com.hopeland.pda.example.PublicData;
import com.hopeland.pda.example.R;
import com.hopeland.pda.example.SAED.Models.EpcModel;
import com.hopeland.pda.example.SAED.Network.SocketClient;
import com.pda.rfid.uhf.UHFReader;
import com.pda.rfid.EPCModel;
import com.pda.rfid.IAsynchronousMessage;
import com.port.Adapt;
import com.util.Helper.*;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.media.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import android.view.*;
import android.widget.*;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author RFID_C Read Tag
 */
public class ReadEPCActivity extends UHFBaseActivity implements
        IAsynchronousMessage {

    private RecyclerView recyclerView = null; // Data list object
    private SimpleAdapter sa = null;
    private Button btn_Read = null;
    private TextView tv_TitleTagID = null;
    private TextView tv_TitleTagCount = null;
    private TextView lb_ReadTime = null;
    private TextView lb_ReadSpeed = null;
    private TextView lb_TagCount = null;
    private Spinner sp_ReadType = null;
    private static boolean isStartPingPong = false; //Whether to start reading tag
    private boolean isKeyDown = false; // Whether the board is pressed
    private boolean isLongKeyDown = false; // Whether the board is in long press state
    private int keyDownCount = 0; // Number of times the board is pressed

    private int readTime = 0;
    private int lastReadCount = 0;
    private int totalReadCount = 0; // Total number of reads
    private int speed = 0; // Read speed
    private static int _ReadType = 0; // 0:Read EPC，1:Read TID
    private HashMap<String, EPCModel> hmList = new HashMap<String, EPCModel>();
    private final Object hmList_Lock = new Object();
    private boolean flag = true; //
    private Boolean IsFlushList = true; //Whether to refresh the list
    private final Object beep_Lock = new Object();
    ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_SYSTEM,
            ToneGenerator.MAX_VOLUME);

    private static boolean isPowerLowShow = false;//Low or not

    private final int MSG_RESULT_READ = MSG_USER_BEG + 1; //constant
    private final int MSG_FLUSH_READTIME = MSG_USER_BEG + 2;
    private final int MSG_UHF_POWERLOW = MSG_USER_BEG + 3;

    private IAsynchronousMessage log = null;


    //saed :
    TextView notConnectTv;

    @SuppressLint("SetTextI18n")
    @Override
    protected void msgProcess(Message msg) {// هنا يتم استقبال الرسائل المرسلة لل handler
        switch (msg.what) {
            case MSG_RESULT_READ:
                saedShowList(); // Show the list
                break;
            case MSG_FLUSH_READTIME:
                if (lb_ReadTime != null) { //Refresh read time
                    readTime++;
                    lb_ReadTime.setText("Time:" + readTime + "S");
                }
                break;
            case MSG_UHF_POWERLOW: //Low power
                //TODO:
                break;
            default:
                super.msgProcess(msg);
                break;
        }
    }

    // Read
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

    //Clear
    public void Clear(View v) {
        totalReadCount = 0;
        readTime = 0;
        hmList.clear();
        saedShowList();
    }

    // Back
    public void Back(View v) {
        if (btn_Read.getText().toString()
                .equals(getString(R.string.btn_read_stop))) {
            showMsg(getString(R.string.uhf_please_stop), null);
            return;
        }
        ReadEPCActivity.this.finish();
    }

    //init
    protected void Init() {
        log = this;
        if (!UHF_Init(log)) { // 打开模块电源失败
            showMsg(getString(R.string.uhf_low_power_info),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            ReadEPCActivity.this.finish();
                        }
                    });
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
            Refush();

            recyclerView = (RecyclerView) this.findViewById(R.id.rc_Main);
            tv_TitleTagID = (TextView) findViewById(R.id.tv_TitleTagID);
            tv_TitleTagCount = (TextView) findViewById(R.id.tv_TitleTagCount);
            lb_ReadTime = (TextView) findViewById(R.id.lb_ReadTime);
            lb_ReadSpeed = (TextView) findViewById(R.id.lb_ReadSpeed);
            lb_TagCount = (TextView) findViewById(R.id.lb_TagCount);
            btn_Read = (Button) findViewById(R.id.btn_Read);
            btn_Read.setText(getString(R.string.btn_read));
            sp_ReadType = (Spinner) findViewById(R.id.sp_ReadType);
            this.notConnectTv = findViewById(R.id.not_connect);

            //

            sp_ReadType
                    .setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(AdapterView<?> arg0,
                                                   View arg1, int arg2, long arg3) {
                            if (isStartPingPong)
                                return;
                            int selectItem = sp_ReadType
                                    .getSelectedItemPosition();
                            if (PublicData._IsCommand6Cor6B.equals("6C")) {// 读6C标签
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

    //Refush
    protected void Refush() {
        IsFlushList = true;
        // thread
        Helper_ThreadPool.ThreadPool_StartSingle(() -> {
            while (IsFlushList) {
                try {
                    sendMessage(MSG_RESULT_READ, null);
                    Thread.sleep(1000); // Refresh every second
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // The buzzer sounds (الصوت)
        Helper_ThreadPool.ThreadPool_StartSingle(() -> {
            while (IsFlushList) {
                synchronized (beep_Lock) {
                    try {
                        beep_Lock.wait();
                    } catch (InterruptedException ignored) {
                    }
                }
                if (IsFlushList) {
                    toneGenerator
                            .startTone(ToneGenerator.TONE_PROP_BEEP);
                }

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

    long x = 0;

/*    //show the list
    @SuppressLint("SetTextI18n")
    protected void ShowList() {
//        if (!isStartPingPong)
//            return; todo : تعليق هون كان موجود
        map.put("EPC", String.valueOf(x++));
        map.put("EPC", String.valueOf(x += 2));

        list.add(map);

        sa = new SimpleAdapter(this, list, R.layout.epclist_item,
                new String[]{"EPC", "ReadCount"}, new int[]{
                R.id.EPCList_TagID, R.id.EPCList_ReadCount});

//        sa = new SimpleAdapter(this, GetData(), R.layout.epclist_item,
//                new String[]{"EPC", "ReadCount"}, new int[]{
//                R.id.EPCList_TagID, R.id.EPCList_ReadCount});

        recyclerView.setAdapter(sa);
        recyclerView.invalidate();

        if (lb_ReadTime != null) { // 刷新读取时间
            readTime++;
            lb_ReadTime.setText("Time:" + readTime + "S");
        }
        if (lb_ReadSpeed != null) { // 刷新读取速度
            if (flag) {
                speed = totalReadCount - lastReadCount;
                if (speed < 0)
                    speed = 0;
                lastReadCount = totalReadCount;
                lb_ReadSpeed.setText("SP:" + speed + "T/S");
            }
            // flag = !flag;
        }
        if (lb_TagCount != null) // 刷新标签总数
            lb_TagCount.setText("Total:" + hmList.size());

        if (socketClient.isConnected())
            socketClient.sendDataList(list);

    }  */


    //show the list
    @SuppressLint("SetTextI18n")
    protected void saedShowList() {
        if (!isStartPingPong)
            return;

//        String first = "" + 32154651 + x;
//        long second = x += 1L;
//
//        EpcModel epcModel = new EpcModel();
//        epcModel.setData(new Pair<>(first, second));
//        epcModel.setSent(sentEpc.contains((String) first));
//
//        adapterList.add(epcModel);
//
//        initAdapter(adapterList);
        initAdapter(saedGetData());

        if (lb_ReadTime != null) { // 刷新读取时间
            readTime++;
            lb_ReadTime.setText("Time:" + readTime + "S");
        }
        if (lb_ReadSpeed != null) { // 刷新读取速度
            if (flag) {
                speed = totalReadCount - lastReadCount;
                if (speed < 0)
                    speed = 0;
                lastReadCount = totalReadCount;
                lb_ReadSpeed.setText("SP:" + speed + "T/S");
            }
            // flag = !flag;
        }
        if (lb_TagCount != null) // 刷新标签总数
            lb_TagCount.setText("Total:" + hmList.size());

        if (socketClient.isConnected())
            socketClient.sendDataList(adapterList, sentEpc);
    }

    // Get the update data source (جلب البيانات من الجهاز وتحويلها لقائمة أوبجكت )
    @SuppressWarnings({"rawtypes", "unused"})
    protected List<Map<String, Object>> GetData() {
        List<Map<String, Object>> rt = new ArrayList<Map<String, Object>>();
        synchronized (hmList_Lock) {
            Iterator iter = hmList.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                String key = (String) entry.getKey();
                EPCModel val = (EPCModel) entry.getValue();
                Map<String, Object> map = new HashMap<String, Object>();
                if (_ReadType == 0)
                    map.put("EPC", val._EPC);
                else if (_ReadType == 1)
                    map.put("EPC", val._TID);
                else
                    map.put("EPC", val._UserData);

                map.put("ReadCount", val._TotalCount);
                rt.add(map);
            }
        }
        return rt;
    }

    ArrayList<EpcModel> adapterList = new ArrayList<>();

    // Get the update data source (جلب البيانات من الجهاز وتحويلها لقائمة أوبجكت )
    @SuppressWarnings({"rawtypes", "unused"})
    protected ArrayList<EpcModel> saedGetData() {
        synchronized (hmList_Lock) {
            Iterator iter = hmList.entrySet().iterator();

            adapterList.clear();

            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                String key = (String) entry.getKey();
                EPCModel val = (EPCModel) entry.getValue();
                String first;
                long second;
                if (_ReadType == 0)
                    first = val._EPC;
                else if (_ReadType == 1)
                    first = val._TID;
                else
                    first = val._UserData;

                second = val._TotalCount;

                EpcModel epcModel = new EpcModel();
                epcModel.setData(new Pair<>(first, second));
                epcModel.setSent(sentEpc.contains((String) first));

                adapterList.add(epcModel);
            }
        }
        return adapterList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.setContentView(R.layout.uhf_read);
        SharedPreference.getInstance(this);

        ip = SharedPreference.getIp().replaceAll("\\s+", "");
        port = SharedPreference.getPort();

//        ip = "192.168.1.103";
//        port = 8888;

        initSocket(ip, port);

//        SAED();

    }

//    Handler handler;
//    Runnable runnable;
//
//    public void SAED() {
//
//        runnable = () -> {
//            saedShowList();
//            handler.postDelayed(runnable, 5000);
//        };
//
//        handler = new Handler(getMainLooper());
//        handler.postDelayed(runnable, 10000);
//    }

    @Override
    protected void onStart() {
        super.onStart();
        showCustomBar(getString(R.string.tv_Read_Title),
                getString(R.string.str_back), null,
                R.drawable.left, 0,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Back(v);
                    }
                },
                null);
    }

    @Override
    protected void onDestroy() {
        //destroy
        super.onDestroy();
        this.tryConnect = false;
        this.socketClient.close();
//        if (handler != null)
//            handler.removeCallbacks(runnable);
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
        //super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("PDADemo", "onKeyDown keyCode = " + keyCode);
        if ((Adapt.DEVICE_TYPE_HY820 == Adapt.getDeviceType() && (keyCode == KeyEvent.KEYCODE_F9 /* RFID扳机*/ || keyCode == 285  /* 左快捷*/ || keyCode == 286  /* 右快捷*/))
                || ((Adapt.getSN().startsWith("K3")) && (keyCode == KeyEvent.KEYCODE_F1 || keyCode == KeyEvent.KEYCODE_F5))
                || ((Adapt.getSN().startsWith("K6")) && (keyCode == KeyEvent.KEYCODE_F1 || keyCode == KeyEvent.KEYCODE_F5))) { // 按下扳机

            btn_Read.setText(getString(R.string.btn_read_stop));
            sp_ReadType.setEnabled(false);
            btn_Read.setClickable(false);
            if (!isKeyDown) {
                isKeyDown = true; //
                if (!isStartPingPong) {
                    Clear(null);
                    Pingpong_Stop(); // stop
                    isStartPingPong = true;
                    String rt;
                    if (PublicData._IsCommand6Cor6B.equals("6C")) {// 6C
                        GetEPC_6C();
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
        Log.d("PDADemo", "onKeyUp keyCode = " + keyCode);
        if ((Adapt.DEVICE_TYPE_HY820 == Adapt.getDeviceType() && (keyCode == KeyEvent.KEYCODE_F9 /* RFID扳机*/ || keyCode == 285  /* 左快捷*/ || keyCode == 286  /* 右快捷*/))
                || ((Adapt.getSN().startsWith("K3")) && (keyCode == KeyEvent.KEYCODE_F1 || keyCode == KeyEvent.KEYCODE_F5))
                || ((Adapt.getSN().startsWith("K6")) && (keyCode == KeyEvent.KEYCODE_F1 || keyCode == KeyEvent.KEYCODE_F5))) { // 放开扳机

            CLReader.Stop();
            isStartPingPong = false;
            keyDownCount = 0;
            isKeyDown = false;
            isLongKeyDown = false;
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
                    tModel._TotalCount++;
                    model._TotalCount = tModel._TotalCount;
                    hmList.remove(model._EPC + model._TID);
                    hmList.put(model._EPC + model._TID, model);
                } else {
                    hmList.put(model._EPC + model._TID, model);
                }
            }
            synchronized (beep_Lock) {
                beep_Lock.notify();
            }
            totalReadCount++;
        } catch (Exception ex) {
            Log.d("Debug", "Tags output exceptions:" + ex.getMessage());
        }

    }

    // Start reading tag
    public void PingPong_Read() {
        if (isStartPingPong)
            return;
        isStartPingPong = true;
        Clear(null);
        Helper_ThreadPool.ThreadPool_StartSingle(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!isPowerLowShow) {
                        String rt;
                        if (PublicData._IsCommand6Cor6B.equals("6C")) {// Read 6C
                            GetEPC_6C();
                        } else {// Read 6B
                            rt = CLReader.Get6B(_NowAntennaNo + "|1" + "|1" + "|" + "1,000F");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Stop reading tag
    public void Pingpong_Stop() {
        isStartPingPong = false;
        CLReader.Stop();
    }

    //6C,Read tag
    private int GetEPC_6C() {
        int ret = -1;
        String text = tv_TitleTagID.getText().toString();
        if (text.equals("EPC")) {
            ret = UHFReader._Tag6C.GetEPC(_NowAntennaNo, 1);
        } else if (text.equals("TID")) {
            ret = UHFReader._Tag6C.GetEPC_TID(_NowAntennaNo, 1);
        } else if (text.equals("UserData")) {
            ret = UHFReader._Tag6C.GetEPC_TID_UserData(_NowAntennaNo, 1, 0, 6);
        }
        return ret;
    }


    //saed:
    public SocketClient socketClient = new SocketClient();
    /**
     * to checking if can reConnect with socket <p>
     * will be false when onDestroy Activity
     */
    public boolean tryConnect = true;
    /**
     * socket ip address
     */
    public String ip;
    /**
     * socket port
     */
    public int port;
    AdapterEpc adapter;
    public ArrayList<String> sentEpc = new ArrayList<>();

    /**
     * start connection with socket
     *
     * @param mIp   socket ip address
     * @param mPort socket port
     */
    public void initSocket(String mIp, int mPort) {
        new Thread(() -> { //لانه لا يمكن الاتصال من ال UI thread
            while (tryConnect) { // من أجل المحاولة والمحاولة حتى تمام عملية الاتصال

                //اذا الاتصال تم
                if (socketClient.connect(mIp, mPort)) {
                    runOnUiThread(() -> notConnectTv.setVisibility(View.GONE));
                    this.tryConnect = false;
                    break;
                } else // اذا لم يتصل
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }

        }).start();

        //call back active when connect stat change
        socketClient.setOnChangeConnectStatListener(connect -> {
            runOnUiThread(() -> {
                if (connect)
                    notConnectTv.setVisibility(View.GONE);
                else
                    notConnectTv.setVisibility(View.VISIBLE);
            });
        });
    }


    void initAdapter(ArrayList<EpcModel> list) {

        for (int i = 0; i < list.size(); i++)
            if (sentEpc.contains(list.get(i).data.first))
                list.get(i).setSent(true);

        if (adapter == null)
            adapter = new AdapterEpc(this, list);
        else {
            adapter.setAndRefresh(list);
            return;
        }

        initRecycler();
    }

    void initRecycler() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

}
