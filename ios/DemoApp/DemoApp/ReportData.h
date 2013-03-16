//
//  ReportData.h
//  DemoApp
//
//  Created by Jacob Wan on 3/15/13.
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface ReportData : NSObject

@property (nonatomic, strong) NSString *name;
@property (nonatomic, strong) NSString *value;

- (id)initWithName:(NSString *)name Value:(NSString *)value;

@end
