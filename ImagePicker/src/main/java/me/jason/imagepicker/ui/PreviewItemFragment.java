package me.jason.imagepicker.ui;

import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.blankj.utilcode.util.ToastUtils;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;
import me.jason.imagepicker.R;
import me.jason.imagepicker.internal.entity.Item;
import me.jason.imagepicker.internal.entity.SelectionSpec;
import me.jason.imagepicker.utils.PhotoMetadataUtils;

public class PreviewItemFragment extends Fragment {
    private static final String ARGS_ITEM = "args_item";

    private Item item;

    private ImageViewTouch previewImage;
    private ImageView previewVideoCover;
    private ImageView previewVideoBtn;

    public static PreviewItemFragment newInstance(Item item) {
        PreviewItemFragment fragment = new PreviewItemFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARGS_ITEM, item);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) return;
        item = getArguments().getParcelable(ARGS_ITEM);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_preview_item, container, false);
        previewImage = rootView.findViewById(R.id.previewImage);
        previewImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
        previewImage.setSingleTapListener(() -> clickImage());
        previewVideoCover = rootView.findViewById(R.id.previewVideoCover);
        previewVideoBtn = rootView.findViewById(R.id.previewVideoBtn);
        previewVideoBtn.setOnClickListener(v -> {
            ToastUtils.showShort("点击了播放");
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Point size = PhotoMetadataUtils.getBitmapSize(item.getContentUri(), getActivity());
        if (item.isImage()) {
            //图片
            previewImage.setVisibility(View.VISIBLE);
            if (item.isGif()) {
                SelectionSpec.getInstance().imageEngine.loadGifImage(getContext(), size.x, size.y, previewImage, item.getContentUri());
            } else {
                SelectionSpec.getInstance().imageEngine.loadImage(getContext(), size.x, size.y, previewImage, item.getContentUri());
            }
            previewVideoCover.setVisibility(View.GONE);
            previewVideoBtn.setVisibility(View.GONE);
        } else {
            //视频
            previewImage.setVisibility(View.GONE);
            previewVideoCover.setVisibility(View.VISIBLE);
            SelectionSpec.getInstance().imageEngine.loadImage(getContext(), size.x, size.y, previewVideoCover, item.getContentUri());
            previewVideoBtn.setVisibility(View.VISIBLE);
        }
    }

    private void clickImage() {
        if (getActivity() instanceof PreviewItemActivity) {
            ((PreviewItemActivity) getActivity()).autoHideToolbar();
        }
    }

    public void resetView() {
        if (previewImage != null) previewImage.resetMatrix();
    }

    public Item getItem() {
        return item;
    }
}
