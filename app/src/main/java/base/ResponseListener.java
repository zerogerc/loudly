package base;

/*
    R - is result (Object)
 */
public abstract class ResponseListener {
    public abstract void onSuccess(Object result);
    public abstract void onFail(String error);
}
