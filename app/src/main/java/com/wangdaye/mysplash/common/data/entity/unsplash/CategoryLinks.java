package com.wangdaye.mysplash.common.data.entity.unsplash;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Category links.
 * */

public class CategoryLinks implements Parcelable {

    /**
     * self : https://api.unsplash.com/categories/2
     * photos : https://api.unsplash.com/categories/2/photos
     */
    public String self;
    public String photos;

    /** <br> parcel. */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.self);
        dest.writeString(this.photos);
    }

    public CategoryLinks() {
    }

    protected CategoryLinks(Parcel in) {
        this.self = in.readString();
        this.photos = in.readString();
    }

    public static final Parcelable.Creator<CategoryLinks> CREATOR = new Parcelable.Creator<CategoryLinks>() {
        @Override
        public CategoryLinks createFromParcel(Parcel source) {
            return new CategoryLinks(source);
        }

        @Override
        public CategoryLinks[] newArray(int size) {
            return new CategoryLinks[size];
        }
    };
}
