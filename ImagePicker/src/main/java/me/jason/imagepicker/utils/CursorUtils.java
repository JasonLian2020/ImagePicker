package me.jason.imagepicker.utils;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import me.jason.imagepicker.internal.entity.Album;
import me.jason.imagepicker.internal.entity.Item;

public class CursorUtils {

    public static List<Album> getAllAlbum(Cursor cursor) {
        List<Album> albumList = null;
        if (cursor != null) {
            albumList = new ArrayList<>();
            while (cursor.moveToNext()) {
                Album album = Album.valueOf(cursor);
                albumList.add(album);
            }
        }
        return albumList;
    }

    public static List<Item> getAllItem(Cursor cursor) {
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
}
