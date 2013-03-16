//
//  RUMProperty.h
//  DemoApp
//
//  Created by Jacob Wan on 3/15/13.
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import <CoreData/CoreData.h>

@interface DemoAppRUMProperty : NSManagedObject

@property (nonatomic, retain) NSString *reportId;
@property (nonatomic, retain) NSString *property;
@property (nonatomic, retain) NSString *value;
@property (nonatomic, retain) NSString *timestamp;
@property (nonatomic, retain) NSString *requestSignature;

@end
