package com.tecnologiasmoviles.iua.fitmusic.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseRefs {
    private static DatabaseReference racesRef = FirebaseDatabase.getInstance().getReference().child("races");

    public static DatabaseReference getRacesRef() {
        return racesRef;
    }
}
