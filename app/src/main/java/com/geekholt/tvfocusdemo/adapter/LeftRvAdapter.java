package com.geekholt.tvfocusdemo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.geekholt.tvfocusdemo.R;
import com.geekholt.tvfocusdemo.viewholder.LeftViewHolder;


/**
 * @author 吴灏腾
 * @date 2018/12/17
 * @describe TODO
 */

public class LeftRvAdapter extends RecyclerView.Adapter<LeftViewHolder> {
    private Context context;

    public LeftRvAdapter(Context context) {
        this.context = context;
    }

    @Override
    public LeftViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_left_view, parent, false);
        LeftViewHolder leftViewHolder = new LeftViewHolder(view);
        return leftViewHolder;
    }

    @Override
    public void onBindViewHolder(LeftViewHolder holder, int position) {

    }


    @Override
    public int getItemCount() {
        return 20;
    }
}
