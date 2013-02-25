//
//  NetworkTypeReport.m
//  Radar
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import "NetworkTypeReport.h"

@interface NetworkTypeReport()
@property NSInteger type;
@property NSInteger subType;
@property (copy) NSString *requestSignature;

@end

@implementation NetworkTypeReport

@synthesize type = _type;
@synthesize subType = _subType;
@synthesize requestSignature = _requestSignature;

- (id)initWithNetworkType:(NSInteger)type SubType:(NSInteger)subType AndRequestSignature:(NSString *)requestSignature {
    if (self = [super init]) {
        self.type = type;
        self.subType = subType;
        self.requestSignature = requestSignature;
    }
    return self;
}

- (NSString *)hostname {
    return @"report.init.cedexis-radar.net";
}

- (NSArray *)pathParts {
    NSMutableArray *result = [[NSMutableArray alloc] init];
    [result addObject:@"f3"];
    [result addObject:[NSNumber numberWithInteger:self.type]];
    [result addObject:[NSNumber numberWithInteger:self.subType]];
    [result addObject:self.requestSignature];
    return result;
}

- (NSDictionary *)dictionaryFrom:(NSData *)data {
    return [[NSDictionary alloc] init];
}

- (NSString *)queryString {
    return @"";
}

@end
