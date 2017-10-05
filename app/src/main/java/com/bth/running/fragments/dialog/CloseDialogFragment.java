package com.bth.running.fragments.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.bth.running.R;

/**
 * @author Martin Macheiner
 *         Date: 03.10.2017.
 */

public class CloseDialogFragment extends DialogFragment {

    public interface OnCloseItemClickedListener {

        void onContinueRunClicked();

        void onStopRunClicked();

    }

    private OnCloseItemClickedListener listener;

    public static CloseDialogFragment newInstance() {
        CloseDialogFragment fragment = new CloseDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public CloseDialogFragment setOnCloseItemClickedListener(OnCloseItemClickedListener listener) {
        this.listener = listener;
        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getContext())
                .setIcon(R.mipmap.ic_launcher)
                .setTitle("Stop running")
                .setMessage("Do you want to continue the run in the background")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (listener != null) {
                            listener.onContinueRunClicked();
                        }
                    }
                })
                .setNegativeButton("Stop", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (listener != null) {
                            listener.onStopRunClicked();
                        }
                    }
                })
                .create();
    }
}
