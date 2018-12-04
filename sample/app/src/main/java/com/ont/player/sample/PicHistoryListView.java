package com.ont.player.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ont.player.sample.def.IDataListener;
import com.ont.player.sample.def.IListAdapterListener;
import com.ont.player.sample.def.IRequestDef;
import com.ont.player.sample.mode.PicHistoryInfo;
import com.ont.player.sample.network.NetworkClient;
import com.ont.player.sample.network.PicRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by armou on 2018/7/18.
 */

public class PicHistoryListView extends OntListView {

    private SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
    private PicHistoryActivity mHostActivity;

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

    public PicHistoryListView setHostActivity(PicHistoryActivity hostActivity) {

        this.mHostActivity = hostActivity;
        return this;
    }

    @Override
    public void getListData() {

        PicRequest request = new PicRequest(mHostActivity.getApiKey());
        request.setChannel_id(mHostActivity.getChannelId());
        request.setDevice_id(mHostActivity.getDeviceId());
        request.setRequest_listener(new IDataListener() {
            @Override
            public void onComplete(int apiErr, int dataErr, String response) {

                if (apiErr != IRequestDef.IRequestResultDef.ERR_OK) {

                    Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
                    updateData(false, null);
                    return;
                }

                updateData(true, parseJson(response));
            }
        });
        NetworkClient.doRequest(request);
    }

    @Override
    public void initAdapterListener() {

        mListAdapterListener = new IListAdapterListener() {

            public void onVideoItemClick(IListItem iListItem) {

                String picurl = ((PicHistoryInfo)iListItem).getFileUrl();
                String picname = ((PicHistoryInfo)iListItem).getFileName();
                Intent intent = new Intent(getActivity(), PicShowActivity.class);
                intent.putExtra("picurl", picurl);
                intent.putExtra("picname", picname);
                startActivity(intent);
            }

            @Override
            public void onGetView(OntListAdapter.OntViewHolder holder, IListItem iListItem) {

                holder.txtTitle.setText(((PicHistoryInfo)iListItem).getFileName());
                Glide.with( getActivity() ).load( ((PicHistoryInfo)iListItem).getFileUrl() ).thumbnail(0.1f).error(R.drawable.ic_launcher_background).into( holder.img ) ;
            }
        };
    }

    private List<IListItem> parseJson(String response) {

        try {

            JSONArray picList = new JSONArray(response);
            if (picList == null || picList.length() <= 0) {

                return null;
            }

            int size = picList.length();
            List<IListItem> serverHistoryPicList = new ArrayList<>(0);
            for (int index = 0; index < size; index++) {

                JSONObject videoObj = (JSONObject) picList.get(index);
                if (videoObj == null) {
                    continue;
                }
                PicHistoryInfo serverHistoryObj = new PicHistoryInfo();
                serverHistoryObj.setFileType(videoObj.optString("type"));
                serverHistoryObj.setFileSize(videoObj.optString("size"));
                serverHistoryObj.setFileDate(videoObj.optString("date"));
                serverHistoryObj.setFileFormat(videoObj.optString("format"));
                serverHistoryObj.setFileName(videoObj.optString("name"));
                serverHistoryObj.setFileUrl(videoObj.optString("url"));
                serverHistoryObj.setFileUrl("https://publish-pic-cpu.baidu.com/10f0e3cc-5340-4037-bc70-f4541c7cdb22.jpeg@q_90,w_450");
                serverHistoryPicList.add(serverHistoryObj);
            }
            return serverHistoryPicList;

        } catch (JSONException e) {
        }

        return null;

        /* test data */
        /*List<DeviceHistoryInfo> serverHistoryPicList = new ArrayList<>(0);
        DeviceHistoryInfo serverHistoryObj = new DeviceHistoryInfo();
        serverHistoryObj.setFileUrl("https://gss3.bdstatic.com/-Po3dSag_xI4khGkpoWK1HF6hhy/baike/w%3D268%3Bg%3D0/sign=44f5f5397ac6a7efb926af20c5c1c86c/8ad4b31c8701a18b243242a9922f07082938fe7f.jpg");
        serverHistoryPicList.add(serverHistoryObj);
        return serverHistoryPicList;*/
    }
}
