@file:OptIn(ExperimentalForeignApi::class)

package com.conradkramer.wallet

import io.github.oshai.kotlinlogging.KLogger
import io.ktor.utils.io.core.toByteArray
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import kotlinx.cinterop.utf8
import kotlinx.cinterop.value
import kotlinx.coroutines.suspendCancellableCoroutine
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
import platform.CoreFoundation.CFStringGetCString
import platform.CoreFoundation.CFStringGetLength
import platform.CoreFoundation.CFStringGetMaximumSizeForEncoding
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSNumber
import platform.Foundation.NSString
import platform.Foundation.numberWithInt
import platform.Foundation.stringWithUTF8String
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthentication
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics
import platform.Security.SecAccessControlCreateWithFlags
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecKeyCopyPublicKey
import platform.Security.SecKeyCreateDecryptedData
import platform.Security.SecKeyCreateEncryptedData
import platform.Security.SecKeyCreateRandomKey
import platform.Security.SecKeyRef
import platform.Security.errSecDuplicateItem
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAccessControlPrivateKeyUsage
import platform.Security.kSecAccessControlUserPresence
import platform.Security.kSecAttrAccessControl
import platform.Security.kSecAttrAccessGroup
import platform.Security.kSecAttrAccessibleWhenUnlockedThisDeviceOnly
import platform.Security.kSecAttrApplicationLabel
import platform.Security.kSecAttrIsPermanent
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
import platform.Security.kSecPrivateKeyAttrs
import platform.Security.kSecReturnAttributes
import platform.Security.kSecReturnRef
import platform.Security.kSecUseAuthenticationContext
import platform.Security.kSecUseDataProtectionKeychain
import kotlin.coroutines.resume

actual class BiometricPromptHost

