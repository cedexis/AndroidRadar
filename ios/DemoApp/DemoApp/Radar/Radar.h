//
//  Radar.h
//  Radar
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "RadarCommunication.h"

@interface Radar : NSObject

- (void)startRUMInitWithZoneId:(NSUInteger)zoneId
                    CustomerId:(NSUInteger)customerId
                   CompletionQueue:(dispatch_queue_t)initCompletionQueue
                     InitCompletion:(radar_comm_complete_block_t)initCompletion;

- (void)enableReporting:(BOOL)enabled;
- (void)enableReporting:(BOOL)enabled WithPollingInterval:(NSInteger)interval;
- (void)flush;

- (BOOL)scheduleRemoteProbingWithZoneId:(NSUInteger)zoneId
                          AndCustomerId:(NSUInteger)customerId;
- (NSUInteger)reportEvent:(NSString *)eventName;
- (NSUInteger)reportEvent:(NSString *)eventName WithTags:(NSUInteger)tags;
- (void)reportSlice:(NSString *)name Start:(BOOL)start;
- (void)reportProperty:(NSString *)property Value:(NSString *)value ForReport:(NSUInteger)reportId;
- (void)reportProperty:(NSString *)property Value:(NSString *)value;

+ (instancetype)instance;

@end
