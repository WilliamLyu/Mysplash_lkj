package com.wangdaye.mysplash.me.view.widget;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.wangdaye.mysplash.Mysplash;
import com.wangdaye.mysplash.R;
import com.wangdaye.mysplash.common.data.entity.item.MyFollowUser;
import com.wangdaye.mysplash.common.i.model.LoadModel;
import com.wangdaye.mysplash.common.i.model.MyFollowModel;
import com.wangdaye.mysplash.common.i.model.PagerModel;
import com.wangdaye.mysplash.common.i.model.ScrollModel;
import com.wangdaye.mysplash.common.i.presenter.LoadPresenter;
import com.wangdaye.mysplash.common.i.presenter.MyFollowPresenter;
import com.wangdaye.mysplash.common.i.presenter.PagerPresenter;
import com.wangdaye.mysplash.common.i.presenter.ScrollPresenter;
import com.wangdaye.mysplash.common.i.presenter.SwipeBackPresenter;
import com.wangdaye.mysplash.common.i.view.LoadView;
import com.wangdaye.mysplash.common.i.view.MyFollowView;
import com.wangdaye.mysplash.common.i.view.PagerView;
import com.wangdaye.mysplash.common.i.view.ScrollView;
import com.wangdaye.mysplash.common.i.view.SwipeBackView;
import com.wangdaye.mysplash.common.basic.activity.MysplashActivity;
import com.wangdaye.mysplash.common.ui.adapter.MyFollowAdapter;
import com.wangdaye.mysplash.common.ui.widget.SwipeBackCoordinatorLayout;
import com.wangdaye.mysplash.common.ui.widget.nestedScrollView.NestedScrollFrameLayout;
import com.wangdaye.mysplash.common.ui.widget.swipeRefreshView.BothWaySwipeRefreshLayout;
import com.wangdaye.mysplash.common.utils.AnimUtils;
import com.wangdaye.mysplash.common.utils.BackToTopUtils;
import com.wangdaye.mysplash.common.utils.DisplayUtils;
import com.wangdaye.mysplash.common.utils.helper.ImageHelper;
import com.wangdaye.mysplash.common.utils.manager.ThemeManager;
import com.wangdaye.mysplash.me.model.widget.LoadObject;
import com.wangdaye.mysplash.me.model.widget.MyFollowObject;
import com.wangdaye.mysplash.me.model.widget.PagerObject;
import com.wangdaye.mysplash.me.model.widget.ScrollObject;
import com.wangdaye.mysplash.me.presenter.widget.LoadImplementor;
import com.wangdaye.mysplash.me.presenter.widget.MyFollowImplementor;
import com.wangdaye.mysplash.me.presenter.widget.PagerImplementor;
import com.wangdaye.mysplash.me.presenter.widget.ScrollImplementor;
import com.wangdaye.mysplash.me.presenter.widget.SwipeBackImplementor;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * My follow user view.
 *
 * This view is used to show followers fo application user.
 *
 * */

