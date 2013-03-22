//
//  InitData.m
//  Radar
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import "InitData.h"
#import <stdlib.h>

@interface InitData()
@property (copy) NSString *myHostname;
@property NSString *currentValue;
@property (copy) NSString *requestSignature;
@end

@implementation InitData

@synthesize requestorZoneId = _requestorZoneId;
@synthesize requestorCustomerId = _requestorCustomerId;
@synthesize timestamp = _timestamp;
@synthesize transactionId = _transactionId;
@synthesize myHostname = _myHostname;
@synthesize currentValue = _currentValue;
@synthesize requestSignature = _requestSignature;

- (id)initWithRequestorZoneId:(NSUInteger)requestorZoneId
          RequestorCustomerId:(NSUInteger)requestorCustomerId
                 AndTimestamp:(NSUInteger)timestamp {
    if (self = [super init]) {
        self.requestorZoneId = requestorZoneId;
        self.requestorCustomerId = requestorCustomerId;
        self.timestamp = timestamp;
        self.transactionId = arc4random();
        self.myHostname = @"init.cedexis-radar.net";
    }
    return self;
}

- (NSString *)hostname {
    return [NSString stringWithFormat:@"i1-io-0-1-%d-%d-%u-i.%@",
            self.requestorZoneId,
            self.requestorCustomerId,
            self.transactionId,
            self.myHostname];
}

- (NSString*)description {
    return [NSString stringWithFormat: @"InitData: requestorZoneId=%lu, requestorCustomerId=%lu, timestamp=%lu",
            (unsigned long)self.requestorZoneId,
            (unsigned long)self.requestorCustomerId,
            (unsigned long)self.timestamp];
}

- (NSArray *)pathParts {
    NSMutableArray *result = [[NSMutableArray alloc] init];
    [result addObject:@"i1"];
    [result addObject:[NSString stringWithFormat:@"%lu", (unsigned long)self.timestamp]];
    [result addObject:[NSString stringWithFormat:@"%lu", (unsigned long)self.transactionId]];
    [result addObject:@"xml"];
    return result;
}

- (NSDictionary *)dictionaryFrom:(NSData *)data {
    NSXMLParser *parser = [[NSXMLParser alloc] initWithData:data];
    [parser setDelegate:self];
    BOOL success = [parser parse];
    NSLog(@"Parsing result: %@", success ? @"YES" : @"NO");
    
    NSMutableDictionary *result = [[NSMutableDictionary alloc] init];
    [result setValue:self.requestSignature forKey:@"requestSignature"];
    return result;
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName {
    //NSLog(@"Element ended: %@", elementName);
    if ([elementName isEqualToString:@"requestSignature"]) {
        self.requestSignature = self.currentValue;
    }
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string {
    //NSLog(@"Found characters: %@", string);
    self.currentValue = string;
}

- (NSString *)queryString {
    return @"";
}

- (NSDictionary *)toDictionary {
    NSMutableDictionary *result = [[NSMutableDictionary alloc] init];
    [result setValue:@"init" forKey:@"type"];
    [result setValue:[NSNumber numberWithUnsignedInt:self.requestorZoneId]
              forKey:@"requestorZoneId"];
    [result setValue:[NSNumber numberWithUnsignedInt:self.requestorCustomerId]
              forKey:@"requestorCustomerId"];
    [result setValue:[NSNumber numberWithUnsignedInt:self.transactionId]
              forKey:@"transactionId"];
    [result setValue:[NSNumber numberWithUnsignedInt:self.timestamp]
              forKey:@"timestamp"];
    return result;
}

@end
