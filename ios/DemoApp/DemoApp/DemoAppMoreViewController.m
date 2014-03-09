//
//  DemoAppMoreViewController.m
//  DemoApp
//
//  Created by Jacob Wan on 3/14/13.
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import "DemoAppMoreViewController.h"
#import "DemoAppAppDelegate.h"
#import "Radar.h"
#import "RadarVars.h"

@interface DemoAppMoreViewController ()

@end

@implementation DemoAppMoreViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)sendEmail:(id)sender {
    MFMailComposeViewController *controller = [[MFMailComposeViewController alloc] init];
    controller.mailComposeDelegate = self;
    [controller setSubject:@"Support Request"];
    [controller setMessageBody:@"Hello,\n\n" isHTML:NO];
    [controller setToRecipients:[NSArray arrayWithObject:@"support@cedexis.com"]];
    
    [self presentViewController:controller animated:YES completion:NULL];
}

- (void)mailComposeController:(MFMailComposeViewController *)controller
          didFinishWithResult:(MFMailComposeResult)result
                        error:(NSError *)error {
    
    NSUInteger reportId = [[Radar instance] reportEvent:RadarEventsUserEmail];
    
    switch (result)
    {
        case MFMailComposeResultCancelled:
            [[Radar instance] reportProperty:RadarPropertiesUserEmailResult
                                       Value:@"cancelled"
                                   ForReport:reportId];
            break;
        case MFMailComposeResultSaved:
            [[Radar instance] reportProperty:RadarPropertiesUserEmailResult
                                       Value:@"saved"
                                   ForReport:reportId];
            break;
        case MFMailComposeResultSent:
            [[Radar instance] reportProperty:RadarPropertiesUserEmailResult
                                       Value:@"sent"
                                   ForReport:reportId];
            break;
        case MFMailComposeResultFailed:
            [[Radar instance] reportProperty:RadarPropertiesUserEmailResult
                                       Value:[NSString stringWithFormat:@"failure: %@", [error description]]
                                   ForReport:reportId];
            break;
        default:
            break;
    }
    
    // Close the Mail Interface
    [self dismissViewControllerAnimated:YES completion:NULL];
}

-(void)clearDatabase:(id)sender {
    NSLog(@"Clearing database");
    NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"Report"];
    request.includesPropertyValues = NO;
    DemoAppAppDelegate *del = [[UIApplication sharedApplication] delegate];
    NSError *error;
    NSArray *reports = [del.managedObjectContext executeFetchRequest:request
                                                               error:&error];
    for (NSManagedObject *obj in reports) {
        [del.managedObjectContext deleteObject:obj];
    }
    [del.managedObjectContext save:&error];
    
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:nil
                                                    message:@"Database cleared"
                                                   delegate:nil
                                          cancelButtonTitle:@"Ok"
                                          otherButtonTitles:nil];
    [alert show];
    
    [[Radar instance] reportEvent:RadarEventsUserClearedDatabase
                         WithTags:RadarTagsLevelWarning];
}

- (IBAction)doRemoteProbing:(id)sender {
    UIAlertView *alert;
    if ([[Radar instance] scheduleRemoteProbingWithZoneId:1 AndCustomerId:13363]) {
        alert = [[UIAlertView alloc] initWithTitle:nil
                                           message:@"Remote probing scheduled"
                                          delegate:nil
                                 cancelButtonTitle:@"Ok"
                                 otherButtonTitles:nil];
        
        [[Radar instance] reportEvent:RadarEventsUserRemoteProbing];
    }
    else {
        alert = [[UIAlertView alloc] initWithTitle:nil
                                           message:@"Remote probing not scheduled"
                                          delegate:nil
                                 cancelButtonTitle:@"Ok"
                                 otherButtonTitles:nil];
    }
    [alert show];
}

@end
