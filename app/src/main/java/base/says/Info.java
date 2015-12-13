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

    public Info difference(Info info) {
        return new Info(info.like - like, info.repost - repost, info.comment - comment);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Info)) {
            return false;
        }
        Info second = (Info) o;
        return like == second.like && repost == second.repost && comment == second.comment;
    }

}
