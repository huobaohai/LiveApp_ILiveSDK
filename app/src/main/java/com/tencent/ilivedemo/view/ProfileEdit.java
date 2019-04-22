package com.tencent.ilivedemo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tencent.ilivedemo.R;

// 自定义可编辑的控件
public class ProfileEdit extends LinearLayout {

    private TextView mKeyView;
    private TextView mValueView;
    private ImageView mRightArrowView;

    public ProfileEdit(Context context) {
        super(context);
        init();
    }

    public ProfileEdit(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProfileEdit(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() { //将布局转为View;
        LayoutInflater.from(getContext()).inflate(R.layout.profile_view_item, this
                , true);
        findAllViews();
    }

    private void findAllViews() {
        mKeyView = (TextView) findViewById(R.id.profile_key);
        mValueView = (TextView) findViewById(R.id.profile_value);
        mRightArrowView = (ImageView) findViewById(R.id.right_arrow);
    }
    //设置每个横条的信息的内容
    public void set(String key) {
        mKeyView.setText(key);
    }

//    设置每个横条的信息的内容
    public void set(String key, String value) {
        mKeyView.setText(key);
        mValueView.setText(value);
    }

    public String getValue() {
        return mValueView.getText().toString();
    }

    //将箭头的隐藏掉，作为个人信息编辑也的下半部分的不可编辑的部分
    protected void disableEdit() {
        mRightArrowView.setVisibility(GONE);
    }
}
