package com.wangdaye.mysplash.common.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wangdaye.mysplash.Mysplash;
import com.wangdaye.mysplash.R;
import com.wangdaye.mysplash.common.basic.FooterAdapter;
import com.wangdaye.mysplash.common.basic.activity.LoadableActivity;
import com.wangdaye.mysplash.common.data.entity.unsplash.ActionObject;
import com.wangdaye.mysplash.common.data.entity.unsplash.Collection;
import com.wangdaye.mysplash.common.data.entity.unsplash.FollowingResult;
import com.wangdaye.mysplash.common.data.entity.unsplash.LikePhotoResult;
import com.wangdaye.mysplash.common.data.entity.unsplash.Photo;
import com.wangdaye.mysplash.common.data.entity.unsplash.User;
import com.wangdaye.mysplash.common.data.service.PhotoService;
import com.wangdaye.mysplash.common.basic.activity.MysplashActivity;
import com.wangdaye.mysplash.common.ui.dialog.DownloadRepeatDialog;
import com.wangdaye.mysplash.common.ui.dialog.SelectCollectionDialog;
import com.wangdaye.mysplash.common.ui.widget.CircleImageView;
import com.wangdaye.mysplash.common.ui.widget.CircularProgressIcon;
import com.wangdaye.mysplash.common.ui.widget.freedomSizeView.FreedomImageView;
import com.wangdaye.mysplash.common.utils.DisplayUtils;
import com.wangdaye.mysplash.common.utils.FileUtils;
import com.wangdaye.mysplash.common.utils.helper.NotificationHelper;
import com.wangdaye.mysplash.common.utils.helper.DatabaseHelper;
import com.wangdaye.mysplash.common.utils.helper.DownloadHelper;
import com.wangdaye.mysplash.common.utils.helper.ImageHelper;
import com.wangdaye.mysplash.common.utils.helper.IntentHelper;
import com.wangdaye.mysplash.common.utils.manager.AuthManager;
import com.wangdaye.mysplash.common.utils.manager.ThemeManager;
import com.wangdaye.mysplash.user.view.activity.UserActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Following adapter.
 *
 * Adapter for {@link RecyclerView} to show {@link FollowingResult}.
 *
 * */

