package com.ont.player.sample.def;

import com.ont.player.sample.IListItem;
import com.ont.player.sample.OntListAdapter;

/**
 * Created by betali on 2018/4/25.
 */

public interface IListAdapterListener {

    void onVideoItemClick(IListItem iListItem);
    void onGetView(OntListAdapter.OntViewHolder holder, IListItem iListItem);
}
