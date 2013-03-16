//
//  RUMData.m
//  Radar
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import "RUMData.h"

@interface RUMData()

@property NSUInteger reportId;
@property (copy) NSString *eventName;
@property NSUInteger tags;
@property UInt64 timestamp;
@property (copy) NSString *requestSignature;

@end

@implementation RUMData

@synthesize reportId = _reportId;
@synthesize eventName = _eventName;
@synthesize tags = _tags;
@synthesize timestamp = _timestamp;
@synthesize requestSignature = _requestSignature;

- (id)initWithReportId:(NSUInteger)reportId
             EventName:(NSString *)eventName
                  Tags:(NSUInteger)tags
             Timestamp:(UInt64)timestamp
      RequestSignature:(NSString *)requestSignature {
    if (self = [super init])
    {
        self.reportId = reportId;
        self.eventName = eventName;
        self.tags = tags;
        self.timestamp = timestamp;
        self.requestSignature = requestSignature;
    }
    return self;
}

- (NSString *)hostname {
    return @"report.init.cedexis-radar.net";
}

- (NSDictionary *)dictionaryFrom:(NSData *)data {
    return nil;
}

- (NSArray *)pathParts {
    NSMutableArray *result = [[NSMutableArray alloc] init];
    [result addObject:@"r1"];
    [result addObject:[NSString stringWithFormat:@"%lu", (unsigned long)self.reportId]];
    [result addObject:self.eventName];
    [result addObject:[NSString stringWithFormat:@"%lu", (unsigned long)self.tags]];
    [result addObject:[NSString stringWithFormat:@"%llu", self.timestamp]];
    [result addObject:self.requestSignature];
    return result;
}

- (NSString *)queryString {
    return @"";
}

- (NSDictionary *)toDictionary {
    NSMutableDictionary *result = [[NSMutableDictionary alloc] init];
    [result setValue:@"rumevent" forKey:@"type"];
    [result setValue:[NSNumber numberWithUnsignedInteger:self.reportId] forKey:@"reportId"];
    [result setValue:self.eventName forKey:@"eventName"];
    [result setValue:[NSNumber numberWithUnsignedInteger:self.tags] forKey:@"tags"];
    [result setValue:[NSNumber numberWithUnsignedLongLong:self.timestamp] forKey:@"timestamp"];
    [result setValue:self.requestSignature forKey:@"requestSignature"];
    return result;
}

@end
