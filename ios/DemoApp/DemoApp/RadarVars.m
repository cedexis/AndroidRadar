//
//  RadarVars.m
//  DemoApp
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import "RadarVars.h"

// RUM events
NSString *const RadarEventsAppDidFinishLaunching = @"app did finish launching";
NSString *const RadarEventsAppWillResignActive = @"app will resign active";
NSString *const RadarEventsAppDidEnterBackground = @"app did enter background";
NSString *const RadarEventsAppWillEnterForeground = @"app will enter foreground";
NSString *const RadarEventsAppDidBecomeActive = @"app did become active";
NSString *const RadarEventsUserClearedDatabase = @"user cleared database";
NSString *const RadarEventsUserEmail = @"user email";
NSString *const RadarEventsShowAboutViewLoadStart = @"about view load start";
NSString *const RadarEventsShowAboutViewLoadEnd = @"about view load end";
NSString *const RadarEventsSpeedTest = @"speed test";
NSString *const RadarEventsUserRemoteProbing = @"remote probing scheduled";

// RUM slices
NSString *const RadarSliceAppActive = @"app active";
NSString *const RadarSliceAboutView = @"about view";

// RUM properties
NSString *const RadarPropertiesDeviceId = @"device id";
NSString *const RadarPropertiesDeviceName = @"device name";
NSString *const RadarPropertiesDeviceSystemName = @"device system name";
NSString *const RadarPropertiesDeviceSystemVersion = @"device system version";
NSString *const RadarPropertiesUserEmailResult = @"user email result";
