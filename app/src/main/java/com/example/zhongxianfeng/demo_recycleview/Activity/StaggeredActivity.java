package com.example.zhongxianfeng.demo_recycleview.Activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.TypedValue;
import com.example.zhongxianfeng.demo_recycleview.Adapter.StaggerAdapter;
import com.example.zhongxianfeng.demo_recycleview.Api.Api;
import com.example.zhongxianfeng.demo_recycleview.Api.service.CommonService;
import com.example.zhongxianfeng.demo_recycleview.Bean.Meizi;
import com.example.zhongxianfeng.demo_recycleview.R;
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

public class StaggeredActivity extends AppCompatActivity {
    private static final String TAG = "StaggeredActivity";

    @BindView(R.id.stagger_recycleView)
    public RecyclerView recyclerView;
    @BindView(R.id.stagger_coordinatorLayout)
    public CoordinatorLayout coordinatorLayout;
    @BindView(R.id.stagger_swipe_refresh)
    public SwipeRefreshLayout swipeRefreshLayout;
    public StaggeredGridLayoutManager staggeredGridLayoutManager;
    private ItemTouchHelper itemTouchHelper;
    private StaggerAdapter staggerAdapter;
    private Retrofit retrofit;
    private int lastVisibleItem;
    public List<String> urls;
    private CommonService meiziService;
    private int page = 1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stagger);
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

    private void init(){
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(3,StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        swipeRefreshLayout.setProgressViewOffset(false,0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
    }

    private void setListener(){
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 1;
                getData();
            }
        });
        itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int dragFlags=0;
                if(recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager ||recyclerView.getLayoutManager() instanceof GridLayoutManager){
                    dragFlags=ItemTouchHelper.UP|ItemTouchHelper.DOWN|ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT;
                }
                return makeMovementFlags(dragFlags,0);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                Log.d(TAG, "onMove: ");
                int from = viewHolder.getAdapterPosition();
                int to = viewHolder1.getAdapterPosition();
                String movedItems = urls.get(from);
                urls.remove(from);
                urls.add(to,movedItems);
                //更新适配器中item的位置
                staggerAdapter.notifyItemMoved(from,to);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && lastVisibleItem +2 >= staggeredGridLayoutManager.getItemCount()) {
                    page++;
                    getData();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int[] positions= staggeredGridLayoutManager.findLastVisibleItemPositions(null);
                //根据StaggeredGridLayoutManager设置的列数来定
                lastVisibleItem =Math.max(positions[0],positions[1]);
            }
        });
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
                    if (urls == null){
                        urls = new ArrayList<>();
                    }
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
        if(staggerAdapter==null){
            Log.d(TAG, "updateAdapter: ");
            recyclerView.setAdapter(staggerAdapter = new StaggerAdapter(StaggeredActivity.this,urls));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            itemTouchHelper.attachToRecyclerView(recyclerView);
        }else{
            staggerAdapter.notifyDataSetChanged();
        }
        swipeRefreshLayout.setRefreshing(false);
    }
}
