/*
 * This is the source code of iGap for Android
 * It is licensed under GNU AGPL v3.0
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright © 2017 , iGap - www.iGap.net
 * iGap Messenger | Free, Fast and Secure instant messaging application
 * The idea of the RooyeKhat Media Company - www.RooyeKhat.co
 * All rights reserved.
 */

package net.iGap.module;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import net.iGap.G;
import net.iGap.R;
import net.iGap.Theme;
import net.iGap.fragments.FragmentMap;
import net.iGap.helper.HelperError;
import net.iGap.helper.HelperLogMessage;
import net.iGap.helper.HelperMimeType;
import net.iGap.interfaces.IResendMessage;
import net.iGap.messageprogress.CircleProgress.CircularProgressView;
import net.iGap.module.structs.StructMessageInfo;
import net.iGap.proto.ProtoGlobal;
import net.iGap.proto.ProtoUserUpdateStatus;
import net.iGap.realm.RealmAttachment;
import net.iGap.realm.RealmRoom;
import net.iGap.realm.RealmRoomFields;
import net.iGap.realm.RealmRoomMessage;
import net.iGap.realm.RealmRoomMessageFields;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import me.leolin.shortcutbadger.ShortcutBadger;

import static net.iGap.G.context;
import static net.iGap.module.AndroidUtils.suitablePath;

public final class AppUtils {
    private AppUtils() throws InstantiationException {
        throw new InstantiationException("This class is not for instantiation.");
    }


    public static String suitableThumbFileName(String name) {
        if (HelperMimeType.isFileImage(name.toLowerCase())) {
            return name;
        } else {
            return name.replaceFirst("([\\w\\W]+)(\\.(\\w+))$", "$1.jpg");
        }
    }

    /**
     * change enum to string for simple showing in toolbar when get status
     *
     * @param status UserUpdateStatus
     */

    public static String getStatsForUser(String status) {

        String userStatus = "";
        if ((status == null) || (status.equals(ProtoUserUpdateStatus.UserUpdateStatus.Status.OFFLINE.toString()))) {
            userStatus = context.getResources().getString(R.string.last_seen_recently);
        } else if (status.equals(ProtoGlobal.RegisteredUser.Status.LONG_TIME_AGO.toString())) {
            userStatus = context.getResources().getString(R.string.long_time_ago);
        } else if (status.equals(ProtoGlobal.RegisteredUser.Status.LAST_MONTH.toString())) {
            userStatus = context.getResources().getString(R.string.last_month);
        } else if (status.equals(ProtoGlobal.RegisteredUser.Status.LAST_WEEK.toString())) {
            userStatus = context.getResources().getString(R.string.last_week);
        } else if (status.equals(ProtoGlobal.RegisteredUser.Status.RECENTLY.toString())) {
            userStatus = context.getResources().getString(R.string.recently);
        } else if (status.equals(ProtoGlobal.RegisteredUser.Status.SUPPORT.toString())) {
            userStatus = context.getResources().getString(R.string.support);
        } else if (status.equals(ProtoGlobal.RegisteredUser.Status.SERVICE_NOTIFICATIONS.toString())) {
            userStatus = context.getResources().getString(R.string.service_notification);
        } else if (status.equals(ProtoGlobal.RegisteredUser.Status.ONLINE.toString())) {
            userStatus = context.getResources().getString(R.string.online);
        } else if (status.equals(ProtoGlobal.RegisteredUser.Status.EXACTLY.toString())) {
            userStatus = ProtoGlobal.RegisteredUser.Status.EXACTLY.toString();
        }
        return userStatus;
    }

