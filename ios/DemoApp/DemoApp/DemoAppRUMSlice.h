//
//  DemoAppRUMSlice.h
//  DemoApp
//
//  Created by Jacob Wan on 3/15/13.
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import <CoreData/CoreData.h>

@interface DemoAppRUMSlice : NSManagedObject

@property (nonatomic, retain) NSString *name;
@property (nonatomic, retain) NSString *start;
@property (nonatomic, retain) NSString *timestamp;
@property (nonatomic, retain) NSString *requestSignature;

@end
