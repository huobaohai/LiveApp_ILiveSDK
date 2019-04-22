package com.tencent.ilivedemo.demos;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.av.sdk.AVContext;
import com.tencent.ilivedemo.R;
import com.tencent.ilivedemo.model.Constants;
import com.tencent.ilivedemo.model.MessageObservable;
import com.tencent.ilivedemo.model.StatusObservable;
import com.tencent.ilivedemo.model.UserInfo;
import com.tencent.ilivedemo.uiutils.DemoFunc;
import com.tencent.ilivedemo.uiutils.DlgMgr;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.ILiveConstants;
import com.tencent.ilivesdk.ILiveSDK;
import com.tencent.ilivesdk.core.ILiveLoginManager;
import com.tencent.ilivesdk.core.ILiveRoomManager;
import com.tencent.ilivesdk.core.ILiveRoomOption;
import com.tencent.ilivesdk.data.ILiveMessage;
import com.tencent.ilivesdk.data.msg.ILiveTextMessage;
import com.tencent.ilivesdk.listener.ILiveEventHandler;
import com.tencent.ilivesdk.listener.ILiveMessageListener;
import com.tencent.ilivesdk.tools.quality.ILiveQualityData;
import com.tencent.ilivesdk.tools.quality.LiveInfo;
import com.tencent.ilivesdk.view.AVRootView;

import java.util.Map;

import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.ui.widget.DanmakuView;

import static com.tencent.ilivesdk.data.ILiveMessage.ILIVE_MSG_TYPE_TEXT;


public class DemoGuest extends Activity implements View.OnClickListener, ILiveMessageListener, ILiveLoginManager.TILVBStatusListener {
    private final String TAG = "DemoGuest";
    private EditText etMsg;
    private AVRootView arvRoot;
    private TextView tvMsg;
    private ScrollView svScroll;
    private TextView hostName;
    private TextView hostTitle;

    private boolean isInfoOn = true;
    private boolean isDanmu = true;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private String strMsg = "";

    private DanmakuView danmakuView;
    private DanmakuContext danmakuContext;
    private BaseDanmakuParser parser = new BaseDanmakuParser() {
        @Override
        protected IDanmakus parse() {
            return new Danmakus();
        }
    };

