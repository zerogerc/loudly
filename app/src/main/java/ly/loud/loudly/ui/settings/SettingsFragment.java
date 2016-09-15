package ly.loud.loudly.ui.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import ly.loud.loudly.R;
import ly.loud.loudly.networks.Networks.Network;
import ly.loud.loudly.ui.auth.AuthFragment;

import static ly.loud.loudly.networks.Networks.FB;
import static ly.loud.loudly.networks.Networks.INSTAGRAM;
import static ly.loud.loudly.networks.Networks.OK;
import static ly.loud.loudly.networks.Networks.VK;

public class SettingsFragment extends PreferenceFragmentCompat {

    // ToDo: It's ugly, fix it
    private static final String FACEBOOK_KEY = "network_facebook";
    private static final String INSTAGRAM_KEY = "network_instagram";
    private static final String VK_KEY = "network_vk";
    private static final String OK_KEY = "network_ok";

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
            case OK_KEY:
                openAuthFragment(OK);
                return true;
            default:
                return super.onPreferenceTreeClick(preference);
        }
    }
}
