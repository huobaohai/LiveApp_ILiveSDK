package com.tencent.ilivedemo.model;

public class Constants {
    // 存储
    public static final String USERINFO = "userInfo";
    public static final String ACCOUNT = "account";
    public static final String PWD = "password";
    public static final String ROOM = "room";

    // 角色
    public static final String ROLE_MASTER = "LiveMaster";
    public static final String ROLE_GUEST = "Guest";
    public static final String ROLE_LIVEGUEST = "LiveGuest";

    public static final String HD_ROLE = "HD";
    public static final String SD_ROLE = "SD";
    public static final String LD_ROLE = "LD";
    public static final String HD_GUEST_ROLE = "HDGuest";
    public static final String SD_GUEST_ROLE = "SDGuest";
    public static final String LD_GUEST_ROLE = "LDGuest";

    // 直播业务id和appid，可在控制台的 直播管理中查看
    public static final int BIZID = 8525;
    public static final int APPID = 1253488539;     // 直播appid

    // 直播的API鉴权Key，可在控制台的 直播管理 => 接入管理 => 直播码接入 => 接入配置 中查看
    public static final String MIX_API_KEY = "45eeb9fc2e4e6f88b778e0bbd9de3737";
    // 固定地址
    public static final String MIX_SERVER = "http://fcgi.video.qcloud.com";

    public static final int MAX_SIZE = 50;

}
