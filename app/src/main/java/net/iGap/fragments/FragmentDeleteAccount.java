/*
 * This is the source code of iGap for Android
 * It is licensed under GNU AGPL v3.0
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright © 2017 , iGap - www.iGap.net
 * iGap Messenger | Free, Fast and Secure instant messaging application
 * The idea of the RooyeKhat Media Company - www.RooyeKhat.co
 * All rights reserved.
 */

package net.iGap.fragments;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import net.iGap.BuildConfig;
import net.iGap.G;
import net.iGap.R;
import net.iGap.helper.HelperError;
import net.iGap.helper.HelperString;
import net.iGap.interfaces.OnUserDelete;
import net.iGap.interfaces.OnUserGetDeleteToken;
import net.iGap.libs.rippleeffect.RippleView;
import net.iGap.module.AppUtils;
import net.iGap.module.EditTextAdjustPan;
import net.iGap.module.SmsRetriver.SMSReceiver;
import net.iGap.proto.ProtoUserDelete;
import net.iGap.request.RequestUserDelete;
import net.iGap.request.RequestUserGetDeleteToken;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentDeleteAccount extends BaseFragment {

    private String regex = null;
    private String smsMessage = null;
    private EditTextAdjustPan edtDeleteAccount;
    private RippleView txtSet;
    private CountDownTimer countDownTimer;
    private String phone;
    private ViewGroup ltTime;
    private ProgressBar prgWaiting;
    private boolean isFirstClick = true;
    public static final String TAG = FragmentDeleteAccount.class.getSimpleName();
    private SMSReceiver smsReceiver;

    public FragmentDeleteAccount() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return attachToSwipeBack(inflater.inflate(R.layout.fragment_delete_account, container, false));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().getString("PHONE") != null) {

            phone = getArguments().getString("PHONE");
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.fda_ll_toolbar).setBackgroundColor(Color.parseColor(G.appBarColor));

        G.onUserGetDeleteToken = new OnUserGetDeleteToken() {
            @Override
            public void onUserGetDeleteToken(int resendDelay, String tokenRegex, String tokenLength) {
                regex = tokenRegex;
                setCode();
            }

            @Override
            public void onUserGetDeleteError(final int majorCode, int minorCode, final int time) {

                G.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        switch (majorCode) {
                            case 152:
                                dialogWaitTime(R.string.USER_GET_DELETE_TOKEN_MAX_TRY_LOCK, time, majorCode);
                                break;
                            case 153:
                                dialogWaitTime(R.string.USER_GET_DELETE_TOKEN_MAX_SEND, time, majorCode);
                                break;
                        }
                    }
                });
            }
        };

        new RequestUserGetDeleteToken().userGetDeleteToken();

        ViewGroup rootDeleteAccount = (ViewGroup) view.findViewById(R.id.rootDeleteAccount);
        rootDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        RippleView txtBack = (RippleView) view.findViewById(R.id.stda_ripple_back);
        txtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) G.context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                removeFromBaseFragment(FragmentDeleteAccount.this);
            }
        });

        prgWaiting = (ProgressBar) view.findViewById(R.id.stda_prgWaiting_addContact);
        AppUtils.setProgresColler(prgWaiting);

        ltTime = (ViewGroup) view.findViewById(R.id.stda_layout_time);

        TextView txtPhoneNumber = (TextView) view.findViewById(R.id.stda_txt_phoneNumber);
        if (phone != null) txtPhoneNumber.setText("" + phone);

        txtSet = (RippleView) view.findViewById(R.id.stda_ripple_set);
        txtSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (edtDeleteAccount.getText().length() > 0) {

                    new MaterialDialog.Builder(G.fragmentActivity).title(G.fragmentActivity.getResources().getString(R.string.delete_account))
                            .titleColor(G.context.getResources().getColor(android.R.color.black))
                            .content(R.string.sure_delete_account).positiveText(G.fragmentActivity.getResources().getString(R.string.B_ok)).negativeText(G.fragmentActivity.getResources().getString(R.string.B_cancel))
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {

                                    //                                    String verificationCode = HelperString.regexExtractValue(smsMessage, regex);
                                    String verificationCode = edtDeleteAccount.getText().toString();
                                    if (verificationCode != null && !verificationCode.isEmpty() && isFirstClick) {

                                        isFirstClick = false;
                                        G.onUserDelete = new OnUserDelete() {
                                            @Override
                                            public void onUserDeleteResponse() {
                                                hideProgressBar();

                                            }

                                            @Override
                                            public void Error(final int majorCode, final int minorCode, final int time) {

                                                hideProgressBar();
                                                isFirstClick = true;
                                                G.handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (dialog.isShowing()) dialog.dismiss();
                                                        switch (majorCode) {
                                                            case 158:
                                                                dialogWaitTime(R.string.USER_DELETE_MAX_TRY_LOCK, time, majorCode);
                                                                break;
                                                        }
                                                    }
                                                });
                                            }

                                            @Override
                                            public void TimeOut() {
                                                hideProgressBar();
                                                isFirstClick = true;
                                                G.handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        HelperError.showSnackMessage(G.fragmentActivity.getResources().getString(R.string.time_out), false);
                                                    }
                                                });
                                            }
                                        };

                                        showProgressBar();
                                        new RequestUserDelete().userDelete(verificationCode, ProtoUserDelete.UserDelete.Reason.OTHER);
                                    }
                                }
                            }).show();
                } else {


                    HelperError.showSnackMessage(G.fragmentActivity.getResources().getString(R.string.please_enter_code_for_verify), false);

                }
            }
        });

        edtDeleteAccount = (EditTextAdjustPan) view.findViewById(R.id.stda_edt_dleteAccount);

        final View viewLineBottom = view.findViewById(R.id.stda_line_below_editText);
        txtSet.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    viewLineBottom.setBackgroundColor(G.context.getResources().getColor(R.color.toolbar_background));
                } else {
                    viewLineBottom.setBackgroundColor(G.context.getResources().getColor(R.color.line_edit_text));
                }
            }
        });

        final TextView txtTimerLand = (TextView) view.findViewById(R.id.stda_txt_time);

        G.handler.post(new Runnable() {
            @Override
            public void run() {

                long time;
                if (BuildConfig.DEBUG) {
                    time = 3 * 1000;
                } else {
                    time = 1000 * 60;
                }

                countDownTimer = new CountDownTimer(time, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        int seconds = (int) ((millisUntilFinished) / 1000);
                        int minutes = seconds / 60;
                        seconds = seconds % 60;
                        txtTimerLand.setText("" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
                    }

                    @Override
                    public void onFinish() {

                        ltTime.setVisibility(View.GONE);
                    }
                };
                countDownTimer.start();
            }
        });

        startSMSListener();

    }


    private void startSMSListener() {
        try {
            smsReceiver = new SMSReceiver();
            smsReceiver.setOTPListener(new SMSReceiver.OTPReceiveListener() {
                @Override
                public void onOTPReceived(String message) {

                    try {
                        G.handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                smsMessage = message;
                                setCode();
                            }
                        }, 500);
                    } catch (Exception e1) {
                        e1.getStackTrace();
                    }

                    unregisterReceiver();

                }

                @Override
                public void onOTPTimeOut() {
                    Log.e(TAG, "OTP Time out");
                }

                @Override
                public void onOTPReceivedError(String error) {
                    Log.e(TAG, error);
                }
            });

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION);
            G.fragmentActivity.registerReceiver(smsReceiver, intentFilter);

            SmsRetrieverClient client = SmsRetriever.getClient(getActivity());

            Task<Void> task = client.startSmsRetriever();
            task.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.e(TAG, "sms API successfully started   ");
                }
            });

            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "sms Fail to start API   ");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unregisterReceiver() {
        try {
            if (smsReceiver != null) {
                G.fragmentActivity.unregisterReceiver(smsReceiver);
                smsReceiver = null;
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }


    private void setCode() {
        if (smsMessage != null && regex != null) {
            countDownTimer.cancel();
            String verificationCode = HelperString.regexExtractValue(smsMessage, regex);
            if (verificationCode.length() > 0) {

                edtDeleteAccount.setText("" + verificationCode);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver();
    }

    private void showProgressBar() {

        G.handler.post(new Runnable() {
            @Override
            public void run() {
                prgWaiting.setVisibility(View.VISIBLE);
                if (G.fragmentActivity != null) {
                    G.fragmentActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            }
        });
    }

    private void hideProgressBar() {
        G.handler.post(new Runnable() {
            @Override
            public void run() {
                prgWaiting.setVisibility(View.GONE);
                if (G.fragmentActivity != null) {
                    G.fragmentActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            }
        });
    }

    private void dialogWaitTime(int title, long time, int majorCode) {
        boolean wrapInScrollView = true;
        final MaterialDialog dialog = new MaterialDialog.Builder(G.fragmentActivity).title(title).customView(R.layout.dialog_remind_time, wrapInScrollView).positiveText(R.string.B_ok).autoDismiss(false).negativeText(R.string.B_cancel).onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.dismiss();
                removeFromBaseFragment(FragmentDeleteAccount.this);

            }
        }).canceledOnTouchOutside(false).onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.dismiss();
                new RequestUserGetDeleteToken().userGetDeleteToken();
            }
        }).show();

        View v = dialog.getCustomView();

        final TextView remindTime = (TextView) v.findViewById(R.id.remindTime);
        CountDownTimer countWaitTimer = new CountDownTimer(time * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) ((millisUntilFinished) / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                remindTime.setText("" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
            }

            @Override
            public void onFinish() {
                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                remindTime.setText("00:00");
            }
        };
        countWaitTimer.start();
    }
}
