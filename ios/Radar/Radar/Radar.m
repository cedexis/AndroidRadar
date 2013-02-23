//
//  Radar.m
//  Radar
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import "Radar.h"

@implementation Radar

+ (void)initializeRUMSession {
    NSLog(@"initializeRUMSession");
}

+ (void)enableReporting:(BOOL)enabled {
    NSLog(@"Radar reporting %@", enabled == YES ? @"enabled" : @"disabled");
}

+ (void)scheduleRemoteProbing {
    NSLog(@"Scheduling remote probing");
}

+ (NSUInteger)reportEvent:(NSString *)eventName {
    return [self reportEvent:eventName WithTags:0];
}

+ (NSUInteger)reportEvent:(NSString *)eventName WithTags:(NSUInteger)tags {
    NSLog(@"reportEvent, eventName: \"%@\", tags: %lu", eventName, (unsigned long)tags);
    return 0;
}

+ (NSUInteger)reportSlice:(NSString *)sliceName Start:(BOOL)start {
    NSLog(@"reportSlice, sliceName: \"%@\" %@", sliceName, start == YES ? @"start" : @"end");
    return 0;
}

+ (NSUInteger)reportProperty:(NSString *)property Value:(NSString *)value {
    NSLog(@"reportProperty, property: \"%@\", value: \"%@\"", property, value);
    return 0;
}

+ (NSUInteger)reportProperty:(NSString *)property Value:(NSString *)value ForReport:(NSUInteger)reportId {
    NSLog(@"reportProperty, property: \"%@\", value: \"%@\", reportId: %lu", property, value, (unsigned long)reportId);
    return 0;
}

@end
