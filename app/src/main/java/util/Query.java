package util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class Query {
    private String serverURL;
    private ArrayList<Parameter> params;

    public Query(String serverURL) {
        this.serverURL = serverURL;
        params = new ArrayList<>();
    }

    public boolean containsParameter(String name) {
        return getParameter(name) != null;
    }

    public void addParameter(Parameter parameter) {
        params.add(parameter);
    }

    public void addParameter(String parameter, Object value) {
        params.add(new Parameter(parameter, value));
    }

    public String getParameter(String name) {
        for (Parameter p: params) {
            if (p.name.equals(name)) {
                return p.value;
            }
        }
        return "";
    }

    public ArrayList<Parameter> getParameters() {
        return params;
    }

    public String getServerURL() {
        return serverURL;
    }

    public static Query fromURL(String url) {
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

    public String toURL() {
        StringBuilder sb = new StringBuilder(serverURL);
        if (this.params.size() > 0) {
            sb.append('?');
            sb.append(parametersToString());
        }
        return sb.toString();
    }

    public String parametersToString() {
        StringBuilder sb = new StringBuilder();
        if (params.size() >= 1) {
            sb.append(params.get(0));
        }
        for (int i = 1; i < params.size(); i++) {
            sb.append('&');
            sb.append(params.get(i));
        }
        return sb.toString();
    }

    public static class Parameter implements Comparable<Parameter>{
        public String name, value;

        public Parameter(String name, Object value) {
            this.name = name;
            this.value = value.toString();
        }

        @Override
        public int compareTo(Parameter another) {
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
