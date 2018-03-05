package com.cxromos.castbox.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Casts implements Parcelable {
    public String msg;
    public int code;
    @SerializedName("data")
    public List<Cast> list;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.msg);
        dest.writeInt(this.code);
        dest.writeList(this.list);
    }

    public Casts() {
    }

    protected Casts(Parcel in) {
        this.msg = in.readString();
        this.code = in.readInt();
        this.list = new ArrayList<Cast>();
        in.readList(this.list, Cast.class.getClassLoader());
    }

    public static final Parcelable.Creator<Casts> CREATOR = new Parcelable.Creator<Casts>() {
        @Override
        public Casts createFromParcel(Parcel source) {
            return new Casts(source);
        }

        @Override
        public Casts[] newArray(int size) {
            return new Casts[size];
        }
    };
}

