#import <Foundation/Foundation.h>

#define MACRO_STRING_(m) #m
#define MACRO_STRING(m) @MACRO_STRING_(m)

static NSString * const AppBundleIdentifier = MACRO_STRING(APP_BUNDLE_IDENTIFIER);
static NSString * const ViewServiceBundleIdentifier = MACRO_STRING(VIEW_SERVICE_BUNDLE_IDENTIFIER);
static NSString * const AppGroupIdentifier = MACRO_STRING(APP_GROUP_IDENTIFIER);
