package com.example.zhongxianfeng.demo_recycleview.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import com.example.zhongxianfeng.demo_recycleview.R;



import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "test";
    @BindView(R.id.button3)
    public Button button3;

    @BindView(R.id.button4)
    public Button button4;

    @BindView(R.id.button5)
    public Button button5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.button3)
    public void Click3(){
        startActivity(new Intent(MainActivity.this,GridActivity.class));
    }

    @OnClick(R.id.button4)
    public void Click4(){
        startActivity(new Intent(MainActivity.this,LineActivity.class));
    }


    @OnClick(R.id.button5)
    public void Click5(){
        startActivity(new Intent(MainActivity.this,StaggeredActivity.class));
    }
}
