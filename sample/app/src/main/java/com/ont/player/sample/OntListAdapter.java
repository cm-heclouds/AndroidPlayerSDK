package com.ont.player.sample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ont.player.sample.def.IListAdapterListener;

import java.util.List;

/**
 * Created by betali on 2018/7/31.
 */

public class OntListAdapter extends BaseAdapter {

    private Context mContext;
    private IListAdapterListener mListAdapterListener;
    private List<IListItem> mData;

    public OntListAdapter(Context mContext) {

        this.mContext = mContext;
    }

    public void setListAdapterListener(IListAdapterListener listAdapterListener) {

        this.mListAdapterListener = listAdapterListener;
    }

    public void updateData(List<IListItem> data) {

        this.mData = data;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return null == mData ? 0 : mData.size();
    }

    @Override
    public IListItem getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        OntViewHolder holder = null;
        if (null == view) {

            holder = new OntViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.item_common, viewGroup, false);
            holder.txtTitle = view.findViewById(R.id.txt_title);
            holder.txtTime = view.findViewById(R.id.txt_desc);
            holder.txtSize = view.findViewById(R.id.txt_tail);
            holder.img = view.findViewById(R.id.pic);
            view.setTag(holder);
        } else {

            holder = (OntViewHolder) view.getTag();
        }

        IListItem item = getItem(i);
        /*if(mPicture){
            holder.txtTitle.setText(item.getFileName());
        }
        else {
            holder.txtTitle.setText(item.getVideo_title());
        }*/
        if (mListAdapterListener != null) {

            mListAdapterListener.onGetView(holder, item);
        }

        /*if(mPicture){
            Glide.with( getActivity() ).load( item.getFileUrl() ).thumbnail(0.1f).error(R.drawable.ic_launcher_background).into( holder.img ) ;
        }
        else {
            Glide.with(getActivity()).load(item.getThumbnailPath()).placeholder(R.drawable.ic_launcher_background).error(R.drawable.ic_launcher_background)
                    .into(holder.img);
        }*/

        return view;
    }

    public static class OntViewHolder {

        TextView txtTitle;
        TextView txtTime;
        TextView txtSize;
        ImageView img;
    }
}
