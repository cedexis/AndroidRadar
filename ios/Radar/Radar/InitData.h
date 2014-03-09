//
//  InitData.h
//  Radar
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CommunicationData.h"

@interface InitData : NSObject<CommunicationData, NSXMLParserDelegate>

- (id)initWithRequestorZoneId:(NSInteger)requestorZoneId
          RequestorCustomerId:(NSInteger)requestorCustomerId
                 AndTimestamp:(NSInteger)timestamp;

@property NSInteger requestorZoneId;
@property NSInteger requestorCustomerId;
@property NSInteger timestamp;
@property NSInteger transactionId;

@end
