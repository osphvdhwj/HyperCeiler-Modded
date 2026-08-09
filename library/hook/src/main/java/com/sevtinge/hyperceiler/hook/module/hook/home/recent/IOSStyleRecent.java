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
        findAndHookMethod(Settings.Global.class, "getInt", ContentResolver.class, String.class, int.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                String name = (String) param.args[1];
                if ("task_stack_view_layout_style".equals(name)) {
                    param.setResult(2);
                }
            }
        });

        Class<?> recentsUtilsCls = findClassIfExists("com.miui.home.launcher.RecentsAndFSGestureUtils");
        if (recentsUtilsCls == null) {
            recentsUtilsCls = findClassIfExists("com.miui.home.recents.util.RecentsAndGestureUtils");
        }
        if (recentsUtilsCls != null) {
            hookAllMethods(recentsUtilsCls, "isRecentsHorizontal", returnConstant(false));
            hookAllMethods(recentsUtilsCls, "isRecentsLayoutHorizontal", returnConstant(false));
            hookAllMethods(recentsUtilsCls, "isRecentsVertical", returnConstant(true));
            hookAllMethods(recentsUtilsCls, "isRecentsLayoutVertical", returnConstant(true));
        }

        Class<?> recentsModelCls = findClassIfExists("com.miui.home.recents.RecentsModel");
        if (recentsModelCls != null) {
            hookAllMethods(recentsModelCls, "isRecentsHorizontal", returnConstant(false));
            hookAllMethods(recentsModelCls, "isRecentsVertical", returnConstant(true));
        }

        Class<?> cornerRadiusUtil = findClassIfExists("com.miui.home.recents.util.WindowCornerRadiusUtil");
        if (cornerRadiusUtil != null) {
            hookAllMethods(cornerRadiusUtil, "getTaskViewCornerRadius", new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    param.setResult(90);
                }
            });
        }

        Class<?> horizontalStyle = findClassIfExists("com.miui.home.recents.TaskStackViewLayoutStyleHorizontal");
        if (horizontalStyle != null) {
            findAndHookMethod(horizontalStyle, "createScaleDismissAnimation", View.class, float.class, new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    View view = (View) param.args[0];
                    int screenHeight = view.getResources().getDisplayMetrics().heightPixels;
                    ObjectAnimator transY = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, view.getTranslationY(), -screenHeight * 1.1f);
                    ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, view.getScaleX(), 0.8f);
                    ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, view.getScaleY(), 0.8f);
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
