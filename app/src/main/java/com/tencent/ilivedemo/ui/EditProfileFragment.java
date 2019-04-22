package com.tencent.ilivedemo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.TIMFriendshipManager;
import com.tencent.TIMUserProfile;
import com.tencent.TIMValueCallBack;
import com.tencent.ilivedemo.DemoApp;
import com.tencent.ilivedemo.R;
import com.tencent.ilivedemo.uiutils.DlgMgr;
import com.tencent.ilivedemo.view.ProfileEdit;
import com.tencent.ilivedemo.view.ProfileText;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.core.ILiveLoginManager;

public class EditProfileFragment extends Fragment {

    private ImageView mAvatarImg;
    private TextView mNickNameTv;
    private TextView mIdTv;
    private ProfileEdit mGenderEdt;
    private ProfileEdit mSignEdt;
    private ProfileEdit mRenzhengEdt;
    private ProfileEdit mLocationEdt;

    private ProfileText mLevelView;

    private TextView logoutBtn;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.profileframent_layout, container, false);
        findAllViews(mainView);
        setListeners();
        getSelfProfile();
        setIconKey();//设置字段和icon
        return mainView;
    }

    private void getSelfProfile() {

        TIMUserProfile profile = DemoApp.getApp().getProfile();
        if (profile==null){
            TIMFriendshipManager.getInstance().getSelfProfile(new TIMValueCallBack<TIMUserProfile>() {
                @Override
                public void onError(int i, String s) {
                    Toast.makeText(getContext(), "获取个人信息失败： " + i + " : " + s , Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(TIMUserProfile timUserProfile) {
                    DemoApp.getApp().saveSelfProfile(timUserProfile);
                    getSelfProfile();
                }
            });
        }else {
            mNickNameTv.setText(TextUtils.isEmpty(profile.getNickName()) ? profile.getIdentifier() : profile.getNickName());
            mIdTv.setText("ID: " + profile.getIdentifier());
        }
    }

    private void setIconKey() {
        mGenderEdt.set("性别");
        mSignEdt.set("签名");
        mRenzhengEdt.set("认证");
        mLocationEdt.set("地区");
        mLevelView.set("等级");
    }

    private void findAllViews(View view) {

        mAvatarImg = (ImageView) view.findViewById(R.id.profile_avatar);
        mNickNameTv = (TextView) view.findViewById(R.id.profile_name);
        mIdTv = (TextView) view.findViewById(R.id.profile_id);
        mGenderEdt = (ProfileEdit) view.findViewById(R.id.gender);
        mSignEdt = (ProfileEdit) view.findViewById(R.id.sign);
        mRenzhengEdt = (ProfileEdit) view.findViewById(R.id.renzheng);
        mLocationEdt = (ProfileEdit) view.findViewById(R.id.location);

        mLevelView = (ProfileText) view.findViewById(R.id.level);

        logoutBtn = (TextView) view.findViewById(R.id.tv_logout);
    }

    private void setListeners() {

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ILiveLoginManager.getInstance().iLiveLogout(new ILiveCallBack() {
                    @Override
                    public void onSuccess(Object data) {
                        startActivity(new Intent(getActivity(), LoginActivity.class));
                        getActivity().finish();
                    }

                    @Override
                    public void onError(String module, int errCode, String errMsg) {
                        DlgMgr.showMsg(getContext(), "logout error : " + errCode + "|" + errMsg);
                    }
                });
            }
        });

        // 其他控件待实现

    }
}
