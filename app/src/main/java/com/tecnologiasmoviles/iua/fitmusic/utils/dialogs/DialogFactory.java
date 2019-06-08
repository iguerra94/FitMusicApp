package com.tecnologiasmoviles.iua.fitmusic.utils.dialogs;

public class DialogFactory {

    private static DialogFactory instance;

    private DialogFactory() {}

    public static DialogFactory getInstance() {
        if (instance == null) {
            instance = new DialogFactory();
        }
        return instance;
    }

    public static IGenericDialog getRegisterRaceQuestionDialog() {
        return DialogRegisterRaceQuestion.getInstance();
    }

    public static IGenericDialog getRegisterinRaceDialog() {
        return DialogRegisteringRace.getInstance();
    }

    public static IGenericDialog getLoadingMusicFromFirebaseDialog() {
        return DialogLoadingMusicFromFirebase.getInstance();
    }

    public static IGenericDialog getExitApplicationDialog() {
        return DialogExitApplication.getInstance();
    }

    public static IGenericDialog getRaceNotRegisteredDialog() {
        return DialogRaceNotRegistered.getInstance();
    }

}