package com.cxromos.castbox.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Track implements Parcelable {
    public String description;
    public String author;
    public String title;
    public String cover;
    public String releaseDate;
    public String link;
    public List<String> urls;
    public int duration;
    public String type;
    public String id;
    public long size;
    public int state = 1;

    @Override
    public String toString() {
        return "Track{" +
                "description='" + description + '\'' +
                ", author='" + author + '\'' +
                ", title='" + title + '\'' +
                ", cover='" + cover + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                ", link='" + link + '\'' +
                ", urls=" + urls +
                ", duration=" + duration +
                ", type='" + type + '\'' +
                ", id='" + id + '\'' +
                ", size=" + size +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.description);
        dest.writeString(this.author);
        dest.writeString(this.title);
        dest.writeString(this.cover);
        dest.writeString(this.releaseDate);
        dest.writeString(this.link);
        dest.writeStringList(this.urls);
        dest.writeInt(this.duration);
        dest.writeString(this.type);
        dest.writeString(this.id);
        dest.writeLong(this.size);
        dest.writeInt(this.state);
    }

    public Track() {
    }

    protected Track(Parcel in) {
        this.description = in.readString();
        this.author = in.readString();
        this.title = in.readString();
        this.cover = in.readString();
        this.releaseDate = in.readString();
        this.link = in.readString();
        this.urls = in.createStringArrayList();
        this.duration = in.readInt();
        this.type = in.readString();
        this.id = in.readString();
        this.size = in.readLong();
        this.state = in.readInt();
    }

    public static final Parcelable.Creator<Track> CREATOR = new Parcelable.Creator<Track>() {
        @Override
        public Track createFromParcel(Parcel source) {
            return new Track(source);
        }

        @Override
        public Track[] newArray(int size) {
            return new Track[size];
        }
    };
}
