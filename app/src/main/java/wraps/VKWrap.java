package wraps;

import android.app.Activity;
import android.content.Intent;

import ly.loud.loudly.AuthActivity;

public class VKWrap implements Wrappable {
    private static final String CLIENT_ID = "5133011";
    Activity parent;

    public VKWrap(Activity parent) {
        this.parent = parent;
    }

    @Override
    public WebResponse authorize() {
        Activity waiter = new Activity() {
            @Override
            protected void onActivityResult(int requestCode, int resultCode, Intent data) {
                super.onActivityResult(requestCode, resultCode, data);
                response = data.getStringExtra("RESPONSE");
            }
        }
        Intent temp = new Intent(parent, AuthActivity.class);
        temp.putExtra("URL", generateAuthorizeURL());
        parent.startActivityForResult(temp, 1);

    }

    private String generateAuthorizeURL() {
        return "https://oauth.vk.com/authorize?client_id=" + CLIENT_ID + "&redirect_uri=https://oauth.vk.com/blank.html&display_type=mobile&response_type=token";
    }

}
