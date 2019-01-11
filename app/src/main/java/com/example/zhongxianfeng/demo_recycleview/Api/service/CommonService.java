package com.example.zhongxianfeng.demo_recycleview.Api.service;

import com.example.zhongxianfeng.demo_recycleview.Bean.Meizi;
import com.example.zhongxianfeng.demo_recycleview.Bean.Repo;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CommonService {
    @GET("users/{user}/repos")
    Observable<List<Repo>> listRepos(@Path("user") String user);

    @GET("api/data/福利/10/{page}")
    Observable<Meizi> getMeizi(@Path("page") int page);
}
