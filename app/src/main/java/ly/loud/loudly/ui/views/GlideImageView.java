package ly.loud.loudly.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import ly.loud.loudly.R;
import ly.loud.loudly.base.attachments.Image;
import ly.loud.loudly.ui.Loudly;

//TODO: width and height not greater that image,getHeight image.getWidth
//TODO: setBackGround to white after load (for small images)

/**
 * ImageView with bunch of options to load image using <code>Glide</code>.
 * It's has fixed <code>width</code> (from xml) and proper height (matching image ratio).
 * Please don't set <code>layout_width</code> equal to <code>wrap_content</code>
 *
 * @see #setScale(double)
 */
public class GlideImageView extends ImageView {
    private double scale;

    /**
     * Create instance of ScalableImageView.
     * @param context current context
     * @param attrs attrs from xml
     */
    public GlideImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        scale = 0;
    }

    /**
     * Set scale to ImageView. After this method <code>height</code> of the ImageView
     * will be equal to <code>width * scale</code>.
     * @param scale given scale for height resize
     */
    private void setScale(double scale) {
        this.scale = scale;
        requestLayout();
    }

    /**
     * Load given <code>image</code> by <code>image.getUri()</code>. This method performs resizing of <code>ImageView</code>
     * based on <code>image.getHeight()</code> and <code>image.getWidth()</code> first.
     * @param image given image
     */
    public void loadImage(Image image) {
        if (image == null) {
            setScale(0);
            return;
        }
        if (image.getHeight() == 0 || image.getWidth() == 0) {
            //TODO: remove this enterprise
            setScale(0.75);
//            Glide.with(getContext())
//                    .load(image.getUri())
//                    .fitCenter()
//                    .into(this);
            setBackgroundResource(R.color.white_color);
        } else {
            setScale(((double) image.getHeight()) / image.getWidth());
        }
        Glide.with(getContext())
                .load(image.getUri())
                .fitCenter()
                .into(this);
    }

    /**
     * Perform loading of image by Url. Set height equal to width.
     * @param url given url
     */
    public void loadCircularShapeImageByUrl(String url) {
        setScale(1);
        Glide.with(getContext())
                .load(url)
                .asBitmap()
                .fitCenter()
                .into(new BitmapImageViewTarget(this) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(Loudly.getContext().getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        setImageDrawable(circularBitmapDrawable);
                    }
                });
    }

    /**
     * Load given image and apply CircledTransformation to it.
     * @param image given image
     */
    public void loadCircularShapeImage(Image image) {
        setScale(1); //always has equal dimensions
        loadCircularShapeImageByUrl(image.getUri().toString()); //TODO: needs testing
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), ((int) (scale * getMeasuredWidth())));
    }
}