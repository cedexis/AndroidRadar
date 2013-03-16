//
//  Init.m
//  DemoApp
//
//  Created by Jacob Wan on 3/15/13.
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import "Init.h"

@implementation Init

@dynamic requestorZoneId;
@dynamic requestorCustomerId;
@dynamic transactionId;
@dynamic timestamp;

-(NSString *)description {
    return [NSString stringWithFormat:@"Init (%@, %@, %@, %@)",
            self.requestorZoneId,
            self.requestorCustomerId,
            self.transactionId,
            self.timestamp];
}

@end
