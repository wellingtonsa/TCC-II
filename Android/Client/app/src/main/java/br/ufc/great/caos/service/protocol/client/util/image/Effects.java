package br.ufc.great.caos.service.protocol.client.util.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class Effects {
    public String BlackAndWhite(String encodedImage){
        byte[] decodedImageByteArray = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap original = BitmapFactory.decodeByteArray(decodedImageByteArray, 0, decodedImageByteArray.length);

        Bitmap converted = Bitmap.createBitmap(original.getWidth(), original.getHeight(), original.getConfig());

        int A, R, G, B;
        int colorPixel;
        int width = original.getWidth();
        int height = original.getHeight();

        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                colorPixel = original.getPixel(x, y);
                A = Color.alpha(colorPixel);
                R = Color.red(colorPixel);
                G = Color.green(colorPixel);
                B = Color.blue(colorPixel);

                R = (R + G + B)  / 3;
                G = R;
                B = R;

                converted.setPixel(x, y, Color.argb(A,R,G,B));
            }
        }

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        converted.compress(Bitmap.CompressFormat.JPEG, 100, byteStream);
        byte[] byteArray = byteStream.toByteArray();
        String baseString = Base64.encodeToString(byteArray,Base64.DEFAULT);

        return baseString;
    }
}