@Suppress("UNCHECKED_CAST")
internal actual data class HardwareKeyStore(
    private val applicationGroup: String,
    private val logger: KLogger,
) : KeyStore<AuthenticationContext> {
    override val canStore: Boolean
        get() = LAContext().canEvaluatePolicy(LAPolicyDeviceOwnerAuthentication, null)

    override val all: Set<String>
        get() = memScoped {
            logger.info { "Enumerating keys" }

            val query = dictionary().autorelease(this)
            CFDictionarySetValue(query, kSecAttrKeyClass, kSecAttrKeyClassPrivate)
            CFDictionarySetValue(query, kSecMatchLimit, kSecMatchLimitAll)
            CFDictionarySetValue(query, kSecReturnAttributes, kCFBooleanTrue)

            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query, result.ptr)
            val values = result.value as CFArrayRef? ?: when (status) {
                errSecItemNotFound -> {
                    logger.info { "No keys were found" }
                    return emptySet()
                }
                else -> throw Exception("Error enumerating keys: $status")
            }
            values.autorelease(this)

            logger.info { "Successfully enumerated ${CFArrayGetCount(values)} keys" }

            return values.iterator()
                .mapNotNull { CFDictionaryGetValue(it as CFDictionaryRef, kSecAttrApplicationLabel) as CFDataRef? }
                .map(CFDataRef::toByteArray)
                .map(ByteArray::decodeToString)
                .toSet()
        }

    override fun generate(id: String) = generate(id, true)

    private fun generate(id: String, secure: Boolean = true): Unit = memScoped {
        logger.info { "Generating key $id" }

        val bitsRef = 256.bridgingRetain().autorelease(this)

        val create = dictionary(id).autorelease(this)
        CFDictionarySetValue(create, kSecAttrKeyType, kSecAttrKeyTypeEC)
        CFDictionarySetValue(create, kSecAttrKeySizeInBits, bitsRef)
        CFDictionarySetValue(create, kSecAttrIsPermanent, kCFBooleanTrue)
        if (secure) {
            if (!Platform.isSimulator) {
                val privateKeyAttributes = CFDictionaryCreateMutable(
                    null,
                    3,
                    kCFTypeDictionaryKeyCallBacks.ptr,
                    kCFTypeDictionaryValueCallBacks.ptr,
                )?.autorelease(this)

                val accessControl = SecAccessControlCreateWithFlags(
                    null,
                    kSecAttrAccessibleWhenUnlockedThisDeviceOnly,
                    kSecAccessControlPrivateKeyUsage or kSecAccessControlUserPresence,
                    null,
                )!!.autorelease(this)

                CFDictionarySetValue(
                    privateKeyAttributes,
                    kSecAttrAccessControl,
                    accessControl,
                )
                CFDictionarySetValue(create, kSecPrivateKeyAttrs, privateKeyAttributes)
            }

            CFDictionarySetValue(create, kSecAttrTokenID, kSecAttrTokenIDSecureEnclave)
        }

        val error = alloc<CFErrorRefVar>()
        val key = SecKeyCreateRandomKey(create, error.ptr)
            ?: when (val generateStatus = CFErrorGetCode(error.value).toInt()) {
                errSecDuplicateItem -> {
                    logger.info { "Key $id already exists in the keychain" }
                    delete(id)
                    generate(id)
                    return
                }
                errSecItemNotFound -> {
                    logger.info { "Key $id could not be made in the secure enclave, making an insecure key" }
                    generate(id, false)
                    return
                }
                else -> throw Exception("Error generating key: $generateStatus")
            }
        key.autorelease(this)

        logger.info { "Generated key $id successfully" }
    }

    override fun encrypt(id: String, data: ByteArray) = memScoped {
        logger.info { "Encrypting data of length ${data.size} with key $id" }

        val query = dictionary(id).autorelease(this)
        CFDictionarySetValue(query, kSecAttrKeyClass, kSecAttrKeyClassPrivate)
        CFDictionarySetValue(query, kSecReturnRef, kCFBooleanTrue)

        val result = alloc<CFTypeRefVar>()
        val copyStatus = SecItemCopyMatching(query, result.ptr)
        val privateKey = result.value as SecKeyRef? ?: throw Exception("Error fetching key $id: $copyStatus")
        privateKey.autorelease(this)

        val publicKey = SecKeyCopyPublicKey(privateKey)
            ?: throw Exception("Failed tp copy the public key from the private key")

        val error = alloc<CFErrorRefVar>()
        val encryptedData = SecKeyCreateEncryptedData(
            publicKey,
            kSecKeyAlgorithmECIESEncryptionCofactorX963SHA256AESGCM,
            data.bridgingRetain().autorelease(this),
            error.ptr,
        ) ?: when (val encryptStatus = CFErrorGetCode(error.value).toInt()) {
            else -> throw Exception("Error encrypting data with key $id: $encryptStatus")
        }
        encryptedData.autorelease(this)

        logger.info { "Encrypted data of length ${data.size} with key $id" }

        encryptedData.toByteArray()
    }

    override fun delete(id: String) = memScoped {
        logger.info { "Deleting key $id" }

        val query = dictionary(id).autorelease(this)
        when (val status = SecItemDelete(query)) {
            errSecSuccess -> logger.info { "Deleted key $id successfully" }
            errSecItemNotFound -> logger.warn { "Cannot delete key $id because it does not exist" }
            else -> throw Exception("Error deleting key $id: $status")
        }
    }

    override fun reset() = memScoped {
        logger.info { "Deleting all keys" }

        val query = dictionary().autorelease(this)
        when (val status = SecItemDelete(query)) {
            errSecSuccess -> logger.info { "Deleted all keys successfully" }
            errSecItemNotFound -> logger.warn { "No keys were found" }
            else -> throw Exception("Error deleting all keys: $status")
        }
    }

    override fun context(id: String) = AuthenticationContext(id)

    override suspend fun prompt(
        context: AuthenticationContext,
        host: BiometricPromptHost?,
        info: BiometricPromptInfo,
    ) = suspendCancellableCoroutine { continuation ->
        val inner = context.context
        inner.localizedCancelTitle = info.cancelTitle
        inner.evaluatePolicy(context.policy, info.reason) { success, error ->
            if (!success) logger.error { "Failed to evaluate local authentication policy: $error" }
            continuation.resume(success)
        }
        continuation.invokeOnCancellation { inner.invalidate() }
    }

    override fun <R> decrypt(
        context: AuthenticationContext,
        data: ByteArray,
        handler: (data: ByteArray) -> R,
    ): R = memScoped {
        logger.info { "Decrypting data of size ${data.size} with key ${context.id}" }

        val query = dictionary(context.id).autorelease(this)
        CFDictionarySetValue(query, kSecAttrKeyClass, kSecAttrKeyClassPrivate)
        CFDictionarySetValue(query, kSecReturnRef, kCFBooleanTrue)
        CFDictionarySetValue(
            query,
            kSecUseAuthenticationContext,
            CFBridgingRetain(context.context)?.autorelease(this),
        )

        val result = alloc<CFTypeRefVar>()
        val copyStatus = SecItemCopyMatching(query, result.ptr)
        val key = result.value as SecKeyRef? ?: throw Exception("Error fetching key ${context.id}: $copyStatus")
        key.autorelease(this)

        val error = alloc<CFErrorRefVar>()
        val decryptedData = SecKeyCreateDecryptedData(
            key,
            kSecKeyAlgorithmECIESEncryptionCofactorX963SHA256AESGCM,
            data.bridgingRetain().autorelease(this),
            error.ptr,
        ) ?: when (val decryptStatus = CFErrorGetCode(error.value).toInt()) {
            else -> throw Exception("Failed to decrypt data with key ${context.id}: $decryptStatus")
        }
        decryptedData.autorelease(this)

        handler(decryptedData.toByteArray())
    }

    private fun dictionary(id: String) = memScoped {
        val dictionary = dictionary()
        val labelRef = id.toByteArray().bridgingRetain().autorelease(this)
        CFDictionarySetValue(dictionary, kSecAttrApplicationLabel, labelRef)
        dictionary
    }

    private fun dictionary() = memScoped {
        val groupRef = applicationGroup.bridgingRetain().autorelease(this)

        val dictionary = CFDictionaryCreateMutable(
            null,
            10,
            kCFTypeDictionaryKeyCallBacks.ptr,
            kCFTypeDictionaryValueCallBacks.ptr,
        )!!
        CFDictionarySetValue(dictionary, kSecAttrAccessGroup, groupRef)
        CFDictionarySetValue(dictionary, kSecClass, kSecClassKey)
        CFDictionarySetValue(dictionary, kSecUseDataProtectionKeychain, kCFBooleanTrue)
        dictionary
    }
}

