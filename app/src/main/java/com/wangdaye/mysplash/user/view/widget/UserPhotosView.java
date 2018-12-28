package com.wangdaye.mysplash.user.view.widget;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.wangdaye.mysplash.R;
import com.wangdaye.mysplash.common.basic.activity.MysplashActivity;
import com.wangdaye.mysplash.common.data.entity.unsplash.Photo;
import com.wangdaye.mysplash.common.data.entity.unsplash.User;
import com.wangdaye.mysplash.common.i.model.LoadModel;
import com.wangdaye.mysplash.common.i.model.PagerModel;
import com.wangdaye.mysplash.common.i.model.PhotosModel;
import com.wangdaye.mysplash.common.i.model.ScrollModel;
import com.wangdaye.mysplash.common.i.presenter.LoadPresenter;
import com.wangdaye.mysplash.common.i.presenter.PagerPresenter;
import com.wangdaye.mysplash.common.i.presenter.PhotosPresenter;
import com.wangdaye.mysplash.common.i.presenter.ScrollPresenter;
import com.wangdaye.mysplash.common.i.presenter.SwipeBackPresenter;
import com.wangdaye.mysplash.common.i.view.LoadView;
import com.wangdaye.mysplash.common.i.view.PagerView;
import com.wangdaye.mysplash.common.i.view.PhotosView;
import com.wangdaye.mysplash.common.i.view.ScrollView;
import com.wangdaye.mysplash.common.i.view.SwipeBackView;
import com.wangdaye.mysplash.common.ui.widget.SwipeBackCoordinatorLayout;
import com.wangdaye.mysplash.common.ui.widget.nestedScrollView.NestedScrollFrameLayout;
import com.wangdaye.mysplash.common.ui.widget.swipeRefreshView.BothWaySwipeRefreshLayout;
import com.wangdaye.mysplash.common.utils.AnimUtils;
import com.wangdaye.mysplash.common.utils.BackToTopUtils;
import com.wangdaye.mysplash.common.utils.DisplayUtils;
import com.wangdaye.mysplash.common.utils.manager.ThemeManager;
import com.wangdaye.mysplash.user.model.widget.LoadObject;
import com.wangdaye.mysplash.user.model.widget.PagerObject;
import com.wangdaye.mysplash.user.model.widget.PhotosObject;
import com.wangdaye.mysplash.user.model.widget.ScrollObject;
import com.wangdaye.mysplash.user.presenter.widget.LoadImplementor;
import com.wangdaye.mysplash.user.presenter.widget.PagerImplementor;
import com.wangdaye.mysplash.user.presenter.widget.PhotosImplementor;
import com.wangdaye.mysplash.user.presenter.widget.ScrollImplementor;
import com.wangdaye.mysplash.user.presenter.widget.SwipeBackImplementor;
import com.wangdaye.mysplash.user.view.activity.UserActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * User photos view.
 *
 * This view is used to show photos for {@link UserActivity}.
 *
 * */

