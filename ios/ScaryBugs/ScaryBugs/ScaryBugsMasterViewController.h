//
//  ScaryBugsMasterViewController.h
//  ScaryBugs
//
//  Created by Jacob Wan on 2/21/13.
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import <UIKit/UIKit.h>

@class ScaryBugsDetailViewController;

@interface ScaryBugsMasterViewController : UITableViewController

@property (strong, nonatomic) ScaryBugsDetailViewController *detailViewController;
@property (strong) NSMutableArray *bugs;

@end