public class FollowingAdapter extends FooterAdapter<RecyclerView.ViewHolder>
        implements PhotoHolder.OnClickPhotoItemListener,
        SelectCollectionDialog.OnCollectionsChangedListener {

    private Context a;
    private RecyclerView recyclerView;

    private List<FollowingResult> resultList; // this list is used to save the feed data.
    private List<ViewType> typeList; // this list is used to save the display information of view holder.

    private PhotoService photoService;

    private int columnCount;

    private static final int MAXI_PHOTO_COUNT_LIST = 7;
    private static final int MAXI_PHOTO_COUNT_GIRD = 11;

    private static final String KEY_FOLLOWING_ADAPTER_RESULT_POSITION = "following_adapter_result_position";

    /**
     * This class is used to save the view holder's information.
     * */
    private class ViewType {
        // data
        int resultPosition;
        int objectPosition;

        @ViewTypeRule
        int type;

        ViewType(int resultPosition, int objectPosition, int type) {
            this.resultPosition = resultPosition;
            this.objectPosition = objectPosition;
            this.type = type;
        }
    }

    @IntDef({
            TitleHolder.VIEW_TYPE_TITLE,
            PhotoHolder.VIEW_TYPE_PHOTO,
            UserHolder.VIEW_TYPE_USER,
            MoreHolder.VIEW_TYPE_MORE})
    @interface ViewTypeRule {}

    public FollowingAdapter(Context a, List<FollowingResult> list) {
        this(a, list, DisplayUtils.getGirdColumnCount(a));
    }

    private FollowingAdapter(Context a, List<FollowingResult> list, int columnCount) {
        this.a = a;
        this.resultList = list;
        this.typeList = new ArrayList<>();
        buildTypeList(list);
        this.columnCount = columnCount;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        if (isFooter(position)) {
            // footer.
            return FooterHolder.buildInstance(parent);
        } else {
            ViewType viewType = typeList.get(position);
            switch (viewType.type) {
                case TitleHolder.VIEW_TYPE_TITLE: {
                    View v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_following_title, parent, false);
                    return new TitleHolder(v, columnCount);
                }
                case PhotoHolder.VIEW_TYPE_PHOTO: {
                    View v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_following_photo, parent, false);
                    return new PhotoHolder(v, this);
                }
                case UserHolder.VIEW_TYPE_USER: {
                    View v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_following_user, parent, false);
                    return new UserHolder(v, this, columnCount);
                }
                case MoreHolder.VIEW_TYPE_MORE:
                default: {
                    View v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_following_more, parent, false);
                    return new MoreHolder(v, this);
                }
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (!isFooter(position)) {
            if (holder instanceof TitleHolder) {
                ((TitleHolder) holder).onBindView(
                        a, resultList.get(typeList.get(position).resultPosition));
            } else if (holder instanceof PhotoHolder) {
                ((PhotoHolder) holder).onBindView(
                        a,  getPhoto(position), position, columnCount);
            } else if (holder instanceof UserHolder) {
                ((UserHolder) holder).onBindView(a, getUser(position), position);
            } else if (holder instanceof MoreHolder) {
                ((MoreHolder) holder).onBindView(
                        a,
                        resultList.get(typeList.get(position).resultPosition),
                        getPhoto(position),
                        position,
                        columnCount);
            }
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof TitleHolder) {
            ((TitleHolder) holder).onRecycled();
        } else if (holder instanceof PhotoHolder) {
            ((PhotoHolder) holder).onRecycled();
        } else if (holder instanceof UserHolder) {
            ((UserHolder) holder).onRecycled();
        } else if (holder instanceof MoreHolder) {
            ((MoreHolder) holder).onRecycled();
        }
    }

    @Override
    public int getRealItemCount() {
        return typeList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    protected boolean hasFooter() {
        return !DisplayUtils.isLandscape(a)
                && DisplayUtils.getNavigationBarHeight(a.getResources()) != 0;
    }

    // control.

    public void setActivity(MysplashActivity a) {
        this.a = a;
    }

    public void setRecyclerView(RecyclerView v) {
        this.recyclerView = v;
    }

    public void insertItem(FollowingResult item) {
        resultList.add(item);
        addType(item, resultList.size() - 1);
        notifyItemInserted(typeList.size() - 1);
    }

    public void clearItem() {
        resultList.clear();
        typeList.clear();
        notifyDataSetChanged();
    }

    void setLikeForAPhoto(boolean like, int position) {
        if (photoService == null) {
            photoService = PhotoService.getService();
        }
        Photo photo = getPhoto(position);
        if (photo != null) {
            photo.settingLike = true;
            resultList.get(typeList.get(position).resultPosition)
                    .objects.set(typeList.get(position).objectPosition, new ActionObject(photo));
            photoService.setLikeForAPhoto(
                    photo.id,
                    like,
                    new OnSetLikeListener(photo.id, position));
        }
    }

    public boolean isFooterView(int position) {
        return typeList.size() <= position + 1
                || typeList.get(position + 1).type == TitleHolder.VIEW_TYPE_TITLE;
    }

    int getMaxiPhotoCount() {
        if (columnCount > 1) {
            return MAXI_PHOTO_COUNT_GIRD;
        } else {
            return MAXI_PHOTO_COUNT_LIST;
        }
    }

    public void setTitleAvatarVisibility(RecyclerView recyclerView, int lastPosition, int newPosition) {
        RecyclerView.ViewHolder lastHolder = recyclerView.findViewHolderForAdapterPosition(lastPosition);
        if (lastHolder != null && lastHolder instanceof TitleHolder) {
            ((TitleHolder) lastHolder).setAvatarVisibility(true);
        }

        RecyclerView.ViewHolder newHolder = recyclerView.findViewHolderForAdapterPosition(newPosition);
        if (newHolder != null && newHolder instanceof TitleHolder) {
            ((TitleHolder) newHolder).setAvatarVisibility(false);
        }
    }

    // type list.

    private void buildTypeList(List<FollowingResult> resultList) {
        for (int i = 0; i < resultList.size(); i ++) {
            addType(resultList.get(i), i);
        }
    }

    private void addType(FollowingResult result, int i) {
        if (result.verb.equals(FollowingResult.VERB_FOLLOWED)) {
            // if the feed information is about someone followed some users.

            // firstly, add title view.
            typeList.add(new ViewType(i, -1, TitleHolder.VIEW_TYPE_TITLE));
            // then, add all of the user that was followed by the actor.
            for (int j = 0; j < result.objects.size(); j ++) {
                typeList.add(new ViewType(i, j, UserHolder.VIEW_TYPE_USER));
            }
        } else {
            // the feed information is about the operation of photos.

            // firstly, remove the photo without size data.
            for (int j = 0; j < result.objects.size(); j ++) {
                if (result.objects.get(j).width == 0 || result.objects.get(j).height == 0
                        || result.objects.get(j).castToPhoto() == null) {
                    result.objects.remove(j);
                    j --;
                }
            }
            if (result.objects.size() > 0) {
                // then, add title view.
                typeList.add(new ViewType(i, -1, TitleHolder.VIEW_TYPE_TITLE));
                // after that, add up to 'MAX_DISPLAY_PHOTO_COUNT' photos.
                for (int j = 0; j < getMaxiPhotoCount() && j < result.objects.size(); j ++) {
                    typeList.add(new ViewType(i, j, PhotoHolder.VIEW_TYPE_PHOTO));
                }
                // at last, if the number of photos is more than 'MAX_DISPLAY_PHOTO_COUNT',
                // add a view to show that there are more photos.
                if (result.objects.size() > getMaxiPhotoCount()) {
                    typeList.add(new ViewType(i, getMaxiPhotoCount(), MoreHolder.VIEW_TYPE_MORE));
                }
            }
        }
    }

    // actor.

    public User getActor(int position) {
        return resultList.get(typeList.get(position).resultPosition).actors.get(0);
    }

    // verb.

    public String getVerb(int position) {
        return resultList.get(typeList.get(position).resultPosition).verb;
    }

    // user.

    User getUser(int position) {
        ViewType viewType = typeList.get(position);
        switch (viewType.type) {
            case TitleHolder.VIEW_TYPE_TITLE:
            case PhotoHolder.VIEW_TYPE_PHOTO:
            case MoreHolder.VIEW_TYPE_MORE:
                return resultList.get(viewType.resultPosition)
                        .actors.get(0);

            case UserHolder.VIEW_TYPE_USER:
            default:
                return resultList.get(viewType.resultPosition)
                        .objects.get(viewType.objectPosition)
                        .castToUser();
        }
    }

    void updateUser(User user, int position) {
        FollowingResult result = resultList.get(typeList.get(position).resultPosition);
        result.objects.set(typeList.get(position).objectPosition, new ActionObject(user));
        resultList.set(typeList.get(position).resultPosition, result);
    }

    // photo.

    @Nullable
    Photo getPhoto(int position) {
        ViewType viewType = typeList.get(position);
        switch (viewType.type) {
            case PhotoHolder.VIEW_TYPE_PHOTO:
            case MoreHolder.VIEW_TYPE_MORE:
                return resultList.get(viewType.resultPosition)
                        .objects.get(viewType.objectPosition)
                        .castToPhoto();

            case TitleHolder.VIEW_TYPE_TITLE:
            case UserHolder.VIEW_TYPE_USER:
            default:
                return null;
        }
    }

    public void updatePhoto(RecyclerView recyclerView, Photo p, boolean refreshView, boolean probablyRepeat) {
        for (int i = 0; i < resultList.size(); i ++) {
            int j;
            if (!resultList.get(i).verb.equals(FollowingResult.VERB_FOLLOWED)) {
                for (j = 0; j < resultList.get(i).objects.size(); j ++) {
                    if (resultList.get(i).objects.get(j).id.equals(p.id)) {
                        resultList.get(i).objects.set(j, new ActionObject(p));
                        if (refreshView && j < getMaxiPhotoCount()) {
                            updatePhotoItemView(recyclerView, p, i);
                        }
                        if (!probablyRepeat) {
                            return;
                        }
                    }
                }
            }
        }
    }

    private void updatePhotoItemView(@Nullable RecyclerView recyclerView, Photo photo, int position) {
        if (recyclerView == null) {
            notifyItemChanged(position);
        } else if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            int firstVisiblePosition = ((LinearLayoutManager) recyclerView.getLayoutManager())
                    .findFirstVisibleItemPosition();
            int lastVisiblePosition = ((LinearLayoutManager) recyclerView.getLayoutManager())
                    .findLastVisibleItemPosition();
            if (firstVisiblePosition <= position && position <= lastVisiblePosition) {
                // is a visible item.
                RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
                if (holder != null && holder instanceof PhotoHolder) {
                    ((PhotoHolder) holder).update(photo);
                }
            } else {
                notifyItemChanged(position);
            }
        }
    }

    void updatePhoto(Photo photo, int position) {
        FollowingResult result = resultList.get(typeList.get(position).resultPosition);
        result.objects.set(typeList.get(position).objectPosition, new ActionObject(photo));
        resultList.set(typeList.get(position).resultPosition, result);
    }

    public List<Photo> getPhotoListFromAnIndex(Bundle bundle, int fromIndex) {
        int position = bundle.getInt(KEY_FOLLOWING_ADAPTER_RESULT_POSITION, -1);
        return getPhotoList(
                position,
                fromIndex,
                resultList.get(position).objects.size() - 1);
    }

    public List<Photo> getPhotoListToAnIndex(Bundle bundle, int toIndex) {
        return getPhotoList(bundle.getInt(KEY_FOLLOWING_ADAPTER_RESULT_POSITION, -1), 0, toIndex);
    }

    private List<Photo> getPhotoList(int resultPosition, int fromIndex, int toIndex) {
        List<Photo> list = new ArrayList<>();
        if (-1 < resultPosition && resultPosition < resultList.size()
                && !resultList.get(resultPosition).verb.equals(FollowingResult.VERB_FOLLOWED)
                && fromIndex <= toIndex
                && fromIndex > -1
                && toIndex < resultList.get(resultPosition).objects.size()) {
            for (int i = fromIndex; i <= toIndex; i ++) {
                list.add(resultList.get(resultPosition).objects.get(i).castToPhoto());
            }
        }
        return list;
    }

    // feeds.

    public List<FollowingResult> getFeeds() {
        return new ArrayList<>(resultList);
    }

    public void setFeeds(List<FollowingResult> list) {
        resultList.clear();
        resultList.addAll(list);
        typeList = new ArrayList<>();
        buildTypeList(list);
        notifyDataSetChanged();
    }

    /** <br> interface. */

    // on set like listener.

    private class OnSetLikeListener implements PhotoService.OnSetLikeListener {

        private String id;
        private int position;

        OnSetLikeListener(String id, int position) {
            this.id = id;
            this.position = position;
        }

        @Override
        public void onSetLikeSuccess(Call<LikePhotoResult> call, Response<LikePhotoResult> response) {
            if (Mysplash.getInstance() != null && Mysplash.getInstance().getTopActivity() != null) {
                if (typeList.size() < position) {
                    return;
                }
                Photo photo = getPhoto(position);
                if (photo != null && photo.id.equals(id)) {
                    photo.settingLike = false;

                    if (response.isSuccessful() && response.body() != null) {
                        photo.liked_by_user = response.body().photo.liked_by_user;
                        photo.likes = response.body().photo.likes;
                    } else {
                        NotificationHelper.showSnackbar(
                                photo.liked_by_user ?
                                        a.getString(R.string.feedback_unlike_failed) : a.getString(R.string.feedback_like_failed));
                    }

                    resultList.get(typeList.get(position).resultPosition)
                            .objects.set(typeList.get(position).objectPosition, new ActionObject(photo));

                    updateView(photo.liked_by_user);
                }
            }
        }

        @Override
        public void onSetLikeFailed(Call<LikePhotoResult> call, Throwable t) {
            if (Mysplash.getInstance() != null && Mysplash.getInstance().getTopActivity() != null) {
                if (typeList.size() < position) {
                    return;
                }
                Photo photo = getPhoto(position);
                if (photo != null && photo.id.equals(id)) {
                    photo.settingLike = false;

                    NotificationHelper.showSnackbar(
                            photo.liked_by_user ?
                                    a.getString(R.string.feedback_unlike_failed) : a.getString(R.string.feedback_like_failed));

                    resultList.get(typeList.get(position).resultPosition)
                            .objects.set(typeList.get(position).objectPosition, new ActionObject(photo));
                    updateView(photo.liked_by_user);
                }
            }
        }

        private void updateView(boolean to) {
            if (recyclerView != null) {
                StaggeredGridLayoutManager layoutManager
                        = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {int[] firstPositions = layoutManager.findFirstVisibleItemPositions(null);
                    int[] lastPositions = layoutManager.findLastVisibleItemPositions(null);
                    if (firstPositions[0] <= position
                            && position <= lastPositions[lastPositions.length - 1]) {
                        PhotoHolder holder = (PhotoHolder) recyclerView.findViewHolderForAdapterPosition(position);
                        if (holder != null) {
                            holder.likeButton.setResultState(
                                    to ? R.drawable.ic_item_heart_red : R.drawable.ic_item_heart_outline);
                        }
                    }
                }
            }
        }
    }

    // on click photo item listener.

    @Override
    public void onClick(View image, View background, int position) {
        MysplashActivity a = Mysplash.getInstance().getTopActivity();
        if (a != null) {
            ViewType viewType = typeList.get(position);
            ArrayList<Photo> list = new ArrayList<>();
            int headIndex = viewType.objectPosition - 2;
            int size = 5;
            if (headIndex < 0) {
                headIndex = 0;
            }
            if (headIndex + size - 1 > resultList.get(viewType.resultPosition).objects.size() - 1) {
                size = resultList.get(viewType.resultPosition).objects.size() - headIndex;
            }
            for (int i = 0; i < size; i ++) {
                list.add(resultList.get(viewType.resultPosition).objects.get(headIndex + i).castToPhoto());
            }

            Bundle bundle = new Bundle();
            if (a instanceof LoadableActivity) {
                bundle = ((LoadableActivity) a).getBundleOfList();
                bundle.putInt(KEY_FOLLOWING_ADAPTER_RESULT_POSITION, viewType.resultPosition);
            }

            IntentHelper.startPhotoActivity(
                    a, image, background,
                    list, viewType.objectPosition, headIndex,
                    bundle);
        }
    }

    // on collections changed listener.

    @Override
    public void onAddCollection(Collection c) {
        // do nothing.
    }

    @Override
    public void onUpdateCollection(Collection c, User u, Photo p) {
        updatePhoto(recyclerView, p, true, true);
    }
}

