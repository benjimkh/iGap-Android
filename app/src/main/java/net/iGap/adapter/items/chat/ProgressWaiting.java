/*
* This is the source code of iGap for Android
* It is licensed under GNU AGPL v3.0
* You should have received a copy of the license in this archive (see LICENSE).
* Copyright © 2017 , iGap - www.iGap.net
* iGap Messenger | Free, Fast and Secure instant messaging application
* The idea of the RooyeKhat Media Company - www.RooyeKhat.co
* All rights reserved.
*/

package net.iGap.adapter.items.chat;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import net.iGap.R;
import net.iGap.adapter.MessagesAdapter;
import net.iGap.interfaces.IMessageItem;
import net.iGap.module.AppUtils;
import net.iGap.proto.ProtoGlobal;

import java.util.List;

import io.realm.Realm;

public class ProgressWaiting extends AbstractMessage<net.iGap.adapter.items.chat.ProgressWaiting, net.iGap.adapter.items.chat.ProgressWaiting.ViewHolder> {

    public ProgressWaiting(MessagesAdapter<AbstractMessage> mAdapter, IMessageItem messageClickListener) {
        super(mAdapter, false, ProtoGlobal.Room.Type.CHAT, messageClickListener);
    }

    @Override
    public int getType() {
        return R.id.cslp_progress_bar_waiting;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.chat_sub_layout_message;
    }

    @Override
    public void bindView(net.iGap.adapter.items.chat.ProgressWaiting.ViewHolder holder, List payloads) {

        AppUtils.setProgresColler(holder.progressBar);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });


        super.bindView(holder, payloads);
    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        ProgressBar progressBar;

        public ViewHolder(View view) {
            super(view);
            progressBar = (ProgressBar) ViewMaker.getProgressWaitingItemView();
            ((ViewGroup) itemView).addView(progressBar);
        }
    }
}
