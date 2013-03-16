//
//  NetworkType.h
//  DemoApp
//
//  Created by Jacob Wan on 3/15/13.
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NetworkType : NSManagedObject

@property (nonatomic, retain) NSString *type;
@property (nonatomic, retain) NSString *subType;
@property (nonatomic, retain) NSString *requestSignature;

@end
