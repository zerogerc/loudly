package ly.loud.loudly.application.models;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.util.TimeInterval;

public class LoadMoreStrategyModel {

    private static final int DEFAULT_LOAD_DAYS = 7;

    @NonNull
    private Loudly loudlyApplication;

    @NonNull
    private TimeInterval currentTimeInterval;

    @NonNull
    private final Calendar calendar;

    private int loadLastDays = DEFAULT_LOAD_DAYS;

    public LoadMoreStrategyModel(@NonNull Loudly loudlyApplication) {
        this.loudlyApplication = loudlyApplication;
        this.calendar = Calendar.getInstance();
        updateTimeInterval();
    }

    private void updateTimeInterval() {
        long currentTime = System.currentTimeMillis();
        long millis = TimeUnit.MILLISECONDS.convert(loadLastDays, TimeUnit.DAYS);
        currentTimeInterval = TimeInterval.since((currentTime - millis) / 1000);
    }

    /**
     * Get current TimeInterval to show posts for
     */
    @NonNull
    public TimeInterval getCurrentTimeInterval() {
        return currentTimeInterval;
    }

    /**
     * Increase current timestamp according to inner startegy.
     */
    public void generateNextInterval() {
        if (loadLastDays < 10000) { // it hard to imagine user with such a long social media history
            loadLastDays *= 2;
            updateTimeInterval();
        }
    }
}
