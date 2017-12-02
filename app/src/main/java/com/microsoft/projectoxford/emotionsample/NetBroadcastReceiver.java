package com.microsoft.projectoxford.emotionsample;

/**
 * Created by mac on 17/4/25.
 * source: http://www.cnblogs.com/zyw-205520/p/3770705.html
 */


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


import java.util.ArrayList;

import com.microsoft.projectoxford.emotionsample.NetUtil;


public class NetBroadcastReceiver extends BroadcastReceiver {
    public static ArrayList<netEventHandler> mListeners = new ArrayList<netEventHandler>();
    private static String NET_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(NET_CHANGE_ACTION)) {
            Application.mNetWorkState = NetUtil.getNetworkState(context);
            if (mListeners.size() > 0)
                for (netEventHandler handler : mListeners) {
                    handler.onNetChange();
                }
        }
    }

    public static abstract interface netEventHandler {

        public abstract void onNetChange();
    }
}