    private Runnable infoRun = new Runnable() {
        @Override
        public void run() {
            ILiveQualityData qualityData = ILiveRoomManager.getInstance().getQualityData();
            if (null != qualityData) {
                String info = "AVSDK 版本: \t" + AVContext.sdkVersion + "\n\n"
                        + "上行速率:\t" + qualityData.getSendKbps() + "kbps\t"
                        + "上行丢包率:\t" + qualityData.getSendLossRate() / 100 + "%\n\n"
                        + "下行速率:\t" + qualityData.getRecvKbps() + "kbps\t"
                        + "下行丢包率:\t" + qualityData.getRecvLossRate() / 100 + "%\n\n"
                        + "应用CPU:\t" + qualityData.getAppCPURate() + "\t"
                        + "系统CPU:\t" + qualityData.getSysCPURate() + "\n\n";
                for (Map.Entry<String, LiveInfo> entry : qualityData.getLives().entrySet()) {
                    info += "\t" + entry.getKey() + "-" + entry.getValue().getWidth() + "*" + entry.getValue().getHeight() + "\n\n";
                }
                ((TextView) findViewById(R.id.tv_status)).setText(info);
            }
            if (ILiveRoomManager.getInstance().isEnterRoom()) {
                mainHandler.postDelayed(infoRun, 2000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_guest);

        String room_id = getIntent().getStringExtra("room_ID");
        UserInfo.getInstance().getCache(getApplicationContext());

        arvRoot = (AVRootView) findViewById(R.id.arv_root);
        hostName = (TextView) findViewById(R.id.live_host_name);
        hostName.setText(room_id);

        hostTitle = (TextView) findViewById(R.id.live_host_title);
        etMsg = (EditText) findViewById(R.id.guest_say_something);
        tvMsg = (TextView) findViewById(R.id.tv_msg);
        svScroll = (ScrollView) findViewById(R.id.sv_scroll);
        danmakuView = (DanmakuView) findViewById(R.id.danmu_ku);

        ILiveRoomManager.getInstance().initAvRootView(arvRoot);
        MessageObservable.getInstance().addObserver(this);
        StatusObservable.getInstance().addObserver(this);

        joinRoom(room_id);

        // 管理弹幕
        setDanmuku();

        ILiveSDK.getInstance().addEventHandler(new ILiveEventHandler(){
            @Override
            public void onGroupDisband(int roomId, String groupId) {
                if (groupId.equals(ILiveRoomManager.getInstance().getIMGroupId())) {
                    DlgMgr.showMsg(getContenxt(), getString(R.string.str_tips_discuss)).setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            finish();
                        }
                    });
                }
            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
        ILiveRoomManager.getInstance().onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ILiveRoomManager.getInstance().onResume();
    }

    private Context getContenxt(){
        return this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MessageObservable.getInstance().deleteObserver(this);
        StatusObservable.getInstance().deleteObserver(this);
        ILiveRoomManager.getInstance().onDestory();
        isDanmu = false;
        if (danmakuView != null) {
            danmakuView.release();
            danmakuView = null;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.guest_send_msg:
                sendMsg();
                break;
            case R.id.close_live:
                finish();
                break;
            case R.id.iv_info:
                isInfoOn = !isInfoOn;
                ((ImageView) findViewById(R.id.iv_info)).setImageResource(isInfoOn ? R.mipmap.ic_info_on : R.mipmap.ic_info_off);
                findViewById(R.id.tv_status).setVisibility(isInfoOn ? View.VISIBLE : View.INVISIBLE);
                break;
            case R.id.iv_danmu:
                //  开启弹幕
                isDanmu = !isDanmu;
                ((ImageView) findViewById(R.id.iv_danmu)).setImageResource(isDanmu ? R.mipmap.icon_message_pressed : R.mipmap.icon_message_png);
                if (isDanmu) {
                    danmakuView.show();
                } else {
                    danmakuView.hide();
                }
                break;
        }
    }

    @Override
    public void onNewMessage(ILiveMessage message) {
        switch (message.getMsgType()) {
            case ILIVE_MSG_TYPE_TEXT:
                ILiveTextMessage textMessage = (ILiveTextMessage) message;
                addMessage(textMessage.getSender(), DemoFunc.getLimitString(textMessage.getText(), Constants.MAX_SIZE), false);
                break;
        }
    }

    @Override
    public void onForceOffline(int error, String message) {
        finish();
    }

    private Context getContext() {
        return this;
    }

    // 添加消息
    private void addMessage(String sender, String msg, boolean self) {
        String[] msgs = msg.split("@");
        String trueMsg = "";
        if (msgs.length>1){
            trueMsg = msgs[1];
        }else {
            trueMsg = msg;
        }
        if (msgs.length > 1 && msgs[0].equals("host") && !TextUtils.isEmpty(msgs[1])) {
            strMsg += "主播 [" + sender + "] : " + msgs[1] + "\n";
            addDanmaku(trueMsg, false);
        } else if (self) {
            strMsg += "[我] : " + msg + "\n";
            addDanmaku(trueMsg, true);
        } else {
            strMsg += "[" + sender + "] : " + msg + "\n";
            addDanmaku(trueMsg, false);

        }
        tvMsg.setText(strMsg);

        svScroll.fullScroll(View.FOCUS_DOWN);
    }

    // 加入房间
    private void joinRoom(String room_id) {
        int roomId = DemoFunc.getIntValue(room_id, -1);
        if (-1 == roomId) {
            DlgMgr.showMsg(getContext(), getString(R.string.str_tip_num_error));
            return;
        }
        ILiveRoomOption option = new ILiveRoomOption("")
                .controlRole(Constants.ROLE_GUEST)
                .videoMode(ILiveConstants.VIDEOMODE_NORMAL)
                .autoCamera(false)
                .autoMic(false);
        ILiveRoomManager.getInstance().joinRoom(roomId,
                option, new ILiveCallBack() {
                    @Override
                    public void onSuccess(Object data) {
                        afterJoin();
                    }

                    @Override
                    public void onError(String module, int errCode, String errMsg) {
                        DlgMgr.showMsg(getContext(), "create failed:" + module + "|" + errCode + "|" + errMsg);
                    }
                });

    }

    private void afterJoin() {
        UserInfo.getInstance().setRoom(ILiveRoomManager.getInstance().getRoomId());
        UserInfo.getInstance().writeToCache(this);
        findViewById(R.id.iv_danmu).setVisibility(View.VISIBLE);
        findViewById(R.id.iv_info).setVisibility(View.VISIBLE);

        mainHandler.postDelayed(infoRun, 500);
    }

    // 发送消息
    private void sendMsg() {
        final String strMsg = etMsg.getText().toString();
        if (TextUtils.isEmpty(strMsg)) {
            DlgMgr.showMsg(this, getString(R.string.msg_send_empty));
            return;
        } else if (strMsg.length() > Constants.MAX_SIZE) {
            DlgMgr.showMsg(this, getString(R.string.str_send_limit));
            return;
        }

        ILiveTextMessage textMessage = new ILiveTextMessage(strMsg);
        ILiveRoomManager.getInstance().sendGroupMessage(textMessage, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                addMessage(ILiveLoginManager.getInstance().getMyUserId(), strMsg, true);
                etMsg.setText("");
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etMsg.getWindowToken(), 0);
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                DlgMgr.showMsg(getContext(), "sendText failed:" + module + "|" + errCode + "|" + errMsg);
            }
        });
    }

    private void setDanmuku() {

        danmakuView.enableDanmakuDrawingCache(true);
        danmakuView.setCallback(new DrawHandler.Callback() {
            @Override
            public void prepared() {

                if (isDanmu) {
                    danmakuView.start();
                }
            }

            @Override
            public void updateTimer(DanmakuTimer timer) {

            }

            @Override
            public void danmakuShown(BaseDanmaku danmaku) {

            }

            @Override
            public void drawingFinished() {

            }
        });
        danmakuContext = DanmakuContext.create();
        danmakuView.prepare(parser, danmakuContext);

    }

    // 转换单位
    public int sp2px(float spValue) {
        final float fontScale = getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    private void addDanmaku(String content, boolean b) {
        BaseDanmaku danmaku = danmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        danmaku.text = content;
        danmaku.padding = 10;
        danmaku.textColor = Color.WHITE;
        danmaku.textSize = sp2px(20);
        danmaku.setTime(danmakuView.getCurrentTime());
        if (b) {
            danmaku.borderColor = Color.RED;
        }
        danmakuView.addDanmaku(danmaku);
    }
}
