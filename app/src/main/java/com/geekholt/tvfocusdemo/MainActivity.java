package com.geekholt.tvfocusdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.geekholt.tvfocusdemo.adapter.CenterRvAdapter;
import com.geekholt.tvfocusdemo.adapter.LeftRvAdapter;
import com.geekholt.tvfocusdemo.adapter.RightRvAdapter;
import com.geekholt.tvfocusdemo.util.Loger;
import com.geekholt.tvfocusdemo.widget.TvRecyclerView;
import com.geekholt.tvfocusdemo.widget.V7LinearLayoutManager;

public class MainActivity extends AppCompatActivity {
    TvRecyclerView leftRv;
    TvRecyclerView centerRv;
    RecyclerView rightRv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        leftRv = findViewById(R.id.tv_left);
        centerRv = findViewById(R.id.tv_center);
        rightRv = findViewById(R.id.tv_right);

        leftRv.setLayoutManager(new V7LinearLayoutManager(this));
        leftRv.setAdapter(new LeftRvAdapter(this));
        centerRv.setLayoutManager(new V7LinearLayoutManager(this));
        centerRv.setAdapter(new CenterRvAdapter(this));
        rightRv.setLayoutManager(new V7LinearLayoutManager(this));
        rightRv.setAdapter(new RightRvAdapter(this));

        leftRv.setGainFocusListener(new TvRecyclerView.FocusGainListener() {
            @Override
            public void onFocusGain(View child, View focued) {
                Loger.i("leftRv onFocusGain");
            }
        });

        leftRv.setFocusLostListener(new TvRecyclerView.FocusLostListener() {
            @Override
            public void onFocusLost(View lastFocusChild, int direction) {
                Loger.i("leftRv onFocusLost");
            }
        });

        centerRv.setGainFocusListener(new TvRecyclerView.FocusGainListener() {
            @Override
            public void onFocusGain(View child, View focued) {
                Loger.i("centerRv onFocusGain");
            }
        });

        centerRv.setFocusLostListener(new TvRecyclerView.FocusLostListener() {
            @Override
            public void onFocusLost(View lastFocusChild, int direction) {
                Loger.i("centerRv onFocusLost");
            }
        });
    }
}
