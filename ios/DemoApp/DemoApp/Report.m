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

- (UIImage *)image {
    if ([self.type isEqualToString:@"remoteprobe"]) {
        return [UIImage imageNamed:@"ios-radar.png"];
    }
    else if ([self.type hasPrefix:@"rum"]) {
        return [UIImage imageNamed:@"ios-rum.png"];
    }
    else if ([self.type isEqualToString:@"networktype"]) {
        return [UIImage imageNamed:@"ios-network.png"];
    }
    else if ([self.type isEqualToString:@"init"]) {
        return [UIImage imageNamed:@"ios-init.png"];
    }
    return nil;
}

@end
