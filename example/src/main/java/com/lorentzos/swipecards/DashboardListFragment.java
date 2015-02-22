package com.lorentzos.swipecards;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by arkadyark on 15-02-21.
 */
public class DashboardListFragment extends Fragment {

    int pageNumber;
    String username;
    ArrayList<String> activities;
    ArrayAdapter<String> activityAdapter;

    public static DashboardListFragment newInstance(int pageNumber, String username) {

        DashboardListFragment f = new DashboardListFragment();
        f.pageNumber = pageNumber;
        f.username = username;
        f.activities = new ArrayList<>();
        return f;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        activityAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.dashboard_item, R.id.dashboard_item_text_view, activities);

        // Get a reference to our posts
        Firebase ref = new Firebase("https://babystep.firebaseio.com");
        // Attach an listener to read the data at our posts reference

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long threshold = (Long) ((Map<String, Map<String, Map<String, Object>>>) dataSnapshot.getValue()).
                        get("users").get(username).get("threshold");
                for (String activityID : ((Map<String, Map<String, Object>>) dataSnapshot.getValue()).get("activities").keySet()) {
                    Map<String, Object> activity = ((Map<String, Map<String, Map<String, Object>>>) dataSnapshot.getValue()).
                            get("activities").get(activityID);
                    Long activityScore = (Long) activity.get("scariness");
                    String activityName = (String) activity.get("name");
                    if (pageNumber == 0) {
                        if (threshold > activityScore) {
                            activities.add(activityName);
                        }
                    } else if (pageNumber == 1) {
                        if ((threshold + 10) > activityScore && threshold < activityScore) {
                            activities.add(activityName);
                        }
                    } else {
                        if ((threshold + 10) < activityScore) {
                            activities.add(activityName);
                        }
                    }
                }
                activityAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard_list, container, false);
        ((ListView) root.findViewById(R.id.listView)).setAdapter(activityAdapter);
        return root;
    }
}
