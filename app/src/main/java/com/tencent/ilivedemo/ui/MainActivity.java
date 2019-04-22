package com.tencent.ilivedemo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TabHost;

import com.tencent.ilivedemo.R;

public class MainActivity extends AppCompatActivity {

    private FrameLayout container;
    private FragmentTabHost tabHost;
    private android.support.v4.app.FragmentManager fManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        container = (FrameLayout) findViewById(R.id.fragment_container);
        tabHost = (FragmentTabHost) findViewById(R.id.tab_host);

        setTabHost();
    }

    private void setTabHost() {

        tabHost.setup(this, getSupportFragmentManager(),R.id.fragment_container);

        //添加fragment

        {
            TabHost.TabSpec profileTab = tabHost.newTabSpec("livelist").setIndicator(getIndicator(R.drawable.tab_live));
            tabHost.addTab(profileTab, LiveListFragment.class, null);
            tabHost.getTabWidget().setDividerDrawable(null);
        }

        {
            TabHost.TabSpec profileTab = tabHost.newTabSpec("createlive").setIndicator(getIndicator(R.drawable.icon_publish));
            tabHost.addTab(profileTab, null, null);
            tabHost.getTabWidget().setDividerDrawable(null);
        }

        {
            TabHost.TabSpec profileTab = tabHost.newTabSpec("profile").setIndicator(getIndicator(R.drawable.tab_profile));
            tabHost.addTab(profileTab, EditProfileFragment.class, null);
            tabHost.getTabWidget().setDividerDrawable(null);
        }

        tabHost.getTabWidget().getChildTabViewAt(1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 开启自己的直播
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, CreateLiveActivity.class);
                startActivity(intent);
            }
        });
    }

    //指示器的设置
    private View getIndicator(int resId) {
        View tabView = LayoutInflater.from(this).inflate(R.layout.view_indicator, null);
        ImageView tabImg = (ImageView) tabView.findViewById(R.id.tab_icon);
        tabImg.setImageResource(resId);
        return tabView;
    }
}
