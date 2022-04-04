#import <Foundation/Foundation.h>

#define MACRO_STRING_(m) #m
#define MACRO_STRING(m) @MACRO_STRING_(m)

static NSString * const AppBundleIdentifier = MACRO_STRING(APP_BUNDLE_IDENTIFIER);
static NSString * const SignerBundleIdentifier = MACRO_STRING(SIGNER_BUNDLE_IDENTIFIER);
static NSString * const AppGroupIdentifier = MACRO_STRING(APP_GROUP_IDENTIFIER);
