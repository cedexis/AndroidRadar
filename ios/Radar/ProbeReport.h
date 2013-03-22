//
//  ProbeReport.h
//  Radar
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CommunicationData.h"

@interface ProbeReport : NSObject<CommunicationData>

- (id)initWithProviderZoneId:(NSUInteger)providerZoneId
          ProviderCustomerId:(NSUInteger)providerCustomerId
                  ProviderId:(NSUInteger)providerId
                ProbeTypeNum:(NSInteger)probeTypeNum
                ResponseCode:(NSInteger)responseCode
                 Measurement:(NSInteger)measurement
         AndRequestSignature:(NSString *)requestSignature;

@end
