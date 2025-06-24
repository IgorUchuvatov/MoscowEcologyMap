package com.example.ecologemoscow;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class EcologicalRoute implements Parcelable {
    public String name;
    public String link;
    public String description;
    public List<Coordinate> path; // Список координат для маршрута
    public transient float averageRating = 0;
    public transient int ratingCount = 0;

    public EcologicalRoute(String name, String link, String description, List<Coordinate> path) {
        this.name = name;
        this.link = link;
        this.description = description;
        this.path = path;
    }

    protected EcologicalRoute(Parcel in) {
        name = in.readString();
        link = in.readString();
        description = in.readString();
        path = new ArrayList<>();
        in.readList(path, Coordinate.class.getClassLoader());
        averageRating = in.readFloat();
        ratingCount = in.readInt();
    }

    public static final Creator<EcologicalRoute> CREATOR = new Creator<EcologicalRoute>() {
        @Override
        public EcologicalRoute createFromParcel(Parcel in) {
            return new EcologicalRoute(in);
        }

        @Override
        public EcologicalRoute[] newArray(int size) {
            return new EcologicalRoute[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(link);
        dest.writeString(description);
        dest.writeList(path);
        dest.writeFloat(averageRating);
        dest.writeInt(ratingCount);
    }

    public static class Coordinate implements Parcelable {
        public double latitude;
        public double longitude;

        public Coordinate(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        protected Coordinate(Parcel in) {
            latitude = in.readDouble();
            longitude = in.readDouble();
        }

        public static final Creator<Coordinate> CREATOR = new Creator<Coordinate>() {
            @Override
            public Coordinate createFromParcel(Parcel in) {
                return new Coordinate(in);
            }

            @Override
            public Coordinate[] newArray(int size) {
                return new Coordinate[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeDouble(latitude);
            dest.writeDouble(longitude);
        }
    }
} 