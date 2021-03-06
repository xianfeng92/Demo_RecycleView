package com.example.zhongxianfeng.demo_recycleview.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import com.example.zhongxianfeng.demo_recycleview.R;
import com.squareup.picasso.Picasso;

import java.util.List;


public class GridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener,View.OnLongClickListener{

    private static final String TAG = "GridAdapter";

    private Context context;
    private List<String> urls;

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view);
        void onItemLongClick(View view);
    }

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;


    public GridAdapter(Context context, List<String> urls){
        this.context = context;
        this.urls = urls;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(context).inflate(R.layout.grid_meizi_item,viewGroup,false);
            MyViewHolder holder = new MyViewHolder(view);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            Picasso.with(context).load(urls.get(i)).into(((MyViewHolder) viewHolder).iv);
    }

    @Override
    public int getItemCount() {
        return urls.size();
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null){
            mOnItemClickListener.onItemClick(v);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (mOnItemClickListener != null){
            mOnItemClickListener.onItemLongClick(v);
        }
        return false;
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageButton iv;

        MyViewHolder(View view)
        {
            super(view);
            iv = view.findViewById(R.id.iv);
        }
    }
}
