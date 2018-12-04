package com.ont.player.sample;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.ont.player.sample.def.IListAdapterListener;
import com.ont.player.sample.mode.DeviceEntryInfo;

import java.util.ArrayList;
import java.util.List;


public abstract class OntListView extends Fragment {

    protected Context mContext;
    protected DeviceEntryInfo mDeviceEntryInfo;
    protected IListAdapterListener mListAdapterListener;
    private OntListAdapter mVideoAdapter;
    private ListView mListView;
    private FrameLayout mLoadingView;
    private FrameLayout mLoadingRotate;
    private Animation mRotateAnimation;

    protected abstract void getListData();
    protected abstract void initAdapterListener();

    public void setContext(Context context) {

        this.mContext = context;
    }

    public void setDeviceEntryInfo(DeviceEntryInfo deviceEntryInfo) {

        this.mDeviceEntryInfo = deviceEntryInfo;
    }

    public void updateData() {

        mLoadingRotate.startAnimation(mRotateAnimation);
        mLoadingView.setVisibility(View.VISIBLE);
        getListData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.page_list, container, false);

        mListView = view.findViewById(R.id.list_view);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (mListAdapterListener != null) {

                    mListAdapterListener.onVideoItemClick(mVideoAdapter.getItem(i));
                }
            }
        });

        initAdapterListener();
        mVideoAdapter = new OntListAdapter(getActivity());
        mVideoAdapter.setListAdapterListener(mListAdapterListener);
        mListView.setAdapter(mVideoAdapter);

        mLoadingRotate = view.findViewById(R.id.loading_icon);
        mLoadingView = view.findViewById(R.id.loading_view);
        mRotateAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.anim_circle_rotate);
        LinearInterpolator interpolator = new LinearInterpolator();
        mRotateAnimation.setInterpolator(interpolator);

        return view;
    }

    protected void updateData(boolean success, List<IListItem> dataList){

        mLoadingRotate.clearAnimation();
        mLoadingView.setVisibility(View.GONE);
        if (success) {
            if (dataList == null) {
                dataList = new ArrayList<>(0);
            }
            mVideoAdapter.updateData(dataList);
        }
    }
}
