package com.cxromos.castbox.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Cast implements Parcelable {
    public String feedUrl;
    public String releaseDate;
    public String description;
    public String title;
    @SerializedName("cover-bg")
    public String coverBig;
    public String author;
    public int trackCount;
    @SerializedName("cover-sm")
    public String coverSmall;
    public String key;
    @SerializedName("itunes_id")
    public String itunesId;
    @SerializedName("cover-me")
    public String coverMedium;
    public String type;

    @Override
    public String toString() {
        return "Cast{" +
                "feedUrl='" + feedUrl + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                ", description='" + description + '\'' +
                ", title='" + title + '\'' +
                ", coverBig='" + coverBig + '\'' +
                ", author='" + author + '\'' +
                ", trackCount=" + trackCount +
                ", coverSmall='" + coverSmall + '\'' +
                ", key='" + key + '\'' +
                ", itunesId='" + itunesId + '\'' +
                ", coverMedium='" + coverMedium + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.feedUrl);
        dest.writeString(this.releaseDate);
        dest.writeString(this.description);
        dest.writeString(this.title);
        dest.writeString(this.coverBig);
        dest.writeString(this.author);
        dest.writeInt(this.trackCount);
        dest.writeString(this.coverSmall);
        dest.writeString(this.key);
        dest.writeString(this.itunesId);
        dest.writeString(this.coverMedium);
        dest.writeString(this.type);
    }

    public Cast() {
    }

    protected Cast(Parcel in) {
        this.feedUrl = in.readString();
        this.releaseDate = in.readString();
        this.description = in.readString();
        this.title = in.readString();
        this.coverBig = in.readString();
        this.author = in.readString();
        this.trackCount = in.readInt();
        this.coverSmall = in.readString();
        this.key = in.readString();
        this.itunesId = in.readString();
        this.coverMedium = in.readString();
        this.type = in.readString();
    }

    public static final Parcelable.Creator<Cast> CREATOR = new Parcelable.Creator<Cast>() {
        @Override
        public Cast createFromParcel(Parcel source) {
            return new Cast(source);
        }

        @Override
        public Cast[] newArray(int size) {
            return new Cast[size];
        }
    };
}
