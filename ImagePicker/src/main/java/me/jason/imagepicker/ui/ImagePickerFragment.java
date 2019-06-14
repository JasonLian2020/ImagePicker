package me.jason.imagepicker.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import me.jason.imagepicker.R;
import me.jason.imagepicker.internal.entity.Album;
import me.jason.imagepicker.internal.entity.Item;
import me.jason.imagepicker.internal.entity.SelectionSpec;
import me.jason.imagepicker.internal.model.AlbumMediaCollection;
import me.jason.imagepicker.ui.adapter.AlbumMeidaAdapter;
import me.jason.imagepicker.ui.widget.MediaGridInset;
import me.jason.imagepicker.utils.ThreadUtils;

public class ImagePickerFragment extends Fragment {
    public static final String EXTRA_ALBUM = "extra_album";

    private Album album;

    private RecyclerView recyclerview;
    private AlbumMeidaAdapter mAdapter;

    private final AlbumMediaCollection mAlbumMediaCollection = new AlbumMediaCollection();

    public static ImagePickerFragment newInstance(Album album) {
        ImagePickerFragment fragment = new ImagePickerFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_ALBUM, album);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("jason", "onCreate");
        if (getArguments() == null) return;
        album = getArguments().getParcelable(EXTRA_ALBUM);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("jason", "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_image_picker, container, false);
        initRecyclerView(rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("jason", "onActivityCreated");

        mAlbumMediaCollection.onCreate(getActivity(), new AlbumMediaCollection.AlbumMediaCallbacks() {
            @Override
            public void onAlbumMediaLoad(Cursor cursor) {
                Log.d("jason", "onAlbumMediaLoad");
                List<Item> itemList = getAllItem(cursor);
                if (ThreadUtils.isMainThread()) {
                    updateUIByInit(itemList);
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> updateUIByInit(itemList));
                }
            }

            @Override
            public void onAlbumMediaReset() {
                Log.d("jason", "onAlbumMediaReset");
                if (ThreadUtils.isMainThread()) {
                    updateUIByReset();
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> updateUIByReset());
                }
            }
        });
        mAlbumMediaCollection.load(album, SelectionSpec.getInstance().capture);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("jason", "onDestroy");
        mAlbumMediaCollection.onDestroy();
    }

    private void initRecyclerView(View rootView) {
        int spanCount = 3;
        int spacing = getResources().getDimensionPixelSize(R.dimen.media_grid_spacing);
        recyclerview = rootView.findViewById(R.id.recyclerView);
        recyclerview.setHasFixedSize(true);
        recyclerview.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        recyclerview.addItemDecoration(new MediaGridInset(spanCount, spacing, false));
        mAdapter = new AlbumMeidaAdapter(null);
        mAdapter.bindToRecyclerView(recyclerview);
    }

    private List<Item> getAllItem(Cursor cursor) {
        List<Item> itemList = null;
        if (cursor != null) {
            itemList = new ArrayList<>();
            while (cursor.moveToNext()) {
                Item album = Item.valueOf(cursor);
                itemList.add(album);
            }
        }
        return itemList;
    }

    private void updateUIByInit(List<Item> itemList) {
        Log.d("jason", "updateUIByInit");
        mAdapter.setNewData(itemList);
        // 用完就释放掉
        mAlbumMediaCollection.onDestroy();
    }

    private void updateUIByReset() {
        Log.d("jason", "updateUIByReset");
        mAdapter.setNewData(null);
    }
}
