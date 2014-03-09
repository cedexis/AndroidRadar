//
//  Radar.m
//  Radar
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import "Radar.h"
#import "RadarCommunication.h"
#import "InitData.h"
#import "ProbeServerQuery.h"
#import "ProbeReport.h"
#import "NetworkTypeReport.h"
#import "Reachability.h"

@interface Radar()

@property BOOL isRadarRunning;

@end

@implementation Radar

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
        self.isRadarRunning = NO;
    }
    return self;
}

# pragma mark Instance methods

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
