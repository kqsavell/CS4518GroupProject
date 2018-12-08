package wpi.jhyuen.deeplearningproject;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class FragmentHeader extends Fragment
{

    private MainActivity parent = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View view = inflater.inflate(R.layout.fragment_header, container, false);

        // Get parent activity
        parent = (MainActivity)getActivity();

        ImageButton emailBtn = (ImageButton)view.findViewById(R.id.driveButton);
        emailBtn.setOnClickListener(parent.emailListener);

        return view;

    }

}
