package com.lorentzos.swipecards;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends Activity {

    private EditText username;
    private EditText password;
    private Firebase firebaseRef;
    Map<String, Object> users;
    private Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Firebase.setAndroidContext(this);
        setupVariables();
    }

    public void authenticateLogin(View view) {
        String usernameString = username.getText().toString();
        String passwordString = password.getText().toString();

        if (users.containsKey(usernameString)) {
            // check password
            if (((Map<String, Object>) users.get(usernameString)).get("password").
                    equals(passwordString)) {
                Toast.makeText(getApplicationContext(), "Welcome, " + usernameString,
                        Toast.LENGTH_SHORT).show();

                if ((Long) ((Map<String, Object>) users.get(usernameString)).get("threshold") == -1) {
                    // First time, profile the user
                    Intent i = new Intent(this, ProfileUserActivity.class);
                    i.putExtra("username", usernameString);
                    startActivity(i);
                } else {
                    Log.d("LoginActivity", "Not first time, go to Dashboard");
                    // Not first time, go right to the action
                    //TODO Intent i = new Intent(this, DashboardActivity.class);
//                    i.putExtra("username", usernameString);
//                    startActivity(i);
                }
            } else {
                Toast.makeText(getApplicationContext(), "Invalid password",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // not in there, add
            Map<String, Object> newUser = new HashMap<String, Object>();
            newUser.put("password", passwordString);
            newUser.put("threshold", -1);
            firebaseRef.child("users").child(usernameString).setValue(newUser);
            Toast.makeText(getApplicationContext(), "New account created!",
                    Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, ProfileUserActivity.class);
            i.putExtra("username", usernameString);
            startActivity(i);
        }
    }

    private void setupVariables() {
        username = (EditText) findViewById(R.id.usernameET);
        password = (EditText) findViewById(R.id.passwordET);
        login = (Button) findViewById(R.id.loginBtn);
        firebaseRef = new Firebase("https://babystep.firebaseio.com");

        firebaseRef.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                users = (Map<String, Object>) snapshot.getValue();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }
}