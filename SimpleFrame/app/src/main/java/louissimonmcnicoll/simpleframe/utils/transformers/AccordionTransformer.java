package louissimonmcnicoll.simpleframe.utils.transformers;

import androidx.viewpager.widget.ViewPager;
import android.view.View;

/**
 * Created by ussher on 15.06.15.
 */
public class AccordionTransformer implements ViewPager.PageTransformer {
    @Override
    public void transformPage(View page, float position) {
        page.setPivotX(position < 0 ? 0 : page.getWidth());
        page.setScaleX(position < 0 ? 1f + position : 1f - position);
    }
}