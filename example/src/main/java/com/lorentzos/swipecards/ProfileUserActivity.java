package com.lorentzos.swipecards;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ProfileUserActivity extends Activity {

    private ArrayList<Integer> scaryList;
    private ArrayList<Integer> swipeResults;
    private ArrayList<String> stringAdapterList;
    private ArrayList<String> nameList;
    private ArrayList<Integer> nContributorsList;
    private ArrayList<String> activityNameList;
    private ArrayAdapter<String> arrayAdapter;
    private Firebase firebaseRef;
    private String username;

    static int ACTIVITIES_TO_PROFILE = 10;

    @InjectView(R.id.frame) SwipeFlingAdapterView flingContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        username = getIntent().getExtras().getString("username");
        setContentView(R.layout.activity_my);
        ButterKnife.inject(this);

        // The indexes in these 3 lists represent the same object. I.e., index 0 in each of these lists is the same dataObject.
        scaryList = new ArrayList<Integer>();
        // stringAdapterList is the same as nameList but stringAdapterList gets deleted and nameList remains
        stringAdapterList = new ArrayList<String>();
        nameList = new ArrayList<String>();
        swipeResults = new ArrayList<Integer>();
        nContributorsList = new ArrayList<Integer>();
        activityNameList = new ArrayList<String>();

        // from the user's responses on the particular activities, compute personal threshold
        arrayAdapter = new ArrayAdapter<>(ProfileUserActivity.this, R.layout.item, R.id.helloText, stringAdapterList);
        flingContainer.setAdapter(arrayAdapter);

        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                stringAdapterList.remove(0);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject
                swipeResults.add(0);

                // the reason why we check if done here is because the card exit is the last thing that happens in a swipe
                ifDoneThenFinish();
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                swipeResults.add(1);

                // the reason why we check if done here is because the card exit is the last thing that happens in a swipe
                ifDoneThenFinish();
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                // Ask for more data here if you want more when the thing is empty
                //stringAdapterList.add("XML ".concat(String.valueOf(i)));
                //arrayAdapter.notifyDataSetChanged();
                //Log.d("LIST", "notified");
                //i++;
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
                View view = flingContainer.getSelectedView();
                view.findViewById(R.id.item_swipe_right_indicator).setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
                view.findViewById(R.id.item_swipe_left_indicator).setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
            }
        });

        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                makeToast(ProfileUserActivity.this, "Clicked: "+nameList.get(itemPosition));
            }
        });

        Firebase.setAndroidContext(this);
        firebaseRef = new Firebase("https://babystep.firebaseio.com/");

        // pull activities from firebase and add them to an arraylist
        Firebase scoresRef = firebaseRef.child("activities");
        scoresRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.d("Snapshot", ""+snapshot.getValue());

                int count = 0;
                for(Object activityKey : ((Map<Object, Object>) snapshot.getValue()).keySet()) {
                    if (count < ACTIVITIES_TO_PROFILE) {
                        Object activity = ((Map<Object, Object>) snapshot.getValue()).get(activityKey);

                        //Log.d("ProfileUserActivity", "activity name: "+activityKey);
                        activityNameList.add("" + activityKey);
                        stringAdapterList.add("" + ((Map<Object, Object>) activity).get("name"));
                        nameList.add("" + ((Map<Object, Object>) activity).get("name"));
                        scaryList.add(Integer.parseInt("" + ((Map<Object, Object>) activity).get("scariness")));
                        nContributorsList.add(Integer.parseInt("" + ((Map<Object, Object>) activity).get("nContributors")));

                        count += 1;

                        //Log.d("ProfileUserActivity", "Activity title: "+((Map<Object, Object>)activity).get("name"));
                        //Log.d("ProfileUserActivity", "Activity scariness: "+((Map<Object, Object>)activity).get("scariness"));
                    }
                }
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.d("Snapshot", "The read failed: " + firebaseError.getMessage());
            }
        });
    }

    static void makeToast(Context ctx, String s){
        Toast.makeText(ctx, s, Toast.LENGTH_SHORT).show();
    }

    public void ifDoneThenFinish(){
        if (stringAdapterList.size() == 0){
            int threshold = 0;
            int numSelected = 0;
            //Log.d("ProfileUserActivity", "\nPrinting results:");
            for(int i=0; i < nameList.size(); i++){
                if(swipeResults.get(i) == 1) {
                    threshold = threshold + scaryList.get(i);
                    numSelected++;
                }
            }

            threshold = threshold/numSelected;

            // no need to check if firebaseref is null since it has been created by now

            // Push personal threshold back to firebase when done with user data
            Map<String, Object> newThreshold = new HashMap<String, Object>();
            newThreshold.put("threshold", threshold);

            for(int i=0; i <nameList.size(); i++){
                // scariness should go down or up if you say it should
                if((swipeResults.get(i) == 1 && threshold < scaryList.get(i)) ||
                        (swipeResults.get(i) == 0 && threshold > scaryList.get(i))){
                    // Scariness only goes down because you calculated the new threshold and your new threshold is relatively low
                    // likewise scariness only goes up because you calculated the new threshold and your new threshold is relatively high
                    // We simply add the new threshold value to the running average and then divide by the new total number of contributors
                    // ex: "how would you, a person, add a number to a number that was already averaged out by 9 people? (numberToAdd + runningTotal*9) / (9+1)

                    int nC = nContributorsList.get(i);
                    // This next line is just updating the scariness. Could have also done scaryList.set(i, nC*....)
                    int newScariness = (nC*scaryList.get(i) + threshold) / (nC + 1);
                    // This next line increases the nContributors for that activity by one in firebase
                    HashMap<String, Object> newEntry = new HashMap<String, Object>();
                    newEntry.put("scariness", newScariness);
                    newEntry.put("nContributors", nC + 1);

                    Log.d("New Scariness", ""+newScariness);
                    // child has to be something else like activity1 or activity2 etc as in firebase
                    firebaseRef.child("activities").child(activityNameList.get(i)).updateChildren(newEntry);
                }
            }

            Log.d("ProfileUserActivity", "User: "+username+" has threshold: "+threshold);
            firebaseRef.child("users").child(username).updateChildren(newThreshold);

            // TODO get import for DashboardActivity and then go to new activity
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);

            // TODO test this method
        }
    }
}
