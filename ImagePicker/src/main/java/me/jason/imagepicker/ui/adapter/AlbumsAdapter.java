package me.jason.imagepicker.ui.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import me.jason.imagepicker.R;
import me.jason.imagepicker.internal.entity.Album;
import me.jason.imagepicker.internal.entity.SelectionSpec;

public class AlbumsAdapter extends BaseAdapter {
    private Context context;
    private List<Album> albumList;

    public AlbumsAdapter(Context context, List<Album> albumList) {
        this.context = context;
        this.albumList = albumList;
    }

    public void setAlbumList(List<Album> albumList) {
        this.albumList = albumList;
        notifyDataSetChanged();
    }

    public List<Album> getAlbumList() {
        return albumList;
    }

    @Override
    public int getCount() {
        return albumList == null ? 0 : albumList.size();
    }

    @Override
    public Object getItem(int position) {
        return albumList == null ? null : albumList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.album_list_item, parent, false);
            viewHolder.albumCover = convertView.findViewById(R.id.album_cover);
            viewHolder.albumName = convertView.findViewById(R.id.album_name);
            viewHolder.albumMediaCount = convertView.findViewById(R.id.album_media_count);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Album album = albumList.get(position);
        viewHolder.albumName.setText(album.getDisplayName(context));
        viewHolder.albumMediaCount.setText(String.valueOf(album.getCount()));
        SelectionSpec.getInstance().imageEngine.loadThumbnail(context,
                context.getResources().getDimensionPixelSize(R.dimen.media_grid_size), null,
                viewHolder.albumCover,
                Uri.fromFile(new File(album.getCoverPath())));
        return convertView;
    }

    private static class ViewHolder {
        private ImageView albumCover;
        private TextView albumName;
        private TextView albumMediaCount;
    }
}
