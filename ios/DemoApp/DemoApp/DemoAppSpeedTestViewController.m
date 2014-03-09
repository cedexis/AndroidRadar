//
//  DemoAppSpeedTestViewController.m
//  DemoApp
//
//  Created by Jacob Wan on 3/16/13.
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import "DemoAppSpeedTestViewController.h"
#import "DemoAppAppDelegate.h"
#import "Radar.h"
#import "RadarVars.h"

@interface DemoAppSpeedTestViewController ()
@property (nonatomic, strong) NSMutableDictionary *providerData;
@property (nonatomic, strong) NSMutableArray *tableProviders;
@property (nonatomic, strong) NSString *currentValue;
@property (nonatomic, strong) NSString *requestSignature;
@property (nonatomic, strong) NSString *countryCode;
@property (nonatomic) NSUInteger reloadId;
@end

@implementation DemoAppSpeedTestViewController

@synthesize tableView = _tableView;
@synthesize providerData = _providerData;
@synthesize tableProviders = _tableProviders;
@synthesize currentValue = _currentValue;
@synthesize countryCode = _countryCode;
@synthesize reloadId = _reloadId;

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

- (void)parser:(NSXMLParser *)parser
 didEndElement:(NSString *)elementName
  namespaceURI:(NSString *)namespaceURI
 qualifiedName:(NSString *)qName {
    if ([elementName isEqualToString:@"requestSignature"]) {
        self.requestSignature = self.currentValue;
    }
    else if ([elementName isEqualToString:@"countryIso"]) {
        self.countryCode = self.currentValue;
    }
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string {
    self.currentValue = string;
}

- (NSMutableDictionary *)providerData {
    if (nil != _providerData) {
        return _providerData;
    }
    _providerData = [[NSMutableDictionary alloc] init];
    return _providerData;
}

- (NSMutableArray *)tableProviders {
    if (nil != _tableProviders) {
        return _tableProviders;
    }
    _tableProviders = [[NSMutableArray alloc] init];
    return _tableProviders;
}

- (NSMutableArray *)tableProvidersForCategory:(NSString *)category {
    for (NSDictionary *categoryProviders in self.tableProviders) {
        if ([[categoryProviders objectForKey:@"category"] isEqualToString:category]) {
            return [categoryProviders objectForKey:@"providers"];
        }
    }
    return nil;
}

- (void)speedTestFailed:(NSString *)reason {
    dispatch_async(dispatch_get_main_queue(), ^{
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:nil
                                                        message:reason
                                                       delegate:nil
                                              cancelButtonTitle:@"Ok"
                                              otherButtonTitles:nil];
        [alert show];
    });
}

- (void)loadPublicProviders:(NSData *)providerData ReloadId:(NSUInteger)reloadId {
    if (self.reloadId != reloadId) {
        return;
    }
    NSError *parseError;
    id providers = [NSJSONSerialization JSONObjectWithData:providerData
                                                   options:0
                                                     error:&parseError];
    NSMutableDictionary *targetProviders = [self providerData];
    for (NSDictionary *provider in providers) {
        NSString *category = [provider valueForKey:@"provider_category"];
        NSMutableDictionary *categoryProviders = [targetProviders objectForKey:category];
        if (nil == categoryProviders) {
            categoryProviders = [[NSMutableDictionary alloc] init];
            [targetProviders setObject:categoryProviders forKey:category];
        }
        
        id providerId = [provider objectForKey:@"provider_id"];
        NSString *providerName = [provider objectForKey:@"provider_name"];
        if (nil != providerName) {
            NSMutableDictionary *newProvider = [[NSMutableDictionary alloc] init];
            [newProvider setObject:providerName forKey:@"providerName"];
            [newProvider setObject:[provider objectForKey:@"iso_weight"] forKey:@"isoWeight"];
            [newProvider setObject:[provider objectForKey:@"iso_weight_list"] forKey:@"isoWeightList"];
            [newProvider setObject:[provider objectForKey:@"weight"] forKey:@"weight"];
            
            for (NSDictionary *probe in [provider objectForKey:@"probes"]) {
                NSString *key;
                switch ([[probe objectForKey:@"probe_type_num"] integerValue]) {
                    case 1:
                        key = @"coldURL";
                        break;
                    case 0:
                        key = @"rttURL";
                        break;
                    case 14:
                        key = @"throughputURL";
                        break;
                    case 2:
                        key = @"customURL";
                        break;
                }
                if (key) {
                    [newProvider setObject:[probe objectForKey:@"url"] forKey:key];
                }
                
                if (14 == [[probe objectForKey:@"probe_type_num"] integerValue]) {
                    [newProvider setObject:[probe objectForKey:@"file_size_hint"] forKey:@"throughputFileSizeHint"];
                }
            }
            [categoryProviders setObject:newProvider forKey:providerId];
        }
    }
}

