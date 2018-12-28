package com.wangdaye.mysplash.common.ui.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.wangdaye.mysplash.R;
import com.wangdaye.mysplash.common.basic.fragment.MysplashDialogFragment;
import com.wangdaye.mysplash.common.utils.DisplayUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Path dialog.
 *
 * This dialog is used to show the download path.
 *
 * */

public class PathDialog extends MysplashDialogFragment {

    @BindView(R.id.dialog_path_container)
    CoordinatorLayout container;

    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_path, null, false);
        ButterKnife.bind(this, view);
        initWidget(view);
        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
    }

    @Override
    public CoordinatorLayout getSnackbarContainer() {
        return container;
    }

    private void initWidget(View v) {
        TextView content = ButterKnife.findById(v, R.id.dialog_path_text);
        DisplayUtils.setTypeface(getActivity(), content);
    }

    // interface.

    @OnClick(R.id.dialog_path_copyBtn) void copy() {
        ((ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE))
                .setPrimaryClip(
                        ClipData.newPlainText(
                                "storage/emulated/0/Pictures/Mysplash",
                                "storage/emulated/0/Pictures/Mysplash"));
    }

    @OnClick(R.id.dialog_path_enterBtn) void enter() {
        dismiss();
    }
}
