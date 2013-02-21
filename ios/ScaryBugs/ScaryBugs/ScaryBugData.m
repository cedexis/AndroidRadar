//
//  ScaryBugData.m
//  ScaryBugs
//
//  Created by Jacob Wan on 2/21/13.
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import "ScaryBugData.h"

@implementation ScaryBugData

@synthesize title = _title;
@synthesize rating = _rating;

- (id)initWithTitle:(NSString *)title AndRating:(float)rating {
    if (self = [super init]) {
        self.title = title;
        self.rating = rating;
    }
    return self;
}

@end
