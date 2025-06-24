package com.example.ecologemoscow.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OsrmResponse {
    @SerializedName("routes")
    private List<Route> routes;

    public List<Route> getRoutes() {
        return routes;
    }

    public static class Route {
        @SerializedName("geometry")
        private String geometry;

        public String getGeometry() {
            return geometry;
        }
    }
} 