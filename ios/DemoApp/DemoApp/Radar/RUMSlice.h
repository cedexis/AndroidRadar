//
//  RUMSlice.h
//  Radar
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CommunicationData.h"

@interface RUMSlice : NSObject<CommunicationData>

- (id)initWithName:(NSString *)name
             Start:(BOOL)start
         Timestamp:(UInt64)timestamp
  RequestSignature:(NSString *)requestSignature;

@end
