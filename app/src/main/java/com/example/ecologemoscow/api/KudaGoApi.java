package com.example.ecologemoscow.api;

import com.example.ecologemoscow.models.Park;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface KudaGoApi {
    @GET("places/")
    Call<KudaGoResponse> getParks(@Query("location") String location,
                                @Query("categories") String categories,
                                @Query("fields") String fields,
                                @Query("page_size") int pageSize,
                                @Query("page") int page,
                                @Query("lat") double lat,
                                @Query("lon") double lon,
                                @Query("radius") int radius);

    class KudaGoResponse {
        @SerializedName("results")
        private List<Park> parks;

        public List<Park> getParks() {
            return parks;
        }
    }
} 