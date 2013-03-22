//
//  RUMProperty.h
//  Radar
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CommunicationData.h"

@interface RUMProperty : NSObject<CommunicationData>

- (id)initWithReportId:(NSUInteger)reportId
              Property:(NSString *)property
                 Value:(NSString *)value
             Timestamp:(UInt64)timestamp
      RequestSignature:(NSString *)requestSignature;

@end
