//
//  DemoAppSpeedTestViewController.h
//  DemoApp
//
//  Created by Jacob Wan on 3/16/13.
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface DemoAppSpeedTestViewController : UIViewController <UITableViewDataSource, UITableViewDelegate, NSXMLParserDelegate>

@property (weak, nonatomic) IBOutlet UITableView *tableView;

- (IBAction)doSpeedTest:(id)sender;

@end
