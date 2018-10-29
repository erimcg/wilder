package net.n0code.wilder.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import net.n0code.wilder.R;
import net.n0code.wilder.db.LocalDB;
import net.n0code.wilder.obj.InvalidConstructionException;
import net.n0code.wilder.obj.User;
import net.n0code.wilder.ui.TripList;

public class CreateAccount extends AppCompatActivity {

    private String TAG = "***CREATE-ACCOUNT***";
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_create);

        sharedPref = getSharedPreferences(
                getString(R.string.wilderSharedPreferences), Context.MODE_PRIVATE);

        CheckBox cb = (CheckBox) findViewById(R.id.stayLoggedInCheckBox);
        cb.setChecked(sharedPref.getBoolean(getString(R.string.prefStayLoggedInKey), false));

        LocalDB.openDB(this);
    }

    public void createAccountButtonHandler(View v) {
        EditText userNameET = (EditText) findViewById(R.id.usernameEditText);
        String username = userNameET.getText().toString();

        EditText passwordET = (EditText) findViewById(R.id.passwordEditText);
        String password = passwordET.getText().toString();

        EditText emailET = (EditText) findViewById(R.id.emailEditText);
        String email = emailET.getText().toString();

        if (LocalDB.emailExists(email)) {
            emailET.setError("Email address already exists");
            emailET.requestFocus();
            return;
        }

        User user;
        try {
            user = new User(username, password, email);
        }
        catch (InvalidConstructionException e) {
            String error = e.toString();
            switch (error) {
                case User.INVALID_USERNAME:
                    userNameET.setError(User.INVALID_USERNAME_MSSG);
                    break;
                case User.INVALID_PASSWORD:
                    passwordET.setError(User.INVALID_PASSWORD_MSSG);
                    break;
                case User.INVALID_EMAIL:
                    emailET.setError(User.INVALID_EMAIL_MSSG);
            }
            return;
        }

        int code = LocalDB.addUser(user);

        if (code == LocalDB.SUCCESS) {
            sharedPref.edit().putString(getString(R.string.prefCurrentUserEmailKey), user.getEmail()).apply();

            startActivity(new Intent(this, TripList.class));
            finish();
            return;
        }

        // TODO: Log this error message and display in SnackBar
        Log.d(TAG, "Error getting newly created user from db");
    }

    public void stayLoggedInCheckboxHandler(View v) {
        CheckBox cb = (CheckBox) findViewById(R.id.stayLoggedInCheckBox);
        sharedPref.edit().putBoolean(getString(R.string.prefStayLoggedInKey), cb.isChecked()).apply();
    }

}
