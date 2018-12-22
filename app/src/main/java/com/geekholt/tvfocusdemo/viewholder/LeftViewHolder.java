package com.geekholt.tvfocusdemo.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.geekholt.tvfocusdemo.R;

/**
 * @author 吴灏腾
 * @date 2018/12/17
 * @describe
 */

public class LeftViewHolder extends RecyclerView.ViewHolder{
    private TextView numTxt;
    public LeftViewHolder(View itemView) {
        super(itemView);
        numTxt = itemView.findViewById(R.id.txt_num);
    }

    public void bindView(int position){
        numTxt.setText(String.valueOf(position));
    }
}