- (BOOL)speedTestInit {
    NSUInteger timestamp = [[NSDate date] timeIntervalSince1970];
    NSUInteger transactionId = arc4random();
    NSString *urlString = [NSString stringWithFormat:@"http://i1-io-0-1-1-10816-%lu-i.%@/i1/%lu/%lu/xml",
                           (unsigned long)transactionId,
                           @"init.cedexis-radar.net",
                           (unsigned long)timestamp,
                           (unsigned long)transactionId];
    NSURL *url = [NSURL URLWithString:urlString];
    NSURLRequest *request = [NSURLRequest
                             requestWithURL:url
                             cachePolicy:NSURLRequestReloadIgnoringLocalAndRemoteCacheData
                             timeoutInterval:12.0];
    
    NSHTTPURLResponse *response;
    NSError *error;
    NSData *data = [NSURLConnection sendSynchronousRequest:request
                                         returningResponse:&response
                                                     error:&error];
    
    if (nil == data) {
        [self speedTestFailed:@"Failed to perform Radar init request.  Please check your connection and try again."];
    }
    else if (200 != [response statusCode]) {
        [self speedTestFailed:[NSString stringWithFormat:@"There was a problem with the Radar init request."
                               "\n\nStatus code: %ld",
                               (long)[response statusCode]]];
    }
    else {
        NSXMLParser *parser = [[NSXMLParser alloc] initWithData:data];
        [parser setDelegate:self];
        if ([parser parse]) {
            return YES;
        }
        else {
            [self speedTestFailed:@"Failed to parse init result."];
        }
    }
    return NO;
}

- (void)setupProviders:(NSUInteger)reloadId {
    if (self.reloadId != reloadId) {
        return;
    }
    //self.countryCode = @"RU";
    NSMutableArray *tableProviders = [self tableProviders];
    
    // Put these in the desired
    NSArray *orderedCategories = [NSArray arrayWithObjects:
                                  @"Delivery Networks",
                                  @"Cloud Computing",
                                  @"Dynamic Content",
                                  @"Web Benchmarks",
                                  @"Other Cloud Services", nil];
    for (NSString *category in orderedCategories) {
        [tableProviders addObject:[NSDictionary
                                   dictionaryWithObjectsAndKeys:
                                   category,
                                   @"category",
                                   [[NSMutableArray alloc] init],
                                   @"providers",
                                   nil]];
    }

    for (NSString *category in self.providerData) {
        NSLog(@"Category: %@", category);
        NSDictionary *categoryProviders = [self.providerData objectForKey:category];
        for (NSString *providerId in categoryProviders) {
            BOOL include = NO;
            NSDictionary * provider = [categoryProviders objectForKey:providerId];
            NSArray *isoWeightList = [provider objectForKey:@"isoWeightList"];
            if (0 < [isoWeightList count]) {
                if ([isoWeightList containsObject:self.countryCode]) {
                    if (0 < [[provider objectForKey:@"isoWeight"] integerValue]) {
                        include = YES;
                    }
                }
                else {
                    if (0 < [[provider objectForKey:@"weight"] integerValue]) {
                        include = YES;
                    }
                }
            }
            else {
                if (0 < [[provider objectForKey:@"weight"] integerValue]) {
                    include = YES;
                }
            }
            if (include) {
                NSMutableArray *targetArray = [self tableProvidersForCategory:category];
                if (targetArray) {
                    NSMutableDictionary *newProvider = [NSMutableDictionary dictionaryWithDictionary:provider];
                    [newProvider setObject:providerId forKey:@"providerId"];
                    [targetArray addObject:newProvider];
                }
            }
        }
    }
    
    NSMutableArray *toDiscard = [NSMutableArray array];
    for (NSDictionary *categoryProviders in tableProviders) {
        NSMutableArray *providers = [categoryProviders objectForKey:@"providers"];
        if (0 < [providers count]) {
            [providers sortUsingComparator:^NSComparisonResult(id obj1, id obj2) {
                NSString *left = [obj1 objectForKey:@"providerName"];
                NSString *right = [obj2 objectForKey:@"providerName"];
                return [left compare:right options:NSCaseInsensitiveSearch];
            }];
        }
        else {
            [toDiscard addObject:categoryProviders];
        }
    }
    NSLog(@"To discard: %@", toDiscard);
    [self.tableProviders removeObjectsInArray:toDiscard];
    NSLog(@"Table Providers: %@", self.tableProviders);
}

