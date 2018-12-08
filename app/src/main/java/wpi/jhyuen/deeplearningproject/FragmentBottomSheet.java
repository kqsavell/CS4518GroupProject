package wpi.jhyuen.deeplearningproject;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class FragmentBottomSheet extends Fragment {

    // Fragment Variables
    int inferenceStatus = 0;
    private MainActivity parent = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_bottomsheet, container, false);

        // Get parent activity
        parent = (MainActivity)getActivity();

        // get bottom sheet view
        final ConstraintLayout mBottomSheet = (ConstraintLayout) view.findViewById(R.id.bottomsheet);
        Switch mSwitch = (Switch) view.findViewById(R.id.inferenceswitch);
        final ImageButton mButton = (ImageButton) view.findViewById(R.id.imageButton3);
        final TextView mInferenceText = (TextView) view.findViewById(R.id.inferenceText);


        // Get parent activity
        parent = (MainActivity)getActivity();

        // Hook up event handlers
        ImageButton photoBtn = (ImageButton)view.findViewById(R.id.cameraButton);
        photoBtn.setOnClickListener(parent.fromCameraListener);
        ImageButton galleryBtn = (ImageButton)view.findViewById(R.id.galleryButton);
        galleryBtn.setOnClickListener(parent.fromPhotosListener);
        ImageButton emailBtn = (ImageButton)view.findViewById(R.id.driveButton);
        emailBtn.setOnClickListener(parent.emailListener);

        // init bottom sheet behavior from mBottomSheet object
        final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        parent.bsBehavior = bottomSheetBehavior;

        // change state of bottom sheet
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }

            }
        });

        mSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(inferenceStatus == 1) {
                    inferenceStatus = 0;
                    parent.useOnDevice = false;
                    mInferenceText.setText("Off-Device");
                } else if(inferenceStatus == 0){
                    inferenceStatus = 1;
                    parent.useOnDevice = true;
                    mInferenceText.setText("On-Device");
                }

            }
        });

        // here are some overrides for reference if you choose to use BottomSheetBehavior in your next project
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                mButton.setRotation(slideOffset * 180);
            }
        });

        return view;

    }
}
