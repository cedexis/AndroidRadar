//
//  ProbeReport.m
//  Radar
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import "ProbeReport.h"

@interface ProbeReport()
@property NSUInteger providerZoneId;
@property NSUInteger providerCustomerId;
@property NSUInteger providerId;
@property NSInteger probeTypeNum;
@property NSInteger responseCode;
@property NSInteger measurement;
@property (copy) NSString *requestSignature;

@end

@implementation ProbeReport

@synthesize providerZoneId = _providerZoneId;
@synthesize providerCustomerId = _providerCustomerId;
@synthesize providerId = _providerId;
@synthesize probeTypeNum = _probeTypeNum;
@synthesize responseCode = _responseCode;
@synthesize measurement = _measurement;
@synthesize requestSignature = _requestSignature;

- (id)initWithProviderZoneId:(NSUInteger)providerZoneId
          ProviderCustomerId:(NSUInteger)providerCustomerId
                  ProviderId:(NSUInteger)providerId
                ProbeTypeNum:(NSInteger)probeTypeNum
                ResponseCode:(NSInteger)responseCode
                 Measurement:(NSInteger)measurement
         AndRequestSignature:(NSString *)requestSignature {
    if (self = [super init]) {
        self.providerZoneId = providerZoneId;
        self.providerCustomerId = providerCustomerId;
        self.providerId = providerId;
        self.probeTypeNum = probeTypeNum;
        self.responseCode = responseCode;
        self.measurement = measurement;
        self.requestSignature = requestSignature;
    }
    return self;
}

- (NSString *)hostname {
    return @"report.init.cedexis-radar.net";
}

- (NSArray *)pathParts {
    NSMutableArray *result = [[NSMutableArray alloc] init];
    [result addObject:@"f1"];
    [result addObject:self.requestSignature];
    [result addObject:[NSString stringWithFormat:@"%lu", (unsigned long)self.providerZoneId]];
    [result addObject:[NSString stringWithFormat:@"%lu", (unsigned long)self.providerCustomerId]];
    [result addObject:[NSString stringWithFormat:@"%lu", (unsigned long)self.providerId]];
    [result addObject:[NSString stringWithFormat:@"%ld", (unsigned long)self.probeTypeNum]];
    [result addObject:[NSString stringWithFormat:@"%ld", (unsigned long)self.responseCode]];
    [result addObject:[NSString stringWithFormat:@"%ld", (unsigned long)self.measurement]];
    return result;
}

- (NSDictionary *)dictionaryFrom:(NSData *)data {
    return nil;
}

- (NSString *)queryString {
    return @"";
}

- (NSDictionary *)toDictionary {
    NSMutableDictionary *result = [[NSMutableDictionary alloc] init];
    [result setValue:@"remoteprobe" forKey:@"type"];
    [result setValue:[NSNumber numberWithUnsignedInteger:self.providerZoneId] forKey:@"providerZoneId"];
    [result setValue:[NSNumber numberWithUnsignedInteger:self.providerCustomerId] forKey:@"providerCustomerId"];
    [result setValue:[NSNumber numberWithUnsignedInteger:self.providerId] forKey:@"providerId"];
    [result setValue:[NSNumber numberWithInteger:self.probeTypeNum] forKey:@"probeTypeNum"];
    [result setValue:[NSNumber numberWithInteger:self.responseCode] forKey:@"responseCode"];
    [result setValue:[NSNumber numberWithInteger:self.measurement] forKey:@"measurement"];
    [result setValue:self.requestSignature forKey:@"requestSignature"];
    return result;
}

@end
