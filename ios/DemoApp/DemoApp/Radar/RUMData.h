//
//  RUMData.h
//  Radar
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CommunicationData.h"

@interface RUMData : NSObject<CommunicationData>

- (id)initWithReportId:(NSUInteger)reportId
             EventName:(NSString *)eventName
                  Tags:(NSUInteger)tags
             Timestamp:(UInt64)timestamp
      RequestSignature:(NSString *)requestSignature;

@end