actual class AuthenticationContext(actual val id: String, val context: LAContext) {
    actual constructor(id: String) : this(id, LAContext())
    val policy = if (context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, null)) {
        LAPolicyDeviceOwnerAuthenticationWithBiometrics
    } else {
        LAPolicyDeviceOwnerAuthentication
    }

    val biometricsAvailable = (policy == LAPolicyDeviceOwnerAuthenticationWithBiometrics)
}


fun <T : CPointer<U>, U> MemScope.autorelease(value: T): T {
    defer { CFRelease(value as CFTypeRef) }
    return value
}

fun <T : CPointer<U>, U> T.autorelease(scope: MemScope): T = scope.autorelease(this)

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
private fun String.bridgingRetain() = memScoped {
    CFBridgingRetain(NSString.stringWithUTF8String(this@bridgingRetain.utf8.getPointer(this))) as CFStringRef
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun ByteArray.bridgingRetain() = asUByteArray().let {
    CFDataCreate(null, it.refTo(0), it.size.convert()) as CFDataRef
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun CFDataRef.toByteArray() = ByteArray(CFDataGetLength(this).convert())
    .also { CFDataGetBytes(this, CFRangeMake(0, it.size.convert()), it.asUByteArray().refTo(0)) }

internal fun CFStringRef.toKotlinString(): String {
    val size = CFStringGetMaximumSizeForEncoding(CFStringGetLength(this), kCFStringEncodingUTF8).toInt()
    return ByteArray(size)
        .also { CFStringGetCString(this@toKotlinString, it.refTo(0), size.convert(), kCFStringEncodingUTF8) }
        .decodeToString()
}

@Suppress("UNCHECKED_CAST")
private fun Int.bridgingRetain() = CFBridgingRetain(NSNumber.numberWithInt(this)) as CFNumberRef
