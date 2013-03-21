//
//  RadarVars.h
//  DemoApp
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#ifndef DemoApp_RadarVars_h
#define DemoApp_RadarVars_h

// RUM tags
typedef NS_ENUM(NSUInteger, RadarTags) {
    // Individual parts of the app
    RadarTagsAppDelegate = 1 << 0,
    
    // Importance
    RadarTagsLevelDebug = 1 << 59,
    RadarTagsLevelInfo = 1 << 60,
    RadarTagsLevelWarning = 1 << 61,
    RadarTagsLevelError = 1 << 62,
    RadarTagsLevelCritical = 1 << 63
};

// RUM events
FOUNDATION_EXPORT NSString *const RadarEventsAppDidFinishLaunching;
FOUNDATION_EXPORT NSString *const RadarEventsAppWillResignActive;
FOUNDATION_EXPORT NSString *const RadarEventsAppDidEnterBackground;
FOUNDATION_EXPORT NSString *const RadarEventsAppWillEnterForeground;
FOUNDATION_EXPORT NSString *const RadarEventsAppDidBecomeActive;
FOUNDATION_EXPORT NSString *const RadarEventsUserClearedDatabase;
FOUNDATION_EXPORT NSString *const RadarEventsUserEmail;
FOUNDATION_EXPORT NSString *const RadarEventsShowAboutViewLoadStart;
FOUNDATION_EXPORT NSString *const RadarEventsShowAboutViewLoadEnd;
FOUNDATION_EXPORT NSString *const RadarEventsSpeedTest;
FOUNDATION_EXPORT NSString *const RadarEventsUserRemoteProbing;

// RUM slices
FOUNDATION_EXPORT NSString *const RadarSliceAppActive;
FOUNDATION_EXPORT NSString *const RadarSliceAboutView;

// RUM properties
FOUNDATION_EXPORT NSString *const RadarPropertiesDeviceId;
FOUNDATION_EXPORT NSString *const RadarPropertiesDeviceName;
FOUNDATION_EXPORT NSString *const RadarPropertiesDeviceSystemName;
FOUNDATION_EXPORT NSString *const RadarPropertiesDeviceSystemVersion;
FOUNDATION_EXPORT NSString *const RadarPropertiesUserEmailResult;

#endif
