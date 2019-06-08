package com.tecnologiasmoviles.iua.fitmusic.utils.dialogs;

import android.app.Dialog;
import android.content.Context;

public interface IGenericDialog {
    Dialog create(Context context, int viewId);
    Dialog getDialog();
}