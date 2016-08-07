package ly.loud.loudly.ui.settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import ly.loud.loudly.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_settings);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, SettingsFragment.newInstance())
                .commit();
    }
}
