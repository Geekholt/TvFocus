package com.geekholt.tvfocusdemo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.geekholt.tvfocusdemo.R;
import com.geekholt.tvfocusdemo.viewholder.LeftViewHolder;
import com.geekholt.tvfocusdemo.viewholder.RightViewHolder;


/**
 * @author 吴灏腾
 * @date 2018/12/17
 * @describe TODO
 */

public class RightRvAdapter extends RecyclerView.Adapter<RightViewHolder> {
    private Context context;

    public RightRvAdapter(Context context) {
        this.context = context;
    }

    @Override
    public RightViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_right_view, parent, false);
        RightViewHolder rightViewHolder = new RightViewHolder(view);
        return rightViewHolder;
    }

    @Override
    public void onBindViewHolder(RightViewHolder holder, int position) {

    }


    @Override
    public int getItemCount() {
        return 20;
    }
}
