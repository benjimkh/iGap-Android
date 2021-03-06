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

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import net.iGap.G;
import net.iGap.R;
import net.iGap.adapter.MessagesAdapter;
import net.iGap.fragments.FragmentChat;
import net.iGap.fragments.FragmentMap;
import net.iGap.helper.HelperFragment;
import net.iGap.helper.HelperPermission;
import net.iGap.interfaces.IMessageItem;
import net.iGap.interfaces.OnGetPermission;
import net.iGap.module.AndroidUtils;
import net.iGap.module.AppUtils;
import net.iGap.module.ReserveSpaceRoundedImageView;
import net.iGap.proto.ProtoGlobal;
import net.iGap.realm.RealmRoom;
import net.iGap.realm.RealmRoomMessageLocation;

import java.io.File;
import java.io.IOException;
import java.util.List;

import io.realm.Realm;

public class LocationItem extends AbstractMessage<LocationItem, LocationItem.ViewHolder> {

    public LocationItem(MessagesAdapter<AbstractMessage> mAdapter, ProtoGlobal.Room.Type type, IMessageItem messageClickListener) {
        super(mAdapter, true, type, messageClickListener);
    }

    @Override
    public int getType() {
        return R.id.chatSubLayoutLocation;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.chat_sub_layout_message;
    }

    @Override
    public void bindView(final ViewHolder holder, List payloads) {
        super.bindView(holder, payloads);

        holder.imgMapPosition.reserveSpace(G.context.getResources().getDimension(R.dimen.dp240), G.context.getResources().getDimension(R.dimen.dp120), getRoomType());

        RealmRoomMessageLocation item = null;

        if (mMessage.forwardedFrom != null) {
            if (mMessage.forwardedFrom.getLocation() != null) {
                item = mMessage.forwardedFrom.getLocation();
            }
        } else {
            if (mMessage.location != null) {
                item = mMessage.location;
            }
        }

        if (item != null) {
            String path = AppUtils.getLocationPath(item.getLocationLat(), item.getLocationLong());

            if (new File(path).exists()) {
                G.imageLoader.displayImage(AndroidUtils.suitablePath(path), holder.imgMapPosition);
            } else {
                RealmRoomMessageLocation finalItem1 = item;
                FragmentMap.loadImageFromPosition(item.getLocationLat(), item.getLocationLong(), new FragmentMap.OnGetPicture() {
                    @Override
                    public void getBitmap(Bitmap bitmap) {
                        holder.imgMapPosition.setImageBitmap(bitmap);
                        AppUtils.saveMapToFile(bitmap, finalItem1.getLocationLat(), finalItem1.getLocationLong());
                    }
                });
            }

            final RealmRoomMessageLocation finalItem = item;
            holder.imgMapPosition.setOnLongClickListener(getLongClickPerform(holder));

            holder.imgMapPosition.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (FragmentChat.isInSelectionMode) {
                        holder.itemView.performLongClick();
                        return;
                    }

                    try {
                        HelperPermission.getLocationPermission(G.currentActivity, new OnGetPermission() {

                            @Override
                            public void Allow() {
                                G.handler.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        FragmentMap fragment = FragmentMap.getInctance(finalItem.getLocationLat(), finalItem.getLocationLong(), FragmentMap.Mode.seePosition,
                                                RealmRoom.detectType(mMessage.roomId).getNumber(), mMessage.roomId, mMessage.senderID);
                                        new HelperFragment(fragment).setReplace(false).load();

                                    }
                                });
                            }

                            @Override
                            public void deny() {

                            }
                        });
                    } catch (IOException | IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    protected void updateLayoutForReceive(ViewHolder holder) {
        super.updateLayoutForReceive(holder);
    }

    @Override
    protected void updateLayoutForSend(ViewHolder holder) {
        super.updateLayoutForSend(holder);
    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    protected static class ViewHolder extends ChatItemHolder implements IThumbNailItem {

        ReserveSpaceRoundedImageView imgMapPosition;

        public ViewHolder(View view) {
            super(view);
            FrameLayout frameLayout = new FrameLayout(G.context);
            frameLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));

            imgMapPosition = new ReserveSpaceRoundedImageView(G.context);
            imgMapPosition.setId(R.id.thumbnail);
            imgMapPosition.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imgMapPosition.setCornerRadius((int) G.context.getResources().getDimension(R.dimen.messageBox_cornerRadius));
            LinearLayout.LayoutParams layout_758 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            imgMapPosition.setLayoutParams(layout_758);

            frameLayout.addView(imgMapPosition);
            m_container.addView(frameLayout);
        }

        @Override
        public ImageView getThumbNailImageView() {
            return imgMapPosition;
        }
    }
}
