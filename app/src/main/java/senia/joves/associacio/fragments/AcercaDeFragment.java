package senia.joves.associacio.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.regex.Pattern;

import senia.joves.associacio.R;

/**
 * Created by Usuario on 23/05/2017.
 */

public class AcercaDeFragment extends DialogFragment {

    TextView lblGit;
    TextView lblLinked;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_acercade, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //capturamos los componentes de la vista
        lblGit = (TextView) getView().findViewById(R.id.lblGit);
        lblLinked = (TextView) getView().findViewById(R.id.lblLinked);

        //asignamos una url a cada textview
        lblLinked.setText(
                Html.fromHtml(
                        "<a href=\"https://www.linkedin.com/in/rub%C3%A9n-vargas-guzman-660b418a/\">LinkedIn</a>"));
        lblLinked.setMovementMethod(LinkMovementMethod.getInstance());

        lblGit.setText(
                Html.fromHtml(
                        "<a href=\"https://github.com/RubenThaBeat\">GitHub</a>"));
        lblGit.setMovementMethod(LinkMovementMethod.getInstance());

    }
}
