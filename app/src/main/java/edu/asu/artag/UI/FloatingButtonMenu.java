package edu.asu.artag.UI;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.util.ArrayList;
import java.util.List;

import edu.asu.artag.R;

import static android.widget.Toast.LENGTH_SHORT;


public class FloatingButtonMenu extends Fragment {

    private static final String TAG = "FBM";
    private FloatingActionMenu menuRed;

    private FloatingActionButton fab1;
    private FloatingActionButton fab2;
    private FloatingActionButton fab3;
    private FloatingActionButton fab4;

    private List<FloatingActionMenu> menus = new ArrayList<>();
    private Handler mUiHandler = new Handler();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.floating_btn_fragment, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        menuRed = (FloatingActionMenu) view.findViewById(R.id.menu_red);

        fab1 = (FloatingActionButton) view.findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) view.findViewById(R.id.fab2);
        fab3 = (FloatingActionButton) view.findViewById(R.id.fab3);
        fab4 = (FloatingActionButton) view.findViewById(R.id.fab4);


        // Set OnclickListener for the four floating button
        fab1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                // Toast.makeText(getActivity(), "btn1", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(),PlaceActivity.class);
                intent.putExtra("loc_long", ((MapsActivity)getActivity()).getLongitude());
                intent.putExtra("loc_lat", ((MapsActivity)getActivity()).getLatitude());
                intent.putExtra("orient_azimuth", ((MapsActivity)getActivity()).getAzimuth());
                intent.putExtra("orient_altitude", ((MapsActivity)getActivity()).getAltitude());
                intent.putExtra("email",((MapsActivity)getActivity()).getEmail());
                startActivity(intent);
            }
        });

        // Collect Activity
        fab2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               // Toast.makeText(getActivity(), "btn2", Toast.LENGTH_SHORT).show();
               // fab2.setVisibility(View.GONE);
                Intent intent = new Intent(getActivity(),CollectActivity.class);
                intent.putExtra("email",((MapsActivity)getActivity()).getEmail());
                startActivity(intent);

            }
        });

        fab3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //fab2.setVisibility(View.VISIBLE);
               // Toast.makeText(getActivity(), "btn3", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(),GalleryActivity.class);
                startActivity(intent);
            }
        });

        fab4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //fab2.setVisibility(View.VISIBLE);
                // Toast.makeText(getActivity(), "btn3", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(),ScoreActivity.class);
                startActivity(intent);
            }
        });

        final FloatingActionButton programFab1 = new FloatingActionButton(getActivity());

        // Set OnClickListener for the whole Floating Button Group
        programFab1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                programFab1.setLabelColors(ContextCompat.getColor(getActivity(), R.color.grey),
                        ContextCompat.getColor(getActivity(), R.color.light_grey),
                        ContextCompat.getColor(getActivity(), R.color.white_transparent));
                programFab1.setLabelTextColor(ContextCompat.getColor(getActivity(), R.color.black));
            }
        });

        ContextThemeWrapper context = new ContextThemeWrapper(getActivity(), R.style.MenuButtonsStyle);


        menuRed.setClosedOnTouchOutside(true);

        menuRed.hideMenuButton(false);


    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        menus.add(menuRed);

        int delay = 400;
        for (final FloatingActionMenu menu : menus) {
            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    menu.showMenuButton(true);
                }
            }, delay);
            delay += 150;
        }


        menuRed.setOnMenuButtonClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (menuRed.isOpened()) {
                    // Toast.makeText(getActivity(), menuRed.getMenuButtonLabelText(), LENGTH_SHORT).show();
                }

                menuRed.toggle(true);
            }
        });
    }
}