/**
 * Title holder.
 *
 * ViewHolder class for {@link FollowingAdapter} to show the title part of following feed data.
 *
 * */
class TitleHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.item_following_title_background)
    RelativeLayout background;

    @BindView(R.id.item_following_title_avatar)
    CircleImageView avatar;

    @BindView(R.id.item_following_title_verbIcon)
    ImageView verbIcon;

    @BindView(R.id.item_following_title_actor)
    TextView actor;

    @BindView(R.id.item_following_title_verb)
    TextView verb;

    private FollowingResult result;
    private boolean avatarVisibility;
    static final int VIEW_TYPE_TITLE = 0;

    TitleHolder(View itemView, int columnCount) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        DisplayUtils.setTypeface(itemView.getContext(), verb);

        avatarVisibility = false;
        setAvatarVisibility(true);

        if (columnCount > 1) {
            StaggeredGridLayoutManager.LayoutParams params
                    = (StaggeredGridLayoutManager.LayoutParams) background.getLayoutParams();
            params.setFullSpan(true);
            background.setLayoutParams(params);
        }
    }

    @SuppressLint("SetTextI18n")
    void onBindView(Context a, FollowingResult result) {
        this.result = result;

        User user = result.actors.get(0);

        actor.setText(user.name);
        ImageHelper.loadAvatar(avatar.getContext(), avatar, user, getAdapterPosition(), null);

        switch (result.verb) {
            case FollowingResult.VERB_LIKED:
                verbIcon.setImageResource(R.drawable.ic_verb_liked);
                verb.setVisibility(View.VISIBLE);
                verb.setText(
                        a.getString(R.string.liked)
                                + " " + result.objects.size()
                                + " " + a.getString(R.string.photos)
                                + ".");
                break;

            case FollowingResult.VERB_COLLECTED:
                verbIcon.setImageResource(R.drawable.ic_verb_collected);
                verb.setVisibility(View.VISIBLE);
                verb.setText(
                        Html.fromHtml(
                                a.getString(R.string.collected)
                                        + " " + result.objects.size()
                                        + " " + a.getString(R.string.photos) + " " + a.getString(R.string.to)
                                        + " <u>" + result.targets.get(0).title + "</u>"
                                        + "."));
                break;

            case FollowingResult.VERB_FOLLOWED:
                verbIcon.setImageResource(
                        ThemeManager.getInstance(a).isLightTheme() ?
                                R.drawable.ic_verb_followed_light : R.drawable.ic_verb_followed_dark);
                verb.setVisibility(View.VISIBLE);
                verb.setText(
                        a.getString(R.string.followed)
                                + " " + result.objects.size()
                                + " " + a.getString(R.string.users)
                                + ".");
                break;

            case FollowingResult.VERB_RELEASE:
                verbIcon.setImageResource(R.drawable.ic_verb_published);
                verb.setVisibility(View.VISIBLE);
                verb.setText(
                        a.getString(R.string.released)
                                + " " + result.objects.size()
                                + " " + a.getString(R.string.photos)
                                + ".");
                break;

            case FollowingResult.VERB_PUBLISHED:
                verbIcon.setImageResource(R.drawable.ic_verb_published);
                verb.setVisibility(View.VISIBLE);
                verb.setText(
                        a.getString(R.string.published)
                                + " " + result.objects.size()
                                + " " + a.getString(R.string.photos)
                                + ".");
                break;

            case FollowingResult.VERB_CURATED:
                verbIcon.setImageResource(R.drawable.ic_verb_curated);
                verb.setVisibility(View.VISIBLE);
                verb.setText(
                        Html.fromHtml(
                                a.getString(R.string.curated)
                                        + " " + result.objects.size()
                                        + " " + a.getString(R.string.photos)
                                        + (result.targets.size() > 0 ?
                                        " " + a.getString(R.string.to)
                                                + " <u>" + result.targets.get(0).title + "</u>"
                                        :
                                        "")
                                        + "."));
                break;

            default:
                verb.setVisibility(View.GONE);
                break;
        }
    }

    void onRecycled() {
        ImageHelper.releaseImageView(avatar);
    }

    public void setAvatarVisibility(boolean visibility) {
        if (visibility != avatarVisibility) {
            avatarVisibility = visibility;

            float alpha = visibility ? 1F : 0F;
            avatar.setAlpha(alpha);
            verbIcon.setAlpha(alpha);
            avatar.setEnabled(visibility);
        }
    }

    // interface.

    @OnClick({
            R.id.item_following_title_avatar,
            R.id.item_following_title_actor}) void checkActor() {
        MysplashActivity a = Mysplash.getInstance().getTopActivity();
        if (a != null) {
            IntentHelper.startUserActivity(
                    a,
                    avatar,
                    background,
                    result.actors.get(0),
                    UserActivity.PAGE_PHOTO);
        }
    }

    @OnClick(R.id.item_following_title_verb) void clickVerb() {
        MysplashActivity a = Mysplash.getInstance().getTopActivity();
        if (a != null) {
            switch (result.verb) {
                case FollowingResult.VERB_COLLECTED:
                case FollowingResult.VERB_CURATED:
                    IntentHelper.startCollectionActivity(
                            a,
                            avatar,
                            background,
                            result.targets.get(0));
                    break;
                case FollowingResult.VERB_RELEASE:
                case FollowingResult.VERB_PUBLISHED:
                    IntentHelper.startUserActivity(
                            a,
                            avatar,
                            background,
                            result.actors.get(0),
                            UserActivity.PAGE_PHOTO);
                    break;
                case FollowingResult.VERB_LIKED:
                    IntentHelper.startUserActivity(
                            a,
                            avatar,
                            background,
                            result.actors.get(0),
                            UserActivity.PAGE_LIKE);
                    break;
            }
        }
    }
}

