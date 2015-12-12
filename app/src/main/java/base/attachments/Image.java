package base.attachments;

import android.graphics.Point;
import android.net.Uri;

import base.SingleNetwork;
import base.says.Info;

/**
 * Image from the internet. Exists in only one network
 */
public class Image implements Attachment, SingleNetwork {
    protected String externalLink;
    protected Point size = new Point(0, 0);

    protected Info info;
    protected int network;
    protected String id;

    public Image() {
        externalLink = null;
        size = new Point(0, 0);
        network = -1;
        id = null;
    }

    public Image(String externalLink, int network, String id) {
        this.externalLink = externalLink;
        this.network = network;
        this.id = id;
    }

    public void setWidth(int width) {
        size.x = width;
    }

    public void setHeight(int height) {
        size.y = height;
    }

    public int getWidth() {
        return size.x;
    }

    public int getHeight() {
        return size.y;
    }

    public Point getSize() {
        return size;
    }

    public void setSize(Point size) {
        this.size = size;
    }

    public String getExternalLink() {
        return externalLink;
    }

    public void setExternalLink(String externalLink) {
        this.externalLink = externalLink;
    }

    public Uri getUri() {
        return Uri.parse(externalLink);
    }

    @Override
    public void setNetwork(int network) {
        this.network = network;
    }

    @Override
    public int getNetwork() {
        return network;
    }

    @Override
    public boolean existsIn(int network) {
        return this.network == network;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Info getInfo() {
        return info;
    }

    @Override
    public void setInfo(Info info) {
        this.info = info;
    }

    @Override
    public int getType() {
        return Attachment.IMAGE;
    }

    @Override
    public String getExtra() {
        return externalLink;
    }
}