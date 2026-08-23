package com.sevtinge.hyperceiler.hook.module.hook.calendar;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * [Experiment] Remove ads, commercial recommendation cards, and promotional feeds from Calendar.
 */
public class ExperimentCalendarRemoveAds extends BaseHook {

    @Override
    public void init() {
        // AdManager hooks
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
        findAndHookMethodSilently("com.android.calendar.ad.AdManager", "isAdShowing", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.android.calendar.ad.AdManager", "loadAd", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) {
                param.setResult(null);
            }
        });

        // CalendarAdHelper hooks
        findAndHookMethodSilently("com.android.calendar.ad.CalendarAdHelper", "isAdClosed", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.calendar.ad.CalendarAdHelper", "shouldShowAd", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.android.calendar.ad.CalendarAdHelper", "isAdAvailable", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });

        // Feed & Commercial manager hooks
        findAndHookMethodSilently("com.android.calendar.homepage.AdFeedManager", "isCommercialEnabled", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.android.calendar.homepage.CommercialManager", "hasCommercialCards", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.android.calendar.homepage.CommercialManager", "isCommercialEnabled", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });

        // Settings hooks
        findAndHookMethodSilently("com.android.calendar.settings.CalendarSettings", "isAdAllowed", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.android.calendar.settings.CalendarSettings", "isRecommendEnabled", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });

        // MIUI Calendar AdHelper
        findAndHookMethodSilently("com.miui.calendar.ad.AdHelper", "isAdEnabled", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
    }
}
