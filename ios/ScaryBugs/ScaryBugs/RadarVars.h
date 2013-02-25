//
//  RadarVars.h
//  ScaryBugs
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#ifndef ScaryBugs_RadarFlags_h
#define ScaryBugs_RadarFlags_h

// RUM tags
typedef NS_ENUM(NSUInteger, RadarTags) {
    // Individual parts of the app
    RadarTagsAppDelegate = 1 << 0,
    RadarTagsMasterViewController = 1 << 1,
    RadarTagsDetailViewController = 1 << 2,
    
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
FOUNDATION_EXPORT NSString *const RadarEventsViewDidAppear;
FOUNDATION_EXPORT NSString *const RadarEventsViewWillDisappear;
FOUNDATION_EXPORT NSString *const RadarEventsViewDidLoad;
FOUNDATION_EXPORT NSString *const RadarEventsAddTapped;
FOUNDATION_EXPORT NSString *const RadarEventsNewImageSelected;
FOUNDATION_EXPORT NSString *const RadarEventsNewImagePresented;
FOUNDATION_EXPORT NSString *const RadarEventsRatingChanged;

// RUM slices
FOUNDATION_EXPORT NSString *const RadarSlicesDetailView;

// RUM properties
FOUNDATION_EXPORT NSString *const RadarPropertiesUserId;
FOUNDATION_EXPORT NSString *const RadarPropertiesRating;

#endif
