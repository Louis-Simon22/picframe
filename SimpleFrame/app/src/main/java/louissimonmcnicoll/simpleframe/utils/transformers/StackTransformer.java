package louissimonmcnicoll.simpleframe.utils.transformers;

import androidx.viewpager.widget.ViewPager;
import android.view.View;

/**
 * Created by ussher on 20.06.15.
 */
public class StackTransformer implements ViewPager.PageTransformer {
    @Override
    public void transformPage(View page, float position) {
        page.setTranslationX(position < 0 ? 0f : -page.getWidth() * position);
    }
}
