//
//  Radar.m
//  Radar
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import <CoreData/CoreData.h>
#import <dispatch/dispatch.h>
#import "Radar.h"
#import "RadarCommunication.h"
#import "InitData.h"
#import "RUMData.h"
#import "RUMProperty.h"
#import "RUMSlice.h"
#import "ProbeServerQuery.h"
#import "ProbeReport.h"
#import "NetworkTypeReport.h"
#import "Reachability.h"

@interface Radar()
@property (copy) NSString *rumRequestSignature;
@property id reportIdLock;
@property NSUInteger lastReportId;
@property NSMutableArray *communicationQueue;
@property dispatch_queue_t asyncQueue;
@property NSMutableArray *tempReportData;
@property BOOL isRadarRunning;

@end

@implementation Radar

@synthesize rumRequestSignature = _rumRequestSignature;
@synthesize reportIdLock = _reportIdLock;
@synthesize lastReportId = _lastReportId;
@synthesize communicationQueue = _communicationQueue;
@synthesize asyncQueue = _asyncQueue;
@synthesize tempReportData = _tempReportData;
@synthesize isRadarRunning = _isRadarRunning;

# pragma mark Singleton methods
+ (instancetype)instance {
    static Radar *result = nil;
    static dispatch_once_t token;
    dispatch_once(&token, ^{
        result = [[Radar alloc] init];
    });
    return result;
}

# pragma mark Initialization methods

- (id)init {
    if (self = [super init]) {
        self.reportIdLock = [[NSObject alloc] init];
        self.communicationQueue = [[NSMutableArray alloc] init];
        self.asyncQueue = dispatch_queue_create("com.cedexis.radar", NULL);
        self.tempReportData = [[NSMutableArray alloc] init];
        self.isRadarRunning = NO;
    }
    return self;
}

# pragma mark Instance methods

- (void)enableReporting:(BOOL)enabled {
    [self enableReporting:enabled WithPollingInterval:2];
}

- (void)enableReporting:(BOOL)enabled WithPollingInterval:(NSInteger)interval {
    NSLog(@"Radar reporting %@", enabled == YES ? @"enabled" : @"disabled");
    static NSTimer * timer = nil;
    if (enabled) {
        if (nil == timer) {
            timer = [NSTimer scheduledTimerWithTimeInterval:interval
                                                     target:self
                                                   selector:@selector(sendAsync)
                                                   userInfo:nil
                                                    repeats:YES];
        }
    }
    else {
        if (nil != timer) {
            [timer invalidate];
            timer = nil;
        }
    }
}

- (void)flush {
    NSLog(@"Warning! Radar.flush not implemented");
}

- (void)communicate:(RadarCommunication *)communicationInfo {
    //NSLog(@"Radar.communicate; communication info: %@", communicationInfo);
    
    NSURL *url = [NSURL URLWithString:[communicationInfo url]];
    NSLog(@"URL: %@", url);
    NSURLRequest *request = [NSURLRequest requestWithURL:url
                                             cachePolicy:NSURLRequestReloadIgnoringLocalAndRemoteCacheData
                                         timeoutInterval:20.0];

    void (^onComplete)(NSURLResponse *response, NSData *data, NSError *error) =
        ^(NSURLResponse *response, NSData *data, NSError *error) {
            if (nil != data) {
                if ((nil != communicationInfo.completionQueue) && (nil != communicationInfo.completion)) {
                    dispatch_async(communicationInfo.completionQueue, ^{
                        communicationInfo.completion([communicationInfo.data dictionaryFrom:data]);
                    });
                }
            }
            else if (nil != error) {
                NSLog(@"Error: %@", error);
            }
        };
    
    NSOperationQueue *queue = [[NSOperationQueue alloc] init];
    [NSURLConnection sendAsynchronousRequest:request
                                       queue:queue
                           completionHandler:onComplete];
    
    dispatch_block_t do_notify = ^(void) {
        NSDictionary *notificationData = [communicationInfo toDictionary];
        NSNotificationCenter *notificationCenter = [NSNotificationCenter defaultCenter];
        [notificationCenter postNotificationName:@"Radar Communication Data"
                                          object:self
                                        userInfo:notificationData];
    };
    
    dispatch_async(dispatch_get_main_queue(), do_notify);
}

