package com.tecnologiasmoviles.iua.fitmusic.utils.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

public class DialogRegisteringRace implements IGenericDialog {

    private static DialogRegisteringRace instance;
    private static Dialog dialog;

    private DialogRegisteringRace() {
    }

    public static DialogRegisteringRace getInstance() {
        if (instance == null) {
            instance = new DialogRegisteringRace();
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

        builder.setView(inflater.inflate(viewId, null));

        dialog = builder.create();
        return dialog;
    }

}