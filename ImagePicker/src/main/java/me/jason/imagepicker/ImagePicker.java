package me.jason.imagepicker;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import java.lang.ref.WeakReference;
import java.util.Set;

public final class ImagePicker {
    private final WeakReference<Activity> mContext;
    private final WeakReference<Fragment> mFragment;

    private ImagePicker(Activity activity) {
        this(activity, null);
    }

    private ImagePicker(Fragment fragment) {
        this(fragment.getActivity(), fragment);
    }

    private ImagePicker(Activity activity, Fragment fragment) {
        mContext = new WeakReference<>(activity);
        mFragment = new WeakReference<>(fragment);
    }

    public static ImagePicker from(Activity activity) {
        return new ImagePicker(activity);
    }

    public static ImagePicker from(Fragment fragment) {
        return new ImagePicker(fragment);
    }

    public SelectionCreator choose(Set<MimeType> mimeTypes) {
        return this.choose(mimeTypes, true);
    }

    public SelectionCreator choose(Set<MimeType> mimeTypes, boolean mediaTypeExclusive) {
        return new SelectionCreator(this, mimeTypes, mediaTypeExclusive);
    }

    @Nullable
    Activity getActivity() {
        return mContext.get();
    }

    @Nullable
    Fragment getFragment() {
        return mFragment != null ? mFragment.get() : null;
    }
}
