package com.sevtinge.hyperceiler.hook.module.hook.home.recent;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ContentResolver;
import android.graphics.RectF;
import android.provider.Settings;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

public class IOSStyleRecent extends BaseHook {
    @Override
    public void init() {
        int cornerRadius = mPrefsMap.getInt("task_view_corners", 36);

        // 1. Force Horizontal layout style 1 (Horizontal Mode ONLY like iOS & HyperOS 3 Stacked)
        findAndHookMethod(Settings.Global.class, "getInt", ContentResolver.class, String.class, int.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                String name = (String) param.args[1];
                if ("task_stack_view_layout_style".equals(name)) {
                    param.setResult(1); // 1 = Horizontal Stacked Layout
                }
            }
        });

        // 2. Enforce Horizontal Recents across all MIUI launcher utilities (STRICT Horizontal ONLY)
        Class<?> recentsUtilsCls = findClassIfExists("com.miui.home.launcher.RecentsAndFSGestureUtils");
        if (recentsUtilsCls == null) {
            recentsUtilsCls = findClassIfExists("com.miui.home.recents.util.RecentsAndGestureUtils");
        }
        if (recentsUtilsCls != null) {
            hookAllMethods(recentsUtilsCls, "isRecentsHorizontal", MethodHook.returnConstant(true));
            hookAllMethods(recentsUtilsCls, "isRecentsLayoutHorizontal", MethodHook.returnConstant(true));
            hookAllMethods(recentsUtilsCls, "isRecentsVertical", MethodHook.returnConstant(false));
            hookAllMethods(recentsUtilsCls, "isRecentsLayoutVertical", MethodHook.returnConstant(false));
            hookAllMethods(recentsUtilsCls, "getRecentsLayoutStyle", MethodHook.returnConstant(1));
        }

        Class<?> recentsModelCls = findClassIfExists("com.miui.home.recents.RecentsModel");
        if (recentsModelCls != null) {
            hookAllMethods(recentsModelCls, "isRecentsHorizontal", MethodHook.returnConstant(true));
            hookAllMethods(recentsModelCls, "isRecentsVertical", MethodHook.returnConstant(false));
            hookAllMethods(recentsModelCls, "getRecentsLayoutStyle", MethodHook.returnConstant(1));
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

        // 4. Copy iOS / HyperOS 3 Stacked Card Overlap & Scale Algorithm
        Class<?> horizontalAlgorithm = findClassIfExists("com.miui.home.recents.views.TaskStackViewsAlgorithmHorizontal");
        if (horizontalAlgorithm != null) {
            hookAllMethods(horizontalAlgorithm, "scaleTaskView", new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    RectF rect = (RectF) param.args[0];
                    callStaticMethod(findClassIfExists("com.miui.home.recents.util.Utilities"), "scaleRectAboutCenter", rect, 0.88f);
                }
            });
        }

        // 5. Custom Task View Card Corner Radius (iOS Style)
        Class<?> cornerRadiusUtil = findClassIfExists("com.miui.home.recents.util.WindowCornerRadiusUtil");
        if (cornerRadiusUtil != null) {
            hookAllMethods(cornerRadiusUtil, "getTaskViewCornerRadius", new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    param.setResult(cornerRadius * 3);
                }
            });
        }

        // 6. iOS-style swipe up dismiss animation
        Class<?> horizontalStyle = findClassIfExists("com.miui.home.recents.TaskStackViewLayoutStyleHorizontal");
        if (horizontalStyle != null) {
            findAndHookMethod(horizontalStyle, "createScaleDismissAnimation", View.class, float.class, new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    View view = (View) param.args[0];
                    int screenHeight = view.getResources().getDisplayMetrics().heightPixels;
                    ObjectAnimator transY = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, view.getTranslationY(), -screenHeight * 1.1f);
                    ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, view.getScaleX(), 0.75f);
                    ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, view.getScaleY(), 0.75f);
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
