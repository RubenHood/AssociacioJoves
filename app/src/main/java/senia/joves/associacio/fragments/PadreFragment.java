package senia.joves.associacio.fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import senia.joves.associacio.R;
import senia.joves.associacio.fragments.error.SinConexionFragment;

/**
 * Created by Usuario on 24/05/2017.
 *
 * Este fragment está creado, para que al iniciarse compruebe si hay internet.
 * Si no hay internet, te manda al fragment de error
 */

public class PadreFragment extends Fragment {

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //comprobamos si hay internet, para lanzar la aplicación o no.
        if (!isNetDisponible() || !isOnlineNet()) {
            getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                            R.anim.enter_from_left, R.anim.exit_to_right)
                    .replace(R.id.contenido, new SinConexionFragment()).commit();
        }

    }

    private boolean isNetDisponible() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo actNetInfo = connectivityManager.getActiveNetworkInfo();

        return (actNetInfo != null && actNetInfo.isConnected());
    }

    public Boolean isOnlineNet() {

        try {
            Process p = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.es");

            int val = p.waitFor();
            boolean reachable = (val == 0);
            return reachable;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
