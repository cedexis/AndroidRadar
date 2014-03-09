//
//  Radar.h
//  Radar
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface Radar : NSObject

- (BOOL)scheduleRemoteProbingWithZoneId:(NSUInteger)zoneId
                          AndCustomerId:(NSUInteger)customerId;

+ (instancetype)instance;

@end
