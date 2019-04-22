package com.tencent.ilivedemo.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.tencent.ilivedemo.R;
import com.tencent.ilivedemo.demos.DemoHost;

public class CreateLiveActivity extends Activity implements View.OnClickListener {

    private TextView createLive;
    private EditText inputRoomId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        initView();
        setListener();
    }

    private void setListener() {
        createLive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CreateLiveActivity.this, DemoHost.class).putExtra("room_id", inputRoomId.getText().toString()));
                finish();
            }
        });
    }

    private void initView() {
        createLive = (TextView) findViewById(R.id.create);
        inputRoomId = (EditText) findViewById(R.id.live_roomId);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_cancel:
                finish();
        }
    }
}
