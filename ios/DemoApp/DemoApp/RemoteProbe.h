//
//  RemoteProbe.h
//  DemoApp
//
//  Created by Jacob Wan on 3/15/13.
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import <CoreData/CoreData.h>

@interface RemoteProbe : NSManagedObject

@property (nonatomic, retain) NSString *providerZoneId;
@property (nonatomic, retain) NSString *providerCustomerId;
@property (nonatomic, retain) NSString *providerId;
@property (nonatomic, retain) NSString *probeTypeNum;
@property (nonatomic, retain) NSString *responseCode;
@property (nonatomic, retain) NSString *measurement;
@property (nonatomic, retain) NSString *requestSignature;

@end
