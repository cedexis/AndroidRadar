//
//  RUMSlice.m
//  Radar
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import "RUMSlice.h"

@interface RUMSlice()
@property (copy) NSString *name;
@property BOOL start;
@property UInt64 timestamp;
@property (copy) NSString *requestSignature;

@end

@implementation RUMSlice

@synthesize name = _name;
@synthesize start = _start;
@synthesize timestamp = _timestamp;
@synthesize requestSignature = _requestSignature;

- (id)initWithName:(NSString *)name
             Start:(BOOL)start
         Timestamp:(UInt64)timestamp
  RequestSignature:(NSString *)requestSignature {
    if (self = [super init]) {
        self.name = name;
        self.start = start;
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
    [result addObject:@"r3"];
    [result addObject:self.name];
    [result addObject:[NSString stringWithFormat:@"%d", self.start ? 1 : 0]];
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
    [result setValue:@"rumslice" forKey:@"type"];
    [result setValue:self.name forKey:@"name"];
    [result setValue:[NSNumber numberWithBool:self.start] forKey:@"start"];
    [result setValue:[NSNumber numberWithUnsignedLongLong:self.timestamp] forKey:@"timestamp"];
    [result setValue:self.requestSignature forKey:@"requestSignature"];
    return result;
}

@end