- (void)sendAsync {
    //NSLog(@"Inside sendAsynchronously");
    // If there's anything in the communication data queue, then shovel it off to
    // Grand Central Dispatch
    @synchronized(self.communicationQueue) {
        while ([self.communicationQueue lastObject]) {
            id radarCommunication = [self.communicationQueue objectAtIndex:0];
            [self.communicationQueue removeObjectAtIndex:0];
            dispatch_queue_t queue = [self asyncQueue];
            dispatch_async(
                queue,
                ^(void) {
                    [self communicate:radarCommunication];
                });
        }
    }
}

- (void)startRUMInitWithZoneId:(NSUInteger)zoneId
                    CustomerId:(NSUInteger)customerId
               CompletionQueue:(dispatch_queue_t)initCompletionQueue
                InitCompletion:(radar_comm_complete_block_t)initCompletion {
    NSUInteger timestamp = [[NSDate date] timeIntervalSince1970];
    NSLog(@"initializeRUMSession, for Zone Id %lu, Customer Id %lu, called at %lu", (unsigned long)zoneId, (unsigned long)customerId, (unsigned long)timestamp);
    
    RadarCommunication *communication = [[RadarCommunication alloc] init];
    communication.data = [[InitData alloc] initWithRequestorZoneId:zoneId
                                               RequestorCustomerId:customerId
                                                      AndTimestamp:timestamp];
    communication.completionQueue = initCompletionQueue;
    communication.completion = ^(NSDictionary *result) {
        @synchronized(self.tempReportData) {
            self.rumRequestSignature = [result valueForKey:@"requestSignature"];
            initCompletion(result);
            //NSLog(@"RUM request signature: %@", self.rumRequestSignature);
            
            while ([self.tempReportData lastObject]) {
                NSMutableDictionary *reportData = [self.tempReportData objectAtIndex:0];
                [self.tempReportData removeObjectAtIndex:0];
                RadarCommunication *communication = [[RadarCommunication alloc] init];
                NSString *reportType = [reportData valueForKey:@"reportType"];
                if ([reportType isEqualToString:@"event"]) {
                    communication.data = [[RUMData alloc]
                                          initWithReportId:[[reportData valueForKey:@"reportId"] unsignedIntegerValue]
                                                 EventName:[reportData valueForKey:@"eventName"]
                                                      Tags:[[reportData valueForKey:@"tags"] unsignedIntegerValue]
                                                 Timestamp:[[reportData valueForKey:@"timestamp"] unsignedLongLongValue]
                                          RequestSignature:self.rumRequestSignature];
                    
                }
                else if ([reportType isEqualToString:@"property"]) {
                    communication.data = [[RUMProperty alloc] initWithReportId:[[reportData valueForKey:@"reportId"] unsignedIntegerValue]
                                                                      Property:[reportData valueForKey:@"property"]
                                                                         Value:[reportData valueForKey:@"value"]
                                                                     Timestamp:[[reportData valueForKey:@"timestamp"] unsignedLongLongValue]
                                                              RequestSignature:self.rumRequestSignature];
                }
                else if ([reportType isEqualToString:@"slice"]) {
                    communication.data = [[RUMSlice alloc] initWithName:[reportData valueForKey:@"name"]
                                                                  Start:[[reportData valueForKey:@"start"] boolValue]
                                                              Timestamp:[[reportData valueForKey:@"timestamp"] unsignedLongLongValue]
                                                       RequestSignature:self.rumRequestSignature];
                }
                else {
                    NSLog(@"Warning! Unexpected report type: %@", reportType);
                    continue;
                }
                
                @synchronized(self.communicationQueue) {
                    [self.communicationQueue addObject:communication];
                }
            }
        }
    };
    
    @synchronized(self.communicationQueue) {
        [self.communicationQueue addObject:communication];
    }
}

- (NSUInteger)reportEvent:(NSString *)eventName {
    return [self reportEvent:eventName WithTags:0];
}

