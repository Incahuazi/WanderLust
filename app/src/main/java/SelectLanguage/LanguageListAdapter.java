package SelectLanguage;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import be.ictera.wanderlust.R;

/**
 * Created by Erik.Rans on 6/06/2017.
 */

public class LanguageListAdapter extends ArrayAdapter<LanguageItem> {


    private LanguageItem[] LanguageItems;

    public LanguageListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull LanguageItem[] items) {
        super(context, resource, items);
        this.LanguageItems = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.languageitem, null);
            }

            LanguageItem it = LanguageItems[position];
            if (it != null) {
                ImageView iv = (ImageView) v.findViewById(R.id.languageImage);
                if (iv != null) {

    //                android.view.ViewGroup.LayoutParams layoutParams = iv.getLayoutParams();
    //                layoutParams.width = 400;
    //                layoutParams.height = 400;
    //                iv.setLayoutParams(layoutParams);

                    iv.setImageDrawable(it.getImage());
                    iv.setAdjustViewBounds(true);
//                    iv.setPadding(10,10,10,10);
                    iv.setPadding(1,1,1,1);


                    if (LanguageItems[position].setText) {
                        //need to set text in blank language circle
                        TextView tv = (TextView) v.findViewById(R.id.languageText);
                        tv.setText(LanguageItems[position].LanguageName);
                    } else {
                        TextView tv = (TextView) v.findViewById(R.id.languageText);
                        tv.setText("");
                    }
                }
            }


        return v;
    }
}
