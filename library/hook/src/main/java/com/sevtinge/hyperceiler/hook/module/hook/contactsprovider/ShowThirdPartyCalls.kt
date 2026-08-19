package com.sevtinge.hyperceiler.hook.module.hook.contactsprovider

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.hookBeforeAllMethods

object ShowThirdPartyCalls : BaseHook() {
    override fun init() {
        val sqliteDbClass = findClassIfExists("android.database.sqlite.SQLiteDatabase") ?: return
        
        sqliteDbClass.hookBeforeAllMethods("rawQueryWithFactory") { param ->
            val sql = param.args[1] as? String ?: return@hookBeforeAllMethods
            
            val miuiFilter = " AND subscription_component_name='com.android.phone/com.android.services.telephony.TelephonyConnectionService'"
            
            if (sql.contains(miuiFilter)) {
                param.args[1] = sql.replace(miuiFilter, "")
            }
        }
    }
}
