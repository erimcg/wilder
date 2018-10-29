package net.n0code.wilder.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import net.n0code.wilder.R;
import net.n0code.wilder.db.LocalDB;
import net.n0code.wilder.obj.User;
import net.n0code.wilder.ui.TripList;

public class Login extends AppCompatActivity {

    private String TAG = "LOGIN";
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_login);

        sharedPref = getSharedPreferences(
                getString(R.string.wilderSharedPreferences), Context.MODE_PRIVATE);

        CheckBox cb = (CheckBox) findViewById(R.id.stayLoggedInCheckBox);
        cb.setChecked(sharedPref.getBoolean(getString(R.string.prefStayLoggedInKey), false));

        LocalDB.openDB(this);
    }

    public void loginButtonHandler(View v) {
        EditText emailET = (EditText) findViewById(R.id.emailEditText);
        String email = emailET.getText().toString();

        EditText passwordET = (EditText) findViewById(R.id.passwordEditText);
        String password = passwordET.getText().toString();

        User user = LocalDB.getUser(email);

        if (user != null) {
            if (user.getPassword().equals(password)) {
                // Add to shared preferences so when user selects stay logged in, we have an
                // email address to use to fetch the user info from the db.
                sharedPref.edit().putString(getString(R.string.prefCurrentUserEmailKey), user.getEmail()).apply();

                startActivity(new Intent(this, TripList.class));
                finish();
            }
            else {
                passwordET.requestFocus();
                passwordET.setText("");
            }
        }
        else {
            emailET.setText("");
            passwordET.setText("");
            emailET.requestFocus();
        }
    }

    public void createAccountButtonHandler(View v) {
        startActivity(new Intent(this, CreateAccount.class));
    }

    public void stayLoggedInCheckboxHandler(View v) {
        CheckBox cb = (CheckBox) findViewById(R.id.stayLoggedInCheckBox);
        sharedPref.edit().putBoolean(getString(R.string.prefStayLoggedInKey), cb.isChecked()).apply();
    }

}
