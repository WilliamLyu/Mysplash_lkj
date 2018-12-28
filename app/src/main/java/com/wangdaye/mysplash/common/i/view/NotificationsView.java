package com.wangdaye.mysplash.common.i.view;

/**
 * Notifications view.
 *
 * A vew which is used to show user's
 * {@link com.wangdaye.mysplash.common.data.entity.unsplash.NotificationResult}.
 *
 * */

public interface NotificationsView {

    void setRefreshing(boolean refreshing);
    void setLoading(boolean loading);

    void setPermitRefreshing(boolean permit);
    void setPermitLoading(boolean permit);

    void initRefreshStart();
    void requestNotificationsSuccess();
    void requestNotificationsFailed(String feedback);
}
