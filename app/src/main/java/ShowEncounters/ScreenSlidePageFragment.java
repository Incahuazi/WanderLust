package ShowEncounters;


import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.io.File;

import be.ictera.wanderlust.R;


public class ScreenSlidePageFragment extends Fragment {

    private myImageView[] pics = new myImageView[]{new myImageView(),new myImageView(),new myImageView()};

    // Container Activity must implement this interface
    public interface OnFragmentInteractionListener {
        public void onImageClicked(String imagePath);
    }

    private ScreenSlidePageFragment.OnFragmentInteractionListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_show_encounter_screen_slide_page, container, false);

        pics[0].imageView = (ImageView) rootView.findViewById(R.id.Pic1);
        pics[1].imageView = (ImageView) rootView.findViewById(R.id.Pic2);
        pics[2].imageView = (ImageView) rootView.findViewById(R.id.Pic3);


        for (int i = 0; i <= 2; i++) {
            pics[i].imagePath = this.getArguments().getString("Image" + (i+1+1)); //+1 for zero base; +1 because first picture is profile pic and is already displayed
            if (pics[i].imagePath !=null && !TextUtils.isEmpty(pics[i].imagePath))
            {
                try {
                    File picture = new File(pics[i].imagePath);
                    Glide.with(this).load(picture).into(pics[i].imageView);

                    final int item = i;
                    pics[i].imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickListener(item);
                        }
                    });

                } catch (Exception e) {
                    //swallow exception on fe corrupt foto
                    e.printStackTrace();
                }
            }
            else
            {
                pics[i].imageView.setImageResource(android.R.drawable.ic_menu_camera);
                pics[i].imageView.setRotation(0);

                Resources r = getResources();
                pics[i].imageView.getLayoutParams().height = r.getDimensionPixelSize(android.R.dimen.notification_large_icon_height);
            }
        }

        TextView textViewDataName = (TextView) rootView.findViewById(R.id.Name);
        textViewDataName.setText(this.getArguments().getString("dataName"));

        TextView textViewDataMessage = (TextView) rootView.findViewById(R.id.Message);
        textViewDataMessage.setText(this.getArguments().getString("dataMessage"));

        TextView textViewDataLocation = (TextView) rootView.findViewById(R.id.Location);
        textViewDataLocation.setText(this.getArguments().getString("dataLocation"));

        return rootView;
    }

    private void onClickListener(int item) {
        mListener.onImageClicked(pics[item].imagePath);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ScreenSlidePageFragment.OnFragmentInteractionListener) {
            mListener = (ScreenSlidePageFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public class myImageView{
        public ImageView imageView;
        public String imagePath;
    }
}



