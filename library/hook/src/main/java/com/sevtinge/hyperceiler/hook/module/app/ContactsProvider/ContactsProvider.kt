package com.sevtinge.hyperceiler.hook.module.app.ContactsProvider

import com.hchen.database.HookBase
import com.sevtinge.hyperceiler.hook.module.base.BaseModule
import com.sevtinge.hyperceiler.hook.module.hook.contactsprovider.ShowThirdPartyCalls

@HookBase(targetPackage = "com.android.providers.contacts")
class ContactsProvider : BaseModule() {
    override fun handleLoadPackage() {
        initHook(ShowThirdPartyCalls, mPrefsMap.getBoolean("contacts_show_third_party_calls"))
    }
}
