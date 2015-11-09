package util;

public class Query extends ParameterBundle {
    private String serverURL;

    public Query(String serverURL) {
        super();
        this.serverURL = serverURL;
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
        String params = super.toString();
        StringBuilder sb = new StringBuilder(serverURL);
        sb.append('?');
        sb.append(params);
        return sb.toString();
    }
}
