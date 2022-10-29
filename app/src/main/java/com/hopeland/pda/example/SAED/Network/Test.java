package com.hopeland.pda.example.SAED.Network;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;

public class Test extends Thread {

    private boolean threadKill = true;
    private final Object lockSend = new Object();

    WebSocketClient webSocketClient;
    private String s;

    public Test(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

    @Override
    public void run() {

        synchronized (lockSend) {

            while (threadKill) {
                Log.d("SAED___", "send: "+s) ;
                try {
                    lockSend.wait();
                } catch (InterruptedException ignored) {
                }

                if (!threadKill)
                    break;

                if (!webSocketClient.isOpen())
                    continue;

                webSocketClient.send(s);

            }
        }
    }


    public void sendString(String s) {

        this.s = s;

        synchronized (lockSend) {
            lockSend.notify();
        }

    }

    public void killSelf() {
        this.threadKill = false;
        synchronized (lockSend) {
            lockSend.notify();
        }
    }
}
