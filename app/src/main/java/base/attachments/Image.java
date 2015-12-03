package base.attachments;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;

import base.Networks;
import ly.loud.loudly.Loudly;

public class Image extends Attachment {
    protected String[] links;
    protected String internalLink;
    protected boolean local;
    {
        localID = 0;
    }

    public Image(String internalLink, String[] links) {
        this.internalLink = internalLink;
        this.links = links;
        this.local = true;
    }

    public Image(String internalLink, boolean local) {
        this.internalLink = internalLink;
        this.local = local;
        this.links = new String[Networks.NETWORK_COUNT];
    }

    public Image(Uri internalLink) {
        this.internalLink = internalLink.toString();
        this.links = new String[Networks.NETWORK_COUNT];
        this.local = true;
    }

    public Uri getUri() {
        return Uri.parse(internalLink);
    }

    public String getMIMEType() {
        return Loudly.getContext().getContentResolver().getType(getUri());
    }

    public InputStream getContent() throws IOException {
        return Loudly.getContext().getContentResolver().openInputStream(getUri());
    }

    public void setLink(int network, String link) {
        links[network] = link;
    }

    public boolean isLocal() {
        return local;
    }

    @Override
    public int getType() {
        return Attachment.IMAGE;
    }

    @Override
    public String[] getLinks() {
        return links;
    }

    @Override
    public String getLink(int network) {
        return links[network];
    }

    @Override
    public String getExtra() {
        return internalLink;
    }
}