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
    [controller setSubject:@"Hello Cedexis"];
    [controller setMessageBody:@"This is a test" isHTML:NO];
    [controller setToRecipients:[NSArray arrayWithObject:@"test@cedexis.com"]];
    
    [self presentViewController:controller animated:YES completion:NULL];
}

- (void)mailComposeController:(MFMailComposeViewController *)controller
          didFinishWithResult:(MFMailComposeResult)result
                        error:(NSError *)error {
    
    DemoAppAppDelegate *del = [[UIApplication sharedApplication] delegate];
    
    NSUInteger reportId = [del.radar reportEvent:RadarEventsUserEmail];
    
    switch (result)
    {
        case MFMailComposeResultCancelled:
            [del.radar reportProperty:RadarPropertiesUserEmailResult
                                Value:@"cancelled"
                            ForReport:reportId];
            break;
        case MFMailComposeResultSaved:
            [del.radar reportProperty:RadarPropertiesUserEmailResult
                                Value:@"saved"
                            ForReport:reportId];
            break;
        case MFMailComposeResultSent:
            [del.radar reportProperty:RadarPropertiesUserEmailResult
                                Value:@"sent"
                            ForReport:reportId];
            break;
        case MFMailComposeResultFailed:
            [del.radar reportProperty:RadarPropertiesUserEmailResult
                                Value:[NSString stringWithFormat:@"failure: %@",
                                       [error description]]
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
    
    [del.radar reportEvent:RadarEventsUserClearedDatabase
                  WithTags:RadarTagsLevelWarning];
}

@end
