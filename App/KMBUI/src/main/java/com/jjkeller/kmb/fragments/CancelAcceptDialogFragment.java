package com.jjkeller.kmb.fragments;

import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjkeller.kmbapi.controller.utility.DeviceInfo;
import com.jjkeller.kmbapi.controller.utility.DeviceUtilities;
import com.jjkeller.kmbui.R;

/**
 * Created by cbrisenoitx on 05/05/2017.<br><br>
 *
 * A Dialog fragment class which shows a title, a message and buttons for cancel and accept actions.
 * <br>You must call its static method {@link CancelAcceptDialogFragment#newInstance(int, String, int, int, int) newInstance} in order to get a correct instance of this class.
 * <br>It also defines an interface for listening both cancel and accept actions, {@link CancelAcceptDialogFragment.OnDialogActionClickedListener}
 *
 */
public class CancelAcceptDialogFragment extends DialogFragment implements View.OnClickListener{

    private static final String EXTRA_TITLE = "EXTRA_TITLE";
    private static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";
    private static final String EXTRA_NEGATIVE_TEXT_ACTION = "EXTRA_NEGATIVE_TEXT_ACTION";
    private static final String EXTRA_POSITIVE_TEXT_ACTION = "EXTRA_POSITIVE_TEXT_ACTION";
    private static final String EXTRA_ACCENT_COLOR = "EXTRA_ACCENT_COLOR";

    private static final int DP10 = (int) DeviceUtilities.ConvertDpToPixel(10);
    private static final int DP1 = (int) DeviceUtilities.ConvertDpToPixel(1);

    private static int screenWidth, screenHeight;
    private OnDialogActionClickedListener onDialogActionClickedListener;

    /**
     * Static method for getting a valid instance of this Dialog fragment.
     * @param title The string resource which will be used as text title.
     * @param message The string resource which will be used as text message.
     * @param accentColor Accent color which will be used for the title's bottom border, cancel and accept text color.
     * @return A valid instance of this Dialog fragment.
     */
    public static CancelAcceptDialogFragment newInstance(@StringRes int title, @NonNull String message, @StringRes int negativeTextAction, @StringRes int positiveTextAction, @ColorRes int accentColor){
        CancelAcceptDialogFragment fragment = new CancelAcceptDialogFragment();
        Bundle bundle = new Bundle();

        bundle.putInt(EXTRA_TITLE, title);
        bundle.putString(EXTRA_MESSAGE, message);
        bundle.putInt(EXTRA_NEGATIVE_TEXT_ACTION, negativeTextAction);
        bundle.putInt(EXTRA_POSITIVE_TEXT_ACTION, positiveTextAction);
        bundle.putInt(EXTRA_ACCENT_COLOR, accentColor);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Specify a theme which does not have actionbar so it does not show an extra bar component on its UI
        int theme = android.R.style.Theme_Holo_Dialog_NoActionBar;

        setStyle(DialogFragment.STYLE_NORMAL, theme);
        setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        verifyArguments();

        View vLayout = inflater.inflate(R.layout.dialog_cancel_accept, container, false);
        View vSeparator = vLayout.findViewById(R.id.vSeparator);
        TextView tvTitle = (TextView)vLayout.findViewById(R.id.tvTitle);
        TextView tvMessage = (TextView)vLayout.findViewById(R.id.tvMessage);
        Button btCancel = (Button)vLayout.findViewById(R.id.btCancel);
        Button btAccept = (Button)vLayout.findViewById(R.id.btAccept);
        int colorAccent = getResources().getColor(getExtraColorAccent());

        //Get screen metrics once or when there is a configuration change
        if (screenWidth == 0 || savedInstanceState != null) {
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            //Get the correct screen dimensions, width and height values depend on device density, that said these values might be inverted when runnning it on tablets
            if (size.x < size.y) {
                //Phone device
                screenWidth = size.x;
                screenHeight = size.y;
            }else{
                //Tablet device (inverted values because natural use of tablets is landscape mode)
                screenHeight = size.x;
                screenWidth = size.y;
            }
        }

        //Obtain the text title's width at runtime so it can draw a bottom border a little wider than it.
        tvTitle.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                View vSeparator = getView().findViewById(R.id.vSeparator);
                TextView tvTitle = (TextView)getView().findViewById(R.id.tvTitle);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(tvTitle.getWidth(), DP1);

                layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                vSeparator.setLayoutParams(layoutParams);
                vSeparator.setPadding(DP10, 0, DP10, 0);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    tvTitle.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }else{
                    //noinspection deprecation
                    tvTitle.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

        vSeparator.setBackgroundColor(colorAccent);
        btCancel.setTextColor(colorAccent);
        btAccept.setTextColor(colorAccent);

        tvTitle.setText(getExtraTitle());
        tvMessage.setText(getExtraMessage());
        btCancel.setText(getExtraNegativeTextAction());
        btAccept.setText(getExtraPositiveTextAction());

        btAccept.setOnClickListener(this);
        btCancel.setOnClickListener(this);

        resizeScrollView(vLayout);

        return vLayout;
    }

