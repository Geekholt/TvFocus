package com.geekholt.tvfocusdemo.widget.focusborder;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @Author：wuhaoteng
 * @Date:2018/12/22
 * @Desc：
 */
public class BorderViewInflater {
    private Context context;
    private FocusBorderView focusBorderView;

    public BorderViewInflater from(Context context) {
        this.context = context;
        return this;
    }

    public FocusBorderView inflate(@LayoutRes int layoutId, ViewGroup parent) {
        focusBorderView = new FocusBorderView(context);
        //使用者自己创建的布局文件生成的view
        View contentView = LayoutInflater.from(context).inflate(layoutId, focusBorderView.getContainerView(), false);
        //加入到focusBorderView中
        focusBorderView.addContent(contentView);
        //将focusBorderView和parent关联
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
        focusBorderView.setLayoutParams(params);
        return focusBorderView;
    }
}
