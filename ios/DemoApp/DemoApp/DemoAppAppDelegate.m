//
//  DemoAppAppDelegate.m
//  DemoApp
//
//  Created by Jacob Wan on 3/14/13.
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import "DemoAppAppDelegate.h"
#import "Radar.h"
#import "RadarVars.h"
#import "Report.h"
#import "Init.h"
#import "RUMEvent.h"
#import "NetworkType.h"
#import "RemoteProbe.h"
#import "DemoAppRUMProperty.h"
#import "DemoAppRUMSlice.h"

@implementation DemoAppAppDelegate

@synthesize managedObjectContext = _managedObjectContext;
@synthesize managedObjectModel = _managedObjectModel;
@synthesize persistentStoreCoordinator = _persistentStoreCoordinator;

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

    [[Radar instance] startRUMInitWithZoneId:1
                                  CustomerId:13363
                             CompletionQueue:dispatch_get_main_queue()
                              InitCompletion:initComplete];
    
    return YES;
}

- (BOOL)application:(UIApplication *)application
    didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    // Register for Radar notifications
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(notify:)
                                                 name:nil
                                               object:[Radar instance]];
    
    // Allow Radar measurements
    [[Radar instance] enableReporting:YES WithPollingInterval:5];
    
    // Report the RUM event
    [[Radar instance] reportEvent:RadarEventsAppDidFinishLaunching
                         WithTags:RadarTagsAppDelegate | RadarTagsLevelDebug];
    
    // Attach any useful properties to the RUM session.  For example, information about
    // the device...
    UIDevice *device = [UIDevice currentDevice];
    [[Radar instance] reportProperty:RadarPropertiesDeviceName Value:[device name]];
    [[Radar instance] reportProperty:RadarPropertiesDeviceSystemName Value:[device systemName]];
    [[Radar instance] reportProperty:RadarPropertiesDeviceSystemVersion Value:[device systemVersion]];
    NSUUID *uniqueId = [[UIDevice currentDevice] identifierForVendor];
    [[Radar instance] reportProperty:RadarPropertiesDeviceId Value:[uniqueId UUIDString]];
    return YES;
}
							
- (void)applicationWillResignActive:(UIApplication *)application
{
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
    
    // Disable Radar measurements.  We do this here (before reporting the RUM event below) to
    // ensure there's minimal impact from Radar on the device as the app is becoming inactive.
    [[Radar instance] enableReporting:NO];
    
    // Report the RUM event.  This report will be sent once the app becomes active again.
    [[Radar instance] reportEvent:RadarEventsAppWillResignActive
                         WithTags:RadarTagsAppDelegate | RadarTagsLevelDebug];
    
    // End RUM slice
    [[Radar instance] reportSlice:RadarSliceAppActive Start:NO];
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
    
    // Report the RUM event
    [[Radar instance] reportEvent:RadarEventsAppDidEnterBackground
                         WithTags:RadarTagsAppDelegate | RadarTagsLevelDebug];
    
    // Flush any remaining reports
    [[Radar instance] flush];
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
    
    // Report the RUM event
    [[Radar instance] reportEvent:RadarEventsAppWillEnterForeground
                         WithTags:RadarTagsAppDelegate | RadarTagsLevelDebug];
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    
    // This event means the application is now in the foreground and active (the normal state for
    // most applications).  This is a good point to re-enable Radar reporting.
    [[Radar instance] enableReporting:YES];
    
    // Start RUM slice
    [[Radar instance] reportSlice:RadarSliceAppActive Start:YES];
    
    // Report the RUM event
    [[Radar instance] reportEvent:RadarEventsAppDidBecomeActive
                         WithTags:RadarTagsAppDelegate | RadarTagsLevelDebug];
}

- (void)applicationWillTerminate:(UIApplication *)application
{
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
    [self saveContext];
}

- (NSNumber *)maxReportId {
    NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"Report"];
    request.fetchLimit = 1;
    NSArray *sorts = [NSArray arrayWithObject:[NSSortDescriptor sortDescriptorWithKey:@"reportId" ascending:NO]];
    request.sortDescriptors = sorts;
    NSError *error;
    Report *report = [self.managedObjectContext executeFetchRequest:request error:&error].lastObject;
    if (nil == report) {
        return nil;
    }
    return report.reportId;
}

