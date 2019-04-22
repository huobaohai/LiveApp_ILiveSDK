package com.tencent.ilivedemo.view;

import android.content.Context;
import android.util.AttributeSet;

// 自定义不可编辑的控件
public class ProfileText extends ProfileEdit {
    public ProfileText(Context context) {
        super(context);
        disableEdit();
    }

    public ProfileText(Context context, AttributeSet attrs) {
        super(context, attrs);
        disableEdit();
    }

    public ProfileText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        disableEdit();
    }
}
