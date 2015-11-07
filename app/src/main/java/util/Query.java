package util;

import java.util.ArrayList;

public class Query {
    public class Parameter implements Comparable<Parameter>{
        public String name, value;

        public Parameter(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public int compareTo(Parameter another) {
            return name.compareTo(another.name);
        }

        @Override
        public String toString() {
            return name + '=' + value;
        }
    }
    private String serverURL;
    private ArrayList<Parameter> parameters;

    public Query(String serverURL) {
        this.serverURL = serverURL;
        parameters = new ArrayList<>();
    }

    public String getServerURL() {
        return serverURL;
    }

    public ArrayList<Parameter> getParameters() {
        return parameters;
    }

    public boolean containsParameter(String name) {
        return getParameter(name) != null;
    }

    public void addParameter(String parameter, String value) {
        parameters.add(new Parameter(parameter, value));
    }

    public String getParameter(String name) {
        for (Parameter p : parameters) {
            if (p.name.equals(name)) {
                return p.value;
            }
        }
        return null;
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
        if (parameters.size() > 1) {
            sb.append('?');
            sb.append(parameters.get(0));
        }
        for (int i = 1; i < parameters.size(); i++) {
            sb.append('&');
            sb.append(parameters.get(i));
        }
        return sb.toString();
    }

}
