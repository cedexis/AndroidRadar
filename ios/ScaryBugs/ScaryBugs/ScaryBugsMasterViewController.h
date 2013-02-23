//
//  ScaryBugsMasterViewController.h
//  ScaryBugs
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import <UIKit/UIKit.h>

@class ScaryBugsDetailViewController;

@interface ScaryBugsMasterViewController : UITableViewController

@property (strong, nonatomic) ScaryBugsDetailViewController *detailViewController;
@property (strong) NSMutableArray *bugs;

@end
