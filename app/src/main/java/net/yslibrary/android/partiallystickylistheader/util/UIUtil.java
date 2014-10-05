package net.yslibrary.android.partiallystickylistheader.util;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;

/**
 * Created by shimizu_yasuhiro on 2014/10/03.
 */
public class UIUtil {

    private static final int[] RES_IDS_ACTIONBAR_SIZE = { android.R.attr.actionBarSize };

    public static int calculateActionBarSize(Context context) {
         if (context == null) {
             return 0;
         }

        Resources.Theme curTheme = context.getTheme();

        if (curTheme == null) {
            return 0;
        }

        TypedArray att = curTheme.obtainStyledAttributes(RES_IDS_ACTIONBAR_SIZE);

        if (att == null) {
            return 0;
        }

        float size = att.getDimension(0, 0);
        att.recycle();

        return (int) size;
    }

}
