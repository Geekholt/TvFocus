package com.geekholt.tvfocusdemo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.geekholt.tvfocusdemo.R;
import com.geekholt.tvfocusdemo.viewholder.CenterViewHolder;
import com.geekholt.tvfocusdemo.viewholder.LeftViewHolder;


/**
 * @author 吴灏腾
 * @date 2018/12/17
 * @describe TODO
 */

public class CenterRvAdapter extends RecyclerView.Adapter<CenterViewHolder> {
    private Context context;

    public CenterRvAdapter(Context context) {
        this.context = context;
    }

    @Override
    public CenterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_center_view, parent, false);
        CenterViewHolder centerViewHolder = new CenterViewHolder(view);
        return centerViewHolder;
    }

    @Override
    public void onBindViewHolder(CenterViewHolder holder, int position) {

    }


    @Override
    public int getItemCount() {
        return 20;
    }
}
