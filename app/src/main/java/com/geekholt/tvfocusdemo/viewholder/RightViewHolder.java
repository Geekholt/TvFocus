package com.geekholt.tvfocusdemo.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.geekholt.tvfocusdemo.R;

/**
 * @author 吴灏腾
 * @date 2018/12/17
 * @describe TODO
 */

public class RightViewHolder extends RecyclerView.ViewHolder {
    private ImageView imgReduce;
    private ImageView imgAdd;

    public RightViewHolder(View itemView) {
        super(itemView);
        imgReduce = itemView.findViewById(R.id.img_reduce);
        imgAdd = itemView.findViewById(R.id.img_add);
        imgReduce.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

            }
        });
    }
}
