//
//  RUMProperty.m
//  Radar
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import "RUMProperty.h"

@interface RUMProperty()
@property NSUInteger reportId;
@property (copy) NSString *property;
@property (copy) NSString *value;
@property UInt64 timestamp;
@property (copy) NSString *requestSignature;

@end

@implementation RUMProperty

@synthesize reportId = _reportId;
@synthesize property = _property;
@synthesize value = _value;
@synthesize timestamp = _timestamp;
@synthesize requestSignature = _requestSignature;

- (id)initWithReportId:(NSUInteger)reportId
              Property:(NSString *)property
                 Value:(NSString *)value
             Timestamp:(UInt64)timestamp
      RequestSignature:(NSString *)requestSignature {
    if (self = [super init]) {
        self.reportId = reportId;
        self.property = property;
        self.value = value;
        self.timestamp = timestamp;
        self.requestSignature = requestSignature;
    }
    return self;
}

- (NSString *)hostname {
    return @"report.init.cedexis-radar.net";
}

- (NSArray *)pathParts {
    NSMutableArray *result = [[NSMutableArray alloc] init];
    [result addObject:@"r2"];
    [result addObject:[NSString stringWithFormat:@"%lu", (unsigned long)self.reportId]];
    [result addObject:self.property];
    [result addObject:self.value];
    [result addObject:[NSString stringWithFormat:@"%llu", self.timestamp]];
    [result addObject:self.requestSignature];
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
    [result setValue:@"rumproperty" forKey:@"type"];
    [result setValue:[NSNumber numberWithUnsignedInteger:self.reportId] forKey:@"reportId"];
    [result setValue:self.property forKey:@"property"];
    [result setValue:self.value forKey:@"value"];
    [result setValue:[NSNumber numberWithUnsignedLongLong:self.timestamp] forKey:@"timestamp"];
    [result setValue:self.requestSignature forKey:@"requestSignature"];
    return result;
}

@end
