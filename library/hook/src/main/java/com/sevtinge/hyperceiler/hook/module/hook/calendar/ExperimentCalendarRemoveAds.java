package com.sevtinge.hyperceiler.hook.module.hook.calendar;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * [Target Feature 7] Remove commercial ads, recommendation cards, and promotional feeds from Xiaomi Calendar.
 */
public class ExperimentCalendarRemoveAds extends BaseHook {

    @Override
    public void init() {
        findAndHookMethodSilently("com.android.calendar.ad.AdManager", "isAdEnabled", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.android.calendar.ad.AdManager", "needShowAd", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.android.calendar.ad.CalendarAdHelper", "isAdVisible", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.android.calendar.feed.AdFeedManager", "isFeedAdEnabled", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.android.calendar.commercial.CommercialManager", "isCommercialEnabled", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
    }
}
