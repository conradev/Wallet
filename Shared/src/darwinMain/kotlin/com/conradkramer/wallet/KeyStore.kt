package com.conradkramer.wallet

import com.conradkramer.wallet.crypto.SHA256Digest
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.utf8
import kotlinx.cinterop.value
import org.koin.dsl.module
import platform.CoreFoundation.CFArrayGetCount
import platform.CoreFoundation.CFArrayGetValueAtIndex
import platform.CoreFoundation.CFArrayRef
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDataGetBytes
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionaryGetValue
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFErrorGetCode
import platform.CoreFoundation.CFErrorRefVar
import platform.CoreFoundation.CFIndex
import platform.CoreFoundation.CFNumberRef
import platform.CoreFoundation.CFRangeMake
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSNumber
import platform.Foundation.NSString
import platform.Foundation.numberWithInt
import platform.Foundation.stringWithUTF8String
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.kLAPolicyDeviceOwnerAuthentication
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecItemUpdate
import platform.Security.SecKeyCopyPublicKey
import platform.Security.SecKeyCreateDecryptedData
import platform.Security.SecKeyCreateEncryptedData
import platform.Security.SecKeyCreateRandomKey
import platform.Security.SecKeyRef
import platform.Security.errSecDuplicateItem
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessControl
import platform.Security.kSecAttrAccessGroup
import platform.Security.kSecAttrApplicationLabel
import platform.Security.kSecAttrApplicationTag
import platform.Security.kSecAttrKeyClass
import platform.Security.kSecAttrKeyClassPrivate
import platform.Security.kSecAttrKeySizeInBits
import platform.Security.kSecAttrKeyType
import platform.Security.kSecAttrKeyTypeEC
import platform.Security.kSecAttrTokenID
import platform.Security.kSecAttrTokenIDSecureEnclave
import platform.Security.kSecClass
import platform.Security.kSecClassKey
import platform.Security.kSecKeyAlgorithmECIESEncryptionCofactorX963SHA256AESGCM
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitAll
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecPrivateKeyAttrs
import platform.Security.kSecReturnAttributes
import platform.Security.kSecReturnRef
import platform.Security.kSecUseAuthenticationContext
import platform.Security.kSecUseDataProtectionKeychain
import platform.Security.kSecValueRef

internal actual data class Authentication(val context: LAContext)

