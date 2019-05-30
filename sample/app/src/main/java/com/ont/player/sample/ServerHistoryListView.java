package com.ont.player.sample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ont.player.sample.def.IDataListener;
import com.ont.player.sample.def.IListAdapterListener;
import com.ont.player.sample.def.IRequestDef;
import com.ont.player.sample.mode.ServerHistoryInfo;
import com.ont.player.sample.network.NetworkClient;
import com.ont.player.sample.network.ServerHistoryListRequest;
import com.ont.player.sample.network.VodPlayTokenRequest;
import com.ont.player.sample.utils.FormatUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by betali on 2018/4/24.
 */

public class ServerHistoryListView extends OntListView {

    private SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = super.onCreateView(inflater, container, savedInstanceState);
        updateData();
        return view;
    }

    @Override
    public void getListData() {

        ServerHistoryListRequest request = new ServerHistoryListRequest(mDeviceEntryInfo.mApiKey);
        request.setDevice_id(mDeviceEntryInfo.mDeviceId)
                .setChannel_id(mDeviceEntryInfo.mChannelId)
                .setRequest_listener(new IDataListener() {

                    @Override
                    public void onComplete(int apiErr, int dataErr, String response) {

                        if (apiErr != IRequestDef.IRequestResultDef.ERR_OK) {

                            updateData(true, addPlayCycleItem(null));
                            return;
                        }

                        if (TextUtils.isEmpty(response)) {

                            updateData(true, addPlayCycleItem(null));
                            return;
                        } else {

                            updateData(true, addPlayCycleItem(parseJson(response)));
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

                final ServerHistoryInfo info = (ServerHistoryInfo)iListItem;
                if (info.isPlay_cycle()) {

                    startActivity(new Intent(getActivity(), PlayerActivity.class)
                            .setData(Uri.parse(IRequestDef.IRequestUrlDef.API_URL + "/vod/get_video_list?device_id=" + mDeviceEntryInfo.mDeviceId + "&channel_id=" + mDeviceEntryInfo.mChannelId))
                            .putExtra(PlayerActivity.IS_LIVE, false)
                            .putExtra(PlayerActivity.IS_LOCAL, false)
                            .putExtra(PlayerActivity.PLAY_CYCLE, true)
                            .putExtra(PlayerActivity.LIVE_URL, IRequestDef.IRequestUrlDef.API_URL + "/play_address?device_id="+ mDeviceEntryInfo.mDeviceId +"&channel_id=" + mDeviceEntryInfo.mChannelId + "&protocol_type=0")
                            .putExtra(PlayerActivity.TOKEN_URL, IRequestDef.IRequestUrlDef.API_URL + "/vod/get_play_token?device_id=" + mDeviceEntryInfo.mDeviceId + "&channel_id=" + mDeviceEntryInfo.mChannelId)
                            .putExtra(PlayerActivity.API_KEY, mDeviceEntryInfo.mApiKey)
                            .putExtra(PlayerActivity.VIDEO_TITLE, "最近7天视频缓存"));

                } else {

                    final String videoId = info.getVideoid();
                    VodPlayTokenRequest request1 = new VodPlayTokenRequest(mDeviceEntryInfo.mApiKey);
                    request1.setDevice_id(mDeviceEntryInfo.mDeviceId);
                    request1.setChannel_id(mDeviceEntryInfo.mChannelId);
                    request1.setVideo_id(info.getVideoid());
                    request1.setRequest_listener(new IDataListener() {
                        @Override
                        public void onComplete(int apiErr, int dataErr, String response) {

                            if (apiErr == IRequestDef.IRequestResultDef.ERR_OK) {

                                try {

                                    JSONObject videoInfo = new JSONObject(response);
                                    String playToken = videoInfo.optString("token") ;

                                    String playUrl = "";
                                    /*if (!TextUtils.isEmpty(info.getRtmp_url())) {

                                        playUrl = info.getRtmp_url() + "?" + playToken;
                                    } else */if (!TextUtils.isEmpty(info.getHls_url())){

                                        playUrl = info.getHls_url() + "?token=" + playToken;
                                    } else {

                                        Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    startActivity(new Intent(getActivity(), PlayerActivity.class)
                                            .setData(Uri.parse(playUrl))
                                            .putExtra(PlayerActivity.IS_LIVE, false)
                                            .putExtra(PlayerActivity.IS_LOCAL, false)
                                            .putExtra(PlayerActivity.PLAY_CYCLE, false)
                                            .putExtra(PlayerActivity.VIDEO_TITLE, videoId));

                                } catch (JSONException e) {

                                }
                            } else {

                                Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    NetworkClient.doRequest(request1);
                }
            }

            @Override
            public void onGetView(OntListAdapter.OntViewHolder holder, IListItem iListItem) {

                ServerHistoryInfo info = (ServerHistoryInfo)iListItem;
                if (info.isPlay_cycle()) {
                    holder.txtTime.setText("时长: 7天循环录制");
                    holder.txtSize.setVisibility(View.GONE);
                    holder.txtTitle.setText("最近7天视频存储");
                    Glide.with(getActivity()).load("").placeholder(R.drawable.ic_launcher_background).error(R.drawable.ic_launcher_background)
                            .into(holder.img);
                } else {
                    holder.txtTime.setText(info.getStart_time() + "-" + info.getEnd_time());
                    holder.txtSize.setVisibility(View.VISIBLE);
                    holder.txtSize.setText(FormatUtils.formatFileSize(info.getSize()));
                    holder.txtTitle.setText(info.getName());
                    Glide.with(getActivity()).load("").placeholder(R.drawable.ic_launcher_background).error(R.drawable.ic_launcher_background)
                            .into(holder.img);
                }
            }
        };
    }

    private List<IListItem> parseJson(String response) {

        try {

            JSONArray videoList = new JSONArray(response);
            if (videoList == null || videoList.length() <= 0) {

                return null;
            }

            int size = videoList.length();
            List<IListItem> serverDeviceVideoInfoList = new ArrayList<>(0);
            for (int index = 0; index < size; index++) {

                JSONObject videoObj = (JSONObject) videoList.get(index);
                if (videoObj == null) {

                    continue;
                }

                ServerHistoryInfo serverHistoryObj = new ServerHistoryInfo();
                serverHistoryObj.setSize(videoObj.optLong("size"));
                serverHistoryObj.setVideoid(videoObj.optString("videoid"));
                serverHistoryObj.setName(videoObj.optString("name"));
                serverHistoryObj.setStart_time(videoObj.optString("start_time"));
                serverHistoryObj.setEnd_time(videoObj.optString("end_time"));
                serverHistoryObj.setRtmp_url(videoObj.optString("rtmp_url"));
                serverHistoryObj.setHls_url(videoObj.optString("hls_url"));
                serverHistoryObj.setPlay_cycle(false);
                serverDeviceVideoInfoList.add(serverHistoryObj);
            }

            return serverDeviceVideoInfoList;
        } catch (JSONException e) {

        }
        return null;
    }

    private List<IListItem> addPlayCycleItem(List<IListItem> dataList) {

        if (dataList == null) {

            dataList = new ArrayList<>(0);
        }

        ServerHistoryInfo serverHistoryObj = new ServerHistoryInfo();
        serverHistoryObj.setPlay_cycle(true);
        dataList.add(0, serverHistoryObj);
        return dataList;
    }
}