/**
 * Photo holder.
 *
 * ViewHolder class for {@link FollowingAdapter} to show photo data.
 *
 * */
class PhotoHolder extends RecyclerView.ViewHolder
        implements ImageHelper.OnLoadImageListener<Photo>,
        DownloadRepeatDialog.OnCheckOrDownloadListener {

    @BindView(R.id.item_following_photo_background)
    RelativeLayout background;

    @BindView(R.id.item_following_photo_image)
    FreedomImageView image;

    @BindView(R.id.item_following_photo_title)
    TextView title;

    @BindView(R.id.item_following_photo_collectionButton)
    ImageButton collectionButton;

    @BindView(R.id.item_following_photo_likeButton)
    CircularProgressIcon likeButton;

    private FollowingAdapter adapter;
    private Photo photo;
    private int position;
    static final int VIEW_TYPE_PHOTO = 1;

    private OnClickPhotoItemListener listener;

    PhotoHolder(View itemView, FollowingAdapter adapter) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.adapter = adapter;
        DisplayUtils.setTypeface(itemView.getContext(), title);
        this.listener = adapter;
    }

    void onBindView(Context a, Photo photo, int position, int columnCount) {
        this.photo = photo;
        this.position = position;

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) background.getLayoutParams();
        int margin = a.getResources().getDimensionPixelSize(R.dimen.little_margin);
        if (columnCount > 1) {
            params.setMargins(0, 0, margin, margin);
            background.setLayoutParams(params);
        } else {
            params.setMargins(
                    a.getResources().getDimensionPixelSize(R.dimen.large_icon_size), 0, margin, margin);
            background.setLayoutParams(params);
        }

        image.setSize(photo.width, photo.height);

        title.setText("");
        image.setShowShadow(false);

        ImageHelper.loadRegularPhoto(image.getContext(), image, photo, position, this);

        if (photo.current_user_collections.size() != 0) {
            collectionButton.setImageResource(R.drawable.ic_item_collected);
        } else {
            collectionButton.setImageResource(R.drawable.ic_item_collect);
        }

        if (photo.settingLike) {
            likeButton.forceSetProgressState();
        } else {
            likeButton.forceSetResultState(photo.liked_by_user ?
                    R.drawable.ic_item_heart_red : R.drawable.ic_item_heart_outline);
        }

        background.setBackgroundColor(
                ImageHelper.computeCardBackgroundColor(background.getContext(), photo.color));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            image.setTransitionName(photo.id + "-" + position + "-cover");
            background.setTransitionName(photo.id + "-" + position + "-background");
        }
    }

    void onRecycled() {
        ImageHelper.releaseImageView(image);
    }

    void update(Photo photo) {
        this.photo = photo;

        ImageHelper.preloadRegularPhoto(image.getContext(), photo);

        if (photo.current_user_collections.size() != 0) {
            collectionButton.setImageResource(R.drawable.ic_item_collected);
        } else {
            collectionButton.setImageResource(R.drawable.ic_item_collect);
        }

        if (photo.settingLike) {
            likeButton.setProgressState();
        } else if (likeButton.getState() == CircularProgressIcon.STATE_PROGRESS) {
            likeButton.setResultState(photo.liked_by_user ?
                    R.drawable.ic_item_heart_red : R.drawable.ic_item_heart_outline);
        } else {
            likeButton.forceSetResultState(photo.liked_by_user ?
                    R.drawable.ic_item_heart_red : R.drawable.ic_item_heart_outline);
        }
    }

    // interface.

    interface OnClickPhotoItemListener {
        void onClick(View image, View background, int position);
    }

    @OnClick(R.id.item_following_photo_background) void clickItem() {
        if (listener != null) {
            listener.onClick(image, background, position);
        }
    }

    @OnClick(R.id.item_following_photo_likeButton) void likePhoto() {
        MysplashActivity a = Mysplash.getInstance().getTopActivity();
        if (a != null) {
            if (AuthManager.getInstance().isAuthorized()) {
                if (likeButton.isUsable() && photo != null) {
                    likeButton.setProgressState();
                    adapter.setLikeForAPhoto(!photo.liked_by_user, position);
                }
            } else {
                IntentHelper.startLoginActivity(a);
            }
        }
    }

    @OnClick(R.id.item_following_photo_collectionButton) void collectPhoto() {
        MysplashActivity a = Mysplash.getInstance().getTopActivity();
        if (a != null) {
            if (!AuthManager.getInstance().isAuthorized()) {
                IntentHelper.startLoginActivity(a);
            } else {
                SelectCollectionDialog dialog = new SelectCollectionDialog();
                dialog.setPhotoAndListener(photo, adapter);
                dialog.show(a.getFragmentManager(), null);
            }
        }
    }

    @OnClick(R.id.item_following_photo_downloadButton) void downloadPhoto() {
        MysplashActivity a = Mysplash.getInstance().getTopActivity();
        if (a != null) {
            if (DatabaseHelper.getInstance(a).readDownloadingEntityCount(photo.id) > 0) {
                NotificationHelper.showSnackbar(a.getString(R.string.feedback_download_repeat));
            } else if (FileUtils.isPhotoExists(a, photo.id)) {
                MysplashActivity activity = Mysplash.getInstance().getTopActivity();
                if (activity != null) {
                    DownloadRepeatDialog dialog = new DownloadRepeatDialog();
                    dialog.setDownloadKey(photo);
                    dialog.setOnCheckOrDownloadListener(this);
                    dialog.show(activity.getFragmentManager(), null);
                }
            } else {
                DownloadHelper.getInstance(a).addMission(a, photo, DownloadHelper.DOWNLOAD_TYPE);
            }
        }
    }

    // on load image listener.

    @Override
    public void onLoadImageSucceed(Photo newT, int index) {
        if (photo.updateLoadInformation(newT)) {
            Photo p = adapter.getPhoto(index);
            if (p != null) {
                p.updateLoadInformation(newT);
                adapter.updatePhoto(p, index);
            }
        }
        title.setText(newT.user.name);
        image.setShowShadow(true);
    }

    @Override
    public void onLoadImageFailed(Photo originalT, int index) {
        // do nothing.
    }

    // on check or download listener. (download repeat dialog)

    @Override
    public void onCheck(Object obj) {
        MysplashActivity a = Mysplash.getInstance().getTopActivity();
        if (a != null) {
            IntentHelper.startCheckPhotoActivity(a, ((Photo) obj).id);
        }
    }

    @Override
    public void onDownload(Object obj) {
        MysplashActivity a = Mysplash.getInstance().getTopActivity();
        if (a != null) {
            DownloadHelper.getInstance(a).addMission(a, photo, DownloadHelper.DOWNLOAD_TYPE);
        }
    }
}

