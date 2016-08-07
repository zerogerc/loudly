package ly.loud.loudly.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Query {
    @NonNull
    private final String serverURL;

    @NonNull
    private final List<Parameter> parameters;

    public Query(@NonNull String serverURL) {
        this.serverURL = serverURL;
        parameters = new ArrayList<>();
    }

    public boolean containsParameter(String name) {
        return getParameter(name) != null;
    }

    @NonNull
    public Query addParameter(@NonNull String parameter, @NonNull Object value) {
        parameters.add(new Parameter(parameter, value));
        return this;
    }

    @Nullable
    public String getParameter(@NonNull String name) {
        for (Parameter p: parameters) {
            if (p.name.equals(name)) {
                return p.value;
            }
        }
        return null;
    }

    @NonNull
    public List<Parameter> getParameters() {
        return parameters;
    }

    @NonNull
    public Query sortParameters() {
        Collections.sort(parameters);
        return this;
    }

    @NonNull
    public String getServerURL() {
        return serverURL;
    }

    @Nullable
    public static Query fromResponseUrl(@NonNull String url) {
        int begin = 0;
        int end = url.indexOf('#', begin);
        if (end == -1) {
            return null;
        }
        Query query = new Query(url.substring(begin, end++));
        begin = end;
        while (end != -1) {
            end = url.indexOf('=', begin);
            String parameter = url.substring(begin, end++);
            begin = end;
            end = url.indexOf('&', begin);
            String value;
            if (end == -1) {
                value = url.substring(begin);
            } else {
                value = url.substring(begin, end++);
                begin = end;
            }
            query.addParameter(parameter, value);
        }
        return query;
    }

    @NonNull
    public String toURL() {
        StringBuilder sb = new StringBuilder(serverURL);
        if (this.parameters.size() > 0) {
            if (serverURL.contains("?")) {
                sb.append('&');
            } else {
                sb.append('?');
            }
            sb.append(parametersToString());
        }
        return sb.toString();
    }

    @NonNull
    public StringBuilder parametersToString() {
        StringBuilder sb = new StringBuilder();
        if (parameters.size() >= 1) {
            sb.append(parameters.get(0));
        }
        for (int i = 1, size = parameters.size(); i < size; i++) {
            sb.append('&');
            sb.append(parameters.get(i));
        }
        return sb;
    }

    public static class Parameter implements Comparable<Parameter>{
        @NonNull
        public final String name;

        @NonNull
        public final String value;

        public Parameter(@NonNull String name, @NonNull Object value) {
            this.name = name;
            this.value = value.toString();
        }

        @Override
        public int compareTo(@NonNull Parameter another) {
            return name.compareTo(another.name);
        }

        @Override
        public String toString() {
            String encodedValue;
            try {
                encodedValue = URLEncoder.encode(value, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                encodedValue = "";
            }
            return name + '=' + encodedValue;
        }
    }
}