    public static void rightFileThumbnailIcon(final ImageView view, ProtoGlobal.RoomMessageType messageType, @Nullable final RealmRoomMessage message) {

        RealmAttachment attachment = null;

        if (message != null) attachment = message.getAttachment();

        if (messageType != null) {
            switch (messageType) {
                case VOICE:
                    setImageDrawable(view, R.drawable.microphone_icon);
                    break;
                case AUDIO:
                case AUDIO_TEXT:
                    setImageDrawable(view, R.drawable.green_music_note);
                    break;
                case STICKER:
                    if (attachment != null && attachment.getLocalFilePath() != null) {
                        G.imageLoader.displayImage(suitablePath(attachment.getLocalFilePath()), view);
                    }
                    break;
                case FILE:
                case FILE_TEXT:
                    if (attachment != null) {
                        if (attachment.getName().toLowerCase().endsWith(".pdf")) {
                            setImageDrawable(view, R.drawable.pdf_icon);
                        } else if (attachment.getName().toLowerCase().endsWith(".txt")) {
                            setImageDrawable(view, R.drawable.txt_icon);
                        } else if (attachment.getName().toLowerCase().endsWith(".exe")) {
                            setImageDrawable(view, R.drawable.exe_icon);
                        } else if (attachment.getName().toLowerCase().endsWith(".docs")) {
                            setImageDrawable(view, R.drawable.docx_icon);
                        } else {
                            setImageDrawable(view, R.drawable.file_icon);
                        }
                    } else {
                        setImageDrawable(view, R.drawable.file_icon);
                    }

                    break;
                case LOCATION:
                    getAndSetPositionPicture(message, view);

                    break;
                default:
                    if (attachment != null) {
                        if (attachment.isFileExistsOnLocal()) {
                            G.imageLoader.displayImage(AndroidUtils.suitablePath(attachment.getLocalFilePath()), view);
                        } else if (attachment.isThumbnailExistsOnLocal()) {
                            G.imageLoader.displayImage(AndroidUtils.suitablePath(attachment.getLocalThumbnailPath()), view);
                        } else {
                            view.setVisibility(View.GONE);
                        }
                    } else {
                        view.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    }

    /**
     * convert message type to appropriate text
     */
    public static String conversionMessageType(ProtoGlobal.RoomMessageType type) {
        return conversionMessageType(type, null, 0);
    }

    /**
     * convert message type to appropriate text and setText if textView isn't null
     */
    private static String returnConversionMessageType(ProtoGlobal.RoomMessageType type){
        String result = "";

        switch (type) {
            case VOICE:
                result = G.fragmentActivity.getResources().getString(R.string.voice_message);
                break;
            case VIDEO:
            case VIDEO_TEXT:
                result = G.fragmentActivity.getResources().getString(R.string.video_message);
                break;
            case FILE:
            case FILE_TEXT:
                result = G.fragmentActivity.getResources().getString(R.string.file_message);
                break;
            case AUDIO:
            case AUDIO_TEXT:
                result = G.fragmentActivity.getResources().getString(R.string.audio_message);
                break;
            case IMAGE:
            case IMAGE_TEXT:
                result = G.fragmentActivity.getResources().getString(R.string.image_message);
                break;
            case CONTACT:
                result = G.fragmentActivity.getResources().getString(R.string.contact_message);
                break;
            case GIF:
            case GIF_TEXT:
                result = G.fragmentActivity.getResources().getString(R.string.gif_message);
                break;
            case LOCATION:
                result = G.fragmentActivity.getResources().getString(R.string.location_message);
                break;
            case WALLET:
                result = G.fragmentActivity.getResources().getString(R.string.wallet_message);
                break;
            case STICKER:
                result = G.fragmentActivity.getResources().getString(R.string.sticker);
                break;
            default:
                break;
        }

        return result;
    }

    public static String conversionMessageType(ProtoGlobal.RoomMessageType type, @Nullable TextView textView, String colorStr) {
        String result = returnConversionMessageType(type);
        if (textView != null && !result.isEmpty()) {
            textView.setTextColor(Color.parseColor(colorStr));
            textView.setText(result);
        }
        return result;
    }

    public static String conversionMessageType(ProtoGlobal.RoomMessageType type, @Nullable TextView textView, int colorId) {
        String result = returnConversionMessageType(type);
        if (textView != null && !result.isEmpty()) {
            textView.setTextColor(ContextCompat.getColor(context, colorId));
            textView.setText(result);
        }
        return result;
    }


    private static void getAndSetPositionPicture(final RealmRoomMessage message, final ImageView view) {

        String path = AppUtils.getLocationPath(message.getLocation().getLocationLat(), message.getLocation().getLocationLong());

        if (new File(path).exists()) {
            G.imageLoader.displayImage(AndroidUtils.suitablePath(AppUtils.getLocationPath(message.getLocation().getLocationLat(), message.getLocation().getLocationLong())), view);
        } else {

            FragmentMap.loadImageFromPosition(message.getLocation().getLocationLat(), message.getLocation().getLocationLong(), new FragmentMap.OnGetPicture() {
                @Override
                public void getBitmap(Bitmap bitmap) {

                    view.setImageBitmap(bitmap);
                    final String savedPath = AppUtils.saveMapToFile(bitmap, message.getLocation().getLocationLat(), message.getLocation().getLocationLong());

                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            if (message.getLocation() != null) {
                                message.getLocation().setImagePath(savedPath);
                            }
                        }
                    });
                    realm.close();
                }
            });
        }
    }