@Suppress("UNCHECKED_CAST")
internal actual class KeyStore(private val applicationGroup: String) {

    actual val canStore: Boolean
        get() = LAContext().canEvaluatePolicy(kLAPolicyDeviceOwnerAuthentication.convert(), null)

    actual val all: List<String>
        get() {
            memScoped {
                val groupRef = applicationGroup.bridgingRetain().autorelease(this)

                val query = CFDictionaryCreateMutable(
                    null,
                    6,
                    kCFTypeDictionaryKeyCallBacks.ptr,
                    kCFTypeDictionaryValueCallBacks.ptr
                )?.autorelease(this)
                CFDictionarySetValue(query, kSecAttrAccessGroup, groupRef)
                CFDictionarySetValue(query, kSecClass, kSecClassKey)
                CFDictionarySetValue(query, kSecAttrKeyClass, kSecAttrKeyClassPrivate)
                CFDictionarySetValue(query, kSecAttrTokenID, kSecAttrTokenIDSecureEnclave)
                CFDictionarySetValue(query, kSecMatchLimit, kSecMatchLimitAll)
                CFDictionarySetValue(query, kSecReturnAttributes, kCFBooleanTrue)

                val attributes = alloc<CFTypeRefVar>()
                when (val status = SecItemCopyMatching(query, attributes.ptr)) {
                    errSecSuccess -> {}
                    errSecItemNotFound -> {
                        return listOf()
                    }
                    else -> throw Exception("Error fetching seeds: $status")
                }

                val values = (attributes.value as CFArrayRef).autorelease(this)
                return values.iterator()
                    .mapNotNull {
                        CFDictionaryGetValue(
                            it as CFDictionaryRef,
                            kSecAttrApplicationLabel
                        )
                    }
                    .map { (it as CFDataRef).toByteArray().encodeHex() }
            }
        }

    actual fun add(data: ByteArray): String {
        val digest = SHA256Digest().digest(data)
        val label = digest.encodeHex()

        memScoped {
            val groupRef = applicationGroup.bridgingRetain().autorelease(this)
            val labelRef = digest.bridgingRetain().autorelease(this)
            val bitsRef = 256.bridgingRetain().autorelease(this)

            val accessControl = keyStoreAccessControlCreate()?.autorelease(this)

            val privateKeyAttributes = CFDictionaryCreateMutable(
                null,
                1,
                kCFTypeDictionaryKeyCallBacks.ptr,
                kCFTypeDictionaryValueCallBacks.ptr
            )?.autorelease(this)
            if (accessControl != null) {
                CFDictionarySetValue(privateKeyAttributes, kSecAttrAccessControl, accessControl)
            }

            val createAttributes = CFDictionaryCreateMutable(
                null,
                7,
                kCFTypeDictionaryKeyCallBacks.ptr,
                kCFTypeDictionaryValueCallBacks.ptr
            )?.autorelease(this)
            CFDictionarySetValue(createAttributes, kSecAttrAccessGroup, groupRef)
            CFDictionarySetValue(createAttributes, kSecAttrKeyType, kSecAttrKeyTypeEC)
            CFDictionarySetValue(createAttributes, kSecAttrKeySizeInBits, bitsRef)
            CFDictionarySetValue(createAttributes, kSecAttrTokenID, kSecAttrTokenIDSecureEnclave)
            CFDictionarySetValue(createAttributes, kSecUseDataProtectionKeychain, kCFBooleanTrue)
            CFDictionarySetValue(createAttributes, kSecAttrApplicationLabel, labelRef)
            CFDictionarySetValue(createAttributes, kSecPrivateKeyAttrs, privateKeyAttributes)

            val error = alloc<CFErrorRefVar>()
            val privateKey = SecKeyCreateRandomKey(createAttributes, error.ptr)

            if (privateKey == null) {
                when (val status = CFErrorGetCode(error.value).toInt()) {
                    errSecSuccess -> {}
                    errSecDuplicateItem -> {
                        return@add label
                    }
                    else -> throw Exception("Error generating key: $status")
                }
            }

            val publicKey = SecKeyCopyPublicKey(privateKey)?.autorelease(this)
            val dataRef = data.bridgingRetain().autorelease(this)
            val encryptedDataRef = SecKeyCreateEncryptedData(
                publicKey,
                kSecKeyAlgorithmECIESEncryptionCofactorX963SHA256AESGCM,
                dataRef,
                error.ptr
            )?.autorelease(this)

            val queryAttributes = CFDictionaryCreateMutable(
                null,
                5,
                kCFTypeDictionaryKeyCallBacks.ptr,
                kCFTypeDictionaryValueCallBacks.ptr
            )?.autorelease(this)
            CFDictionarySetValue(queryAttributes, kSecAttrAccessGroup, groupRef)
            CFDictionarySetValue(queryAttributes, kSecClass, kSecClassKey)
            CFDictionarySetValue(queryAttributes, kSecAttrKeyClass, kSecAttrKeyClassPrivate)
            CFDictionarySetValue(queryAttributes, kSecMatchLimit, kSecMatchLimitAll)
            CFDictionarySetValue(queryAttributes, kSecAttrApplicationLabel, labelRef)

            val updateAtttributes = CFDictionaryCreateMutable(
                null,
                1,
                kCFTypeDictionaryKeyCallBacks.ptr,
                kCFTypeDictionaryValueCallBacks.ptr
            )?.autorelease(this)
            CFDictionarySetValue(updateAtttributes, kSecAttrApplicationTag, encryptedDataRef)

            when (val status = SecItemUpdate(queryAttributes, updateAtttributes)) {
                errSecSuccess -> {}
                else -> throw Exception("Error updating key with encrypted seed: $status")
            }

            return label
        }
    }

    actual fun delete(id: String) {
        memScoped {
            val groupRef = applicationGroup.bridgingRetain().autorelease(this)
            val labelRef = id.decodeHex().bridgingRetain().autorelease(this)

            val query = CFDictionaryCreateMutable(
                null,
                4,
                kCFTypeDictionaryKeyCallBacks.ptr,
                kCFTypeDictionaryValueCallBacks.ptr
            )?.autorelease(this)
            CFDictionarySetValue(query, kSecAttrAccessGroup, groupRef)
            CFDictionarySetValue(query, kSecClass, kSecClassKey)
            CFDictionarySetValue(query, kSecMatchLimit, kSecMatchLimitOne)
            CFDictionarySetValue(query, kSecAttrApplicationLabel, labelRef)

            when (val status = SecItemDelete(query)) {
                errSecSuccess -> {}
                else -> throw Exception("Error deleting seed: $status")
            }
        }
    }

    actual fun use(authentication: Authentication, id: String, use: (data: ByteArray) -> Unit) {
        memScoped {
            val groupRef = applicationGroup.bridgingRetain().autorelease(this)
            val labelRef = id.decodeHex().bridgingRetain().autorelease(this)

            val query = CFDictionaryCreateMutable(
                null,
                6,
                kCFTypeDictionaryKeyCallBacks.ptr,
                kCFTypeDictionaryValueCallBacks.ptr
            )?.autorelease(this)
            CFDictionarySetValue(query, kSecAttrAccessGroup, groupRef)
            CFDictionarySetValue(query, kSecClass, kSecClassKey)
            CFDictionarySetValue(query, kSecAttrKeyClass, kSecAttrKeyClassPrivate)
            CFDictionarySetValue(query, kSecAttrTokenID, kSecAttrTokenIDSecureEnclave)
            CFDictionarySetValue(query, kSecMatchLimit, kSecMatchLimitOne)
            CFDictionarySetValue(query, kSecAttrApplicationLabel, labelRef)
            CFDictionarySetValue(query, kSecReturnAttributes, kCFBooleanTrue)
            CFDictionarySetValue(query, kSecReturnRef, kCFBooleanTrue)
            CFDictionarySetValue(
                query,
                kSecUseAuthenticationContext,
                CFBridgingRetain(authentication.context)?.autorelease(this)
            )

            val attributes = alloc<CFTypeRefVar>()
            when (val status = SecItemCopyMatching(query, attributes.ptr)) {
                errSecSuccess -> {}
                else -> throw Exception("Error fetching seed: $status")
            }

            val result = (attributes.value as CFDictionaryRef).autorelease(this)
            val key = CFDictionaryGetValue(result, kSecValueRef) as SecKeyRef?
            val encryptedData = CFDictionaryGetValue(result, kSecAttrApplicationTag) as CFDataRef?

            val error = alloc<CFErrorRefVar>()
            val data = SecKeyCreateDecryptedData(
                key,
                kSecKeyAlgorithmECIESEncryptionCofactorX963SHA256AESGCM,
                encryptedData,
                error.ptr
            )?.autorelease(this)

            if (data == null) {
                val status = CFErrorGetCode(error.value)
                throw Exception("Error decrypting seed key: $status")
            }

            use(data.toByteArray())
        }
    }
}

