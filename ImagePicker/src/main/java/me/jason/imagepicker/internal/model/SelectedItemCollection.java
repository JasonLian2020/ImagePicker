package me.jason.imagepicker.internal.model;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import me.jason.imagepicker.internal.entity.Item;
import me.jason.imagepicker.utils.PathUtils;

public class SelectedItemCollection {
    public static final String STATE_SELECTION = "state_selection";

    public static final int UNCHECKED = -1;

    private static SelectedItemCollection instance = null;

    private Context mContext;
    private Set<Item> mItems;

    private SelectedItemCollection() {

    }

    public static SelectedItemCollection getInstance() {
        if (instance == null) {
            synchronized (SelectedItemCollection.class) {
                if (instance == null) {
                    instance = new SelectedItemCollection();
                }
            }
        }
        return instance;
    }

    public void onCreate(Context context, Bundle bundle) {
        mContext = context.getApplicationContext();
        if (bundle == null) {
            mItems = new LinkedHashSet<>();
        } else {
            List<Item> saved = bundle.getParcelableArrayList(STATE_SELECTION);
            mItems = new LinkedHashSet<>(saved);
        }
    }

    public void onDestroy() {
        onSelectChanageListener = null;
        clearOnItemChanageListener();
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(STATE_SELECTION, new ArrayList<>(mItems));
    }

    public int count() {
        return mItems == null ? 0 : mItems.size();
    }

    public boolean add(Item item) {
        boolean added = mItems.add(item);
        // 数据发生变化，就回调
        notifyAddItem(item);
        if (onSelectChanageListener != null)
            onSelectChanageListener.onUpdate(asList());
        return added;
    }

    public boolean remove(Item item) {
        boolean removed = mItems.remove(item);
        // 数据发生变化，就回调
        notifyRemoveItem(item);
        if (onSelectChanageListener != null)
            onSelectChanageListener.onUpdate(asList());
        return removed;
    }

    public ArrayList<Item> asList() {
        return new ArrayList<>(mItems);
    }

    public List<Uri> asListOfUri() {
        List<Uri> uris = new ArrayList<>();
        for (Item item : mItems) {
            uris.add(item.getContentUri());
        }
        return uris;
    }

    public List<String> asListOfString() {
        List<String> paths = new ArrayList<>();
        for (Item item : mItems) {
            paths.add(PathUtils.getPath(mContext, item.getContentUri()));
        }
        return paths;
    }

    public boolean isEmpty() {
        return mItems == null || mItems.isEmpty();
    }

    public boolean isSelected(Item item) {
        return mItems.contains(item);
    }

    public boolean reset() {
        if (mItems == null) mItems = new LinkedHashSet<>();
        else mItems.clear();
        if (onSelectChanageListener != null)
            onSelectChanageListener.onUpdate(asList());
        return true;
    }

    public int checkedNumOf(Item item) {
        int index = new ArrayList<>(mItems).indexOf(item);
        return index == -1 ? UNCHECKED : index + 1;
    }

    private OnSelectChanageListener onSelectChanageListener;

    public void setOnSelectChanageListener(OnSelectChanageListener onSelectChanageListener) {
        this.onSelectChanageListener = onSelectChanageListener;
    }

    public interface OnSelectChanageListener {
        void onUpdate(List<Item> items);
    }

    private void notifyAddItem(Item item) {
        if (onItemChanageListeners != null && !onItemChanageListeners.isEmpty()) {
            for (OnItemChanageListener listener : onItemChanageListeners) {
                listener.onAdd(item);
            }
        }
    }

    private void notifyRemoveItem(Item item) {
        if (onItemChanageListeners != null && !onItemChanageListeners.isEmpty()) {
            for (OnItemChanageListener listener : onItemChanageListeners) {
                listener.onRemove(item);
            }
        }
    }

    private List<OnItemChanageListener> onItemChanageListeners;

    public void addOnItemChanageListener(OnItemChanageListener listener) {
        if (onItemChanageListeners == null) onItemChanageListeners = new ArrayList<>();
        onItemChanageListeners.add(listener);
    }

    public void removeOnItemChanageListener(OnItemChanageListener listener) {
        if (onItemChanageListeners != null) onItemChanageListeners.remove(listener);
    }

    public void clearOnItemChanageListener() {
        if (onItemChanageListeners != null) onItemChanageListeners.clear();
    }

    public interface OnItemChanageListener {
        void onAdd(Item item);

        void onRemove(Item item);
    }
}
