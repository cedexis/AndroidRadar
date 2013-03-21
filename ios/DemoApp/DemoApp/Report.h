//
//  Report.h
//  DemoApp
//
//  Created by Jacob Wan on 3/15/13.
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface Report : NSManagedObject

@property (nonatomic, retain) NSNumber *reportId;
@property (nonatomic, retain) NSString *type;

- (NSString *)reportTypeString;

@end
