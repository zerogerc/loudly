package util;

public class Counter {
    int textLength, imageCount, videoCount, linkCount;

    public Counter(int textLength, int imageCount, int videoCount, int linkCount) {
        this.textLength = textLength;
        this.imageCount = imageCount;
        this.videoCount = videoCount;
        this.linkCount = linkCount;
    }

    public int getTextLength() {
        return textLength;
    }

    public int getImageCount() {
        return imageCount;
    }

    public int getVideoCount() {
        return videoCount;
    }

    public int getLinkCount() {
        return linkCount;
    }
}
