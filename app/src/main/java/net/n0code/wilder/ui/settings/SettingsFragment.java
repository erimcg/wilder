package net.n0code.wilder.ui.settings;


import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import net.n0code.wilder.R;

public class SettingsFragment extends PreferenceFragment {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager manager = getPreferenceManager();
        manager.setSharedPreferencesName(getString(R.string.wilderSharedPreferences));

        addPreferencesFromResource(R.xml.preferences);
    }

}
