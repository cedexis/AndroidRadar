//
//  ProbeServerQuery.m
//  Radar
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import "ProbeServerQuery.h"

@interface ProbeServerQuery()
@property NSUInteger requestorZoneId;
@property NSUInteger requestorCustomerId;
@property NSArray* providerIds;
@property BOOL wifi;

@end

@implementation ProbeServerQuery

@synthesize requestorZoneId = _requestorZoneId;
@synthesize requestorCustomerId = _requestorCustomerId;
@synthesize providerIds = _providerIds;
@synthesize wifi = _wifi;

- (id)initWithRequestorZoneId:(NSUInteger)requestorZoneId
          RequestorCustomerId:(NSUInteger)requestorCustomerId
                  ProviderIds:(NSArray *)providerIds
                       OnWifi:(BOOL)wifi {
    if (self = [super init]) {
        self.requestorZoneId = requestorZoneId;
        self.requestorCustomerId = requestorCustomerId;
        self.providerIds = providerIds;
        self.wifi = wifi;
    }
    return self;
}

- (NSString *)hostname {
    return @"probes.cedexis.com";
}

- (NSArray *)pathParts {
    return [[NSArray alloc] init];
}

- (NSDictionary *)dictionaryFrom:(NSData *)data {
    return [[NSDictionary alloc] init];
}

- (NSString *)queryString {
    NSString *providerIds = [self.providerIds componentsJoinedByString:@","];
    NSLog(@"Provider ids: %@", providerIds);
    
    NSMutableString *result = [[NSMutableString alloc] init];
    [result appendFormat:@"z=%lu", (unsigned long)self.requestorZoneId];
    [result appendFormat:@"&c=%lu", (unsigned long)self.requestorCustomerId];
    
    if (0 < [providerIds length]) {
        [result appendFormat:@"&i=%@", providerIds];
    }
    
    [result appendString:@"&fmt=json"];
    [result appendString:@"&m=1"];
    
    if (self.wifi) {
        [result appendString:@"&allowThroughput=1"];
    }
    
    return result;
}

@end
