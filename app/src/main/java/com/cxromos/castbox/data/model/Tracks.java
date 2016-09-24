package com.cxromos.castbox.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Tracks {
    public String msg;
    public int code;
    @SerializedName("data")
    public List<Track> list;
}
