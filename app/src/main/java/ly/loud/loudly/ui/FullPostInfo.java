package ly.loud.loudly.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

import ly.loud.loudly.R;
import ly.loud.loudly.base.Tasks;
import ly.loud.loudly.base.says.Post;
import ly.loud.loudly.base.says.Say;
import ly.loud.loudly.ui.adapter.BaseAdapter;
import ly.loud.loudly.ui.adapter.Item;

public class FullPostInfo extends AppCompatActivity {
    public static final String POST_KEY = "post";

    private ArrayList<Item> elements;
    private Post post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_post_info);

        getSupportActionBar().setHomeButtonEnabled(true);

        elements = new ArrayList<>();

        handleIntent(getIntent());
        initRecyclerView();
    }

    private void handleIntent(Intent intent) {
        Post prev = post;
        post = intent.getParcelableExtra(POST_KEY);
        if (prev == null || Say.COMPARATOR.compare(prev, post) != 0) {
            elements = new ArrayList<>();
            Tasks.CommentsGetter task = new Tasks.CommentsGetter(post, elements, Loudly.getContext().getWraps());
            task.execute();
        }

        elements.add(post);
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = ((RecyclerView) findViewById(R.id.full_post_recycler_view));

        BaseAdapter<FullPostInfo, Item> adapter = new BaseAdapter<>(elements, this);

        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }
}
