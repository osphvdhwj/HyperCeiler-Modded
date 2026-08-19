package com.sevtinge.hyperceiler.hook.module.hook.home.layout

import android.app.Activity
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.hookBeforeAllMethods

object CustomSearchBarProvider : BaseHook() {
    override fun init() {
        val activityClass = findClassIfExists("android.app.Activity") ?: return

        activityClass.hookBeforeAllMethods("startActivity") { param ->
            val intent = param.args[0] as? android.content.Intent ?: return@hookBeforeAllMethods
            
            val componentName = intent.component?.className ?: ""
            val action = intent.action ?: ""
            
            val isGoogleSearch = componentName.contains("googlequicksearchbox.SearchActivity")
            val isMiuiSearch = componentName.contains("com.android.quicksearchbox")
            val isWebSearchAction = action == "android.intent.action.WEB_SEARCH"

            if (isGoogleSearch || isMiuiSearch || isWebSearchAction) {
                val customPkgSet = mPrefsMap.getStringSet("home_layout_searchbar_custom_provider")
                val customPkg = customPkgSet.firstOrNull()
                
                if (!customPkg.isNullOrEmpty()) {
                    val context = param.thisObject as? Activity ?: return@hookBeforeAllMethods
                    val newIntent = context.packageManager.getLaunchIntentForPackage(customPkg)
                    
                    if (newIntent != null) {
                        param.args[0] = newIntent
                    }
                }
            }
        }
    }
}
