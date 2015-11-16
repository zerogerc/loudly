package base;

public class PostInfo {
    public String link;
    public int like, repost, comment;

    public PostInfo(String link) {
        this.link = link;
        like = 0;
        repost = 0;
        comment = 0;
    }

    public void addInfo(int like, int repost, int comment) {
        this.like = like;
        this.repost = repost;
        this.comment = comment;
    }
}
