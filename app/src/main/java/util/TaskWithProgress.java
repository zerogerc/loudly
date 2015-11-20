package util;

import base.Post;
import base.Wrap;

/**
 * A Long Task made specially for working with Social Networks API asynchronously.
 * Requires UIAction onProgressUpdate for sending info about progress, and ResultListener for
 * callback
 */
public abstract class TaskWithProgress<Params, Progress> extends LongTask<Params, Progress> {
    protected UIAction onProgressUpdate;
    protected ResultListener onFinish;
    protected Wrap[] wraps;

    protected TaskWithProgress() {
        super();
    }

    public TaskWithProgress(UIAction onProgressUpdate, ResultListener onFinish, Wrap... wraps) {
        this.onProgressUpdate = onProgressUpdate;
        this.onFinish = onFinish;
        this.wraps = wraps;
    }

    @Override
    protected void onProgressUpdate(Progress... values) {
        super.onProgressUpdate(values);
        onProgressUpdate.execute(context, values);
    }
}
