package me.jason.imagepicker.ui.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import me.jason.imagepicker.R;
import me.jason.imagepicker.internal.entity.CaptureItem;

public class ChooseCaptureAdapter extends BaseQuickAdapter<CaptureItem, BaseViewHolder> {
    public ChooseCaptureAdapter(@Nullable List<CaptureItem> data) {
        super(R.layout.item_choose_capture, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, CaptureItem item) {
        helper.setText(R.id.title, item.getTitle());
    }
}
