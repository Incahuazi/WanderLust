package SelectLanguage;

import android.graphics.drawable.Drawable;

/**
 * Created by Erik.Rans on 6/06/2017.
 */

public class LanguageItem {
    public String LanguageName;
    public Drawable ImageDrawable;

    public LanguageItem(String languageName, Drawable imageDrawable) {
        this.LanguageName = languageName;
        this.ImageDrawable = imageDrawable;
    }

    public Drawable getImage() {
        return ImageDrawable;
    }
}
