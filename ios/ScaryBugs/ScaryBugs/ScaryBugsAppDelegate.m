//
//  ScaryBugsAppDelegate.m
//  ScaryBugs
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import "ScaryBugsAppDelegate.h"
#import "ScaryBugsMasterViewController.h"
#import "ScaryBugDoc.h"
#import "Radar.h"
#import "RadarVars.h"

@interface Radar()

@end

@implementation ScaryBugsAppDelegate

@synthesize radar = _radar;

- (BOOL)application:(UIApplication *)application
    willFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    
    // This is the earliest spot in the application to run code.  We use it to initialize
    // RUM measurements.  Actual network use is postponed, so this shouldn't cause any delay.
    // Basically, Radar just records the current time and queues a report to be sent later.
    
    radar_comm_complete_block_t initComplete = ^(NSDictionary *result) {
        // Here we can read the network type received from the init request (if available).
        NSLog(@"Network type: %@", [result valueForKey:@"networkType"]);
        NSLog(@"RUM request signature: %@", [result valueForKey:@"requestSignature"]);
    };
    
    _radar = [[Radar alloc] initWithRequestorZoneId:1
                                RequestorCustomerId:10660];
    
    [self.radar startRUMInitCompletionQueue:dispatch_get_main_queue()
                             InitCompletion:initComplete];
    
    return YES;
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    ScaryBugDoc *bug1 = [[ScaryBugDoc alloc] initWithTitle:@"Potato Bug"
                                                 AndRating:4
                                             AndThumbImage:[UIImage imageNamed:@"potatoBugThumb.jpg"]
                                              AndFullImage:[UIImage imageNamed:@"potatoBug.jpg"]];
    ScaryBugDoc *bug2 = [[ScaryBugDoc alloc] initWithTitle:@"House Centipede"
                                                 AndRating:3
                                             AndThumbImage:[UIImage
                                                imageNamed:@"centipedeThumb.jpg"]
                                              AndFullImage:[UIImage imageNamed:@"centipede.jpg"]];
    ScaryBugDoc *bug3 = [[ScaryBugDoc alloc] initWithTitle:@"Wolf Spider"
                                                 AndRating:5
                                             AndThumbImage:[UIImage imageNamed:@"wolfSpiderThumb.jpg"]
                                              AndFullImage:[UIImage imageNamed:@"wolfSpider.jpg"]];
    ScaryBugDoc *bug4 = [[ScaryBugDoc alloc] initWithTitle:@"Lady Bug"
                                                 AndRating:1
                                             AndThumbImage:[UIImage imageNamed:@"ladybugThumb.jpg"]
                                              AndFullImage:[UIImage imageNamed:@"ladybug.jpg"]];
    NSMutableArray *bugs = [NSMutableArray arrayWithObjects:bug1, bug2, bug3, bug4, nil];
    
    UINavigationController *navController = (UINavigationController*)self.window.rootViewController;
    ScaryBugsMasterViewController *viewController = [navController.viewControllers objectAtIndex:0];
    viewController.bugs = bugs;
    
    // Override point for customization after application launch.
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        UISplitViewController *splitViewController = (UISplitViewController *)self.window.rootViewController;
        UINavigationController *navigationController = [splitViewController.viewControllers lastObject];
        splitViewController.delegate = (id)navigationController.topViewController;
    }
    
    // Allow Radar measurements
    [self.radar enableReporting:YES WithPollingInterval:5];
    
    // Report the RUM event
    [self.radar reportEvent:RadarEventsAppDidFinishLaunching
                   WithTags:RadarTagsAppDelegate | RadarTagsLevelDebug];
    
    // Attach any useful properties to the RUM session
    [self.radar reportProperty:RadarPropertiesUserId Value:@"some user id"];
    
    return YES;
}
							
- (void)applicationWillResignActive:(UIApplication *)application
{
    // Sent when the application is about to move from active to inactive state. This can occur for
    // certain types of temporary interruptions (such as an incoming phone call or SMS message) or
    // when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame
    // rates. Games should use this method to pause the game.
    
    // Disable Radar measurements.  We do this here (before reporting the RUM event below) to
    // ensure there's minimal impact from Radar on the device as the app is becoming inactive.
    [self.radar enableReporting:NO];
    
    // Report the RUM event.  This report will be sent once the app becomes active again.
    [self.radar reportEvent:RadarEventsAppWillResignActive
                   WithTags:RadarTagsAppDelegate | RadarTagsLevelDebug];
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    // Use this method to release shared resources, save user data, invalidate timers, and store
    // enough application state information to restore your application to its current state in
    // case it is terminated later.
    // If your application supports background execution, this method is called instead of
    // applicationWillTerminate: when the user quits.
    
    // Report the RUM event
    [self.radar reportEvent:RadarEventsAppDidEnterBackground
                   WithTags:RadarTagsAppDelegate | RadarTagsLevelDebug];
    
    // Flush any remaining reports
    [self.radar flush];
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    // Called as part of the transition from the background to the inactive state; here you can undo
    // many of the changes made on entering the background.
    
    // Report the RUM event
    [self.radar reportEvent:RadarEventsAppWillEnterForeground
                   WithTags:RadarTagsAppDelegate | RadarTagsLevelDebug];
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    // Restart any tasks that were paused (or not yet started) while the application was inactive.
    // If the application was previously in the background, optionally refresh the user interface.
    
    // Setup Radar HTTP measurements
    [NSTimer scheduledTimerWithTimeInterval:5 // seconds
                                     target:self
                                   selector:@selector(scheduleRadarHTTP:)
                                   userInfo:nil
                                    repeats:NO];
    
    // This event means the application is now in the foreground and active (the normal state for
    // most applications).  This is a good point to re-enable Radar reporting.
    [self.radar enableReporting:YES];
    
    // Report the RUM event
    [self.radar reportEvent:RadarEventsAppDidBecomeActive
              WithTags:RadarTagsAppDelegate | RadarTagsLevelDebug];
}

- (void)scheduleRadarHTTP:(NSTimer *)timer {
    NSLog(@"Scheduling Radar HTTP session");
    [self.radar scheduleRemoteProbing];
    
    // Now schedule a repeating timer to continue indefinitely, but at a longer interval
    [NSTimer scheduledTimerWithTimeInterval:120 // seconds
                                     target:self
                                   selector:@selector(scheduleRadarHTTP:)
                                   userInfo:nil
                                    repeats:NO];
}

@end