/**
 * User holder.
 *
 * ViewHolder class for {@link FollowingAdapter} to show user information.
 *
 * */
class UserHolder extends RecyclerView.ViewHolder
        implements ImageHelper.OnLoadImageListener<User> {

    @BindView(R.id.item_following_user_background)
    RelativeLayout background;

    @BindView(R.id.item_following_user_avatar)
    CircleImageView avatar;

    @BindView(R.id.item_following_user_title)
    TextView title;

    @BindView(R.id.item_following_user_subtitle)
    TextView subtitle;

    private FollowingAdapter adapter;
    private User user;
    static final int VIEW_TYPE_USER = 2;

    UserHolder(View itemView, FollowingAdapter adapter, int columnCount) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.adapter = adapter;
        DisplayUtils.setTypeface(Mysplash.getInstance().getTopActivity(), subtitle);

        if (columnCount > 1) {
            StaggeredGridLayoutManager.LayoutParams params
                    = (StaggeredGridLayoutManager.LayoutParams) background.getLayoutParams();
            params.setFullSpan(true);
            background.setLayoutParams(params);
        }
    }

    @SuppressLint("SetTextI18n")
    void onBindView(Context a, User user, int position) {
        this.user = user;

        title.setText(user.name);
        if (TextUtils.isEmpty(user.bio)) {
            subtitle.setText(
                    user.total_photos + " " + a.getResources().getStringArray(R.array.user_tabs)[0] + ", "
                            + user.total_collections + " " + a.getResources().getStringArray(R.array.user_tabs)[1] + ", "
                            + user.total_likes + " " + a.getResources().getStringArray(R.array.user_tabs)[2]);
        } else {
            subtitle.setText(user.bio);
        }

        ImageHelper.loadAvatar(avatar.getContext(), avatar, user, position, this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            avatar.setTransitionName(user.username + "-" + position + "-avatar");
            background.setTransitionName(user.username + "-" + position + "-background");
        }
    }

    void onRecycled() {
        ImageHelper.releaseImageView(avatar);
    }

    // interface.

    @OnClick(R.id.item_following_user_background) void cliclItem() {
        MysplashActivity a = Mysplash.getInstance().getTopActivity();
        if (a != null) {
            IntentHelper.startUserActivity(
                    a,
                    avatar,
                    background,
                    user,
                    UserActivity.PAGE_PHOTO);
        }
    }

    // on load image listener.

    @Override
    public void onLoadImageSucceed(User newT, int index) {
        if (user.updateLoadInformation(newT)) {
            User u = adapter.getUser(index);
            u.updateLoadInformation(newT);
            adapter.updateUser(u, index);
        }
    }

    @Override
    public void onLoadImageFailed(User originalT, int index) {
        // do nothing.
    }
}

