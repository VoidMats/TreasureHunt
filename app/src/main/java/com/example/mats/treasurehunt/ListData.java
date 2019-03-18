package com.example.mats.treasurehunt;

import android.os.Parcel;
import android.os.Parcelable;

public class ListData implements Parcelable {

    private String name;
    private int noNodes;
    private long totalTime;
    private String type;
    private String id;

    public ListData() {
        this.name = "";
        this.noNodes = 0;
        this.totalTime = 0;
        this.type = "";
        this.id = "";
    }

    protected ListData(Parcel _in) {
        name = _in.readString();
        noNodes = _in.readInt();
        totalTime = _in.readLong();
        type = _in.readString();
        id = _in.readString();
    }

    public ListData(String _name, int _noNodes, long _totalTime, String _type, String _id) {
        this.name = _name;
        this.noNodes = _noNodes;
        this.totalTime = _totalTime;
        this.type = _type;
        this.id = _id;
    }

    public static final Parcelable.Creator<ListData> CREATOR = new Creator<ListData>() {
        @Override
        public ListData createFromParcel(Parcel _source) {
            return new ListData(_source);
        }

        @Override
        public ListData[] newArray(int _size) {
            return new ListData[_size];
        }
    };

    public void setName(String name) {
        this.name = name;
    }

    public void setNoNodes(int noNodes) {
        this.noNodes = noNodes;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getNoNodes() {
        return noNodes;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel _dest, int flags) {
        _dest.writeString(name);
        _dest.writeInt(noNodes);
        _dest.writeLong(totalTime);
        _dest.writeString(type);
        _dest.writeString(id);
    }
}
