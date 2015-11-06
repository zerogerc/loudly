package base;

/*
    R - is result (Object)
 */
public abstract class ResponseListener<R> {
    public abstract void onSuccess(R result);
    public abstract void onFail(String error);
}
