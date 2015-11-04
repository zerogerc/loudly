package wraps;

public class WebResponse {
    enum ResultType {
        SUCCESS, ERROR, WTF
    }

    ResultType kind;
    Object result;

    public WebResponse(ResultType kind, Object result) {
        this.kind = kind;
        this.result = result;
    }
}
