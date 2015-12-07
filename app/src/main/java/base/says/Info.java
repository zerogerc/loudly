package base.says;

public class Info {
    public int like, repost, comment;

    public Info(int like, int repost, int comment) {
        this.like = like;
        this.repost = repost;
        this.comment = comment;
    }

    public Info() {
        like = 0;
        repost = 0;
        comment = 0;
    }

    public void add(Info info) {
        like += info.like;
        repost += info.repost;
        comment += info.comment;
    }
}
