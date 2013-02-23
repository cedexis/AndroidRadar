//
//  ScaryBugDoc.m
//  ScaryBugs
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import "ScaryBugDoc.h"
#import "ScaryBugData.h"

@implementation ScaryBugDoc

@synthesize data = _data;
@synthesize thumbImage = _thumbImage;
@synthesize fullImage = _fullImage;

- (id)initWithTitle:(NSString *)title AndRating:(float)rating AndThumbImage:(UIImage *)thumbImage
       AndFullImage:(UIImage *)fullImage {
    if (self = [super init]) {
        self.data = [[ScaryBugData alloc] initWithTitle:title AndRating:rating];
        self.thumbImage = thumbImage;
        self.fullImage = fullImage;
    }
    return self;
}

- (NSString*)description {
    return [NSString stringWithFormat: @"ScaryBugDoc: title=%@, rating=%f, thumbImage=%@, image=%@",
            self.data.title, self.data.rating, self.thumbImage, self.fullImage];
}

@end
