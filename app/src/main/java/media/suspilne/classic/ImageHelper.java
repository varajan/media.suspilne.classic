package media.suspilne.classic;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class ImageHelper {
    public static Drawable resize(Drawable image, int width, int height) {
        Bitmap b = ((BitmapDrawable)image).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, width, height, false);
        return new BitmapDrawable(bitmapResized);
    }

    public static Bitmap getBitmap(Drawable drawable){
        return ((BitmapDrawable)drawable).getBitmap();
    }

    public static Drawable getDrawable(Bitmap bitmap){
        return new BitmapDrawable(bitmap);
    }

    public static Drawable getCircularDrawable(Drawable drawable) {
        Bitmap bitmap = getBitmap(drawable);
        if (bitmap == null || bitmap.isRecycled()) {
            return null;
        }
        float radius = bitmap.getWidth() > bitmap.getHeight() ? ((float) bitmap
                .getHeight()) / 2f : ((float) bitmap.getWidth()) / 2f;
        Bitmap canvasBitmap = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP,
                Shader.TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(shader);

        Canvas canvas = new Canvas(canvasBitmap);

        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                radius, paint);

        return getDrawable(canvasBitmap);
    }
}