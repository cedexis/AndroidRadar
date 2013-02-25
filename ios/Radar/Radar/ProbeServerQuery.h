//
//  ProbeServerQuery.h
//  Radar
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CommunicationData.h"

@interface ProbeServerQuery : NSObject<CommunicationData>

- (id)initWithRequestorZoneId:(NSUInteger)requestorZoneId
          RequestorCustomerId:(NSUInteger)requestorCustomerId
                  ProviderIds:(NSArray *)providerIds;

@end
