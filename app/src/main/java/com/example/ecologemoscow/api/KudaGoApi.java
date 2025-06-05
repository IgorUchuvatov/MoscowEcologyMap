package com.example.ecologemoscow.api;

import com.example.ecologemoscow.models.Route;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface KudaGoApi {
    @GET("v1.4/places/")
    Call<List<Route>> getRoutes(
        @Query("location") String location,
        @Query("categories") String categories,
        @Query("fields") String fields,
        @Query("expand") String expand,
        @Query("page_size") int pageSize
    );
} 