package com.geekholt.tvfocusdemo.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.geekholt.tvfocusdemo.R;
import com.geekholt.tvfocusdemo.widget.FocusBorderView;

/**
 * @author 吴灏腾
 * @date 2018/12/17
 * @describe TODO
 */

public class CenterViewHolder extends RecyclerView.ViewHolder {
    private View itemView;
    private TextView numTxt;

    public CenterViewHolder(View itemView) {
        super(itemView);
        this.itemView = itemView;
        numTxt = itemView.findViewById(R.id.txt_num);
    }

    public void bindView(int position) {
        numTxt.setText(String.valueOf(position));
        if (position == 0) {
            //itemView.requestFocus();
        }
    }
}
