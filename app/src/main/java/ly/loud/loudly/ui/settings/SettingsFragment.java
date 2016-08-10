package ly.loud.loudly.ui.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import ly.loud.loudly.R;
import ly.loud.loudly.networks.Networks;
import ly.loud.loudly.networks.Networks.Network;
import ly.loud.loudly.ui.auth.AuthFragment;

import static ly.loud.loudly.networks.Networks.*;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String FACEBOOK_KEY = "network_facebook";
    private static final String INSTAGRAM_KEY = "network_instagram";
    private static final String VK_KEY = "network_vk";
    private static final String LOAD_FOR_KEY = "loadlast";

    @NonNull
    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_activity_settings);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
    }

    private void openAuthFragment(@Network int network) {
        AuthFragment.newInstance(network).show(getFragmentManager(), null);
    }

    @Override
    public boolean onPreferenceTreeClick(@NonNull Preference preference) {
        final String key = preference.getKey();
        switch (key) {
            case FACEBOOK_KEY:
                openAuthFragment(FB);
                return true;
            case INSTAGRAM_KEY:
                openAuthFragment(INSTAGRAM);
                return true;
            case VK_KEY:
                openAuthFragment(VK);
                return true;
            case LOAD_FOR_KEY:
                return true;
            default:
                return super.onPreferenceTreeClick(preference);
        }
    }
}
