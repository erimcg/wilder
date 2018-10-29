package net.n0code.wilder.ui;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;

import net.n0code.wilder.R;

import java.util.ArrayList;


public class POITypePickerDialog extends DialogFragment implements View.OnClickListener {

    String TAG = "*****POITypePicker*****";

    POITypePickerDialogListener mListener;
    ImageButton selectedButton = null;
    Drawable background = null;

    public POITypePickerDialog() {}

    public interface POITypePickerDialogListener {
        public void onSelectClick(DialogFragment dialog, String imageFileName);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            mListener = (POITypePickerDialogListener) getActivity();
            Log.d(TAG, "Activity listener attached");
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement POITypePickerDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View rootView = inflater.inflate(R.layout.poi_type_picker, null);

        ArrayList<View> children = getAllChildren(rootView);
        for (View v: children) {
            if (v instanceof ImageButton) {
                v.setOnClickListener(this);
            }
        }

        builder.setView(rootView)
                // Add action buttons
                .setPositiveButton("Select", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (selectedButton != null) {
                            String type = selectedButton.getContentDescription().toString();
                            mListener.onSelectClick(POITypePickerDialog.this, type);
                        }
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        POITypePickerDialog.this.getDialog().cancel();
                    }
                });

        return builder.create();
    }

    private ArrayList<View> getAllChildren(View v) {
        if (!(v instanceof ViewGroup)) {
            ArrayList<View> viewArrayList = new ArrayList<View>();
            viewArrayList.add(v);
            return viewArrayList;
        }

        ArrayList<View> result = new ArrayList<View>();

        ViewGroup viewGroup = (ViewGroup) v;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {

            View child = viewGroup.getChildAt(i);

            ArrayList<View> viewArrayList = new ArrayList<View>();
            viewArrayList.add(v);
            viewArrayList.addAll(getAllChildren(child));

            result.addAll(viewArrayList);
        }
        return result;
    }

    public void onClick(View v)
    {
        Log.d(TAG, "Button clicked");

        if (!(v instanceof ImageButton)) {
            return;
        }

        if (selectedButton == null) {
            background = v.getBackground();
        }
        else {
            selectedButton.setBackground(background);
        }

        selectedButton = (ImageButton) v;
        selectedButton.setBackground(getResources().getDrawable(R.drawable.round_corner_dark));

        String contentDesc = v.getContentDescription().toString();
        if (!TextUtils.isEmpty(contentDesc))
        {
            int[] pos = new int[2];
            Activity host = (Activity) getContext();
            v.getLocationInWindow(pos);

            int offsetY = getResources().getDimensionPixelSize(R.dimen.toast_offset_y);
            final Toast t = Toast.makeText(getContext(), contentDesc, Toast.LENGTH_SHORT);

            positionToast(t, v, host.getWindow(), 0, offsetY);
            t.show();

            // Set the timer to display the toast for only 0.5 second
            CountDownTimer toastTimer = new CountDownTimer(500, 500) {
                public void onTick(long millisUntilFinished) {
                    t.cancel();
                }
                public void onFinish() {
                    t.cancel();
                }
            };

            t.show();
            toastTimer.start();
        }
    }

    public static void positionToast(Toast toast, View view, Window window, int offsetX, int offsetY) {
        Rect rect = new Rect();
        window.getDecorView().getWindowVisibleDisplayFrame(rect);

        // covert anchor view absolute position to a position which is relative to decor view
        int[] viewLocation = new int[2];
        view.getLocationOnScreen(viewLocation);
        int viewLeft = viewLocation[0] - rect.left;
        int viewTop = viewLocation[1] - rect.top;

        // measure toast to center it relatively to the anchor view
        DisplayMetrics metrics = new DisplayMetrics();
        window.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(metrics.widthPixels, View.MeasureSpec.UNSPECIFIED);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(metrics.heightPixels, View.MeasureSpec.UNSPECIFIED);
        toast.getView().measure(widthMeasureSpec, heightMeasureSpec);
        int toastWidth = toast.getView().getMeasuredWidth();

        // compute toast offsets
        int toastX = viewLeft + (view.getWidth() - toastWidth) / 2 + offsetX;
        int toastY = viewTop - view.getHeight() + offsetY;

        toast.setGravity(Gravity.LEFT | Gravity.TOP, toastX, toastY);
    }

}