    /**
     * make location path with latitude & longitude in temp file
     *
     * @return location path in temp file
     */
    public static String getLocationPath(double locationLat, double locationLong) {
        return G.DIR_TEMP + "/location_" + String.valueOf(locationLat).replace(".", "") + "_" + String.valueOf(locationLong).replace(".", "") + ".png";
    }

    public static String saveMapToFile(Bitmap bitmap, Double latitude, Double longitude) {

        String result = "";

        try {
            if (bitmap == null) return result;

            String fileName = "/location_" + latitude.toString().replace(".", "") + "_" + longitude.toString().replace(".", "") + ".png";
            File file = new File(G.DIR_TEMP, fileName);

            OutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);

            result = file.getPath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * update message status automatically
     *
     * @param view TextView message status
     */
    public static void rightMessageStatus(ImageView view, ProtoGlobal.RoomMessageStatus status, boolean isSenderMe) {

        if (view == null) {
            return;
        }
        if (!isSenderMe) {
            view.setVisibility(View.GONE);
            return;
        } else {
            view.setVisibility(View.VISIBLE);
        }
        switch (status) {
            case DELIVERED:

                setImageDrawable(view, R.drawable.ic_double_check);
//                view.setColorFilter(Color.BLACK);
                view.setColorFilter(Color.parseColor(G.tintImage), PorterDuff.Mode.SRC_IN);
                break;
            case FAILED:
                setImageDrawable(view, R.drawable.ic_error_igap);
                view.setColorFilter(view.getContext().getResources().getColor(R.color.red));
                break;
            case SEEN:
                setImageDrawable(view, R.drawable.ic_double_check);
                view.setColorFilter(Color.parseColor(G.SeenTickColor));
                break;
            case SENDING:
//                view.setColorFilter(view.getContext().getResources().getColor(R.color.black_register));
                view.setColorFilter(Color.parseColor(G.tintImage), PorterDuff.Mode.SRC_IN);
                break;
            case SENT:
                setImageDrawable(view, R.drawable.ic_check);
//                view.setColorFilter(view.getContext().getResources().getColor(R.color.black_register));
                view.setColorFilter(Color.parseColor(G.tintImage), PorterDuff.Mode.SRC_IN);
                break;
            default:
                view.setVisibility(View.GONE);
                break;
        }
    }

    /**
     * update message status automatically
     *
     * @param view TextView message status
     */
    public static void rightMessageStatus(ImageView view, ProtoGlobal.RoomMessageStatus status, ProtoGlobal.RoomMessageType messageType, boolean isSenderMe) {
        if (view == null) {
            return;
        }
        if (!isSenderMe) {
            view.setVisibility(View.GONE);
            return;
        } else {
            view.setVisibility(View.VISIBLE);
        }
        switch (status) {
            case DELIVERED:
                setImageDrawable(view, R.drawable.ic_double_check);
                //DrawableCompat.setTint(view.getDrawable().mutate(), Color.BLACK);
                break;
            case FAILED:
                setImageDrawable(view, R.drawable.ic_error_igap);
                if (messageType == ProtoGlobal.RoomMessageType.IMAGE || messageType == ProtoGlobal.RoomMessageType.VIDEO || messageType == ProtoGlobal.RoomMessageType.GIF) {
                    DrawableCompat.setTint(view.getDrawable().mutate(), Color.WHITE);
                } else {
                    DrawableCompat.setTint(view.getDrawable().mutate(), Color.RED);
                }
                break;
            case LISTENED:
            case SEEN:
                setImageDrawable(view, R.drawable.ic_double_check);
                final Drawable originalDrawable = view.getDrawable();
                final Drawable wrappedDrawable = DrawableCompat.wrap(originalDrawable);
                DrawableCompat.setTintList(wrappedDrawable, ColorStateList.valueOf(view.getContext().getResources().getColor(R.color.iGapColor)));
                break;
            case SENDING:
                setImageDrawable(view, R.drawable.ic_clock);
                if (messageType == ProtoGlobal.RoomMessageType.IMAGE || messageType == ProtoGlobal.RoomMessageType.VIDEO || messageType == ProtoGlobal.RoomMessageType.GIF) {
                    DrawableCompat.setTint(view.getDrawable().mutate(), Color.WHITE);
                } else {
                    DrawableCompat.setTint(view.getDrawable().mutate(), Color.BLACK);
                }
                break;
            case SENT:
                setImageDrawable(view, R.drawable.ic_check);
                if (messageType == ProtoGlobal.RoomMessageType.IMAGE || messageType == ProtoGlobal.RoomMessageType.VIDEO || messageType == ProtoGlobal.RoomMessageType.GIF) {
                    DrawableCompat.setTint(view.getDrawable().mutate(), Color.WHITE);
                } else {
                    DrawableCompat.setTint(view.getDrawable().mutate(), Color.BLACK);
                }
                break;
            default:
                view.setVisibility(View.GONE);
                break;
        }
    }

    public static void setImageDrawable(ImageView view, int res) {
        view.setImageDrawable(net.iGap.messageprogress.AndroidUtils.getDrawable(G.currentActivity, res));
        // view.setImageResource(res);
    }

    /**
     * due to the message type and attachment will be returned appropriate text message
     *
     * @return appropriate text message for showing in view
     */
    public static String rightLastMessage(RealmRoomMessage message) {
        String messageText;
        if (message == null) {
            return null;
        }

        if (message.isDeleted()) {
            return G.fragmentActivity.getString(R.string.deleted_message); //return computeLastMessage(roomId);
        } else if (!TextUtils.isEmpty(message.getMessage())) {
            return message.getMessage();
        } else if (message.getForwardMessage() != null && !TextUtils.isEmpty(message.getForwardMessage().getMessage())) {
            return message.getForwardMessage().getMessage();
        } else if (message.getReplyTo() != null && !TextUtils.isEmpty(message.getReplyTo().getMessage())) {
            return message.getReplyTo().getMessage();
        } else {
            RealmAttachment attachment = message.getAttachment();
            switch (message.getForwardMessage() == null ? message.getMessageType() : message.getForwardMessage().getMessageType()) {
                case AUDIO_TEXT:
                case AUDIO:
                    if (attachment == null) {
                        return null;
                    }
                    messageText = G.fragmentActivity.getString(R.string.last_msg_format_chat, attachment.getName());
                    break;
                case CONTACT:
                    messageText = "contact"; // need to fill messageText with a String because in return check null. this string isn't important.
                    break;
                case FILE_TEXT:
                case FILE:
                    if (attachment == null) {
                        return null;
                    }
                    messageText = G.fragmentActivity.getString(R.string.last_msg_format_chat, attachment.getName());
                    break;
                case GIF_TEXT:
                case GIF:
                    if (attachment == null) {
                        return null;
                    }
                    messageText = G.fragmentActivity.getString(R.string.last_msg_format_chat, attachment.getName());
                    break;
                case IMAGE_TEXT:
                case IMAGE:
                    if (attachment == null) {
                        return null;
                    }
                    messageText = G.fragmentActivity.getString(R.string.last_msg_format_chat, attachment.getName());
                    break;
                case LOCATION:
                    messageText = G.fragmentActivity.getString(R.string.last_msg_format_chat, G.fragmentActivity.getString(R.string.location_message));
                    break;
                case LOG:
                    messageText = G.fragmentActivity.getString(R.string.last_msg_format_chat, HelperLogMessage.deserializeLog(message.getLogs(), false).toString());
                    break;
                case VIDEO_TEXT:
                case VIDEO:
                    if (attachment == null) {
                        return null;
                    }
                    messageText = G.fragmentActivity.getString(R.string.last_msg_format_chat, attachment.getName());
                    break;
                case VOICE:
                    if (attachment == null) {
                        return null;
                    }
                    messageText = G.fragmentActivity.getString(R.string.last_msg_format_chat, attachment.getName());
                    break;
                case WALLET:
                    messageText = "wallet"; // need to fill messageText with a String because in return check null. this string isn't important.
                    break;
                case STICKER:
                    messageText = G.fragmentActivity.getResources().getString(R.string.sticker);
                    break;
                default:
                    messageText = null;
                    break;
            }
        }

        return messageText;
    }

    /**
     * fetch type of message for show in reply view
     *
     * @param realmRoomMessage for detect message type
     * @return final message text
     */
    public static String replyTextMessage(RealmRoomMessage realmRoomMessage, Resources resources) {
        RealmRoomMessage message = RealmRoomMessage.getFinalMessage(realmRoomMessage);
        String messageText = "";
        if (message != null) {
            switch (message.getMessageType()) {
                case TEXT:
                    if (message.getMessage() != null) {
                        messageText = message.getMessage();
                    }
                    break;
                case AUDIO_TEXT:
                case AUDIO:
                    if (message.getAttachment() == null) {
                        return null;
                    }
                    messageText = resources.getString(R.string.audio_message);
                    break;
                case CONTACT:
                    messageText = message.getRoomMessageContact().getFirstName() + "\n" + message.getRoomMessageContact().getLastPhoneNumber();
                    break;
                case FILE_TEXT:
                case FILE:
                    if (message.getAttachment() == null) {
                        return null;
                    }
                   messageText = resources.getString(R.string.file_message);
                    break;
                case STICKER:
                    messageText = resources.getString(R.string.sticker);
                    break;
                case GIF_TEXT:
                case GIF:
                    if (message.getAttachment() == null) {
                        return null;
                    }
                    messageText = resources.getString(R.string.gif_message);
                    break;
                case IMAGE_TEXT:
                case IMAGE:
                    if (message.getAttachment() == null) {
                        return null;
                    }
                    messageText = resources.getString(R.string.image_message);
                    break;
                case LOCATION:
                    messageText = resources.getString(R.string.location_message);
                    break;
                case VIDEO_TEXT:
                case VIDEO:
                    if (message.getAttachment() == null) {
                        return null;
                    }
                    messageText = resources.getString(R.string.video_message);
                    break;
                case VOICE:
                    if (message.getAttachment() == null) {
                        return null;
                    }
                    messageText = resources.getString(R.string.voice_message);
                    break;
                default:
                    messageText = "";
                    break;
            }
        }
        return messageText;
    }

    public static String computeLastMessage(long roomId) {
        Realm realm = Realm.getDefaultInstance();
        String lastMessage = "";
        RealmResults<RealmRoomMessage> realmList = realm.where(RealmRoomMessage.class).equalTo(RealmRoomMessageFields.ROOM_ID, roomId).findAll().sort(RealmRoomMessageFields.MESSAGE_ID, Sort.DESCENDING);
        for (RealmRoomMessage realmRoomMessage : realmList) {
            if (realmRoomMessage != null && !realmRoomMessage.isDeleted()) {
                lastMessage = AppUtils.rightLastMessage(realmRoomMessage);
                break;
            }
        }
        realm.close();
        return lastMessage;
    }

    public static MaterialDialog.Builder buildResendDialog(Context context, int failedMessagesCount, final IResendMessage listener) {
        List<String> items = new ArrayList<>();
        List<Integer> itemsId = new ArrayList<>();
        items.add(context.getString(R.string.resend_chat_message));
        itemsId.add(0);
        if (failedMessagesCount > 1) {
            items.add(String.format(context.getString(R.string.resend_all_messages), failedMessagesCount));
            itemsId.add(1);
        }
        items.add(context.getString(R.string.delete_item_dialog));
        itemsId.add(2);

        int[] newIds = new int[itemsId.size()];
        for (Integer integer : itemsId) {
            newIds[itemsId.indexOf(integer)] = integer;
        }

        return new MaterialDialog.Builder(context).title(R.string.resend_chat_message).negativeText(context.getString(R.string.cancel)).items(items).itemsIds(newIds).itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                switch (itemView.getId()) {
                    case 0:
                        listener.resendMessage();
                        break;
                    case 1:
                        listener.resendAllMessages();
                        break;
                    case 2:
                        listener.deleteMessage();
                        break;
                }
            }
        });
    }

    public static String humanReadableDuration(double d) {
        String output = Double.toString(d);

        if (output.contains(".")) {
            String[] split = output.split("\\.");
            if (split[1].length() > 2) {
                output = split[0] + "." + split[1].charAt(0) + split[1].charAt(1);
            }
        }

        return output;
    }

    public static void setProgresColler(ProgressBar progressBar) {

        try {

            progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor(G.progressColor), PorterDuff.Mode.SRC_IN);

            //  getResources().getColor(R.color.toolbar_background)

        } catch (Exception e) {

        }
    }

    public static void setProgresColor(CircularProgressView progressBar) {

        try {

            progressBar.setColor(Color.parseColor(G.progressColor));
        } catch (Exception e) {

        }
    }

    public static Uri createtUri(File file) {

        Uri outputUri;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            outputUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
        } else {
            outputUri = Uri.fromFile(file);
        }

        return outputUri;
    }

    public static void shareItem(Intent intent, StructMessageInfo messageInfo) {

        try {
            String message = messageInfo.forwardedFrom != null ? messageInfo.forwardedFrom.getMessage() : messageInfo.messageText;
            if (message != null) {
                intent.putExtra(Intent.EXTRA_TEXT, message);
            }
            String filePath = messageInfo.forwardedFrom != null ? messageInfo.forwardedFrom.getAttachment().getLocalFilePath() : messageInfo.attachment.getLocalFilePath();
            if (filePath != null) {

                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", new File(filePath));
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } else {
                    uri = Uri.fromFile(new File(filePath));
                }
                intent.putExtra(Intent.EXTRA_STREAM, uri);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void shareItem(Intent intent, String path) {
        try {
            if (path != null) {
                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", new File(path));
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } else {
                    uri = Uri.fromFile(new File(path));
                }
                intent.putExtra(Intent.EXTRA_STREAM, uri);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setVibrator(long time) {
        Vibrator vShort = (Vibrator) G.context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vShort != null) {
            vShort.vibrate(time);
        }
    }

    public static void closeKeyboard(View v) {
        try {
            InputMethodManager imm = (InputMethodManager) G.fragmentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        } catch (IllegalStateException e) {
            e.getStackTrace();
        }
    }

    public static void error(String error) {
        try {

            HelperError.showSnackMessage(error, true);

        } catch (IllegalStateException e) {
            e.getStackTrace();
        }
    }

    public static long makeRandomId() {
        return SUID.id().get();
        // return Math.abs(UUID.randomUUID().getLeastSignificantBits());
    }

    public static int[] updateBadgeOnly(Realm realm, long roomId) {
        int unreadMessageCount = 0;
        int chatCount = 0;
        int[] result = new int[2];

        RealmResults<RealmRoom> realmRooms = realm.where(RealmRoom.class).equalTo(RealmRoomFields.KEEP_ROOM, false).
                equalTo(RealmRoomFields.MUTE, false).equalTo(RealmRoomFields.IS_DELETED, false).notEqualTo(RealmRoomFields.ID, roomId).findAll();

        for (RealmRoom realmRoom1 : realmRooms) {
            if (realmRoom1.getUnreadCount() > 0) {
                unreadMessageCount += realmRoom1.getUnreadCount();
                ++chatCount;
            }
        }

        try {
            ShortcutBadger.applyCount(G.context, unreadMessageCount);
        } catch (Exception e) {
            e.printStackTrace();
        }

        result[0] = unreadMessageCount;
        result[1] = chatCount;
        return result;
    }

    public static void cleanBadge() {
        try {
            ShortcutBadger.applyCount(G.context, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