- (int)getElapsed:(NSString *)url {
    //NSLog(@"%@", url);
    NSURLRequest *request = [NSURLRequest requestWithURL:[NSURL URLWithString:url]
                                             cachePolicy:NSURLRequestReloadIgnoringLocalAndRemoteCacheData
                                         timeoutInterval:12.0];
    NSError *error;
    NSHTTPURLResponse *response;
    NSDate *start = [NSDate date];
    NSData *data = [NSURLConnection sendSynchronousRequest:request
                                         returningResponse:&response
                                                     error:&error];
    
    NSDate *end = [NSDate date];
    if ((nil != data) && (200 == [response statusCode])) {
        return 1000 * [end timeIntervalSinceDate:start];
    }
    
    return -1;
}

- (void)doMeasurement:(NSMutableDictionary *)provider IndexPath:(NSIndexPath *)indexPath
             ReloadId:(NSUInteger)reloadId {
    if (self.reloadId != reloadId) {
        return;
    }
    NSString *url = [provider objectForKey:@"coldURL"];
    int elapsed;
    if (url) {
        elapsed = [self getElapsed:url];
        if (0 < elapsed) {
            [provider setObject:[NSString stringWithFormat:@"%d ms", elapsed] forKey:@"connectResult"];
        }
        else {
            [provider setObject:@"Error" forKey:@"connectResult"];
        }
    }
    
    if (self.reloadId != reloadId) {
        return;
    }
    url = [provider objectForKey:@"rttURL"];
    if (url) {
        elapsed = [self getElapsed:url];
        if (0 < elapsed) {
            [provider setObject:[NSString stringWithFormat:@"%d ms", elapsed] forKey:@"rttResult"];
        }
        else {
            [provider setObject:@"Error" forKey:@"rttResult"];
        }
    }
    
    if (self.reloadId != reloadId) {
        return;
    }
    url = [provider objectForKey:@"throughputURL"];
    if (url) {
        elapsed = [self getElapsed:url];
        if (0 < elapsed) {
            NSInteger fileSizeHint = [[provider valueForKey:@"throughputFileSizeHint"] integerValue];
            int throughput = 8 * 1000 * fileSizeHint / elapsed;
            [provider setObject:[NSString stringWithFormat:@"%d Kb/s", throughput] forKey:@"throughputResult"];
        }
        else {
            NSLog(@"Elapsed time less than 1");
            [provider setObject:@"Error" forKey:@"throughputResult"];
        }
    }
    
    if (self.reloadId != reloadId) {
        return;
    }
    url = [provider objectForKey:@"customURL"];
    if (url) {
        elapsed = [self getElapsed:url];
        if (0 < elapsed) {
            [provider setObject:[NSString stringWithFormat:@"%d ms", elapsed] forKey:@"customResult"];
        }
        else {
            [provider setObject:@"Error" forKey:@"customResult"];
        }
    }
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if (self.reloadId != reloadId) {
            return;
        }
        [self.tableView reloadRowsAtIndexPaths:[NSArray arrayWithObject:indexPath]
                              withRowAnimation:UITableViewRowAnimationAutomatic];
    });
}

- (void)doMeasurements:(NSUInteger)reloadId {
    if (self.reloadId != reloadId) {
        return;
    }
    NSArray *tableProviders = self.tableProviders;
    int section = 0;
    for (NSDictionary *categoryProviders in tableProviders) {
        int row = 0;
        for (NSMutableDictionary *provider in [categoryProviders objectForKey:@"providers"]) {
            [self doMeasurement:provider IndexPath:[NSIndexPath indexPathForItem:row inSection:section] ReloadId:reloadId];
            row++;
        }
        section++;
    }
}

