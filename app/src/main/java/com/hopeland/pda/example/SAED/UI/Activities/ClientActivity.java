//package com.hopeland.pda.example.SAED.UI.Activities;
//
//import android.annotation.SuppressLint;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.SharedPreferences;
//import android.content.pm.ActivityInfo;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.os.Message;
//
//import android.util.Log;
//import android.view.KeyEvent;
//import android.view.View;
//import android.view.WindowManager;
//import android.widget.FrameLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.lifecycle.Observer;
//import androidx.lifecycle.ViewModelProviders;
//
//import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;
//import com.hopeland.pda.example.R;
//import com.hopeland.pda.example.SAED.AppConfig.FC;
//import com.hopeland.pda.example.SAED.AppConfig.SharedPreference;
//import com.hopeland.pda.example.SAED.Helpers.Converters.GzipConverter;
//import com.hopeland.pda.example.SAED.Helpers.Images.ConverterImage;
//import com.hopeland.pda.example.SAED.Helpers.NoteMessage;
//import com.hopeland.pda.example.SAED.Helpers.View.FTH;
//import com.hopeland.pda.example.SAED.Network.SaedSocket;
//import com.hopeland.pda.example.SAED.UI.Fragments.Client.ClientFragment;
//import com.hopeland.pda.example.SAED.ViewModels.All;
//import com.hopeland.pda.example.SAED.ViewModels.MyViewModel;
//import com.hopeland.pda.example.SAED.ViewModels.Product;
//
//import org.java_websocket.client.WebSocketClient;
//import org.java_websocket.handshake.ServerHandshake;
//import org.jetbrains.annotations.NotNull;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.nio.ByteBuffer;
//
//import java.util.ArrayList;
//import java.util.Set;
//
//import okhttp3.internal.Util;
//
//@SuppressLint("NonConstantResourceId")
//public class ClientActivity extends AppCompatActivity {
//
//    //region GLOBAL
//
//    //region Views
//
//    FrameLayout clientContainer;
//    TextView notConnectTv;
//
//    //endregion
//
//    //region connection config
//    /**
//     * socket ip address
//     */
//    public String ip;
//    /**
//     * socket port
//     */
//    public int port;
//    URI uri;
//
//    //endregion
//
//    //region sockets
//    WebSocketClient webSocketClient;
//    public SaedSocket socket;
//    //endregion
//
//    public MyViewModel myViewModel;
//    Gson gson = new Gson();
//
//    OnReadTag onReadTag;
//
//    //endregion
//
//    private final String TAG = "SAED_";
//
//    @SuppressLint("SourceLockedOrientationActivity")
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_client);
//
//        SharedPreference.getInstance(this);
//
//        myViewModel = ViewModelProviders.of(this).get(MyViewModel.class);
//
//        startClientFragment();
//
//        ip = SharedPreference.getIp().replaceAll("\\s+", "");
//        port = SharedPreference.getPort();
//
//        if (ip == null || ip.isEmpty())
//            return;
//
//        initSocket(ip, port);
//
//        initView();
//    }
//
//    void initView() {
//        clientContainer = findViewById(R.id.client_container);
//        notConnectTv = findViewById(R.id.not_connect);
//    }
//
//    //region getData
//
//    public void getAll() {
//        myViewModel.getAll(socket);
//    }
//
//    public void getProduct(String epc) {
//        myViewModel.getProduct(socket, epc);
//    }
//
//    //endregion
//
//    //region observers
//    final Observer<Boolean> observer = isConnect -> {
//
//        if (isConnect == null)
//            return;
//        if (isConnect)
//            notConnectTv.setVisibility(View.GONE);
//        else
//            notConnectTv.setVisibility(View.VISIBLE);
//    };
//
////endregion
//
//    //region socket
//
//    /**
//     * start connection with socket
//     *
//     * @param mIp   socket ip address
//     * @param mPort socket port
//     */
//    public void initSocket(String mIp, int mPort) {
//
//        initUri(mIp, mPort);
//
//        webSocketClient = new WebSocketClient(uri) {
//            @Override
//            public void onOpen(ServerHandshake h) {
//                Log.d(TAG, "onOpen: ");
//
//                getAll();
//
//                handler.sendEmptyMessage(201);
//            }
//
//            @Override
//            public void onMessage(String message) {
//
//                if (message.trim().equals("401")) {
//                    handler.sendEmptyMessage(401);
//                    return;
//                }
//
//                if (message.trim().equals("200"))
//                    handler.sendEmptyMessage(1000);
//
//            }
//
//            @Override
//            public void onClose(int code, String reason, boolean remote) {
//                Log.d(TAG, "onClose: ");
//                handler.sendEmptyMessage(202);
//            }
//
//            @Override
//            public void onError(Exception ex) {
//                Log.e(TAG, "onError: ", ex);
//                handler.sendEmptyMessage(500);
//            }
//
//            @Override
//            public void onMessage(ByteBuffer bytes) {
//                Log.d(TAG, "onMessage: have message ");
//                byte[] bytes1 = bytes.array();
//                String message = null;
//
//                try {
//                    message = GzipConverter.decompress(bytes1);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                if (message == null) {
//                    handler.sendEmptyMessage(500);
//                    return;
//                }
//
//                if (message.trim().equals("404")) {
//                    handler.sendEmptyMessage(404);
//                    return;
//                }
//
//                if (message.trim().equals("401")) {
//                    handler.sendEmptyMessage(401);
//                    return;
//                }
//
//                if (message.trim().equals("200")) {
//                    handler.sendEmptyMessage(1000);
//                    return;
//                }
//
//                if (message.charAt(0) == '[') {
//
//                    ArrayList<All> alls = gson.fromJson(message,
//                            new TypeToken<ArrayList<All>>() {
//                            }.getType());
//
//                    myViewModel.allLiveData.postValue(alls);
//
//                    return;
//                }
//
//                Product product = gson.fromJson(message, Product.class);
//                product.bitmap = ConverterImage.convertBase64ToBitmap(product.im);
//                product.bitmap = ConverterImage.getResizedBitmap(product.bitmap,300);
//                myViewModel.productLiveData.postValue(product);
//
//                handler.sendEmptyMessage(200);
//            }
//        };
//
//        socket = new SaedSocket(webSocketClient);
//    }
//
//    private void initUri(String mIp, int mPort) {
//        try {
//            uri = new URI("ws://" + mIp + ":" + mPort);
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }
//    }
//
//    //endregion
//
//    //region handler
//
//    private final Handler handler = new Handler(Looper.getMainLooper()) {
//        @Override
//        public void handleMessage(@NonNull Message msg) {
//            switch (msg.what) {
//                case 404:
//                    Toast.makeText(ClientActivity.this,
//                            "not found", Toast.LENGTH_SHORT).show();
//                    break;
//
//                case 500:
//                    Toast.makeText(ClientActivity.this,
//                            "have error", Toast.LENGTH_SHORT).show();
//                    break;
//
//                case 200:
//
//                    break;
//
//                case 201:
//                    myViewModel.connectLiveData.postValue(true);
//                    break;
//
//                case 202:
//
//                    handler.postDelayed(() -> {
//                        if (!socket.isClosed) {
//                            webSocketClient.setConnectionLostTimeout(25);
//                            webSocketClient.reconnect();
//                            Log.d(TAG, "handleMessage: reconnect");
//                        }
//                    }, 30000);
//
//                    myViewModel.connectLiveData.postValue(false);
//                    break;
//
//                case 401:
//                    myViewModel.sendReportLiveData.setValue(false);
//                    break;
//
//                case 1000:
//                    myViewModel.sendReportLiveData.setValue(true);
//
//            }
//        }
//    };
//
//    //endregion
//
//    //region process
//
//    public boolean isStart = false;
//
//
//    public void read() {
//
//    }
//
//    public void stop() {
//
//    }
//
//    //endregion
//
//    //region handler process
//    private final Handler handler1 = new Handler(Looper.getMainLooper()) {
//        @SuppressLint("HandlerLeak")
//        @Override
//        public void handleMessage(Message msg) {
//            if (onReadTag == null)
//                return;
//
//            switch (msg.what) {
//
//                case 1:
//
//                    String epc = msg.getData().getString("data");
//                    int rssi = msg.getData().getInt("rssi");
//
//                    if (epc == null || epc.length() == 0)
//                        return;
//
//                    onReadTag.onRead(epc, rssi);
//
//                    break;
//
//                case 1980:
//                    break;
//            }
//        }
//    };
//
//    //endregion
//
//    void startClientFragment() {
//        FTH.replaceFadFragment(FC.CLIENT_C,
//                this, new ClientFragment());
//    }
//
//    void removeObservers() {
//        myViewModel.connectLiveData.removeObserver(observer);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        removeObservers();
//        stop();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        if (webSocketClient != null && webSocketClient.isClosed())
//            webSocketClient.reconnect();
//
//        myViewModel.connectLiveData.observeForever(observer);
//    }
//
//
//
//    @Override
//    protected void onDestroy() {
//        if (socket != null)
//            socket.close();
//
//        super.onDestroy();
//
//    }
//
//
//    public interface OnReadTag {
//        void onRead(@NotNull String epc, int rssi);
//    }
//}