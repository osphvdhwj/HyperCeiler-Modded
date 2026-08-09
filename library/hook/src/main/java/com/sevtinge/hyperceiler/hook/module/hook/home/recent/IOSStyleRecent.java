package com.sevtinge.hyperceiler.hook.module.hook.home.recent;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ContentResolver;
import android.provider.Settings;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

public class IOSStyleRecent extends BaseHook {
    @Override
    public void init() {
        boolean isHorizontal = mPrefsMap.getBoolean("home_recent_ios_horizontal_mode", true);
        int cornerRadius = mPrefsMap.getInt("task_view_corners", 36);
        int cardScalePercent = mPrefsMap.getInt("home_recent_ios_scale", 100);
        float cardScale = cardScalePercent / 100.0f;

        // 1. Enable layout style 2 (iOS Task Stack)
        findAndHookMethod(Settings.Global.class, "getInt", ContentResolver.class, String.class, int.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                String name = (String) param.args[1];
                if ("task_stack_view_layout_style".equals(name)) {
                    param.setResult(2);
                }
            }
        });

        // 2. Configure Recents orientation (Horizontal by default for iOS mode)
        Class<?> recentsUtilsCls = findClassIfExists("com.miui.home.launcher.RecentsAndFSGestureUtils");
        if (recentsUtilsCls == null) {
            recentsUtilsCls = findClassIfExists("com.miui.home.recents.util.RecentsAndGestureUtils");
        }
        if (recentsUtilsCls != null) {
            hookAllMethods(recentsUtilsCls, "isRecentsHorizontal", MethodHook.returnConstant(isHorizontal));
            hookAllMethods(recentsUtilsCls, "isRecentsLayoutHorizontal", MethodHook.returnConstant(isHorizontal));
            hookAllMethods(recentsUtilsCls, "isRecentsVertical", MethodHook.returnConstant(!isHorizontal));
            hookAllMethods(recentsUtilsCls, "isRecentsLayoutVertical", MethodHook.returnConstant(!isHorizontal));
        }

        Class<?> recentsModelCls = findClassIfExists("com.miui.home.recents.RecentsModel");
        if (recentsModelCls != null) {
            hookAllMethods(recentsModelCls, "isRecentsHorizontal", MethodHook.returnConstant(isHorizontal));
            hookAllMethods(recentsModelCls, "isRecentsVertical", MethodHook.returnConstant(!isHorizontal));
        }

        // 3. Fix TaskViewThumbnail visibility & thumbnail display in stack view
        Class<?> taskViewThumbnailCls = findClassIfExists("com.miui.home.recents.views.TaskViewThumbnail");
        if (taskViewThumbnailCls != null) {
            hookAllMethods(taskViewThumbnailCls, "onTaskDataLoaded", new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    View thumbnailView = (View) param.thisObject;
                    thumbnailView.setAlpha(1.0f);
                    thumbnailView.setVisibility(View.VISIBLE);
                }
            });
            hookAllMethods(taskViewThumbnailCls, "setDim", new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    param.args[0] = 0.0f; // Remove dim filter
                }
            });
        }

        // 4. Custom Task View Card Scale & Rounded Corners (iOS Style)
        Class<?> cornerRadiusUtil = findClassIfExists("com.miui.home.recents.util.WindowCornerRadiusUtil");
        if (cornerRadiusUtil != null) {
            hookAllMethods(cornerRadiusUtil, "getTaskViewCornerRadius", new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    param.setResult(cornerRadius * 3);
                }
            });
        }

        Class<?> taskViewCls = findClassIfExists("com.miui.home.recents.views.TaskView");
        if (taskViewCls != null && cardScale != 1.0f) {
            hookAllMethods(taskViewCls, "onMeasure", new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    View view = (View) param.thisObject;
                    view.setScaleX(cardScale);
                    view.setScaleY(cardScale);
                }
            });
        }

        // 5. iOS-style dismiss animation
        Class<?> horizontalStyle = findClassIfExists("com.miui.home.recents.TaskStackViewLayoutStyleHorizontal");
        if (horizontalStyle != null) {
            findAndHookMethod(horizontalStyle, "createScaleDismissAnimation", View.class, float.class, new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    View view = (View) param.args[0];
                    int screenHeight = view.getResources().getDisplayMetrics().heightPixels;
                    ObjectAnimator transY = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, view.getTranslationY(), -screenHeight * 1.1f);
                    ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, view.getScaleX(), 0.75f * cardScale);
                    ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, view.getScaleY(), 0.75f * cardScale);
                    ObjectAnimator alpha = ObjectAnimator.ofFloat(view, View.ALPHA, view.getAlpha(), 0.0f);

                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.playTogether(transY, scaleX, scaleY, alpha);
                    animatorSet.setDuration(220);
                    animatorSet.setInterpolator(new AccelerateInterpolator(1.5f));
                    param.setResult(animatorSet);
                }
            });
        }
    }
}
