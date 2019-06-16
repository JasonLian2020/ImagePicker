package me.jason.imagepicker.internal.model;

import android.content.Context;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import me.jason.imagepicker.internal.entity.Item;

public class SelectedItemCollection {
    public static final String STATE_SELECTION = "state_selection";

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
        mItems = new LinkedHashSet<>();
    }

    public void onDestroy() {
        //TODO:
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(STATE_SELECTION, new ArrayList<>(mItems));
    }

    public int count() {
        return mItems == null ? 0 : mItems.size();
    }

    public boolean add(Item item) {
        boolean added = mItems.add(item);
        return added;
    }
}