@SuppressLint("ViewConstructor")
public class MyFollowUserView extends NestedScrollFrameLayout
        implements MyFollowView, PagerView, LoadView, ScrollView, SwipeBackView,
        BothWaySwipeRefreshLayout.OnRefreshAndLoadListener,
        MyFollowAdapter.OnFollowStateChangedListener {

    @BindView(R.id.container_loading_view_large_progressView)
    CircularProgressView progressView;

    @BindView(R.id.container_loading_view_large_feedbackContainer)
    RelativeLayout feedbackContainer;

    @BindView(R.id.container_loading_view_large_feedbackTxt)
    TextView feedbackText;

    @BindView(R.id.container_photo_list_swipeRefreshLayout)
    BothWaySwipeRefreshLayout refreshLayout;

    @BindView(R.id.container_photo_list_recyclerView)
    RecyclerView recyclerView;

    private MyFollowModel myFollowModel;
    private MyFollowPresenter myFollowPresenter;

    private PagerModel pagerModel;
    private PagerPresenter pagerPresenter;

    private LoadModel loadModel;
    private LoadPresenter loadPresenter;

    private ScrollModel scrollModel;
    private ScrollPresenter scrollPresenter;

    private SwipeBackPresenter swipeBackPresenter;

    public MyFollowUserView(MysplashActivity a, int followType,
                            int index, boolean selected) {
        super(a);
        this.initialize(a, followType, index, selected);
    }

    // init.

    @SuppressLint("InflateParams")
    private void initialize(MysplashActivity a, int followType,
                            int index, boolean selected) {
        View loadingView = LayoutInflater.from(getContext())
                .inflate(R.layout.container_loading_view_large, this, false);
        addView(loadingView);

        View contentView = LayoutInflater.from(getContext())
                .inflate(R.layout.container_photo_list, null);
        addView(contentView);

        ButterKnife.bind(this, this);
        initModel(a, followType, index, selected);
        initPresenter();
        initView();
    }

    // init.

    private void initModel(MysplashActivity a, int followType,
                           int index, boolean selected) {
        this.myFollowModel = new MyFollowObject(
                new MyFollowAdapter(a, new ArrayList<MyFollowUser>(Mysplash.DEFAULT_PER_PAGE), this),
                followType);
        this.pagerModel = new PagerObject(index, selected);
        this.loadModel = new LoadObject(LoadModel.LOADING_STATE);
        this.scrollModel = new ScrollObject();
    }

    private void initPresenter() {
        this.myFollowPresenter = new MyFollowImplementor(myFollowModel, this);
        this.pagerPresenter = new PagerImplementor(pagerModel, this);
        this.loadPresenter = new LoadImplementor(loadModel, this);
        this.scrollPresenter = new ScrollImplementor(scrollModel, this);
        this.swipeBackPresenter = new SwipeBackImplementor(this);
    }

    private void initView() {
        this.initContentView();
        this.initLoadingView();
    }

    private void initContentView() {
        refreshLayout.setColorSchemeColors(ThemeManager.getContentColor(getContext()));
        refreshLayout.setProgressBackgroundColorSchemeColor(ThemeManager.getRootColor(getContext()));
        refreshLayout.setOnRefreshAndLoadListener(this);
        refreshLayout.setPermitRefresh(false);
        refreshLayout.setVisibility(GONE);

        recyclerView.setAdapter(myFollowPresenter.getAdapter());
        recyclerView.setLayoutManager(
                new GridLayoutManager(
                        getContext(),
                        DisplayUtils.getGirdColumnCount(getContext())));
        recyclerView.addOnScrollListener(onScrollListener);
    }

    private void initLoadingView() {
        progressView.setVisibility(VISIBLE);

        feedbackContainer.setVisibility(GONE);

        ImageView feedbackImg = ButterKnife.findById(
                this, R.id.container_loading_view_large_feedbackImg);
        ImageHelper.loadResourceImage(getContext(), feedbackImg, R.drawable.feedback_no_photos);

    }

    // control.

    @Override
    public boolean isParentOffset() {
        return true;
    }

    public int getDeltaValue() {
        return myFollowPresenter.getDeltaValue();
    }

    // interface.

    // on click listener.

    @OnClick(R.id.container_loading_view_large_feedbackBtn) void retryRefresh() {
        myFollowPresenter.initRefresh(getContext());
    }

    // on refresh an load listener.

    @Override
    public void onRefresh() {
        myFollowPresenter.refreshNew(getContext(), false);
    }

    @Override
    public void onLoad() {
        myFollowPresenter.loadMore(getContext(), false);
    }

    // on follow state changed listener.

    @Override
    public void onFollowStateChanged(String username, int position, boolean switchTo, boolean succeed) {
        if (succeed) {
            myFollowPresenter.setDeltaValue(switchTo ? 1 : -1);
        }
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int firstPosition = layoutManager.findFirstVisibleItemPosition();
        int lastPosition = layoutManager.findLastVisibleItemPosition();
        if (firstPosition <= position && position <= lastPosition) {
            MyFollowAdapter.ViewHolder holder
                    = (MyFollowAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
            holder.setSwitchResult(succeed);
        }
    }

    // on scroll listener.

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
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
    public void requestMyFollowSuccess() {
        loadPresenter.setNormalState();
    }

    @Override
    public void requestMyFollowFailed(String feedback) {
        if (myFollowPresenter.getAdapter().getItemCount() > 0) {
            loadPresenter.setNormalState();
        } else {
            feedbackText.setText(feedback);
            loadPresenter.setFailedState();
        }
    }

    // pager view.

    @Override
    public void onSaveInstanceState(Bundle bundle) {

    }

    @Override
    public void onRestoreInstanceState(Bundle bundle) {

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
                && !myFollowPresenter.isRefreshing() && !myFollowPresenter.isLoading());
    }

    @Override
    public boolean checkNeedBackToTop() {
        return scrollPresenter.needBackToTop();
    }

    @Override
    public void refreshPager() {
        myFollowPresenter.initRefresh(getContext());
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
        myFollowPresenter.cancelRequest();
    }

    @Override
    public void setKey(String key) {
        // do nothing.
    }

    @Override
    public String getKey() {
        return null;
    }

    @Override
    public int getItemCount() {
        if (loadPresenter.getLoadState() != LoadModel.NORMAL_STATE) {
            return 0;
        } else {
            return myFollowPresenter.getAdapter().getItemCount();
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
        animShow(progressView);
        animHide(feedbackContainer);
        animHide(refreshLayout);
    }

    @Override
    public void setFailedState(@Nullable MysplashActivity activity, int old) {
        animShow(feedbackContainer);
        animHide(progressView);
        animHide(refreshLayout);
    }

    @Override
    public void setNormalState(@Nullable MysplashActivity activity, int old) {
        animShow(refreshLayout);
        animHide(progressView);
        animHide(feedbackContainer);
    }

    // scroll view.

    @Override
    public void scrollToTop() {
        BackToTopUtils.scrollToTop(recyclerView);
    }

    @Override
    public void autoLoad(int dy) {
        int lastVisibleItem = ((GridLayoutManager) recyclerView.getLayoutManager())
                .findLastVisibleItemPosition();
        int totalItemCount = recyclerView.getAdapter().getItemCount();
        if (myFollowPresenter.canLoadMore()
                && lastVisibleItem >= totalItemCount - 10 && totalItemCount > 0 && dy > 0) {
            myFollowPresenter.loadMore(getContext(), false);
        }
        if (!ViewCompat.canScrollVertically(recyclerView, -1)) {
            scrollPresenter.setToTop(true);
        } else {
            scrollPresenter.setToTop(false);
        }
        if (!ViewCompat.canScrollVertically(recyclerView, 1) && myFollowPresenter.isLoading()) {
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
                        || myFollowPresenter.getAdapter().getItemCount() <= 0;

            default:
                return true;
        }
    }
}
