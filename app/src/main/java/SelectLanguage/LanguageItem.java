package SelectLanguage;

import android.graphics.drawable.Drawable;
import android.media.Image;

/**
 * Created by Erik.Rans on 6/06/2017.
 */

public class LanguageItem {
    public String LanguageName;
    public Drawable ImageDrawable;
    public boolean setText;
    public String LanguageCode;

    public LanguageItem(String languageName, Drawable imageDrawable, boolean setText, String LanguageCode) {
        this.LanguageName = languageName;
        this.ImageDrawable = imageDrawable;
        this.setText = setText;
        this.LanguageCode = LanguageCode;
    }

    public Drawable getImage() {
        return ImageDrawable;
    }
}