- (void)doSpeedTestReloadId:(NSUInteger)reloadId {
    if (self.reloadId != reloadId) {
        return;
    }
    NSURL *url = [NSURL URLWithString:@"http://probes.cedexis.com/publicproviders"];
    
    // Obtain the public providers list
    NSURLRequest *request = [NSURLRequest
                             requestWithURL:url
                                cachePolicy:NSURLRequestReloadIgnoringLocalAndRemoteCacheData
                            timeoutInterval:20.0];
    
    NSHTTPURLResponse *response;
    NSError *error;
    NSData *data = [NSURLConnection sendSynchronousRequest:request
                                         returningResponse:&response
                                                     error:&error];
    if (nil == data) {
        [self speedTestFailed:@"Failed to download public provider data"
             " from Cedexis.  Please check your connection and try again."];
    }
    else if (200 != [response statusCode]) {
        [self speedTestFailed:[NSString stringWithFormat:@"There was a problem"
            " downloading public provider data from Cedexis\n\nStatus code: %ld",
                (long)[response statusCode]]];
    }
    else {
        [self loadPublicProviders:data ReloadId:reloadId];
        if ([self speedTestInit]) {
            [self setupProviders:reloadId];
            
            dispatch_async(dispatch_get_main_queue(), ^{
                [self.tableView reloadData];
            });
            
            [self doMeasurements:reloadId];
        }
    }
}

- (IBAction)doSpeedTest:(id)sender {
    NSLog(@"Running speed test");
    
    // Reinitialize providers
    _providerData = nil;
    _tableProviders = nil;
    
    NSUInteger reloadId = self.reloadId = arc4random();
    
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [self doSpeedTestReloadId:reloadId];
    });
    
    // Report RUM event
    [[Radar instance] reportEvent:RadarEventsSpeedTest];
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [self.tableProviders count];
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
    int i = 0;
    for (NSDictionary *categoryProviders in self.tableProviders) {
        if (i == section) {
            return [categoryProviders objectForKey:@"category"];
        }
        i++;
    }
    return @"Unknown";
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    int i = 0;
    for (NSDictionary *categoryProviders in self.tableProviders) {
        NSArray *providers = [categoryProviders objectForKey:@"providers"];
        if (i == section) {
            return [providers count];
        }
        i++;
    }
    return 0;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *cellId = @"SpeedTestCell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellId
                                                            forIndexPath:indexPath];
    
    // Configure the cell...
    if (nil == cell) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault
                                      reuseIdentifier:cellId];
    }
    
    // Set up the cell
    [self configureCell:cell ForRowAtIndexPath:indexPath];
    
    return cell;
}

- (void)configureCell:(UITableViewCell *)cell ForRowAtIndexPath:(NSIndexPath *)indexPath {
    int i = 0;
    NSArray *tableProviders = self.tableProviders;
    for (NSDictionary *categoryProviders in tableProviders) {
        if (i == indexPath.section) {
            NSArray *array = [categoryProviders objectForKey:@"providers"];
            NSDictionary *provider = [array objectAtIndex:indexPath.row];
            cell.textLabel.text = [provider objectForKey:@"providerName"];
            id connectResult = [provider objectForKey:@"connectResult"];
            id rttResult = [provider objectForKey:@"rttResult"];
            id throughputResult = [provider objectForKey:@"throughputResult"];
            id customResult = [provider objectForKey:@"customResult"];
            NSMutableArray *cellTextParts = [NSMutableArray array];
            if (nil != connectResult) {
                [cellTextParts addObject:[NSString stringWithFormat:@"Connect: %@", connectResult]];
            }
            
            if (nil != rttResult) {
                [cellTextParts addObject:[NSString stringWithFormat:@"RTT: %@", rttResult]];
            }
            
            if (nil != throughputResult) {
                [cellTextParts addObject:[NSString stringWithFormat:@"Throughput: %@", throughputResult]];
            }
            
            if (nil != customResult) {
                [cellTextParts addObject:[NSString stringWithFormat:@"Custom: %@", customResult]];
            }
            
            cell.detailTextLabel.numberOfLines = [cellTextParts count];
            if (0 < [cellTextParts count]) {
                cell.detailTextLabel.text = [cellTextParts componentsJoinedByString:@"\n"];
            }
            else {
                cell.detailTextLabel.text = @"Queued";
            }
        }
        i++;
    }
}

@end
