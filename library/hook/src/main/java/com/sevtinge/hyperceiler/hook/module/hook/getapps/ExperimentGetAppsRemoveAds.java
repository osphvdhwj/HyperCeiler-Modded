package com.sevtinge.hyperceiler.hook.module.hook.getapps;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

public class ExperimentGetAppsRemoveAds extends BaseHook {

    @Override
    public void init() {
        findAndHookMethodSilently("com.xiaomi.market.ui.ad.MarketAdManager", "isAd",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(false);
                }
            });

        findAndHookMethodSilently("com.xiaomi.market.ui.ad.MarketAdManager", "needShowMarketAd",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(false);
                }
            });

        findAndHookMethodSilently("com.xiaomi.market.ui.ad.MarketAdManager", "shouldDisplayAdBanner",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(false);
                }
            });

        findAndHookMethodSilently("com.xiaomi.market.ui.banner.BannerAdPresenter", "isPromotionCard",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(false);
                }
            });

        findAndHookMethodSilently("com.xiaomi.market.ui.push.PushAdManager", "canPushAd",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(false);
                }
            });

        findAndHookMethodSilently("com.xiaomi.market.ui.push.PushAdManager", "shouldShowPushAd",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(false);
                }
            });

        findAndHookMethodSilently("com.xiaomi.market.data.model.MarketAd", "isCommercialAd",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(false);
                }
            });
    }
}
