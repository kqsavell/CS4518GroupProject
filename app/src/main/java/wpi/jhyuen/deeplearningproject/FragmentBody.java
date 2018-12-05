package wpi.jhyuen.deeplearningproject;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

public class FragmentBody extends Fragment {

    // Fragment Variables
    private MainActivity parent = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_body, container, false);

        // Get parent activity
        parent = (MainActivity)getActivity();

        // Hook up event handlers
        ImageButton photoBtn = (ImageButton)view.findViewById(R.id.cameraBtn);
        photoBtn.setOnClickListener(parent.fromCameraListener);

        return view;
    }
}
