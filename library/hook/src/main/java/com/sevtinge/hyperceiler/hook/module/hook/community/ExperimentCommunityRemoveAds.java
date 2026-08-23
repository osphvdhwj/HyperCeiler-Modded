package com.sevtinge.hyperceiler.hook.module.hook.community;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

public class ExperimentCommunityRemoveAds extends BaseHook {

    @Override
    public void init() {
        findAndHookMethodSilently("com.xiaomi.vipaccount.ad.AdManager", "shouldShowAd",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(false);
                }
            });

        findAndHookMethodSilently("com.xiaomi.vipaccount.ad.AdManager", "isAdEnabled",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(false);
                }
            });

        findAndHookMethodSilently("com.xiaomi.vipaccount.ad.SplashAdHelper", "canShowSplash",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(false);
                }
            });

        findAndHookMethodSilently("com.xiaomi.vipaccount.ad.SplashAdHelper", "needShowSplash",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(false);
                }
            });

        findAndHookMethodSilently("com.xiaomi.vipaccount.ad.SplashAdHelper", "showSplashAd",
            new MethodHook() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            });

        findAndHookMethodSilently("com.xiaomi.vipaccount.data.model.FeedItem", "isAdItem",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(false);
                }
            });

        findAndHookMethodSilently("com.xiaomi.vipaccount.data.model.FeedItem", "isSponsored",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(false);
                }
            });
    }
}