@SuppressLint("ViewConstructor")
public class UserPhotosView extends NestedScrollFrameLayout
        implements PhotosView, PagerView, LoadView, ScrollView, SwipeBackView,
        BothWaySwipeRefreshLayout.OnRefreshAndLoadListener {

    @BindView(R.id.container_loading_view_mini_progressView)
    CircularProgressView progressView;

    @BindView(R.id.container_loading_view_mini_retryButton)
    Button retryButton;

    @BindView(R.id.container_photo_list_swipeRefreshLayout)
    BothWaySwipeRefreshLayout refreshLayout;

    @BindView(R.id.container_photo_list_recyclerView)
    RecyclerView recyclerView;

    private PhotosModel photosModel;
    private PhotosPresenter photosPresenter;

    private PagerModel pagerModel;
    private PagerPresenter pagerPresenter;

    private LoadModel loadModel;
    private LoadPresenter loadPresenter;

    private ScrollModel scrollModel;
    private ScrollPresenter scrollPresenter;

    private SwipeBackPresenter swipeBackPresenter;

    private static class SavedState implements Parcelable {

        String order;
        int page;
        boolean over;

        SavedState(UserPhotosView view) {
            this.order = view.photosModel.getPhotosOrder();
            this.page = view.photosModel.getPhotosPage();
            this.over = view.photosModel.isOver();
        }

        private SavedState(Parcel in) {
            this.order = in.readString();
            this.page = in.readInt();
            this.over = in.readByte() != 0;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(this.order);
            out.writeInt(this.page);
            out.writeByte(this.over ? (byte) 1 : (byte) 0);
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    public UserPhotosView(UserActivity a, User u, int type, int id,
                          int index, boolean selected) {
        super(a);
        this.setId(id);
        this.initialize(a, u, type, index, selected);
    }

    // init.

    @SuppressLint("InflateParams")
    private void initialize(UserActivity a, User u, int type,
                            int index, boolean selected) {
        View loadingView = LayoutInflater.from(getContext())
                .inflate(R.layout.container_loading_view_mini, this, false);
        addView(loadingView);
        View contentView = LayoutInflater.from(getContext())
                .inflate(R.layout.container_photo_list, null);
        addView(contentView);

        ButterKnife.bind(this, this);
        initModel(a, u, type, index, selected);
        initPresenter(a);
        initView();
    }

    private void initModel(UserActivity a, User u, int type,
                           int index, boolean selected) {
        this.photosModel = new PhotosObject(a, u, type);
        this.pagerModel = new PagerObject(index, selected);
        this.loadModel = new LoadObject(LoadModel.LOADING_STATE);
        this.scrollModel = new ScrollObject();
    }

    private void initPresenter(MysplashActivity a) {
        this.photosPresenter = new PhotosImplementor(photosModel, this);
        this.pagerPresenter = new PagerImplementor(pagerModel, this);
        this.loadPresenter = new LoadImplementor(loadModel, this);
        this.scrollPresenter = new ScrollImplementor(scrollModel, this);
        this.swipeBackPresenter = new SwipeBackImplementor(this);

        loadPresenter.bindActivity(a);
    }

    private void initView() {
        retryButton.setVisibility(GONE);

        refreshLayout.setColorSchemeColors(ThemeManager.getContentColor(getContext()));
        refreshLayout.setProgressBackgroundColorSchemeColor(ThemeManager.getRootColor(getContext()));
        refreshLayout.setOnRefreshAndLoadListener(this);
        refreshLayout.setPermitRefresh(false);
        refreshLayout.setVisibility(GONE);

        int navigationBarHeight = DisplayUtils.getNavigationBarHeight(getResources());
        refreshLayout.setDragTriggerDistance(
                BothWaySwipeRefreshLayout.DIRECTION_BOTTOM,
                navigationBarHeight + getResources().getDimensionPixelSize(R.dimen.normal_margin));

        int columnCount = DisplayUtils.getGirdColumnCount(getContext());
        recyclerView.setAdapter(photosPresenter.getAdapter());
        if (columnCount > 1) {
            int margin = getResources().getDimensionPixelSize(R.dimen.little_margin);
            recyclerView.setPadding(margin, margin, 0, 0);
        } else {
            recyclerView.setPadding(0, 0, 0, 0);
        }
        recyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.addOnScrollListener(scrollListener);

        photosPresenter.getAdapter().setRecyclerView(recyclerView);
    }

    // control.

    @Override
    public boolean isParentOffset() {
        return true;
    }

    public List<Photo> loadMore(List<Photo> list, int headIndex, boolean headDirection) {
        if ((headDirection && photosPresenter.getAdapter().getRealItemCount() < headIndex)
                || (!headDirection && photosPresenter.getAdapter().getRealItemCount() < headIndex + list.size())) {
            return new ArrayList<>();
        }

        if (!headDirection && photosPresenter.canLoadMore()) {
            photosPresenter.loadMore(getContext(), false);
        }
        if (!ViewCompat.canScrollVertically(recyclerView, 1) && photosPresenter.isLoading()) {
            refreshLayout.setLoading(true);
        }

        if (headDirection) {
            if (headIndex == 0) {
                return new ArrayList<>();
            } else {
                return photosPresenter.getAdapter().getPhotoData().subList(0, headIndex - 1);
            }
        } else {
            if (photosPresenter.getAdapter().getRealItemCount() == headIndex + list.size()) {
                return new ArrayList<>();
            } else {
                return photosPresenter.getAdapter()
                        .getPhotoData()
                        .subList(
                                headIndex + list.size(),
                                photosPresenter.getAdapter().getRealItemCount() - 1);
            }
        }
    }

    // photo.

    public void updatePhoto(Photo p, boolean refreshView) {
        photosPresenter.getAdapter().updatePhoto(recyclerView, p, refreshView, false);
    }

    /**
     * Get the photos from the adapter in this view.
     *
     * @return Photos in adapter.
     * */
    public List<Photo> getPhotos() {
        return photosPresenter.getAdapter().getPhotoData();
    }

    /**
     * Set photos to the adapter in this view.
     *
     * @param list Photos that will be set to the adapter.
     * */
    public void setPhotos(List<Photo> list) {
        if (list == null) {
            list = new ArrayList<>();
        }
        photosPresenter.getAdapter().setPhotoData(list);
        if (list.size() == 0) {
            refreshPager();
        } else {
            loadPresenter.setNormalState();
        }
    }

    public String getOrder() {
        return photosPresenter.getOrder();
    }

    // interface.

    // on click listener.

    @OnClick(R.id.container_loading_view_mini_retryButton) void retryRefresh() {
        photosPresenter.initRefresh(getContext());
    }

    // on refresh an load listener.

    @Override
    public void onRefresh() {
        photosPresenter.refreshNew(getContext(), false);
    }

    @Override
    public void onLoad() {
        photosPresenter.loadMore(getContext(), false);
    }

    // on scroll listener.

    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            scrollPresenter.autoLoad(dy);
        }
    };

    // view.

    // photos view.

    @Override
    public void setRefreshing(boolean refreshing) {
        refreshLayout.setRefreshing(refreshing);
    }

    @Override
    public void setLoading(boolean loading) {
        refreshLayout.setLoading(loading);
    }

    @Override
    public void setPermitRefreshing(boolean permit) {
        refreshLayout.setPermitRefresh(permit);
    }

    @Override
    public void setPermitLoading(boolean permit) {
        refreshLayout.setPermitLoad(permit);
    }

    @Override
    public void initRefreshStart() {
        loadPresenter.setLoadingState();
    }

    @Override
    public void requestPhotosSuccess() {
        loadPresenter.setNormalState();
    }

    @Override
    public void requestPhotosFailed(String feedback) {
        if (photosPresenter.getAdapter().getRealItemCount() > 0) {
            loadPresenter.setNormalState();
        } else {
            loadPresenter.setFailedState();
        }
    }

    // pager view.

    // pager view.

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putParcelable(String.valueOf(getId()), new SavedState(this));
    }

    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        SavedState ss = bundle.getParcelable(String.valueOf(getId()));
        if (ss != null) {
            photosPresenter.setOrder(ss.order);
            photosPresenter.setPage(ss.page);
            photosPresenter.setOver(ss.over);
        }
    }

    @Override
    public void checkToRefresh() { // interface
        if (pagerPresenter.checkNeedRefresh()) {
            pagerPresenter.refreshPager();
        }
    }

    @Override
    public boolean checkNeedRefresh() {
        return loadPresenter.getLoadState() == LoadModel.FAILED_STATE
                || (loadPresenter.getLoadState() == LoadModel.LOADING_STATE
                && !photosPresenter.isRefreshing() && !photosPresenter.isLoading());
    }

    @Override
    public boolean checkNeedBackToTop() {
        return scrollPresenter.needBackToTop();
    }

    @Override
    public void refreshPager() {
        photosPresenter.initRefresh(getContext());
    }

    @Override
    public void setSelected(boolean selected) {
        pagerPresenter.setSelected(selected);
    }

    @Override
    public void scrollToPageTop() { // interface.
        scrollPresenter.scrollToTop();
    }

    @Override
    public void cancelRequest() {
        photosPresenter.cancelRequest();
    }

    @Override
    public void setKey(String key) {
        photosPresenter.setOrder(key);
    }

    @Override
    public String getKey() {
        return photosPresenter.getOrder();
    }

    @Override
    public int getItemCount() {
        if (loadPresenter.getLoadState() != LoadModel.NORMAL_STATE) {
            return 0;
        } else {
            return photosPresenter.getAdapter().getRealItemCount();
        }
    }

    @Override
    public boolean canSwipeBack(int dir) {
        return swipeBackPresenter.checkCanSwipeBack(dir);
    }

    @Override
    public boolean isNormalState() {
        return loadPresenter.getLoadState() == LoadModel.NORMAL_STATE;
    }

    // load view.

    @Override
    public void animShow(View v) {
        AnimUtils.animShow(v);
    }

    @Override
    public void animHide(final View v) {
        AnimUtils.animHide(v);
    }

    @Override
    public void setLoadingState(@Nullable MysplashActivity activity, int old) {
        if (activity != null && pagerPresenter.isSelected()) {
            DisplayUtils.setNavigationBarStyle(
                    activity, false, activity.hasTranslucentNavigationBar());
        }
        animShow(progressView);
        animHide(retryButton);
        animHide(refreshLayout);
    }

    @Override
    public void setFailedState(@Nullable MysplashActivity activity, int old) {
        animShow(retryButton);
        animHide(progressView);
        animHide(refreshLayout);
    }

    @Override
    public void setNormalState(@Nullable MysplashActivity activity, int old) {
        if (activity != null && pagerPresenter.isSelected()) {
            DisplayUtils.setNavigationBarStyle(
                    activity, true, activity.hasTranslucentNavigationBar());
        }
        animShow(refreshLayout);
        animHide(progressView);
        animHide(retryButton);
    }

    // scroll view.

    @Override
    public void scrollToTop() {
        BackToTopUtils.scrollToTop(recyclerView);
    }

    @Override
    public void autoLoad(int dy) {
        int[] lastVisibleItems = ((StaggeredGridLayoutManager) recyclerView.getLayoutManager())
                .findLastVisibleItemPositions(null);
        int totalItemCount = photosPresenter.getAdapter().getRealItemCount();
        if (photosPresenter.canLoadMore()
                && lastVisibleItems[lastVisibleItems.length - 1] >= totalItemCount - 10
                && totalItemCount > 0
                && dy > 0) {
            photosPresenter.loadMore(getContext(), false);
        }
        if (!ViewCompat.canScrollVertically(recyclerView, -1)) {
            scrollPresenter.setToTop(true);
        } else {
            scrollPresenter.setToTop(false);
        }
        if (!ViewCompat.canScrollVertically(recyclerView, 1) && photosPresenter.isLoading()) {
            refreshLayout.setLoading(true);
        }
    }

    @Override
    public boolean needBackToTop() {
        return !scrollPresenter.isToTop()
                && loadPresenter.getLoadState() == LoadModel.NORMAL_STATE;
    }

    // swipe back view.

    @Override
    public boolean checkCanSwipeBack(int dir) {
        switch (loadPresenter.getLoadState()) {
            case LoadModel.NORMAL_STATE:
                return SwipeBackCoordinatorLayout.canSwipeBack(recyclerView, dir)
                        || photosPresenter.getAdapter().getRealItemCount() <= 0;

            default:
                return true;
        }
    }
}
