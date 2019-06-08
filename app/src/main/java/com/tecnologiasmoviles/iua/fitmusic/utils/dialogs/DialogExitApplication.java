package com.tecnologiasmoviles.iua.fitmusic.utils.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;

import com.tecnologiasmoviles.iua.fitmusic.services.FitMusicForegroundService;
import com.tecnologiasmoviles.iua.fitmusic.utils.SharedPrefsKeys;
import com.tecnologiasmoviles.iua.fitmusic.utils.SharedPrefsManager;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

public class DialogExitApplication implements IGenericDialog {

    private static DialogExitApplication instance;
    private static Dialog dialog;

    private DialogExitApplication() {}

    public static DialogExitApplication getInstance() {
        if (instance == null) {
            instance = new DialogExitApplication();
        }
        return instance;
    }

    public Dialog getDialog() {
        return dialog;
    }

    @Override
    public Dialog create(Context context, int viewId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LayoutInflater inflater = ((FragmentActivity) context).getLayoutInflater();

        builder.setView(inflater.inflate(viewId, null))
                // Add action buttons
                .setPositiveButton("Si", (dialog, id) -> positiveButtonAction(context))
                .setNegativeButton("No", (dialog, id) -> negativeButtonAction(dialog));

        dialog = builder.create();
        return dialog;
    }

    private void positiveButtonAction(Context context) {
        SharedPrefsManager.getInstance(context).saveBoolean(SharedPrefsKeys.IS_RUNNING_KEY, false);
        FitMusicForegroundService.stopForegroundServiceIntent(context);
    }

    private void negativeButtonAction(DialogInterface dialog) {
        dialog.cancel();
    }

}