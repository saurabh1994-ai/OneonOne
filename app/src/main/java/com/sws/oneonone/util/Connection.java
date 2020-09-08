package com.sws.oneonone.util;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Connection {
    private Socket mSocket;

    boolean isUpdate = false;

    public static Connection instance = new Connection();

    private Connection() {
    }

    public static Connection getInstance() {
        if (instance == null)
            instance = new Connection();
        return instance;
    }

   public void connectSocket(String id) {
        {
            try {
                IO.Options options = new IO.Options();
                options.forceNew = true;
                options.reconnection = true;

                options.query = "token=" +  id; // //PreferenceStore.Companion.getInstance(ApplicationLoader.applicationContext).getUserId();
                mSocket = IO.socket("http://54.169.45.113:3006", options);
                mSocket.connect();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

   public boolean socketStatus() {
        if (mSocket != null) {
            return true;

        } else {
            return false;
        }

    }





    public void StopSocket() {
        if (mSocket != null) {
            mSocket.disconnect();
        }

    }


        public void ListenerOff() {
            if (mSocket != null) {
                mSocket.off("comment", comment);
                isUpdate = true;
            }
        }



    public void
    onReceived() {
        if (mSocket != null) {
            Log.e("Socket_onReceived", socketStatus() + " checked");
            mSocket.on("comment", comment);

        }

    }



    private Emitter.Listener comment = args -> {

        JSONObject data = (JSONObject) args[0];
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.commentData, data);
            Log.e("privateJobRequest", "Receiving now");
        });
    };

}