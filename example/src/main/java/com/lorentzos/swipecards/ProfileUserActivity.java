package com.lorentzos.swipecards;

import android.app.Activity;
import android.content.Context;
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
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ProfileUserActivity extends Activity {

    private ArrayList<Integer> scaryList;
    private ArrayList<String> stringList;
    private ArrayAdapter<String> arrayAdapter;
    private int i;

    @InjectView(R.id.frame) SwipeFlingAdapterView flingContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        ButterKnife.inject(this);

        // TODO in the login activity:
        // see if there exists a user profile with this user
        // if there isn't, make a new user profile and push that to firebase
        // if there is, pull that user profile
        scaryList = new ArrayList<Integer>();
        stringList = new ArrayList<String>();

        // from the user's responses on the particular activities, compute personal threshold
        arrayAdapter = new ArrayAdapter<>(ProfileUserActivity.this, R.layout.item, R.id.helloText, stringList);
        flingContainer.setAdapter(arrayAdapter);


        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                stringList.remove(0);
                arrayAdapter.notifyDataSetChanged();
                if (stringList.size() == 0){
                    // TODO Push personal threshold back to firebase when done
                    // TODO go to new activity
                }
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject
                makeToast(ProfileUserActivity.this, "Left!");
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                makeToast(ProfileUserActivity.this, "Right!");
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                // Ask for more data here if you want more when the thing is empty
                //stringList.add("XML ".concat(String.valueOf(i)));
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
                makeToast(ProfileUserActivity.this, "Clicked!");
            }
        });

        Firebase.setAndroidContext(this);
        Firebase firebaseRef = new Firebase("https://babystep.firebaseio.com/");

        // pull activities from firebase and add them to an arraylist
        Firebase scoresRef = firebaseRef.child("activities");
        scoresRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //Log.d("Snapshot", ""+snapshot.getValue());
                for(Object activity : ((Map<Object, Object>) snapshot.getValue()).values()) {
                    stringList.add(""+((Map<Object, Object>)activity).get("name"));
                    scaryList.add(Integer.parseInt(""+((Map<Object, Object>)activity).get("scariness")));
                    //Log.d("Activity", "Activity name: "+((Map<Object, Object>)activity).get("name"));
                    //Log.d("Activity", "Activity scariness: "+((Map<Object, Object>)activity).get("scariness"));
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

    @OnClick(R.id.right)
    public void right() {
        /**
         * Trigger the right event manually.
         */
        flingContainer.getTopCardListener().selectRight();
    }

    @OnClick(R.id.left)
    public void left() {
        flingContainer.getTopCardListener().selectLeft();
    }

}
