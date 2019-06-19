package me.jason.imagepicker.ui;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.shuyu.gsyvideoplayer.GSYVideoManager;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;
import me.jason.imagepicker.R;
import me.jason.imagepicker.internal.entity.Item;
import me.jason.imagepicker.internal.entity.SelectionSpec;
import me.jason.imagepicker.utils.PathUtils;
import me.jason.imagepicker.utils.PhotoMetadataUtils;
import me.jason.imagepicker.video.PreviewGSYVideoPlayer;

public class PreviewItemFragment extends Fragment {
    private static final String ARGS_ITEM = "args_item";
    private static final String ARGS_POSITION = "args_position";

    private Item item;
    private int position;

    private ImageViewTouch previewImage;
    private PreviewGSYVideoPlayer videoPlayer;

    public static PreviewItemFragment newInstance(Item item, int position) {
        PreviewItemFragment fragment = new PreviewItemFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARGS_ITEM, item);
        bundle.putInt(ARGS_POSITION, position);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) return;
        item = getArguments().getParcelable(ARGS_ITEM);
        position = getArguments().getInt(ARGS_POSITION);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (videoPlayer != null) {
            videoPlayer.releaseListener();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_preview_item, container, false);
        if (rootView.getChildCount() > 0) rootView.removeAllViews();
        if (item.isImage()) {
            previewImage = new ImageViewTouch(getContext());
            previewImage.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
            previewImage.setBackgroundColor(Color.BLACK);
            previewImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
            previewImage.setSingleTapListener(() -> clickImage());
            rootView.addView(previewImage);
        } else {
            videoPlayer = new PreviewGSYVideoPlayer(getContext(), videoPlayer -> {
                if (getActivity() instanceof PreviewItemActivity) {
                    TextView currentTextView = ((PreviewItemActivity) getActivity()).getCurrentTextView();
                    currentTextView.setText(R.string.preview_item_video_current_time_def);
                    TextView totalTextView = ((PreviewItemActivity) getActivity()).getTotalTextView();
                    totalTextView.setText(R.string.preview_item_video_total_time_def);
                    SeekBar progressBar = ((PreviewItemActivity) getActivity()).getProgressBar();
                    progressBar.setOnSeekBarChangeListener(videoPlayer);
                    progressBar.setOnTouchListener(videoPlayer);
                    progressBar.setProgress(0);
                    ViewGroup bottomContainer = ((PreviewItemActivity) getActivity()).getBottomContainer();
                    ViewGroup topContainer = ((PreviewItemActivity) getActivity()).getTopContainer();
                    videoPlayer.setCurrentTimeTextView(currentTextView);
                    videoPlayer.setTotalTimeTextView(totalTextView);
                    videoPlayer.setProgressBar(progressBar);
                    videoPlayer.setBottomContainer(bottomContainer);
                    videoPlayer.setTopContainer(topContainer);
                }
            });
            videoPlayer.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
            rootView.addView(videoPlayer);
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Point size = PhotoMetadataUtils.getBitmapSize(item.getContentUri(), getActivity());
        if (item.isImage()) {
            //图片
            if (item.isGif()) {
                SelectionSpec.getInstance().imageEngine.loadGifImage(getContext(), size.x, size.y, previewImage, item.getContentUri());
            } else {
                SelectionSpec.getInstance().imageEngine.loadImage(getContext(), size.x, size.y, previewImage, item.getContentUri());
            }
        } else {
            //视频
            //增加封面
            ImageView imageView = new ImageView(getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
            SelectionSpec.getInstance().imageEngine.loadImage(getContext(), size.x, size.y, imageView, item.getContentUri());
            videoPlayer.setThumbImageView(imageView);
            //是否可以滑动调整
            videoPlayer.setIsTouchWiget(false);
            String path = PathUtils.getPath(getContext(), item.getContentUri());
            String url = "file://" + path;
            Log.d("jason", "path = " + path);
            Log.d("jason", "url = " + url);
            videoPlayer.setUp(url, false, null);
            videoPlayer.startPlayLogic();
        }
    }

    private void clickImage() {
        if (getActivity() instanceof PreviewItemActivity) {
            ((PreviewItemActivity) getActivity()).showLayout();
        }
    }

    public void resetView() {
        Log.d("jason", "resetView: position = " + position);
        if (previewImage != null) previewImage.resetMatrix();
        if (videoPlayer != null) GSYVideoManager.releaseAllVideos();
    }

    public Item getItem() {
        return item;
    }
}
