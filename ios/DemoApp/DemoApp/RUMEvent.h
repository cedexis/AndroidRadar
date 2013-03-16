//
//  RUMEvent.h
//  DemoApp
//
//  Created by Jacob Wan on 3/15/13.
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface RUMEvent : NSManagedObject

@property (nonatomic, retain) NSString *reportId;
@property (nonatomic, retain) NSString *eventName;
@property (nonatomic, retain) NSString *tags;
@property (nonatomic, retain) NSString *timestamp;
@property (nonatomic, retain) NSString *requestSignature;

@end
