//
//  InitData.h
//  Radar
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CommunicationData.h"

@interface InitData : NSObject<CommunicationData, NSXMLParserDelegate>

- (id)initWithRequestorZoneId:(NSUInteger)requestorZoneId
          RequestorCustomerId:(NSUInteger)requestorCustomerId
                 AndTimestamp:(NSUInteger)timestamp;

@property NSUInteger requestorZoneId;
@property NSUInteger requestorCustomerId;
@property NSUInteger timestamp;
@property NSUInteger transactionId;

@end
