package com.lorentzos.swipecards;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by arkadyark on 15-02-21.
 */
public class DashboardListFragment extends Fragment {

    int pageNumber;
    String username;
    ArrayList<String> activities;
    // hacky, to make other fragment adapter work (with button on the side)
    ArrayList<HashMap<String, String>> learningActivities;
    HashMap<String, Long> scarinessMap;
    ArrayAdapter<String> activityAdapter;
    SimpleAdapter simpleAdapter;

    public static DashboardListFragment newInstance(int pageNumber, String username) {

        DashboardListFragment f = new DashboardListFragment();
        f.pageNumber = pageNumber;
        f.username = username;
        return f;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Reload username if needed
        if(savedInstanceState != null) {
            username = savedInstanceState.getString("username");
        }

        activities = new ArrayList<>();
        learningActivities = new ArrayList<HashMap<String, String>>();
        scarinessMap = new HashMap<>();
        activityAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.dashboard_item, R.id.dashboard_item_text_view, activities);

        simpleAdapter = new SimpleAdapter(getActivity(),
                learningActivities, R.layout.learning_activity_item,
                new String[] {"name", "dumb"}, new int[] {R.id.textView, R.id.deleteButton});

        simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view.getId() == R.id.deleteButton) {
                    Button b = (Button) view;
                    b.setOnClickListener(new View.OnClickListener() {
                        String textViewValue;
                        @Override
                        public void onClick(View v) {
                            for(int i=0; i<((ViewGroup)v.getParent()).getChildCount(); ++i) {
                                View nextChild = ((ViewGroup) v.getParent()).getChildAt(i);
                                if (nextChild.getId() == R.id.textView) {
                                    textViewValue = ((TextView) nextChild).getText().toString();
                                }
                            }
                            Long newThreshold = scarinessMap.get(textViewValue);
                            Log.d("newThreshold", ""+newThreshold);
                            Firebase userRef = new Firebase("https://babystep.firebaseio.com/users/");
                            Map<String, Object> newThresholdMap = new HashMap<String, Object>();
                            newThresholdMap.put("threshold", newThreshold);
                            userRef.child(username).updateChildren(newThresholdMap);
                        }
                    });
                    return true;
                }
                return false;
            }
        });

        // Get a reference to our posts
        Firebase ref = new Firebase("https://babystep.firebaseio.com");
        // Attach an listener to read the data at our posts reference

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                activities.clear();
                learningActivities.clear();
                Long threshold = (Long) ((Map<String, Map<String, Map<String, Object>>>) dataSnapshot.getValue()).
                        get("users").get(username).get("threshold");
                for (String activityID : ((Map<String, Map<String, Object>>) dataSnapshot.getValue()).get("activities").keySet()) {
                    Map<String, Object> activity = ((Map<String, Map<String, Map<String, Object>>>) dataSnapshot.getValue()).
                            get("activities").get(activityID);
                    Long activityScore = (Long) activity.get("scariness");
                    scarinessMap.put((String) activity.get("name"), activityScore);
                    String activityName = (String) activity.get("name");
                    if (pageNumber == 0) {
                        if (threshold >= activityScore) {
                            activities.add(activityName);
                        }
                    } else if (pageNumber == 1) {
                        if ((threshold + 10) > activityScore && threshold < activityScore) {
                            HashMap<String, String> newActivity = new HashMap<String, String>();
                            newActivity.put("name", activityName);
                            newActivity.put("dumb", "");
                            learningActivities.add(newActivity);
                        }
                    } else {
                        if ((threshold + 10) <= activityScore) {
                            activities.add(activityName);
                        }
                    }
                }
                simpleAdapter.notifyDataSetChanged();
                activityAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("username", username);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard_list, container, false);
        String explanation;
        if (pageNumber == 0) {
            explanation = "These are things you're comfortable with.";
        } else if (pageNumber == 1) {
            explanation = "These are things just a babystep outside of your comfort zone. Try them!";
        } else {
            explanation = "These are things way outside of your comfort zone, to aspire to.";
        }
        ((TextView) root.findViewById(R.id.activitySectionDescription)).setText(explanation);

        if (pageNumber == 1) {
            ((ListView) root.findViewById(R.id.listView)).setAdapter(simpleAdapter);
        } else {
            ((ListView) root.findViewById(R.id.listView)).setAdapter(activityAdapter);
        }
        return root;
    }
}
