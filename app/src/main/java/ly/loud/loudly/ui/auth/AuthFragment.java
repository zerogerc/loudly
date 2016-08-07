package ly.loud.loudly.ui.auth;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.AuthModel;
import ly.loud.loudly.networks.Networks.Network;
import ly.loud.loudly.util.BroadcastSendingTask;
import ly.loud.loudly.util.Broadcasts;
import rx.Observable;

import static rx.android.schedulers.AndroidSchedulers.mainThread;
import static rx.schedulers.Schedulers.io;

public class AuthFragment extends DialogFragment {
    private static final String NETWORK_FIELD = "network";

    @SuppressWarnings("NullableProblems") // Butterkniife
    @BindView(R.id.progressBar)
    @NonNull
    ProgressBar circle;


    @SuppressWarnings("NullableProblems") // Butterkniife
    @BindView(R.id.webView)
    @NonNull
    WebView webView;

    @Inject
    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    AuthModel authModel;

    @Network
    int network;

    /**
     * Indicates whether this fragment created view for the first time.
     * Should be accessed only from main thread.
     */
    private boolean firstRun = true;

    public static AuthFragment newInstance(int network) {
        Bundle args = new Bundle();
        args.putInt(NETWORK_FIELD, network);

        AuthFragment fragment = new AuthFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Loudly.getContext().getAppComponent().inject(this);

        network = getArguments().getInt(NETWORK_FIELD);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.activity_auth, null);
        ButterKnife.bind(this, rootView);
        builder.setView(rootView);
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(NETWORK_FIELD, network);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (firstRun) {
            circle.setVisibility(View.VISIBLE);
            authModel.getAuthUrl(network)
                    .flatMap(url -> authModel
                            .finishAuthorization(createUrlsObservable(url), network))
                    .subscribeOn(io())
                    .observeOn(mainThread())
                    .doOnSuccess(success -> {
                        if (success) {
                            // ToDo: handle success
                        } else {
                            // ToDo: handle failure
                        }
                        clearWebView();
                        dismiss();
                    })
                    .subscribe();
            firstRun = false;
        }
    }

    @CheckResult
    @NonNull
    private Observable<String> createUrlsObservable(String initialUrl) {
        return Observable.create(observer -> {
            webView.post(() -> {
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        observer.onNext(url);
                        return super.shouldOverrideUrlLoading(view, url);
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        circle.setVisibility(View.INVISIBLE);
                    }
                });
                webView.loadUrl(initialUrl);
            });
        });
    }

    public void clearWebView() {
        this.webView.loadUrl("about:blank");
    }
}