- (NSUInteger)reportEvent:(NSString *)eventName WithTags:(NSUInteger)tags {
    UInt64 timestamp = (UInt64)([[NSDate date] timeIntervalSince1970] * 1000);
    NSLog(@"reportEvent, eventName: \"%@\", tags: %lu, timestamp: %llu", eventName, (unsigned long)tags, timestamp);
    
    NSUInteger reportId;
    @synchronized(self.reportIdLock) {
        self.lastReportId++;
        reportId = self.lastReportId;
    }
    
    @synchronized(self.tempReportData) {
        if (nil == self.rumRequestSignature) {
            // The init request hasn't completed yet
            NSLog(@"Storing report data until init request completes");
            NSMutableDictionary *temp = [[NSMutableDictionary alloc] init];
            [temp setValue:@"event" forKey:@"reportType"];
            [temp setValue:[NSNumber numberWithUnsignedInteger:reportId] forKey:@"reportId"];
            [temp setValue:eventName forKey:@"eventName"];
            [temp setValue:[NSNumber numberWithUnsignedInteger:tags] forKey:@"tags"];
            [temp setValue:[NSNumber numberWithUnsignedLongLong:timestamp] forKey:@"timestamp"];
            [self.tempReportData addObject:temp];
        }
        else {
            RadarCommunication *communication = [[RadarCommunication alloc] init];
            communication.data = [[RUMData alloc] initWithReportId:reportId
                                                         EventName:eventName
                                                              Tags:tags
                                                         Timestamp:timestamp
                                                  RequestSignature:self.rumRequestSignature];
            @synchronized(self.communicationQueue) {
                [self.communicationQueue addObject:communication];
            }
        }
    }
    
    return reportId;
}

- (void)reportSlice:(NSString *)name Start:(BOOL)start {
    UInt64 timestamp = (UInt64)([[NSDate date] timeIntervalSince1970] * 1000);
    NSLog(@"reportSlice, sliceName: \"%@\" %@, timestamp: %llu", name, start == YES ? @"start" : @"end", timestamp);
    
    @synchronized(self.tempReportData) {
        if (nil == self.rumRequestSignature) {
            // The init request hasn't completed yet
            NSLog(@"Storing report data until init request completes");
            NSMutableDictionary *temp = [[NSMutableDictionary alloc] init];
            [temp setValue:@"slice" forKey:@"reportType"];
            [temp setValue:name forKey:@"name"];
            [temp setValue:[NSNumber numberWithBool:start] forKey:@"start"];
            [temp setValue:[NSNumber numberWithUnsignedLongLong:timestamp] forKey:@"timestamp"];
            [self.tempReportData addObject:temp];
        }
        else {
            RadarCommunication *communication = [[RadarCommunication alloc] init];
            communication.data = [[RUMSlice alloc] initWithName:name
                                                          Start:start
                                                      Timestamp:timestamp
                                               RequestSignature:self.rumRequestSignature];
            @synchronized(self.communicationQueue) {
                [self.communicationQueue addObject:communication];
            }
        }
    }
}

- (void)reportProperty:(NSString *)property Value:(NSString *)value ForReport:(NSUInteger)reportId {
    UInt64 timestamp = (UInt64)([[NSDate date] timeIntervalSince1970] * 1000);
    NSLog(@"reportProperty, property: \"%@\", value: \"%@\", reportId: %lu, timestamp: %llu",
          property, value, (unsigned long)reportId, timestamp);
    
    @synchronized(self.tempReportData) {
        if (nil == self.rumRequestSignature) {
            // The init request hasn't completed yet
            NSLog(@"Storing report data until init request completes");
            NSMutableDictionary *temp = [[NSMutableDictionary alloc] init];
            [temp setValue:@"property" forKey:@"reportType"];
            [temp setValue:property forKey:@"property"];
            [temp setValue:value forKey:@"value"];
            [temp setValue:[NSNumber numberWithUnsignedInteger:reportId] forKey:@"reportId"];
            [temp setValue:[NSNumber numberWithUnsignedLongLong:timestamp] forKey:@"timestamp"];
            [self.tempReportData addObject:temp];
        }
        else {
            RadarCommunication *communication = [[RadarCommunication alloc] init];
            communication.data = [[RUMProperty alloc] initWithReportId:reportId
                                                              Property:property
                                                                 Value:value
                                                         Timestamp:timestamp
                                                  RequestSignature:self.rumRequestSignature];
            @synchronized(self.communicationQueue) {
                [self.communicationQueue addObject:communication];
            }
        }
    }
}

- (void)reportProperty:(NSString *)property Value:(NSString *)value {
    [self reportProperty:property Value:value ForReport:0];
}

