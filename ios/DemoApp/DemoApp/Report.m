//
//  Report.m
//  DemoApp
//
//  Created by Jacob Wan on 3/15/13.
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import "Report.h"


@implementation Report

@dynamic reportId;
@dynamic type;

- (NSString *)reportTypeString {
    if ([self.type isEqualToString:@"remoteprobe"]) {
        return @"Remote Probe";
    }
    else if ([self.type isEqualToString:@"rumevent"]) {
        return @"RUM Event";
    }
    else if ([self.type isEqualToString:@"rumslice"]) {
        return @"RUM Slice";
    }
    else if ([self.type isEqualToString:@"rumproperty"]) {
        return @"RUM Property";
    }
    else if ([self.type isEqualToString:@"networktype"]) {
        return @"Network Type";
    }
    else if ([self.type isEqualToString:@"init"]) {
        return @"Init Request";
    }
    return @"Unknown";
}

@end
