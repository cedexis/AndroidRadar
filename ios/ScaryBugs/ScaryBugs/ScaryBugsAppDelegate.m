//
//  ScaryBugsAppDelegate.m
//  ScaryBugs
//
//  Created by Jacob Wan on 2/21/13.
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import "ScaryBugsAppDelegate.h"
#import "ScaryBugsMasterViewController.h"
#import "ScaryBugDoc.h"

@implementation ScaryBugsAppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    ScaryBugDoc *bug1 = [[ScaryBugDoc alloc] initWithTitle:@"Potato Bug" AndRating:4 AndThumbImage:[UIImage imageNamed:@"potatoBugThumb.jpg"] AndFullImage:[UIImage imageNamed:@"potatoBug.jpg"]];
    ScaryBugDoc *bug2 = [[ScaryBugDoc alloc] initWithTitle:@"House Centipede" AndRating:3 AndThumbImage:[UIImage imageNamed:@"centipedeThumb.jpg"] AndFullImage:[UIImage imageNamed:@"centipede.jpg"]];
    ScaryBugDoc *bug3 = [[ScaryBugDoc alloc] initWithTitle:@"Wolf Spider" AndRating:5 AndThumbImage:[UIImage imageNamed:@"wolfSpiderThumb.jpg"] AndFullImage:[UIImage imageNamed:@"wolfSpider.jpg"]];
    ScaryBugDoc *bug4 = [[ScaryBugDoc alloc] initWithTitle:@"Lady Bug" AndRating:1 AndThumbImage:[UIImage imageNamed:@"ladybugThumb.jpg"] AndFullImage:[UIImage imageNamed:@"ladybug.jpg"]];
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
    return YES;
}
							
- (void)applicationWillResignActive:(UIApplication *)application
{
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
}

- (void)applicationWillTerminate:(UIApplication *)application
{
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
}

@end
