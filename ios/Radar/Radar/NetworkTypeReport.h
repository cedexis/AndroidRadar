//
//  NetworkTypeReport.h
//  Radar
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CommunicationData.h"

@interface NetworkTypeReport : NSObject<CommunicationData>

- (id)initWithNetworkType:(NSInteger)type
                  SubType:(NSInteger)subType
      AndRequestSignature:(NSString *)requestSignature;

@end