- (void)probeWithJson:(NSDictionary *)json AndRequestSignature:(NSString *)requestSignature {
    NSDictionary *providerInfo = [json valueForKey:@"p"];
    if (nil == providerInfo) {
        return;
    }
    
    NSUInteger providerZoneId = [[providerInfo valueForKey:@"z"] unsignedIntegerValue];
    NSUInteger providerCustomerId = [[providerInfo valueForKey:@"c"] unsignedIntegerValue];
    NSUInteger providerId = [[providerInfo valueForKey:@"i"] unsignedIntegerValue];
    
    NSArray *probes = [providerInfo valueForKey:@"p"];
    if (nil == probes) {
        return;
    }
    
    BOOL cacheBusting = YES;
    if (0 == [[providerInfo valueForKey:@"b"] integerValue]) {
        cacheBusting = NO;
    }
    
    for (id probe in probes) {
        //NSLog(@"%@", probe);
        NSString * rawURL = [probe valueForKey:@"u"];
        if (nil == rawURL) {
            return;
        }
        NSMutableString *temp = [[NSMutableString alloc] initWithString:rawURL];
        if (cacheBusting) {
            NSString *cacheBustingString = [NSString stringWithFormat:@"rnd=%@",
                                            CFUUIDCreateString(NULL, CFUUIDCreate(NULL))];
            
            NSRange test = [temp rangeOfString:@"?"];
            if (0 < test.length) {
                [temp appendFormat:@"&%@", cacheBustingString];
            }
            else {
                [temp appendFormat:@"?%@", cacheBustingString];
            }
        }
        NSURL *url = [NSURL URLWithString:temp];
        NSLog(@"Probe URL: %@", url);
        
        NSURLRequest *request = [NSURLRequest requestWithURL:url
                                                 cachePolicy:NSURLRequestReloadIgnoringLocalAndRemoteCacheData
                                             timeoutInterval:20.0];
        NSHTTPURLResponse *response;
        NSError *error;
        NSDate *start = [NSDate date];
        NSData *data = [NSURLConnection sendSynchronousRequest:request
                                             returningResponse:&response
                                                         error:&error];
        
        NSInteger probeTypeNum = [[probe valueForKey:@"t"] unsignedIntegerValue];
        if ((nil != data) && (200 == [response statusCode])) {
            NSDate *end = [NSDate date];
            NSInteger elapsed = 1000 * [end timeIntervalSinceDate:start];
            //NSLog(@"Elapsed: %ld", (long)elapsed);
            NSInteger measurement = elapsed;
            if ((14 == probeTypeNum) || (15 == probeTypeNum)
                || (23 == probeTypeNum) || (30 == probeTypeNum)) {
                NSInteger fileSizeHint = [[probe valueForKey:@"s"] integerValue];
                measurement = 8 * 1000 * fileSizeHint / elapsed;
            }
            
            // Send report
            RadarCommunication *radarServerComm = [[RadarCommunication alloc] init];
            radarServerComm.data = [[ProbeReport alloc] initWithProviderZoneId:providerZoneId
                                                            ProviderCustomerId:providerCustomerId
                                                                    ProviderId:providerId
                                                                  ProbeTypeNum:probeTypeNum
                                                                  ResponseCode:0
                                                                   Measurement:measurement
                                                           AndRequestSignature:requestSignature];
            
            url = [NSURL URLWithString:[radarServerComm url]];
            NSLog(@"Radar server URL: %@", url);
            request = [NSURLRequest requestWithURL:url
                                       cachePolicy:NSURLRequestReloadIgnoringLocalAndRemoteCacheData
                                   timeoutInterval:60.0];
            
            data = [NSURLConnection sendSynchronousRequest:request
                                         returningResponse:&response
                                                     error:&error];
            
            if ((nil == data) || (200 != [response statusCode])) {
                NSLog(@"Radar communication error (remote probing report)");
            }
            
            dispatch_block_t do_notify = ^(void) {
                NSDictionary *notificationData = [radarServerComm.data toDictionary];
                NSNotificationCenter *notificationCenter = [NSNotificationCenter defaultCenter];
                [notificationCenter postNotificationName:@"Radar Communication Data"
                                                  object:self
                                                userInfo:notificationData];
            };
            
            dispatch_async(dispatch_get_main_queue(), do_notify);
        }
        else {
            NSLog(@"Probe download error");
            
            // Send report
            RadarCommunication *radarServerComm = [[RadarCommunication alloc] init];
            radarServerComm.data = [[ProbeReport alloc] initWithProviderZoneId:providerZoneId
                                                            ProviderCustomerId:providerCustomerId
                                                                    ProviderId:providerId
                                                                  ProbeTypeNum:probeTypeNum
                                                                  ResponseCode:1
                                                                   Measurement:0
                                                           AndRequestSignature:requestSignature];
            
            url = [NSURL URLWithString:[radarServerComm url]];
            request = [NSURLRequest requestWithURL:url
                                       cachePolicy:NSURLRequestReloadIgnoringLocalAndRemoteCacheData
                                   timeoutInterval:60.0];
            
            data = [NSURLConnection sendSynchronousRequest:request
                                         returningResponse:&response
                                                     error:&error];
            
            if ((nil == data) || (200 != [response statusCode])) {
                NSLog(@"Radar communication error (remote probing error report)");
            }
            
            dispatch_block_t do_notify = ^(void) {
                NSDictionary *notificationData = [radarServerComm.data toDictionary];
                NSNotificationCenter *notificationCenter = [NSNotificationCenter defaultCenter];
                [notificationCenter postNotificationName:@"Radar Communication Data"
                                                  object:self
                                                userInfo:notificationData];
            };
            
            dispatch_async(dispatch_get_main_queue(), do_notify);
        }
    }
}

