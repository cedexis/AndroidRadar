//
//  ReportData.m
//  DemoApp
//
//  Created by Jacob Wan on 3/15/13.
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import "ReportData.h"

@implementation ReportData

@synthesize name = _name;
@synthesize value = _value;

-(id)initWithName:(NSString *)name Value:(NSString *)value {
    if (self = [super init]) {
        _name = name;
        _value = value;
    }
    return self;
}

@end
