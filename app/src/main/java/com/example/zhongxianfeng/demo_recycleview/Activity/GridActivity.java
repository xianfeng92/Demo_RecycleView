package com.example.zhongxianfeng.demo_recycleview.Activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import com.example.zhongxianfeng.demo_recycleview.Adapter.GridAdapter;
import com.example.zhongxianfeng.demo_recycleview.Api.Api;
import com.example.zhongxianfeng.demo_recycleview.Api.service.CommonService;
import com.example.zhongxianfeng.demo_recycleview.Bean.Meizi;
import com.example.zhongxianfeng.demo_recycleview.R;
import com.example.zhongxianfeng.demo_recycleview.Utils.SnackbarUtil;
import java.util.ArrayList;
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

public class GridActivity extends AppCompatActivity {

    private static final String TAG = "test";

    @BindView(R.id.grid_recycler)
    public RecyclerView recyclerview;

    @BindView(R.id.grid_coordinatorLayout)
    public CoordinatorLayout coordinatorLayout;

    @BindView(R.id.grid_swipe_refresh)
    public SwipeRefreshLayout swipeRefreshLayout;

    private GridAdapter mAdapter;
    private GridLayoutManager mLayoutManager;
    private int lastVisibleItem;
    private ItemTouchHelper itemTouchHelper;
    private Retrofit retrofit;
    private CommonService meiziService;
    private List<String> urls = new ArrayList<>();
    private int page = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);
        ButterKnife.bind(this);
        init();
        setListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
         retrofit = new Retrofit.Builder()
                .baseUrl(Api.APP_DOMAIN)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
         meiziService = retrofit.create(CommonService.class);
         getData();
    }

    private void getData(){
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
    

    private void updateAdapter(final List<String> urls) {
        if(mAdapter==null){
            Log.d(TAG, "updateAdapter: ");
            recyclerview.setAdapter(mAdapter = new GridAdapter(GridActivity.this,urls));
            mAdapter.setOnItemClickListener(new GridAdapter.OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view) {
                    int position=recyclerview.getChildAdapterPosition(view);
                    Log.d(TAG, "onItemClick: "+position);
                    SnackbarUtil.ShortSnackbar(coordinatorLayout,"点击第"+position+"个",SnackbarUtil.Info).show();
                }

                @Override
                public void onItemLongClick(View view) {
                    itemTouchHelper.startDrag(recyclerview.getChildViewHolder(view));
                }
            });
            itemTouchHelper.attachToRecyclerView(recyclerview);
        }else{
            mAdapter.notifyDataSetChanged();
        }
        swipeRefreshLayout.setRefreshing(false);
    }


    private void init () {
        mLayoutManager = new GridLayoutManager(GridActivity.this, 3, GridLayoutManager.VERTICAL, false);
        recyclerview.setLayoutManager(mLayoutManager);
        swipeRefreshLayout.setProgressViewOffset(false, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
    }

    private void setListener(){
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 1;
                getData();
            }
        });

        itemTouchHelper=new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int dragFlags=0;
                if(recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager ||recyclerView.getLayoutManager() instanceof GridLayoutManager){
                    dragFlags=ItemTouchHelper.UP|ItemTouchHelper.DOWN|ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT;
                }
                return makeMovementFlags(dragFlags,0);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                Log.d(TAG, "onMove: ");
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

            }

            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }
        });

        //recyclerview滚动监听
        recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //0：当前屏幕停止滚动；1时：屏幕在滚动 且 用户仍在触碰或手指还在屏幕上；2时：随用户的操作，屏幕上产生的惯性滑动；
                // 滑动状态停止并且剩余少于两个item时，自动加载下一页
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && lastVisibleItem +2>=mLayoutManager.getItemCount()) {
                    page++;
                    getData();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //获取加载的最后一个可见视图在适配器的位置。
                lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();

            }
        });
    }
}
