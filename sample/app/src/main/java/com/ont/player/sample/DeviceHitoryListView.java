package com.ont.player.sample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ont.player.sample.def.IDataListener;
import com.ont.player.sample.def.IListAdapterListener;
import com.ont.player.sample.def.IRequestDef;
import com.ont.player.sample.mode.DeviceHistoryInfo;
import com.ont.player.sample.network.HistoryVideoCmdRequest;
import com.ont.player.sample.network.NetworkClient;
import com.ont.player.sample.network.PlayUrlRequest;
import com.ont.player.sample.utils.FormatUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by betali on 2018/8/1.
 */

public class DeviceHitoryListView extends OntListView {

    private int all_count;
    private int cur_count;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = super.onCreateView(inflater, container, savedInstanceState);
        updateData();
        return view;
    }

    @Override
    protected void getListData() {

        HistoryVideoCmdRequest request = new HistoryVideoCmdRequest(mDeviceEntryInfo.mApiKey)
                .setDevice_id(mDeviceEntryInfo.mDeviceId)
                .setChannel_id(mDeviceEntryInfo.mChannelId)
                .setPage(1)
                .setPer_page(10)
                .setRequest_listener(new IDataListener() {
                    @Override
                    public void onComplete(int apiErr, int dataErr, String response) {

                        if (apiErr != IRequestDef.IRequestResultDef.ERR_OK) {

                            updateData(false, null);
                            return;
                        }

                        if (TextUtils.isEmpty(response)) {

                            updateData(false, null);
                            return;
                        } else {

                            updateData(true, parseJson(response));
                        }
                    }
                });
        NetworkClient.doRequest(request);
    }

    @Override
    protected void initAdapterListener() {

        mListAdapterListener = new IListAdapterListener() {

            @Override
            public void onVideoItemClick(IListItem iListItem) {

                final String begin_time = ((DeviceHistoryInfo)iListItem).getBeginTime();
                final String end_time = ((DeviceHistoryInfo)iListItem).getEndTime();
                final String video_title = ((DeviceHistoryInfo)iListItem).getVideo_title();
                PlayUrlRequest request = new PlayUrlRequest(mDeviceEntryInfo.mApiKey)
                        .setIs_live(false)
                        .setChannel_id(mDeviceEntryInfo.mChannelId)
                        .setDevice_id(mDeviceEntryInfo.mDeviceId)
                        .setProtocol_type("0")
                        .setBegin_time(begin_time)
                        .setEnd_time(end_time)
                        .setRequest_listener(new IDataListener() {
                            @Override
                            public void onComplete(int apiErr, int dataErr, String response) {

                                if (apiErr == IRequestDef.IRequestResultDef.ERR_OK) {

                                    startActivity(new Intent(getActivity(), PlayerActivity.class)
                                            .setData(Uri.parse(response))
                                            .putExtra(PlayerActivity.IS_LIVE, false)
                                            .putExtra(PlayerActivity.IS_LOCAL, false)
                                            .putExtra(PlayerActivity.VIDEO_TITLE, video_title));
                                } else {

                                    Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                NetworkClient.doRequest(request);
            }

            @Override
            public void onGetView(OntListAdapter.OntViewHolder holder, IListItem iListItem) {

                DeviceHistoryInfo info = (DeviceHistoryInfo)iListItem;
                holder.txtTime.setText(info.getBeginTime() + "-" + info.getEndTime());
                holder.txtSize.setText(FormatUtils.formatFileSize(info.getSize()));
                holder.txtTitle.setText(info.getVideo_title());
                Glide.with(getActivity()).load(info.getThumbnailPath()).placeholder(R.drawable.ic_launcher_background).error(R.drawable.ic_launcher_background)
                        .into(holder.img);
            }
        };
    }

    private List<IListItem> parseJson(String response) {

        try {
            JSONObject respObj = new JSONObject(response);
            if (respObj == null) {
                return null;
            }

            String resp_data = respObj.optString("resp_data");
            if (TextUtils.isEmpty(resp_data)) {
                return null;
            }

            // base64
            resp_data = new String(Base64.decode(resp_data.getBytes(), Base64.DEFAULT));
            JSONObject videoListObj = new JSONObject(resp_data);
            if (videoListObj == null) {

                return null;
            }
            all_count = videoListObj.optInt("all_count");
            cur_count = videoListObj.optInt("cur_count");
            JSONArray videoList = videoListObj.optJSONArray("rvods");

            if (videoList == null || videoList.length() <= 0) {

                return null;
            }

            List<IListItem> deviceVideoInfoList = new ArrayList<>(0);
            for (int index = 0; index < videoList.length(); index++) {

                JSONObject videoObj = (JSONObject) videoList.get(index);
                if (videoObj == null) {

                    continue;
                }

                DeviceHistoryInfo deviceHistoryInfo = new DeviceHistoryInfo();
                deviceHistoryInfo.setChannel_id(videoObj.optInt("channel_id"));
                deviceHistoryInfo.setVideo_title(videoObj.optString("video_title"));
                deviceHistoryInfo.setBeginTime(videoObj.optString("beginTime"));
                deviceHistoryInfo.setEndTime(videoObj.optString("endTime"));
                deviceHistoryInfo.setSize(videoObj.optLong("size", 0));
                deviceVideoInfoList.add(deviceHistoryInfo);
            }
            return deviceVideoInfoList;
        } catch (JSONException e) {

        }
        return null;
    }
}