- (void)notify:(NSNotification *)notification {
    if ([Radar instance] == [notification object]) {
        NSDictionary *userInfo = notification.userInfo;
        NSLog(@"Info: %@", userInfo);
        
        // Get the next report id
        NSNumber *reportId = [NSNumber numberWithInt:1];
        NSNumber *maxReportId = [self maxReportId];
        if (maxReportId != nil) {
            // Increment the current max report id
            reportId = [NSNumber numberWithInt:1 + [maxReportId intValue]];
        }
        
        Report *report =
            [NSEntityDescription insertNewObjectForEntityForName:@"Report"
                                          inManagedObjectContext:self.managedObjectContext];
        
        NSString *type = [userInfo valueForKey:@"type"];
        report.reportId = reportId;
        report.type = type;
        
        if ([type isEqualToString:@"init"]) {
            Init *temp = [NSEntityDescription insertNewObjectForEntityForName:@"Init"
                                                       inManagedObjectContext:self.managedObjectContext];
            temp.requestorZoneId = [[userInfo valueForKey:@"requestorZoneId"] stringValue];
            temp.requestorCustomerId = [[userInfo valueForKey:@"requestorCustomerId"] stringValue];
            temp.transactionId = [[userInfo valueForKey:@"transactionId"] stringValue];
            temp.timestamp = [[userInfo valueForKey:@"timestamp"] stringValue];
            [report setValue:temp forKey:@"initDetail"];
            [temp setValue:report forKey:@"report"];
        }
        else if ([type isEqualToString:@"rumevent"]) {
            RUMEvent *temp = [NSEntityDescription insertNewObjectForEntityForName:@"RUMEvent"
                                                               inManagedObjectContext:self.managedObjectContext];
            temp.reportId = [[userInfo valueForKey:@"reportId"] stringValue];
            temp.eventName = [userInfo valueForKey:@"eventName"];
            temp.tags = [[userInfo valueForKey:@"tags"] stringValue];
            temp.timestamp = [[userInfo valueForKey:@"timestamp"] stringValue];
            temp.requestSignature = [userInfo valueForKey:@"requestSignature"];
            [report setValue:temp forKey:@"rumEventDetail"];
            [temp setValue:report forKey:@"report"];
        }
        else if ([type isEqualToString:@"rumslice"]) {
            DemoAppRUMSlice *temp = [NSEntityDescription insertNewObjectForEntityForName:@"RUMSlice"
                                                                  inManagedObjectContext:self.managedObjectContext];
            temp.name = [userInfo valueForKey:@"name"];
            temp.start = [[userInfo valueForKey:@"start"] stringValue];
            temp.timestamp = [[userInfo valueForKey:@"timestamp"] stringValue];
            temp.requestSignature = [userInfo valueForKey:@"requestSignature"];
            [report setValue:temp forKey:@"rumSliceDetail"];
            [temp setValue:report forKey:@"report"];
        }
        else if ([type isEqualToString:@"rumproperty"]) {
            DemoAppRUMProperty *temp = [NSEntityDescription insertNewObjectForEntityForName:@"RUMProperty"
                                                              inManagedObjectContext:self.managedObjectContext];
            temp.reportId = [[userInfo valueForKey:@"reportId"] stringValue];
            temp.property = [userInfo valueForKey:@"property"];
            temp.value = [userInfo valueForKey:@"value"];
            temp.timestamp = [[userInfo valueForKey:@"timestamp"] stringValue];
            temp.requestSignature = [userInfo valueForKey:@"requestSignature"];
            [report setValue:temp forKey:@"rumPropertyDetail"];
            [temp setValue:report forKey:@"report"];
        }
        else if ([type isEqualToString:@"remoteprobe"]) {
            RemoteProbe *temp = [NSEntityDescription insertNewObjectForEntityForName:@"RemoteProbe"
                                                                     inManagedObjectContext:self.managedObjectContext];
            temp.providerZoneId = [[userInfo valueForKey:@"providerZoneId"] stringValue];
            temp.providerCustomerId = [[userInfo valueForKey:@"providerCustomerId"] stringValue];
            temp.providerId = [[userInfo valueForKey:@"providerId"] stringValue];
            temp.probeTypeNum = [[userInfo valueForKey:@"probeTypeNum"] stringValue];
            temp.responseCode = [[userInfo valueForKey:@"responseCode"] stringValue];
            temp.measurement = [[userInfo valueForKey:@"measurement"] stringValue];
            temp.requestSignature = [userInfo valueForKey:@"requestSignature"];
            [report setValue:temp forKey:@"remoteProbeDetail"];
            [temp setValue:report forKey:@"report"];
        }
        else if ([type isEqualToString:@"networktype"]) {
            NetworkType *temp = [NSEntityDescription insertNewObjectForEntityForName:@"NetworkType"
                                                                     inManagedObjectContext:self.managedObjectContext];
            temp.type = [[userInfo valueForKey:@"networkType"] stringValue];
            temp.subType = [[userInfo valueForKey:@"networkSubType"] stringValue];
            temp.requestSignature = [userInfo valueForKey:@"requestSignature"];
            [report setValue:temp forKey:@"networkTypeDetail"];
            [temp setValue:report forKey:@"report"];
        }
        else {
            // Unexpected report type
            NSLog(@"Unexpected report type: %@", type);
            exit(1);
        }

        [self saveContext];
    }
}

