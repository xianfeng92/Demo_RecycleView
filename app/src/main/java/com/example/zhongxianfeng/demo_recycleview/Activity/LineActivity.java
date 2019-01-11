package com.example.zhongxianfeng.demo_recycleview.Activity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

import com.example.zhongxianfeng.demo_recycleview.Adapter.LineAdapter;
import com.example.zhongxianfeng.demo_recycleview.Api.Api;
import com.example.zhongxianfeng.demo_recycleview.Api.service.CommonService;
import com.example.zhongxianfeng.demo_recycleview.Bean.Meizi;
import com.example.zhongxianfeng.demo_recycleview.R;
import com.example.zhongxianfeng.demo_recycleview.Utils.SnackbarUtil;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class LineActivity extends AppCompatActivity {
    private static final String TAG = "test";

    @BindView(R.id.line_recycler)
    public RecyclerView recyclerView;
    @BindView(R.id.line_coordinatorLayout)
    public CoordinatorLayout coordinatorLayout;
    @BindView(R.id.line_swipe_refresh)
    public SwipeRefreshLayout swipeRefreshLayout;

    public LineAdapter mAdapter;
    public List<String> urls = new ArrayList<>();
    public LinearLayoutManager mlayoutManager;
    private int lastVisibleItem;
    private ItemTouchHelper itemTouchHelper;
    private Retrofit retrofit;
    private CommonService meiziService;
    private int screenwidth;
    private int page = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line);
        ButterKnife.bind(this);
        init();
        setListener();
        retrofit = new Retrofit.Builder()
                .baseUrl(Api.APP_DOMAIN)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        meiziService = retrofit.create(CommonService.class);
        //获取屏幕宽度
        WindowManager wm = (WindowManager) LineActivity.this
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        screenwidth =outMetrics.widthPixels;
    }


    @Override
    protected void onResume() {
        super.onResume();
        getMeizi();
    }

    private void init(){
        mlayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mlayoutManager);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,R.color.colorPrimaryDark,R.color.colorAccent);
        swipeRefreshLayout.setProgressViewOffset(false, 0,  (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
    }

    private void setListener(){
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 1;
                getMeizi();
            }
        });
        itemTouchHelper=new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                Log.d(TAG, "getMovementFlags: ");
                int dragFlags=0,swipeFlags=0;
                if(recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager){
                    dragFlags=ItemTouchHelper.UP|ItemTouchHelper.DOWN|ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT;
                }else if(recyclerView.getLayoutManager() instanceof LinearLayoutManager){
                    dragFlags=ItemTouchHelper.UP|ItemTouchHelper.DOWN;
                    //设置侧滑方向为从左到右和从右到左都可以
                    swipeFlags = ItemTouchHelper.START|ItemTouchHelper.END;
                }
                return makeMovementFlags(dragFlags,swipeFlags);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                Log.d(TAG, "onMove: ");
                int from=viewHolder.getAdapterPosition();
                int to=target.getAdapterPosition();
                Collections.swap(urls,from,to);
                mAdapter.notifyItemMoved(from,to);
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                Log.d(TAG, "onSwiped: ");
                mAdapter.removeItem(viewHolder.getAdapterPosition());

            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if(actionState==ItemTouchHelper.ACTION_STATE_DRAG){
                    viewHolder.itemView.setBackgroundColor(Color.LTGRAY);
                }
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                viewHolder.itemView.setBackgroundColor(Color.WHITE);
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                viewHolder.itemView.setAlpha(1- Math.abs(dX)/screenwidth);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //0：当前屏幕停止滚动；1时：屏幕在滚动 且 用户仍在触碰或手指还在屏幕上；2时：随用户的操作，屏幕上产生的惯性滑动；
                // 滑动状态停止并且剩余两个item时自动加载
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && lastVisibleItem +2>=mlayoutManager.getItemCount()) {
                    page++;
                    getMeizi();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //获取加载的最后一个可见视图在适配器的位置。
                lastVisibleItem = mlayoutManager.findLastVisibleItemPosition();
            }

        });
    }

    private void getMeizi(){
        meiziService.getMeizi(page)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Meizi>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Meizi meizis) {
                for (Meizi.ResultsBean resultsBean:meizis.results){
                    Log.d(TAG, "onNext: "+resultsBean.url);
                    urls.add(resultsBean.url);
                }
                updateAdapter(urls);
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError: "+e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete: ");
            }
        });
    }

    private void updateAdapter(List<String> urls){
        Log.d(TAG, "updateAdapter: ");
        if (mAdapter == null){
            mAdapter = new LineAdapter(LineActivity.this,urls);
            recyclerView.setAdapter(mAdapter);
            mAdapter.setRecycleViewItemClickListener(new LineAdapter.onRecycleViewItemClickListener() {
                @Override
                public void onItemClick(View view) {
                    int position=recyclerView.getChildAdapterPosition(view);
                    Log.d(TAG, "onItemClick: "+position);
                    SnackbarUtil.ShortSnackbar(coordinatorLayout,"点击第"+position+"个",SnackbarUtil.Info).show();
                }
            });
            itemTouchHelper.attachToRecyclerView(recyclerView);
        }else {
            mAdapter.notifyDataSetChanged();
        }
        swipeRefreshLayout.setRefreshing(false);
    }
}
