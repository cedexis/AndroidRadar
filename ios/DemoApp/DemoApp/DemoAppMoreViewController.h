//
//  DemoAppMoreViewController.h
//  DemoApp
//
//  Created by Jacob Wan on 3/14/13.
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MessageUI/MessageUI.h>

@interface DemoAppMoreViewController : UIViewController <MFMailComposeViewControllerDelegate>
- (IBAction)sendEmail:(id)sender;
- (IBAction)clearDatabase:(id)sender;
- (IBAction)doRemoteProbing:(id)sender;
@end
