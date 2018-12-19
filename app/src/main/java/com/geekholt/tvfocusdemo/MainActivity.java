package com.geekholt.tvfocusdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.geekholt.tvfocusdemo.adapter.CenterRvAdapter;
import com.geekholt.tvfocusdemo.adapter.LeftRvAdapter;
import com.geekholt.tvfocusdemo.adapter.RightRvAdapter;

public class MainActivity extends AppCompatActivity {
    RecyclerView leftRv;
    RecyclerView centerRv;
    RecyclerView rightRv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        leftRv = findViewById(R.id.tv_left);
        centerRv = findViewById(R.id.tv_center);
        rightRv = findViewById(R.id.tv_right);

        leftRv.setLayoutManager(new LinearLayoutManager(this));
        leftRv.setAdapter(new LeftRvAdapter(this));
        centerRv.setLayoutManager(new LinearLayoutManager(this));
        centerRv.setAdapter(new CenterRvAdapter(this));
        rightRv.setLayoutManager(new LinearLayoutManager(this));
        rightRv.setAdapter(new RightRvAdapter(this));
    }
}
