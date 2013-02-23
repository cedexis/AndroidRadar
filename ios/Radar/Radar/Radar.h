//
//  Radar.h
//  Radar
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface Radar : NSObject

+ (void)initializeRUMSession;
+ (void)enableReporting:(BOOL)enabled;

+ (void)scheduleRemoteProbing;
+ (NSUInteger)reportEvent:(NSString *)eventName;
+ (NSUInteger)reportEvent:(NSString *)eventName WithTags:(NSUInteger)tags;
+ (NSUInteger)reportSlice:(NSString *)sliceName Start:(BOOL)start;
+ (NSUInteger)reportProperty:(NSString *)property Value:(NSString *)value;
+ (NSUInteger)reportProperty:(NSString *)property Value:(NSString *)value ForReport:(NSUInteger)reportId;

@end
