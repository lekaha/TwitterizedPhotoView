package co.lkh.android.view.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;

/**
 * Utility of extracting prominent color from a bitmap
 */
public class PaletteUtil {
    final static int DEFAULT_COLOR = Color.TRANSPARENT;

    private static Palette.Swatch getDominantPaletteSwatch(@NonNull final Bitmap bitmap) {
        Palette palette = Palette.from(bitmap).generate();
        return palette.getDominantSwatch();
    }

    private static Palette.Swatch getDarkMutedPaletteSwatch(@NonNull final Bitmap bitmap) {
        Palette palette = Palette.from(bitmap).generate();
        return palette.getDarkMutedSwatch();
    }

    public static int extractProminentColor(@NonNull final Bitmap bitmap) {
        return getDominantPaletteSwatch(bitmap).getRgb();
    }

    public static int extractProminentDarkerColor(@NonNull final Bitmap bitmap) {
        return getDarkMutedPaletteSwatch(bitmap).getRgb();
    }

    public static void asyncExtractProminentColor(@NonNull final Bitmap bitmap,
                                           @NonNull final OnExtractedListener listener) {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                listener.onExtracted(palette.getDominantColor(DEFAULT_COLOR));
            }
        });
    }

    public static void asyncExtractProminentDarkerColor(@NonNull final Bitmap bitmap,
                                                 @NonNull final OnExtractedListener listener) {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                listener.onExtracted(palette.getDarkMutedColor(DEFAULT_COLOR));
            }
        });
    }

    public interface OnExtractedListener {
        void onExtracted(int color);
    }
}
