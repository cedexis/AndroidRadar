//
//  Init.h
//  DemoApp
//
//  Created by Jacob Wan on 3/15/13.
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import <CoreData/CoreData.h>

@interface Init : NSManagedObject

@property (nonatomic, retain) NSString *requestorZoneId;
@property (nonatomic, retain) NSString *requestorCustomerId;
@property (nonatomic, retain) NSString *transactionId;
@property (nonatomic, retain) NSString *timestamp;

@end
