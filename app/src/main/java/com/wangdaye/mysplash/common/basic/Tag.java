package com.wangdaye.mysplash.common.basic;

/**
 * Tag.
 *
 * If an Object need to be displayed in a RecyclerView with
 * {@link com.wangdaye.mysplash.common.ui.adapter.TagAdapter}, it should implement this interface.
 *
 * */

public interface Tag {

    String getTitle();
    String getRegularUrl();
    String getThumbnailUrl();
}
