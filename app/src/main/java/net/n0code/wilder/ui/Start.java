package net.n0code.wilder.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import net.n0code.wilder.R;
import net.n0code.wilder.db.LocalDB;
import net.n0code.wilder.obj.User;
import net.n0code.wilder.ui.login.CreateAccount;
import net.n0code.wilder.ui.login.Login;

public class Start extends Activity {

    private String TAG = "***Start***";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this,getString(R.string.wilderSharedPreferences),
                MODE_PRIVATE, R.xml.preferences, false);

        LocalDB.openDB(this);

        boolean usersExist = LocalDB.usersExist();
        Log.d("Start", "usersExist: " + usersExist);

        if (!usersExist) {
            Log.d("Start", "starting create account");
            startActivity(new Intent(this, CreateAccount.class));
            finish();
            return;
        }

        SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.wilderSharedPreferences), Context.MODE_PRIVATE);

        if (!sharedPref.getBoolean(getString(R.string.prefStayLoggedInKey), false)) {
            startActivity(new Intent(this, Login.class));
        }
        else {
            String email = sharedPref.getString(getString(R.string.prefCurrentUserEmailKey), "unknown");
            User user = LocalDB.getUser(email);

            if (user == null) {
                Log.d(TAG, "Oops, no user found");
                startActivity(new Intent(this, Login.class));
            }
            else {
                startActivity(new Intent(this, TripList.class));
            }
        }

        finish();
    }


}
