/*
* This is the source code of iGap for Android
* It is licensed under GNU AGPL v3.0
* You should have received a copy of the license in this archive (see LICENSE).
* Copyright © 2017 , iGap - www.iGap.net
* iGap Messenger | Free, Fast and Secure instant messaging application
* The idea of the RooyeKhat Media Company - www.RooyeKhat.co
* All rights reserved.
*/

package net.iGap.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardCloseListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardOpenListener;

import net.iGap.G;
import net.iGap.R;
import net.iGap.Theme;
import net.iGap.helper.HelperNotification;
import net.iGap.interfaces.IPopUpListener;
import net.iGap.interfaces.OnVoiceRecord;
import net.iGap.libs.rippleeffect.RippleView;
import net.iGap.module.AppUtils;
import net.iGap.module.ChatSendMessageUtil;
import net.iGap.module.EmojiEditTextE;
import net.iGap.module.LastSeenTimeUtil;
import net.iGap.module.MaterialDesignTextView;
import net.iGap.module.SHP_SETTING;
import net.iGap.module.UploadService;
import net.iGap.module.VoiceRecord;
import net.iGap.proto.ProtoGlobal;
import net.iGap.realm.RealmRegisteredInfo;
import net.iGap.realm.RealmRoomMessage;

import java.io.File;
import java.util.ArrayList;