    @Override
    public void onStart() {
        super.onStart();

        resizeDialogWindow();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        resizeScrollView(getView());
        resizeDialogWindow();
    }

    @StringRes
    private int getExtraTitle(){
        return getArguments().getInt(EXTRA_TITLE);
    }

    private String getExtraMessage(){
        return getArguments().getString(EXTRA_MESSAGE);
    }

    @StringRes
    private int getExtraNegativeTextAction(){
        return getArguments().getInt(EXTRA_NEGATIVE_TEXT_ACTION);
    }

    @StringRes
    private int getExtraPositiveTextAction(){
        return getArguments().getInt(EXTRA_POSITIVE_TEXT_ACTION);
    }

    @ColorRes
    private int getExtraColorAccent(){
        return getArguments().getInt(EXTRA_ACCENT_COLOR);
    }

    private void verifyArguments(){
        if (getArguments() == null || !getArguments().containsKey(EXTRA_TITLE)){
            throw new IllegalArgumentException(String.format("Class %s should be instantiated using its static method 'newInstance'", CancelAcceptDialogFragment.class.getSimpleName()));
        }
    }

    /**
     * Update the scrollview width and height based on device orientation
     * @param layout The scrollview parent layout where it can call its {@link View#findViewById(int), findViewById} method
     */
    private void resizeScrollView(View layout){
        View svText = layout.findViewById(R.id.svText);
        float percentageHeightBasedOnOrientation = (getResources().getInteger(R.integer.dialog_relative_percentage_height)/100f);
        boolean isPortrait = DeviceInfo.GetDeviceOrientation(getActivity().getApplicationContext()) == Configuration.ORIENTATION_PORTRAIT;
        int screenHeightBasedOnOrientation = isPortrait?screenWidth:screenHeight;

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (screenHeightBasedOnOrientation*percentageHeightBasedOnOrientation));
        layoutParams.topMargin = DP10;
        layoutParams.bottomMargin = DP10;
        layoutParams.weight = 1;

        svText.setLayoutParams(layoutParams);
    }

    /**
     * Update the dialog's window width and height based on device orientation
     */
    private void resizeDialogWindow(){
        boolean isPortrait = DeviceInfo.GetDeviceOrientation(getActivity().getApplicationContext()) == Configuration.ORIENTATION_PORTRAIT;
        float percentageWidthBasedOnOrientation = (getResources().getInteger(R.integer.dialog_relative_percentage_width)/100f);
        int screenWidthBasedOnOrientation = isPortrait?screenWidth:screenHeight;

        if(getDialog().getWindow() != null){
            getDialog().getWindow().setLayout((int) (screenWidthBasedOnOrientation*percentageWidthBasedOnOrientation), WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    public void setOnDialogActionClickedListener(OnDialogActionClickedListener onDialogActionClickedListener) {
        this.onDialogActionClickedListener = onDialogActionClickedListener;
    }

    @Override
    public void onClick(View view) {
        if (onDialogActionClickedListener != null) {
            if (view.getId() == R.id.btCancel){
                onDialogActionClickedListener.onDialogCancelClick();
            }else if (view.getId() == R.id.btAccept){
                onDialogActionClickedListener.onDialogAcceptClick();
            }
        }
    }

    public interface OnDialogActionClickedListener {
        void onDialogCancelClick();
        void onDialogAcceptClick();
    }

}
