//
//  MeasurementTableCell.m
//  DemoApp
//
//  Created by Jacob Wan on 3/15/13.
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import "MeasurementTableCell.h"

@implementation MeasurementTableCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
    }
    return self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

-(void)layoutSubviews {
    [super layoutSubviews];
    
    self.imageView.frame = CGRectMake(6,10,20,20);
    self.imageView.bounds = CGRectMake(0,0,20,20);
    self.imageView.contentMode = UIViewContentModeScaleAspectFit;
        
    CGRect tmpFrame = self.textLabel.frame;
    tmpFrame.origin.x = 35;
    self.textLabel.frame = tmpFrame;
        
    //tmpFrame = self.detailTextLabel.frame;
    //tmpFrame.origin.x = 24;
    //self.detailTextLabel.frame = tmpFrame;
}

@end
