package ly.loud.loudly.ui.full_post;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import ly.loud.loudly.R;
import ly.loud.loudly.base.plain.PlainPost;

public class FullPostInfoActivity extends AppCompatActivity {

    private static final String POST_KEY = "post";

    public static void invoke(@NonNull Context context, @NonNull PlainPost post) {
        Intent intent = new Intent(context, FullPostInfoActivity.class);
        intent.putExtra(POST_KEY, post);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_post_info);

        final PlainPost post = getIntent().getParcelableExtra(POST_KEY);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, FullPostInfoFragment.newInstance(post))
                .commit();
    }
}
