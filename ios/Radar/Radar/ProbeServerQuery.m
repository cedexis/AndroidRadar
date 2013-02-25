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

@end

@implementation ProbeServerQuery

@synthesize requestorZoneId = _requestorZoneId;
@synthesize requestorCustomerId = _requestorCustomerId;
@synthesize providerIds = _providerIds;

- (id)initWithRequestorZoneId:(NSUInteger)requestorZoneId
          RequestorCustomerId:(NSUInteger)requestorCustomerId
                  ProviderIds:(NSArray *)providerIds {
    if (self = [super init]) {
        self.requestorZoneId = requestorZoneId;
        self.requestorCustomerId = requestorCustomerId;
        self.providerIds = providerIds;
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
    return result;
}

@end
