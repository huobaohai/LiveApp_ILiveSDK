package com.tencent.ilivedemo;

import android.app.Application;

import com.tencent.TIMUserProfile;
import com.tencent.ilivedemo.model.MessageObservable;
import com.tencent.ilivesdk.ILiveConstants;
import com.tencent.ilivesdk.ILiveSDK;
import com.tencent.ilivesdk.core.ILiveLog;
import com.tencent.ilivesdk.core.ILiveRoomConfig;
import com.tencent.ilivesdk.core.ILiveRoomManager;
import com.tencent.qalsdk.sdk.MsfSdkUtils;

public class DemoApp extends Application {

    private static DemoApp app;
    private TIMUserProfile mSelfProfile;

    @Override
    public void onCreate() {
        super.onCreate();

        app = this;
        if(MsfSdkUtils.isMainProcess(this)){    // 仅在主线程初始化
            // 初始化LiveSDK
            ILiveSDK.getInstance().setCaptureMode(ILiveConstants.CAPTURE_MODE_SURFACETEXTURE);
            ILiveLog.setLogLevel(ILiveLog.TILVBLogLevel.DEBUG);
            ILiveSDK.getInstance().initSdk(this, 1400028096);
            ILiveRoomManager.getInstance().init(new ILiveRoomConfig()
                    .setRoomMsgListener(MessageObservable.getInstance()));
        }
    }

    public static DemoApp getApp(){
        return app;
    }

    public void saveSelfProfile(TIMUserProfile info){
        mSelfProfile = info;
    }

    public TIMUserProfile getProfile(){
        return mSelfProfile;
    }
}
