package com.tecnologiasmoviles.iua.fitmusic.utils.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;

import com.tecnologiasmoviles.iua.fitmusic.utils.SharedPrefsManager;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

public class DialogRaceNotRegistered implements IGenericDialog {

    private static DialogRaceNotRegistered instance;
    private static Dialog dialog;

    private DialogRaceNotRegistered() {}

    public static DialogRaceNotRegistered getInstance() {
        if (instance == null) {
            instance = new DialogRaceNotRegistered();
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
                .setPositiveButton("Volver", (dialog, id) -> positiveButtonAction(context, dialog));

        dialog = builder.create();
        return dialog;
    }

    private void positiveButtonAction(Context context, DialogInterface dialog) {
//        finishRace();
        // Reset all race SharedPrefsKeys
        SharedPrefsManager.initRaceSharedPrefsKeys(context);
        dialog.dismiss();
    }

}