- (NetworkStatus)reachability {
    Reachability *internetReach = [Reachability reachabilityForInternetConnection];
    return [internetReach currentReachabilityStatus];
}

- (void)networkReportJson:(NSDictionary *)json
      AndRequestSignature:(NSString *)requestSignature {
    NetworkStatus internetStatus = [self reachability];
    
    //NSLog(@"Internet reachability: %u", internetStatus);
    NSInteger networkType = 0;
    if (internetStatus == NotReachable) {
        return;
    }
    else if (internetStatus == ReachableViaWiFi) {
        networkType = 1;
    }
    
    RadarCommunication *comm = [[RadarCommunication alloc] init];
    comm.data = [[NetworkTypeReport alloc] initWithNetworkType:networkType
                                                       SubType:0
                                           AndRequestSignature:requestSignature];
    
    NSURL *url = [NSURL URLWithString:[comm url]];
    NSLog(@"Network type report URL: %@", url);
    
    NSURLRequest *request = [NSURLRequest requestWithURL:url
                                             cachePolicy:NSURLRequestReloadIgnoringLocalAndRemoteCacheData
                                         timeoutInterval:60.0];
    
    NSHTTPURLResponse *response;
    NSError *error;
    NSData *data;
    data = [NSURLConnection sendSynchronousRequest:request
                                 returningResponse:&response
                                             error:&error];
    
    if ((nil == data) || (200 != [response statusCode])) {
        NSLog(@"Radar communication error (network type report)");
    }
    
    dispatch_block_t do_notify = ^(void) {
        NSDictionary *notificationData = [comm.data toDictionary];
        NSNotificationCenter *notificationCenter = [NSNotificationCenter defaultCenter];
        [notificationCenter postNotificationName:@"Radar Communication Data"
                                          object:self
                                        userInfo:notificationData];
    };
    
    dispatch_async(dispatch_get_main_queue(), do_notify);
}

- (void)processProviderType:(NSString *)providerSpecType
                       Json:(NSDictionary *)json
        AndRequestSignature:(NSString *)requestSignature {
    if ([providerSpecType isEqualToString:@"networktype"]) {
        [self networkReportJson:json AndRequestSignature:requestSignature];
    }
    else if ([providerSpecType isEqualToString:@"probe"]) {
        [self probeWithJson:json AndRequestSignature:requestSignature];
    }
}