internal actual fun keyStoreModule() = module {
    single { KeyStore(getProperty("app_group_identifier")) }
}

internal expect fun keyStoreAccessControlCreate(): CFTypeRef?

private fun <T : CPointer<U>, U> MemScope.autorelease(value: T): T {
    defer { CFRelease(value as CFTypeRef) }
    return value
}

private fun <T : CPointer<U>, U> T.autorelease(scope: MemScope): T = scope.autorelease(this)

private data class CFArrayIterator(val array: CFArrayRef) : Iterator<CFTypeRef>, Iterable<CFTypeRef> {
    private var index: CFIndex = 0
    private val count = CFArrayGetCount(array)

    override fun hasNext(): Boolean {
        return index < count
    }

    override fun next(): CFTypeRef {
        return CFArrayGetValueAtIndex(array, index++) as CFTypeRef
    }

    override fun iterator(): Iterator<CFTypeRef> {
        return this
    }
}

private fun CFArrayRef.iterator() = CFArrayIterator(this)

@Suppress("UNCHECKED_CAST")
private fun String.bridgingRetain(): CFStringRef {
    return memScoped {
        CFBridgingRetain(NSString.stringWithUTF8String(this@bridgingRetain.utf8.getPointer(this))) as CFStringRef
    }
}

private fun ByteArray.bridgingRetain(): CFDataRef {
    return this.toUByteArray().usePinned { pinned ->
        CFDataCreate(null, pinned.addressOf(0), this@bridgingRetain.size.toLong()) as CFDataRef
    }
}

private fun CFDataRef.toByteArray(): ByteArray {
    val length = CFDataGetLength(this).toInt()
    val byteArray = UByteArray(length).apply {
        usePinned {
            CFDataGetBytes(this@toByteArray, CFRangeMake(0, length.toLong()), it.addressOf(0))
        }
    }
    return byteArray.asByteArray()
}

@Suppress("UNCHECKED_CAST")
private fun Int.bridgingRetain(): CFNumberRef {
    return CFBridgingRetain(NSNumber.numberWithInt(this)) as CFNumberRef
}