/**
 * More holder.
 *
 * ViewHolder class for {@link FollowingAdapter} to show "more" information.
 *
 * */
class MoreHolder extends RecyclerView.ViewHolder
        implements ImageHelper.OnLoadImageListener<Photo> {

    @BindView(R.id.item_following_more_background)
    RelativeLayout background;

    @BindView(R.id.item_following_more_image)
    FreedomImageView image;

    @BindView(R.id.item_following_more_avatar)
    CircleImageView avatar;

    @BindView(R.id.item_following_more_title)
    TextView more;

    private FollowingAdapter adapter;
    private FollowingResult result;
    private Photo photo;
    static final int VIEW_TYPE_MORE = 3;

    MoreHolder(View itemView, FollowingAdapter adapter) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.adapter = adapter;
    }

    @SuppressLint("SetTextI18n")
    void onBindView(Context a, FollowingResult result, Photo photo, int position, int columnCount) {
        this.result = result;
        this.photo = photo;

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) background.getLayoutParams();
        int margin = a.getResources().getDimensionPixelSize(R.dimen.little_margin);
        if (columnCount > 1) {
            params.setMargins(0, 0, margin, margin);
            background.setLayoutParams(params);
        } else {
            params.setMargins(
                    a.getResources().getDimensionPixelSize(R.dimen.large_icon_size), 0, margin, margin);
            background.setLayoutParams(params);
        }

        image.setSize(photo.width, photo.height);

        more.setText(
                (result.objects.size() - adapter.getMaxiPhotoCount())
                        + " " + a.getString(R.string.more));
        ImageHelper.loadRegularPhoto(image.getContext(), image, photo, position, this);
        ImageHelper.loadAvatar(avatar.getContext(), avatar, result.actors.get(0), getAdapterPosition(), null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            avatar.setTransitionName(result.actors.get(0).username + "-" + position + "-avatar");
            background.setTransitionName(result.actors.get(0).username + "-" + position + "-background");
        }
    }

    void onRecycled() {
        ImageHelper.releaseImageView(image);
        ImageHelper.releaseImageView(avatar);
    }

    // interface.

    @OnClick(R.id.item_following_more_background) void clickItem() {
        MysplashActivity a = Mysplash.getInstance().getTopActivity();
        if (a != null) {
            switch (result.verb) {
                case FollowingResult.VERB_COLLECTED:
                case FollowingResult.VERB_CURATED:
                    IntentHelper.startCollectionActivity(
                            a,
                            avatar,
                            background,
                            result.targets.get(0));
                    break;

                case FollowingResult.VERB_RELEASE:
                case FollowingResult.VERB_PUBLISHED:
                    IntentHelper.startUserActivity(
                            a,
                            avatar,
                            background,
                            result.actors.get(0),
                            UserActivity.PAGE_PHOTO);
                    break;

                case FollowingResult.VERB_LIKED:
                    IntentHelper.startUserActivity(
                            a,
                            avatar,
                            background,
                            result.actors.get(0),
                            UserActivity.PAGE_LIKE);
                    break;
            }
        }
    }

    @OnClick(R.id.item_following_more_avatar) void checkActor() {
        MysplashActivity a = Mysplash.getInstance().getTopActivity();
        if (a != null) {
            IntentHelper.startUserActivity(
                    a,
                    avatar,
                    background,
                    result.actors.get(0),
                    UserActivity.PAGE_LIKE);
        }
    }

    // on load image listener.

    @Override
    public void onLoadImageSucceed(Photo newT, int index) {
        if (photo.updateLoadInformation(newT)) {
            Photo p = adapter.getPhoto(index);
            if (p != null) {
                p.updateLoadInformation(newT);
                adapter.updatePhoto(p, index);
            }
        }
    }

    @Override
    public void onLoadImageFailed(Photo originalT, int index) {
        // do nothing.
    }
}