- (BOOL)scheduleRemoteProbingWithZoneId:(NSUInteger)zoneId
                          AndCustomerId:(NSUInteger)customerId {

    dispatch_block_t remoteProbing = ^(void) {
        @try {
            NSUInteger timestamp = [[NSDate date] timeIntervalSince1970];
            RadarCommunication *initCommunication = [[RadarCommunication alloc] init];
            initCommunication.data = [[InitData alloc] initWithRequestorZoneId:zoneId
                                                           RequestorCustomerId:customerId
                                                                  AndTimestamp:timestamp];
            
            NSURL *url = [NSURL URLWithString:[initCommunication url]];
            NSLog(@"Init URL: %@", url);
            
            NSURLRequest *request = [NSURLRequest requestWithURL:url
                                                     cachePolicy:NSURLRequestReloadIgnoringLocalAndRemoteCacheData
                                                 timeoutInterval:20.0];
            
            NSHTTPURLResponse *response;
            NSError *error;
            NSData *data;
            data = [NSURLConnection sendSynchronousRequest:request
                                         returningResponse:&response
                                                     error:&error];
            
            if ((nil == data) || (200 != [response statusCode])) {
                NSLog(@"Radar communication error (init)");
            }
            
            dispatch_block_t do_notify = ^(void) {
                NSDictionary *notificationData = [initCommunication.data toDictionary];
                NSNotificationCenter *notificationCenter = [NSNotificationCenter defaultCenter];
                [notificationCenter postNotificationName:@"Radar Communication Data"
                                                  object:self
                                                userInfo:notificationData];
            };
            
            dispatch_async(dispatch_get_main_queue(), do_notify);
            
            if ((nil != data) && (200 == [response statusCode])) {
                NSString *requestSignature = [[initCommunication.data dictionaryFrom:data] valueForKey:@"requestSignature"];
                if (![requestSignature isKindOfClass:[NSString class]]) {
                    // Something is wrong with the init response
                    return;
                }
                //NSLog(@"Init complete...request signature: %@", requestSignature);
                NSMutableArray *providerIds = [[NSMutableArray alloc] init];
                BOOL keepGoing = YES;
                while (keepGoing) {
                    
                    BOOL onWifi = [self reachability] == ReachableViaWiFi;
                    
                    RadarCommunication *probeServerComm = [[RadarCommunication alloc] init];
                    probeServerComm.data = [[ProbeServerQuery alloc] initWithRequestorZoneId:zoneId
                                                                         RequestorCustomerId:customerId
                                                                                 ProviderIds:providerIds
                                                                                      OnWifi:onWifi];
                    
                    NSError *error;
                    NSURL *url = [NSURL URLWithString:[probeServerComm url]];
                    //NSLog(@"Probe server URL: %@", url);
                    request = [NSURLRequest requestWithURL:url
                                               cachePolicy:NSURLRequestReloadIgnoringLocalAndRemoteCacheData
                                           timeoutInterval:60.0];
                    
                    NSData *data;
                    data = [NSURLConnection sendSynchronousRequest:request
                                                 returningResponse:&response
                                                             error:&error];
                    
                    if ((nil == data) || (200 != [response statusCode])) {
                        NSLog(@"Radar communication error (Probe Server)");
                    }
                    
                    if ((nil != data) && (200 == [response statusCode])) {
                        //NSLog(@"Got probe server response!");
                        NSError *parseError;
                        NSDictionary *json = [NSJSONSerialization JSONObjectWithData:data
                                                                             options:0
                                                                               error:&parseError];
                        //NSLog(@"Provider data: %@", json);
                        
                        NSString *providerSpecType = [json valueForKey:@"a"];
                        if (nil != providerSpecType) {
                            NSDictionary *provider = [json valueForKey:@"p"];
                            if (nil != provider) {
                                id providerId = [provider valueForKey:@"i"];
                                if (nil != providerId) {
                                    NSLog(@"Provider id: %@", providerId);
                                    [providerIds addObject:providerId];
                                    [self processProviderType:providerSpecType
                                                         Json:json
                                          AndRequestSignature:requestSignature];
                                }
                            }
                        }
                        else {
                            // This is most likely normal termination
                            keepGoing = NO;
                        }
                    }
                    else {
                        keepGoing = NO;
                    }
                }
            }
        }
        @finally {
            self.isRadarRunning = NO;
        }
    };

    if ([self isRadarRunning]) {
        NSLog(@"Skipping remote probing. Radar session still active");
        return NO;
    }
    
    self.isRadarRunning = YES;
    NSLog(@"Scheduling remote probing for Zone Id %lu Customer Id %lu", (unsigned long)zoneId, (unsigned long)customerId);
    
    // We're on the main thread here, so shovel this off to Grand Central Dispatch
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0), remoteProbing);
    
    return YES;
}

@end
