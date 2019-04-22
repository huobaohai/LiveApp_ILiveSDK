package com.tencent.ilivedemo.demos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.tencent.av.sdk.AVContext;
import com.tencent.av.sdk.AVVideoCtrl;
import com.tencent.ilivedemo.R;
import com.tencent.ilivedemo.model.Constants;
import com.tencent.ilivedemo.model.MessageObservable;
import com.tencent.ilivedemo.model.StatusObservable;
import com.tencent.ilivedemo.model.UserInfo;
import com.tencent.ilivedemo.ui.MainActivity;
import com.tencent.ilivedemo.uiutils.DemoFunc;
import com.tencent.ilivedemo.uiutils.DlgMgr;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.ILiveConstants;
import com.tencent.ilivesdk.ILiveSDK;
import com.tencent.ilivesdk.adapter.CommonConstants;
import com.tencent.ilivesdk.core.ILiveLoginManager;
import com.tencent.ilivesdk.core.ILiveRoomManager;
import com.tencent.ilivesdk.core.ILiveRoomOption;
import com.tencent.ilivesdk.data.ILiveMessage;
import com.tencent.ilivesdk.data.msg.ILiveTextMessage;
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


public class DemoHost extends Activity implements View.OnClickListener, ILiveMessageListener, ILiveLoginManager.TILVBStatusListener{
    private final String TAG = "DemoHost";
    private TextView tvMsg;
    private ScrollView svScroll;
    private AVRootView arvRoot;
    private EditText hostSendMsg;
    private TextView hostName;

