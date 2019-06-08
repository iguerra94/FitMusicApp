package com.tecnologiasmoviles.iua.fitmusic.utils.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;


import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

public class DialogRegisterRaceQuestion implements IGenericDialog {

    private static DialogRegisterRaceQuestion instance;
    private static Dialog dialog;

    private DialogRegisterRaceQuestion() {}

    public static DialogRegisterRaceQuestion getInstance() {
        if (instance == null) {
            instance = new DialogRegisterRaceQuestion();
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
//        long raceCurrentDistance = SharedPrefsManager.getInstance(context).readLong(SharedPrefsKeys.RACE_CURRENT_DISTANCE_KEY);
//
//        if (raceCurrentDistance >= 500) {
            //new RegisterRaceAsyncTask(context).execute();
//        } else {
//            dialogRaceNotRegistered = (AlertDialog) createDialogRaceNotRegistered();
//            dialogRaceNotRegistered.setCancelable(false);
//            dialogRaceNotRegistered.show();
//        }
    }

    private void negativeButtonAction(DialogInterface dialog) {
        dialog.cancel();
    }

}
