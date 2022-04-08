#import <CoreGraphics/CoreGraphics.h>

CF_ASSUME_NONNULL_BEGIN

typedef void * CGSConnectionID;

typedef CF_ENUM(uint32_t, CGSWindowEvent) {
    kCGSWindowIsObscured = 800,
    kCGSWindowIsUnobscured = 801,
    kCGSWindowIsOrderedIn = 802,
    kCGSWindowIsOrderedOut = 803,
    kCGSWindowIsTerminated = 804,
    kCGSWindowIsChangingScreens = 805,
    kCGSWindowDidMove = 806,
    kCGSWindowDidResize = 807,
    kCGSWindowDidChangeOrder = 808,
    kCGSWindowGeometryDidChange = 809,
    kCGSWindowMonitorDataPending = 810,
    kCGSWindowDidCreate = 811,
    kCGSWindowRightsGrantOffered = 812,
    kCGSWindowRightsGrantCompleted = 813,
    kCGSWindowRecordForTermination = 814,
    kCGSWindowIsVisible = 815,
    kCGSWindowIsInvisible = 816
};

typedef void (*SLSNotifyCallback)(CGSWindowEvent event,
                                  void *data,
                                  uint32_t length,
                                  void * __nullable context);

extern CGSConnectionID SLSMainConnectionID(void);

extern void SLSRegisterConnectionNotifyProc(CGSConnectionID connection,
                                            SLSNotifyCallback callback,
                                            CGSWindowEvent event,
                                            void * __nullable context);

extern void SLSRemoveConnectionNotifyProc(CGSConnectionID connection,
                                          SLSNotifyCallback callback,
                                          CGSWindowEvent event,
                                          void * __nullable context);

extern void SLSRequestNotificationsForWindows(CGSConnectionID connection,
                                              const CGWindowID *windows,
                                              uint32_t count);

extern CFTypeRef SLSWindowQueryWindows(CGSConnectionID connection, CFArrayRef __nullable windows, CFIndex count) CF_RETURNS_RETAINED;
extern CFTypeRef SLSWindowQueryResultCopyWindows(CFTypeRef query) CF_RETURNS_RETAINED;

extern CGError SLSWindowIsVisible(CGSConnectionID connection, CGWindowID window, BOOL *visible);

extern CFIndex SLSWindowIteratorGetCount(CFTypeRef iterator);
extern BOOL SLSWindowIteratorAdvance(CFTypeRef iterator);
extern CGWindowID SLSWindowIteratorGetWindowID(CFTypeRef iterator);
extern pid_t SLSWindowIteratorGetPID(CFTypeRef iterator);
extern CGRect SLSWindowIteratorGetBounds(CFTypeRef iterator);

CF_ASSUME_NONNULL_END
