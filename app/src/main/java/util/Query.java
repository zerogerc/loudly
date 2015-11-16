package util;

public class Query {
    private String serverURL;
    private ParameterBundle params;

    public Query(String serverURL) {
        this.serverURL = serverURL;
        params = new ParameterBundle();
    }

    public boolean containsParameter(String name) {
        return getParameter(name) != null;
    }

    public void addParameter(Parameter parameter) {
        params.addParameter(parameter);
    }

    public void addParameter(String parameter, String value) {
        params.addParameter(new Parameter(parameter, value));
    }

    public String getParameter(String name) {
        return params.getParameter(name);
    }

    public ParameterBundle getParameters() {
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
            String params = this.params.toString();
            sb.append('?');
            sb.append(params);
        }
        return sb.toString();
    }
}