import io.realm.Realm;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ActivityPopUpNotification extends AppCompatActivity {

    public static boolean isPopUpVisible = false;
    public static IPopUpListener popUpListener;


    //////////////////////////////////////////   appbar component
    ViewPager viewPager;
    ArrayList<HelperNotification.StructNotification> mList;
    private TextView txtName;
    private TextView txtLastSeen;

    //////////////////////////////////////////
    private ImageView imvUserPicture;
    private Button btnMessageCounter;
    private View viewAttachFile;
    private View viewMicRecorder;

    //////////////////////////////////////////    attach layout
    private MaterialDesignTextView btnSmileButton;
    private EmojiEditTextE edtChat;
    private MaterialDesignTextView btnMic;

    //////////////////////////////////////////
    private MaterialDesignTextView btnSend;
    private VoiceRecord voiceRecord;
    private boolean sendByEnter = false;
    private AdapterViewPagerClass mAdapter;
    private int listSize = 0;

    private String initialize;
    private String color;

    /////////////////////////////////////////////////////////////////////////////////////////
    private EmojiPopup emojiPopup;

    @Override
    protected void onResume() {
        super.onResume();
        isPopUpVisible = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPopUpVisible = false;
    }

    @Override
    public void onBackPressed() {
        if (emojiPopup != null && emojiPopup.isShowing()) {
            emojiPopup.dismiss();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(G.updateResources(newBase)));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_notification);

        mList = HelperNotification.getInstance().getMessageList();
        new InitComponent();
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    private void changeEmojiButtonImageResource(@StringRes int drawableResourceId) {
        btnSmileButton.setText(drawableResourceId);
    }

    private void setUpEmojiPopup() {
        switch (G.themeColor) {
            case Theme.BLUE_GREY_COMPLETE:
            case Theme.INDIGO_COMPLETE:
            case Theme.BROWN_COMPLETE:
            case Theme.GREY_COMPLETE:
            case Theme.TEAL_COMPLETE:
            case Theme.DARK:

                setEmojiColor(G.getTheme2BackgroundColor(), G.textTitleTheme, G.textTitleTheme);
                break;
            default:
                setEmojiColor(Color.parseColor("#eceff1"), "#61000000", "#61000000");
        }
    }

    private void setEmojiColor(int BackgroundColor, String iconColor, String dividerColor) {
        emojiPopup = EmojiPopup.Builder.fromRootView(findViewById(R.id.ac_ll_parent_notification)).setOnEmojiBackspaceClickListener(new OnEmojiBackspaceClickListener() {
            @Override
            public void onEmojiBackspaceClick(View v) {

            }
        }).setOnEmojiPopupShownListener(new OnEmojiPopupShownListener() {
            @Override
            public void onEmojiPopupShown() {
                changeEmojiButtonImageResource(R.string.md_black_keyboard_with_white_keys);
            }
        }).setOnSoftKeyboardOpenListener(new OnSoftKeyboardOpenListener() {
            @Override
            public void onKeyboardOpen(final int keyBoardHeight) {

            }
        }).setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener() {
            @Override
            public void onEmojiPopupDismiss() {
                changeEmojiButtonImageResource(R.string.md_emoticon_with_happy_face);
            }
        }).setOnSoftKeyboardCloseListener(new OnSoftKeyboardCloseListener() {
            @Override
            public void onKeyboardClose() {
                emojiPopup.dismiss();
            }
        })
                .setBackgroundColor(BackgroundColor)
                .setIconColor(Color.parseColor(iconColor))
                .setDividerColor(Color.parseColor(dividerColor))
                .build(edtChat);
    }

    private void setImageAndTextAppBar(int position) {

        initialize = mList.get(position).initialize;
        color = mList.get(position).color;
        txtName.setText(mList.get(position).name);

        Realm realm = Realm.getDefaultInstance();

        RealmRegisteredInfo realmRegisteredInfo = RealmRegisteredInfo.getRegistrationInfo(realm, mList.get(position).senderId);
        if (realmRegisteredInfo != null) {
            if (realmRegisteredInfo.getStatus().equals(ProtoGlobal.RegisteredUser.Status.EXACTLY.toString())) {
                txtLastSeen.setText(LastSeenTimeUtil.computeTime(realmRegisteredInfo.getId(), realmRegisteredInfo.getLastSeen(), false));
            } else {
                txtLastSeen.setText(realmRegisteredInfo.getStatus());
            }
        } else {
            txtLastSeen.setText("");
        }

        setAvatar(realmRegisteredInfo);

        realm.close();

    }

    /////////////////////////////////////////////////////////////////////////////////////////

    private void setAvatar(RealmRegisteredInfo realmRegisteredInfo) {

        String avatarPath = null;
        if (realmRegisteredInfo != null && realmRegisteredInfo.getAvatars() != null && realmRegisteredInfo.getLastAvatar() != null) {
            String mainFilePath = realmRegisteredInfo.getLastAvatar().getFile().getLocalFilePath();
            if (mainFilePath != null && new File(mainFilePath).exists()) { // if main image is exist showing that
                avatarPath = mainFilePath;
            } else {
                avatarPath = realmRegisteredInfo.getLastAvatar().getFile().getLocalThumbnailPath();
            }
        }

        //Set Avatar For Chat,Group,Channel
        if (avatarPath != null) {
            File imgFile = new File(avatarPath);
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                imvUserPicture.setImageBitmap(myBitmap);
            } else {
//                if (realmRegisteredInfo != null && realmRegisteredInfo.getLastAvatar() != null && realmRegisteredInfo.getLastAvatar().getFile() != null) {
//                     onRequestDownloadAvatar(realmRegisteredInfo.getLastAvatar().getFile());
//                }
                imvUserPicture.setImageBitmap(net.iGap.helper.HelperImageBackColor.drawAlphabetOnPicture((int) imvUserPicture.getContext().getResources().getDimension(R.dimen.dp60), initialize, color));
            }
        } else {
//            if (realmRegisteredInfo != null && realmRegisteredInfo.getLastAvatar() != null && realmRegisteredInfo.getLastAvatar().getFile() != null) {
//                  onRequestDownloadAvatar(realmRegisteredInfo.getLastAvatar().getFile());
//            }
            imvUserPicture.setImageBitmap(net.iGap.helper.HelperImageBackColor.drawAlphabetOnPicture((int) imvUserPicture.getContext().getResources().getDimension(R.dimen.dp60), initialize, color));
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        if (voiceRecord != null) {
            voiceRecord.dispatchTouchEvent(event);
        }

        return super.dispatchTouchEvent(event);
    }

    public static void sendMessage(final String message, final long mRoomId, ProtoGlobal.Room.Type chatType) {
        String identity = Long.toString(System.currentTimeMillis());
        RealmRoomMessage.makeTextMessage(mRoomId, Long.parseLong(identity), message);
        new ChatSendMessageUtil().newBuilder(chatType, ProtoGlobal.RoomMessageType.TEXT, mRoomId).message(message).sendMessage(identity);
    }

    private void goToChatActivity() {
        Intent intent = new Intent(ActivityPopUpNotification.this, ActivityMain.class);
        intent.putExtra(ActivityMain.openChat, mList.get(viewPager.getCurrentItem()).roomId);
        startActivity(intent);
        finish();
    }

    private class InitComponent {

        InitComponent() {
            initMethod();
            initAppbar();
            initViewPager();
            initLayoutAttach();
            setUpEmojiPopup();
        }

        private void initMethod() {

            popUpListener = new IPopUpListener() {
                @Override
                public void onMessageReceive() {

                    viewPager.post(new Runnable() {
                        @Override
                        public void run() {
                            mList = HelperNotification.getInstance().getMessageList();
                            viewPager.setAdapter(mAdapter);
                            btnMessageCounter.setText(1 + "/" + mList.size());
                            setImageAndTextAppBar(viewPager.getCurrentItem());
                            listSize = mList.size();
                        }
                    });
                }
            };

            viewAttachFile = findViewById(R.id.apn_layout_attach_file);

            viewMicRecorder = findViewById(R.id.apn_layout_mic_recorde);

            voiceRecord = new VoiceRecord(ActivityPopUpNotification.this, viewMicRecorder, viewAttachFile, new OnVoiceRecord() {
                @Override
                public void onVoiceRecordDone(String savedPath) {
                    Intent uploadService = new Intent(ActivityPopUpNotification.this, UploadService.class);
                    uploadService.putExtra("Path", savedPath);
                    uploadService.putExtra("Roomid", mList.get(viewPager.getCurrentItem()).roomId);
                    startService(uploadService);

                    // sendVoice(savedPath, unreadList.get(viewPager.getCurrentItem()).getRoomId());

                    finish();
                }

                @Override
                public void onVoiceRecordCancel() {

                }
            });

            // get sendByEnter action from setting value
            SharedPreferences sharedPreferences = getSharedPreferences(SHP_SETTING.FILE_NAME, MODE_PRIVATE);
            int checkedSendByEnter = sharedPreferences.getInt(SHP_SETTING.KEY_SEND_BT_ENTER, 0);
            sendByEnter = checkedSendByEnter == 1;
        }

        private void initAppbar() {

            RippleView rippleBackButton = (RippleView) findViewById(R.id.apn_ripple_back_Button);

            rippleBackButton.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
                @Override
                public void onComplete(RippleView rippleView) {
                    finish();
                }
            });

            findViewById(R.id.apn_ll_toolbar).setBackgroundColor(Color.parseColor(G.appBarColor));

            txtName = (TextView) findViewById(R.id.apn_txt_name);
            txtName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToChatActivity();
                }
            });

            txtLastSeen = (TextView) findViewById(R.id.apn_txt_last_seen);

            imvUserPicture = (ImageView) findViewById(R.id.apn_imv_user_picture);
            imvUserPicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToChatActivity();
                }
            });

            btnMessageCounter = (Button) findViewById(R.id.apn_btn_message_counter);
            btnMessageCounter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        private void initViewPager() {

            viewPager = (ViewPager) findViewById(R.id.apn_view_pager);
            mAdapter = new AdapterViewPagerClass();
            viewPager.setAdapter(mAdapter);
            listSize = mList.size();

            btnMessageCounter.setText(1 + "/" + listSize);

            setImageAndTextAppBar(viewPager.getCurrentItem());

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    btnMessageCounter.setText(position + 1 + "/" + listSize);

                    setImageAndTextAppBar(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }

        private void initLayoutAttach() {

            btnSmileButton = (MaterialDesignTextView) findViewById(R.id.apn_btn_smile_button);
            btnSmileButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    emojiPopup.toggle();
                }
            });

            edtChat = (EmojiEditTextE) findViewById(R.id.apn_edt_chat);

            edtChat.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence text, int i, int i1, int i2) {

                    // if in the seeting page send by enter is on message send by enter key
                    if (text.toString().endsWith(System.getProperty("line.separator"))) {
                        if (sendByEnter) btnSend.performClick();
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                    if (edtChat.getText().length() > 0) {
                        btnMic.animate().alpha(0F).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                btnMic.setVisibility(View.GONE);
                            }
                        }).start();
                        btnSend.animate().alpha(1F).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                btnSend.setVisibility(View.VISIBLE);
                            }
                        }).start();
                    } else {
                        btnMic.animate().alpha(1F).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                btnMic.setVisibility(View.VISIBLE);
                            }
                        }).start();
                        btnSend.animate().alpha(0F).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                btnSend.setVisibility(View.GONE);
                            }
                        }).start();
                    }

                    // android emoji one doesn't support common space unicode
                    // to support space character, a new unicode will be replaced.
                    if (editable.toString().contains("\u0020")) {
                        Editable ab = new SpannableStringBuilder(editable.toString().replace("\u0020", "\u2000"));
                        editable.replace(0, editable.length(), ab);
                    }
                }
            });

            btnMic = (MaterialDesignTextView) findViewById(R.id.apn_btn_mic);
            btnMic.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    voiceRecord.setItemTag("ivVoice");
                    viewAttachFile.setVisibility(View.GONE);
                    viewMicRecorder.setVisibility(View.VISIBLE);


                    AppUtils.setVibrator(50);
                    G.handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            voiceRecord.startVoiceRecord();
                        }
                    }, 60);
                    return true;
                }
            });

            btnSend = (MaterialDesignTextView) findViewById(R.id.apn_btn_send);
            btnSend.setTextColor(Color.parseColor(G.attachmentColor));

            btnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int position = viewPager.getCurrentItem();

                    sendMessage(edtChat.getText().toString(), mList.get(position).roomId, ProtoGlobal.Room.Type.valueOf(mList.get(position).roomType.toString()));

                    edtChat.setText("");

                    finish();
                }
            });
        }
    }

    private class AdapterViewPagerClass extends PagerAdapter {

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public Object instantiateItem(View container, final int position) {

            LayoutInflater inflater = LayoutInflater.from(ActivityPopUpNotification.this);
            ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.sub_layout_activity_popup_notification, (ViewGroup) container, false);

            TextView txtMessage = (TextView) layout.findViewById(R.id.slapn_txt_message);
            txtMessage.setText(mList.get(position).message);

            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToChatActivity();
                }
            });

            ((ViewGroup) container).addView(layout);

            return layout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
