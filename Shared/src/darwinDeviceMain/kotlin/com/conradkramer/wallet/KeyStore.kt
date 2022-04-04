package com.conradkramer.wallet

import platform.CoreFoundation.CFTypeRef
import platform.Security.SecAccessControlCreateWithFlags
import platform.Security.kSecAccessControlPrivateKeyUsage
import platform.Security.kSecAccessControlUserPresence
import platform.Security.kSecAttrAccessibleWhenUnlockedThisDeviceOnly

internal actual fun keyStoreAccessControlCreate(): CFTypeRef? {
    return SecAccessControlCreateWithFlags(
        null,
        kSecAttrAccessibleWhenUnlockedThisDeviceOnly,
        kSecAccessControlPrivateKeyUsage or kSecAccessControlUserPresence,
        null
    )
}