- (void)saveContext
{
    NSError *error = nil;
    NSManagedObjectContext *managedObjectContext = self.managedObjectContext;
    if (managedObjectContext != nil) {
        if ([managedObjectContext hasChanges] && ![managedObjectContext save:&error]) {
            // Replace this implementation with code to handle the error appropriately.
            // abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development.
            NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
            abort();
        }
    }
}

#pragma mark - Core Data stack

// Returns the managed object context for the application.
// If the context doesn't already exist, it is created and bound to the persistent store coordinator for the application.
- (NSManagedObjectContext *)managedObjectContext
{
    if (_managedObjectContext != nil) {
        return _managedObjectContext;
    }
    
    NSPersistentStoreCoordinator *coordinator = [self persistentStoreCoordinator];
    if (coordinator != nil) {
        _managedObjectContext = [[NSManagedObjectContext alloc] init];
        [_managedObjectContext setPersistentStoreCoordinator:coordinator];
    }
    return _managedObjectContext;
}

// Returns the managed object model for the application.
// If the model doesn't already exist, it is created from the application's model.
- (NSManagedObjectModel *)managedObjectModel
{
    if (_managedObjectModel != nil) {
        return _managedObjectModel;
    }
    //NSURL *modelURL = [[NSBundle mainBundle] URLForResource:@"DemoApp" withExtension:@"momd"];
    //_managedObjectModel = [[NSManagedObjectModel alloc] initWithContentsOfURL:modelURL];
    _managedObjectModel = [NSManagedObjectModel mergedModelFromBundles:nil];
    return _managedObjectModel;
}

// Returns the persistent store coordinator for the application.
// If the coordinator doesn't already exist, it is created and the application's store added to it.
- (NSPersistentStoreCoordinator *)persistentStoreCoordinator
{
    if (_persistentStoreCoordinator != nil) {
        return _persistentStoreCoordinator;
    }
    
    NSURL *storeURL = [[self applicationDocumentsDirectory] URLByAppendingPathComponent:@"DemoApp.sqlite"];
    
    NSError *error = nil;
    _persistentStoreCoordinator = [[NSPersistentStoreCoordinator alloc] initWithManagedObjectModel:[self managedObjectModel]];
    if (![_persistentStoreCoordinator addPersistentStoreWithType:NSSQLiteStoreType configuration:nil URL:storeURL options:nil error:&error]) {
        /*
         Replace this implementation with code to handle the error appropriately.
         
         abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development.
         
         Typical reasons for an error here include:
         * The persistent store is not accessible;
         * The schema for the persistent store is incompatible with current managed object model.
         Check the error message to determine what the actual problem was.
         
         
         If the persistent store is not accessible, there is typically something wrong with the file path. Often, a file URL is pointing into the application's resources directory instead of a writeable directory.
         
         If you encounter schema incompatibility errors during development, you can reduce their frequency by:
         * Simply deleting the existing store:
         [[NSFileManager defaultManager] removeItemAtURL:storeURL error:nil]
         
         * Performing automatic lightweight migration by passing the following dictionary as the options parameter:
         @{NSMigratePersistentStoresAutomaticallyOption:@YES, NSInferMappingModelAutomaticallyOption:@YES}
         
         Lightweight migration will only work for a limited set of schema changes; consult "Core Data Model Versioning and Data Migration Programming Guide" for details.
         
         */
        NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
        abort();
    }
    
    return _persistentStoreCoordinator;
}

#pragma mark - Application's Documents directory

// Returns the URL to the application's Documents directory.
- (NSURL *)applicationDocumentsDirectory
{
    return [[[NSFileManager defaultManager] URLsForDirectory:NSDocumentDirectory
                                                   inDomains:NSUserDomainMask] lastObject];
}

@end
