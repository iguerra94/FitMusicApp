package com.tecnologiasmoviles.iua.fitmusic.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.tecnologiasmoviles.iua.fitmusic.R;
import com.tecnologiasmoviles.iua.fitmusic.model.Carrera;
import com.tecnologiasmoviles.iua.fitmusic.utils.RacesAdapter;
import com.tecnologiasmoviles.iua.fitmusic.utils.RacesJSONParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class RacesListFragment extends Fragment {

    private final String LOG_TAG = this.getClass().getSimpleName();

    LinearLayout linearLayoutNoRaces;
    ListView listViewRaces;

    public RacesListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_races_list, container, false);

        AppCompatActivity containerActivity = (AppCompatActivity) getActivity();

        assert containerActivity != null;
        assert containerActivity.getSupportActionBar() != null;

        containerActivity.getSupportActionBar().setTitle(getResources().getString(R.string.toolbar_title_races_list));

        linearLayoutNoRaces = view.findViewById(R.id.llRacesListFragmentNoRaces);
        listViewRaces = view.findViewById(R.id.lVRaces);

        setearAdaptadorListView(listViewRaces);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void setearAdaptadorListView(ListView listViewRaces) {
        try {
            File file = new File(getActivity().getFilesDir(), "races_data.json");
            InputStream stream = new FileInputStream(file);

            List<Carrera> carreras = RacesJSONParser.getRacesJSONStream(stream);

            if (carreras.size() > 0) {
                linearLayoutNoRaces.setVisibility(View.GONE);
                listViewRaces.setVisibility(View.VISIBLE);
                RacesAdapter adapter = new RacesAdapter(getActivity(), carreras);
                listViewRaces.setAdapter(adapter);
                Log.d(LOG_TAG, "ARRAY DE CARRERAS CORRECTO");
            } else {
                linearLayoutNoRaces.setVisibility(View.VISIBLE);
                listViewRaces.setVisibility(View.GONE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}