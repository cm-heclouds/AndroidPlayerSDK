package com.ont.media.player.TimeRuler;

import android.content.res.Resources;

/**
 * 通用工具类
 * Created by HDL on 2017/9/4.
 */

public class CUtils {
    /**
     * dp转px
     *
     * @param dipValue
     * @return
     */
    public static int dip2px(float dipValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /** sp转换px */
    public static int sp2px(int spValue) {
        final float fontScale = Resources.getSystem().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}
