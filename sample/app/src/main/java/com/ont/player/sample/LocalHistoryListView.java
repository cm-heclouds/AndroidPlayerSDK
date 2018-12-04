package com.ont.player.sample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ont.player.sample.def.IListAdapterListener;
import com.ont.player.sample.mode.LocalHistoryInfo;
import com.ont.player.sample.utils.FormatUtils;
import com.ont.player.sample.utils.PermissionUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 用于加载本地视频文件
 */

public class LocalHistoryListView extends OntListView {

    private SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = super.onCreateView(inflater, container, savedInstanceState);
        updateData();
        return view;
    }

    @Override
    public void getListData() {

        if (mContext == null) {

            updateData(false, null);
            return;
        }

        if (!PermissionUtils.checkAndRequestPermission((Activity)mContext, PermissionUtils.PERMISSIONS_STORAGE, PermissionUtils.PERMISSIONS_STORAGE_CODE)) {

            updateData(false, null);
            return;
        }

        new AsyncTask<Void, Void, List<IListItem>>() {

            @Override
            protected List<IListItem> doInBackground(Void... voids) {

                List<IListItem> sysVideoList = new ArrayList<>();
                String[] thumbColumns = {
                        MediaStore.Video.Thumbnails.DATA,
                        MediaStore.Video.Thumbnails.VIDEO_ID};
                String[] mediaColumns = {MediaStore.Video.Media._ID,
                        MediaStore.Video.Media.DATA,
                        MediaStore.Video.Media.DURATION,
                        MediaStore.Video.Media.DISPLAY_NAME,
                        MediaStore.Video.Media.SIZE};

                Cursor cursor = mContext.getContentResolver().query(MediaStore.Video.Media .EXTERNAL_CONTENT_URI,
                        mediaColumns, null, null, MediaStore.Images.Media.DATE_MODIFIED +" desc");

                if (cursor == null) {

                    return sysVideoList;
                }

                if (cursor.moveToFirst()) {

                    do {

                        LocalHistoryInfo info = new LocalHistoryInfo();
                        int id = cursor.getInt(cursor .getColumnIndex(MediaStore.Video.Media._ID));

                        MediaStore.Video.Thumbnails.getThumbnail(mContext.getContentResolver(), id, MediaStore.Video.Thumbnails.MICRO_KIND, null);
                        Cursor thumbCursor = mContext.getContentResolver().query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, thumbColumns,
                                MediaStore.Video.Thumbnails.VIDEO_ID + "=" + id, null, null);
                        if (thumbCursor.moveToFirst()) {
                            info.setThumbnailPath(thumbCursor.getString(thumbCursor .getColumnIndex(MediaStore.Video.Thumbnails.DATA)));
                        }
                        info.setPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)));
                        info.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)));

                        info.setVideo_title(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)));
                        info.setSize(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)));
                        sysVideoList.add(info);
                    } while (cursor.moveToNext());
                }
                return sysVideoList;
            }

            @Override
            protected void onPostExecute(List<IListItem> entityVideos) {

                updateData(true, entityVideos);
            }
        }.execute();
    }

    @Override
    protected void initAdapterListener() {

        mListAdapterListener = new IListAdapterListener() {

            @Override
            public void onVideoItemClick(IListItem iListItem) {

                LocalHistoryInfo info = (LocalHistoryInfo)iListItem;
                String path = info.getPath();
                if (!new File(path).exists()) {
                    Toast.makeText(getActivity(), "文件不存在", Toast.LENGTH_SHORT).show();
                    return;
                }

                path = "file://" + path;
                startActivity(new Intent(getActivity(), PlayerActivity.class)
                        .setData(Uri.parse(path))
                        .putExtra(PlayerActivity.IS_LIVE, false)
                        .putExtra(PlayerActivity.IS_LOCAL, true)
                        .putExtra(PlayerActivity.VIDEO_TITLE, info.getVideo_title()));
            }

            @Override
            public void onGetView(OntListAdapter.OntViewHolder holder, IListItem iListItem) {

                LocalHistoryInfo info = (LocalHistoryInfo)iListItem;
                holder.txtTime.setText("时长:" + formatter.format(new Date(info.getDuration())));
                holder.txtSize.setVisibility(View.VISIBLE);
                holder.txtSize.setText(FormatUtils.formatFileSize(info.getSize()));
                holder.txtTitle.setText(info.getVideo_title());
                Glide.with(getActivity()).load(info.getThumbnailPath()).placeholder(R.drawable.ic_launcher_background).error(R.drawable.ic_launcher_background)
                        .into(holder.img);
            }
        };
    }
}