    private boolean isMicOn = true;
    private boolean isFlashOn = false;
    private boolean isInfoOn = true;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean isDanmu = true;

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
            if (null != qualityData){
                String info = "AVSDK 版本: \t"+ AVContext.sdkVersion+"\n\n"
                        +"上行速率:\t"+qualityData.getSendKbps()+"kbps\t"
                        +"上行丢包率:\t"+qualityData.getSendLossRate()/100+"%\n\n"
                        +"下行速率:\t"+qualityData.getRecvKbps()+"kbps\t"
                        +"下行丢包率:\t"+qualityData.getRecvLossRate()/100+"%\n\n"
                        +"应用CPU:\t"+qualityData.getAppCPURate()+"\t"
                        +"系统CPU:\t"+qualityData.getSysCPURate()+"\n\n";
                for (Map.Entry<String, LiveInfo> entry: qualityData.getLives().entrySet()){
                    info += "\t"+entry.getKey()+"-"+entry.getValue().getWidth()+"*"+entry.getValue().getHeight()+"\n\n";
                }
                ((TextView)findViewById(R.id.tv_status)).setText(info);
            }
            if (ILiveRoomManager.getInstance().isEnterRoom()) {
                mainHandler.postDelayed(infoRun, 2000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_host);

        UserInfo.getInstance().getCache(getApplicationContext());
        String room_ID = getIntent().getStringExtra("room_id");

        hostName = (TextView) findViewById(R.id.live_host_name);
        hostName.setText(room_ID);
        arvRoot = (AVRootView)findViewById(R.id.arv_root);
        tvMsg = (TextView)findViewById(R.id.tv_msg);
        svScroll = (ScrollView)findViewById(R.id.sv_scroll);
        hostSendMsg = (EditText) findViewById(R.id.host_say_something);
        danmakuView = (DanmakuView) findViewById(R.id.danmu_ku);

        ILiveRoomManager.getInstance().initAvRootView(arvRoot);
        MessageObservable.getInstance().addObserver(this);
        StatusObservable.getInstance().addObserver(this);

        initRoleDialog();

        arvRoot.setAutoOrientation(false);
        // 打开摄像头预览
        ILiveRoomManager.getInstance().enableCamera(ILiveConstants.FRONT_CAMERA, true);
        arvRoot.setSubCreatedListener(new AVRootView.onSubViewCreatedListener() {
            @Override
            public void onSubViewCreated() {
                arvRoot.renderVideoView(true, ILiveLoginManager.getInstance().getMyUserId(), CommonConstants.Const_VideoType_Camera, true);
            }
        });

        createRoom(room_ID);

        // 管理弹幕
        setDanmuku();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ILiveConstants.NONE_CAMERA != ILiveRoomManager.getInstance().getActiveCameraId()){
            ILiveRoomManager.getInstance().enableCamera(ILiveRoomManager.getInstance().getActiveCameraId(), false);
        }
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
        switch (view.getId()){
            case R.id.iv_switch:
                Log.v(TAG, "switch->cur: "+ILiveRoomManager.getInstance().getActiveCameraId()+"/"+ILiveRoomManager.getInstance().getCurCameraId());
                if (ILiveConstants.NONE_CAMERA != ILiveRoomManager.getInstance().getActiveCameraId()) {
                    ILiveRoomManager.getInstance().switchCamera(1 - ILiveRoomManager.getInstance().getActiveCameraId());
                }else{
                    ILiveRoomManager.getInstance().switchCamera(ILiveConstants.FRONT_CAMERA);
                }
                break;
            case R.id.iv_flash:
                toggleFlash();
                break;
            case R.id.iv_info:
                isInfoOn = !isInfoOn;
                ((ImageView)findViewById(R.id.iv_info)).setImageResource(isInfoOn ? R.mipmap.ic_info_on : R.mipmap.ic_info_off);
                findViewById(R.id.tv_status).setVisibility(isInfoOn ? View.VISIBLE : View.INVISIBLE);
                break;
            case R.id.iv_mic:
                isMicOn = !isMicOn;
                ILiveRoomManager.getInstance().enableMic(isMicOn);
                ((ImageView)findViewById(R.id.iv_mic)).setImageResource(
                        isMicOn ? R.mipmap.ic_mic_on : R.mipmap.ic_mic_off);
                break;
            case R.id.iv_danmu:
                //  开启弹幕
                isDanmu = !isDanmu;
                ((ImageView)findViewById(R.id.iv_danmu)).setImageResource(isDanmu ? R.mipmap.icon_message_pressed : R.mipmap.icon_message_png);
                if (isDanmu){
                    danmakuView.show();
                }else {
                    danmakuView.hide();
                }
                break;
            case R.id.iv_role:
                if (null != roleDialog)
                    roleDialog.show();
                break;
            case R.id.close_live:
                finish();
                break;
            case R.id.host_send_msg:
                sendMsg("host@" + hostSendMsg.getText().toString());
                break;
        }
    }

    @Override
    public void onNewMessage(ILiveMessage message) {
        switch (message.getMsgType()){
            case ILIVE_MSG_TYPE_TEXT:
                ILiveTextMessage textMessage = (ILiveTextMessage)message;
                addMessage(textMessage.getSender(), DemoFunc.getLimitString(textMessage.getText(), Constants.MAX_SIZE));
                break;
        }
    }

    @Override
    public void onForceOffline(int error, String message) {
        finish();
    }

    // 角色对话框
    private RadioGroupDialog roleDialog;
    private void initRoleDialog() {
        final String[] roles = new String[]{ "高清(960*540,25fps)","标清(640*368,20fps)", "流畅(640*368,15fps)"};
        final String[] values = new String[]{Constants.HD_ROLE, Constants.SD_ROLE, Constants.LD_ROLE};
        roleDialog = new RadioGroupDialog(this, roles);
        roleDialog.setTitle(R.string.str_dt_change_role);
        roleDialog.setSelected(0);

        roleDialog.setOnItemClickListener(new RadioGroupDialog.onItemClickListener() {
            @Override
            public void onItemClick(int position) {
                ILiveRoomManager.getInstance().changeRole(values[position], new ILiveCallBack() {
                    @Override
                    public void onSuccess(Object data) {}

                    @Override
                    public void onError(String module, int errCode, String errMsg) {
                        DlgMgr.showMsg(getContext(), "change failed:"+module+"|"+errCode+"|"+errMsg);
                    }
                });
            }
        });
    }


    private Context getContext(){
        return DemoHost.this;
    }

    // 添加消息
    private void addMessage(String sender, String msg){
        strMsg += "["+sender+"] : "+msg+"\n";
        tvMsg.setText(strMsg);
        addDanmaku(msg, false);
        svScroll.fullScroll(View.FOCUS_DOWN);
    }

    private void joinRoom(String room_ID){
        final int roomId = DemoFunc.getIntValue(room_ID, -1);
        if (-1 == roomId){
            DlgMgr.showMsg(getContext(), getString(R.string.str_tip_num_error));
            return;
        }
        ILiveRoomOption option = new ILiveRoomOption("")
                .autoCamera(ILiveConstants.NONE_CAMERA == ILiveRoomManager.getInstance().getActiveCameraId())
                .videoMode(ILiveConstants.VIDEOMODE_NORMAL)
                .controlRole(Constants.HD_ROLE)
                .autoFocus(true);
        ILiveRoomManager.getInstance().joinRoom(roomId, option, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                afterCreate();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                DlgMgr.showMsg(getContext(), "create failed:"+module+"|"+errCode+"|"+errMsg);
            }
        });
    }

    private void showChoiceDlg(final String room_ID){
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("房间已存在，是否加入房间？")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        joinRoom(room_ID);
                        dialogInterface.dismiss();
                    }
                });
        DlgMgr.showAlertDlg(this, builder);
    }

    // 加入房间
    private void createRoom(final String room_ID){
        final int roomId = DemoFunc.getIntValue(room_ID, -1);
        if (-1 == roomId){
            DlgMgr.showMsg(getContext(), getString(R.string.str_tip_num_error));
            return;
        }
        ILiveRoomOption option = new ILiveRoomOption(ILiveLoginManager.getInstance().getMyUserId())
                .autoCamera(true)
                .videoMode(ILiveConstants.VIDEOMODE_NORMAL)
                .controlRole(Constants.ROLE_MASTER)
                .autoFocus(true);
        ILiveRoomManager.getInstance().createRoom(roomId,
                option, new ILiveCallBack() {
                    @Override
                    public void onSuccess(Object data) {
                        afterCreate();
                    }

                    @Override
                    public void onError(String module, int errCode, String errMsg) {
                        if (module.equals(ILiveConstants.Module_IMSDK) && 10021 == errCode){
                            // 被占用，改加入
                            showChoiceDlg(room_ID);
                        }else {
                            DlgMgr.showMsg(getContext(), "create failed:" + module + "|" + errCode + "|" + errMsg);
                        }
                    }
                });
    }

    private void afterCreate(){
        UserInfo.getInstance().setRoom(ILiveRoomManager.getInstance().getRoomId());
        UserInfo.getInstance().writeToCache(this);
        findViewById(R.id.iv_flash).setVisibility(View.VISIBLE);
        findViewById(R.id.iv_mic).setVisibility(View.VISIBLE);
        findViewById(R.id.iv_info).setVisibility(View.VISIBLE);
        findViewById(R.id.iv_danmu).setVisibility(View.VISIBLE);
        findViewById(R.id.iv_role).setVisibility(View.VISIBLE);

        mainHandler.postDelayed(infoRun, 500);
    }

    private void toggleFlash(){
        if (ILiveConstants.BACK_CAMERA != ILiveRoomManager.getInstance().getActiveCameraId()){
            return;
        }
        AVVideoCtrl videoCtrl = ILiveSDK.getInstance().getAvVideoCtrl();
        if (null == videoCtrl) {
            return;
        }

        final Object cam = videoCtrl.getCamera();
        if ((cam == null) || (!(cam instanceof Camera))) {
            return;
        }
        final Camera.Parameters camParam = ((Camera) cam).getParameters();
        if (null == camParam) {
            return;
        }

        Object camHandler = videoCtrl.getCameraHandler();
        if ((camHandler == null) || (!(camHandler instanceof Handler))) {
            return;
        }

        //对摄像头的操作放在摄像头线程
        if (isFlashOn == false) {
            ((Handler) camHandler).post(new Runnable() {
                public void run() {
                    try {
                        camParam.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        ((Camera) cam).setParameters(camParam);
                        isFlashOn = true;
                    } catch (RuntimeException e) {
                        Log.d(TAG, "setParameters->RuntimeException");
                    }
                }
            });
        } else {
            ((Handler) camHandler).post(new Runnable() {
                public void run() {
                    try {
                        camParam.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        ((Camera) cam).setParameters(camParam);
                        isFlashOn = false;
                    } catch (RuntimeException e) {
                        Log.d(TAG, "setParameters->RuntimeException");
                    }
                }
            });
        }
    }

    // 发送消息
    private void sendMsg(final String strMsg){
        if (TextUtils.isEmpty(strMsg)){
            DlgMgr.showMsg(this, getString(R.string.msg_send_empty));
            return;
        }else if (strMsg.length() > Constants.MAX_SIZE){
            DlgMgr.showMsg(this, getString(R.string.str_send_limit));
            return;
        }
        String[] msgs = strMsg.split("@");
        String trueMsg = "";
        if (msgs.length>1){
            trueMsg = msgs[1];
        }else {
            trueMsg = strMsg;
        }
        ILiveTextMessage textMessage = new ILiveTextMessage(strMsg);
        final String finalTrueMsg = trueMsg;
        ILiveRoomManager.getInstance().sendGroupMessage(textMessage, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                addHostMessage(finalTrueMsg);
                hostSendMsg.setText("");
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(hostSendMsg.getWindowToken(), 0);
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                DlgMgr.showMsg(getContext(), "sendText failed:"+module+"|"+errCode+"|"+errMsg);
            }
        });
    }

    // 添加消息
    private void addHostMessage(String msg){
        strMsg += "主播 [我] : "+msg+"\n";
        tvMsg.setText(strMsg);
        addDanmaku(msg,true);
        svScroll.fullScroll(View.FOCUS_DOWN);
    }

    private void setDanmuku() {

        danmakuView.enableDanmakuDrawingCache(true);
        danmakuView.setCallback(new DrawHandler.Callback() {
            @Override
            public void prepared() {

                if (isDanmu){
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
        danmakuView.prepare(parser,danmakuContext);

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
        if (b){
            danmaku.borderColor = Color.RED;
        }
        danmakuView.addDanmaku(danmaku);
    }
}
