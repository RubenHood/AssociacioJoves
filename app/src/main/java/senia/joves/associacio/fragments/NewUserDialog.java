package senia.joves.associacio.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import senia.joves.associacio.R;

/**
 * Created by Ruben on 08/05/2017.
 */

public class NewUserDialog extends Fragment {

    public NewUserDialog(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.nuevo_socio_fragment, container, false);
    }
}
