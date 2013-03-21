//
//  DemoAppMeasurementDetailViewController.m
//  DemoApp
//
//  Created by Jacob Wan on 3/14/13.
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import "DemoAppMeasurementDetailViewController.h"
#import "DemoAppAppDelegate.h"
#import "ReportData.h"
#import "Report.h"
#import "Init.h"
#import "RUMEvent.h"
#import "DemoAppRUMProperty.h"
#import "DemoAppRUMSlice.h"
#import "NetworkType.h"
#import "RemoteProbe.h"

@interface DemoAppMeasurementDetailViewController ()
@property (nonatomic, strong) NSMutableArray *data;
@end

@implementation DemoAppMeasurementDetailViewController

@synthesize tableView = _tableView;
@synthesize report = _report;
@synthesize data = _data;

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
    
    _data = [[NSMutableArray alloc] init];
    DemoAppAppDelegate *del = [[UIApplication sharedApplication] delegate];
    NSManagedObjectContext *context = del.managedObjectContext;
    NSFetchRequest *request;
    NSError *error;
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"report=%@", self.report];
    if ([self.report.type isEqualToString:@"init"]) {
        request = [NSFetchRequest fetchRequestWithEntityName:@"Init"];
        request.predicate = pred;
        Init *temp = [[context executeFetchRequest:request
                                             error:&error] objectAtIndex:0];
        [self.data addObject:[[ReportData alloc] initWithName:@"Requestor Zone Id"
                                                    Value:temp.requestorZoneId]];
        [self.data addObject:[[ReportData alloc] initWithName:@"Requestor Customer Id"
                                                        Value:temp.requestorCustomerId]];
        [self.data addObject:[[ReportData alloc] initWithName:@"Transaction Id"
                                                        Value:temp.transactionId]];
        [self.data addObject:[[ReportData alloc] initWithName:@"Timestamp"
                                                        Value:temp.timestamp]];
    }
    else if ([self.report.type isEqualToString:@"rumevent"]) {
        request = [NSFetchRequest fetchRequestWithEntityName:@"RUMEvent"];
        request.predicate = pred;
        RUMEvent *temp = [[context executeFetchRequest:request
                                                  error:&error] objectAtIndex:0];
        [self.data addObject:[[ReportData alloc] initWithName:@"Report Id"
                                                        Value:temp.reportId]];
        [self.data addObject:[[ReportData alloc] initWithName:@"Event Name"
                                                        Value:temp.eventName]];
        [self.data addObject:[[ReportData alloc] initWithName:@"Tags"
                                                        Value:temp.tags]];
        [self.data addObject:[[ReportData alloc] initWithName:@"Timestamp"
                                                        Value:temp.timestamp]];
        [self.data addObject:[[ReportData alloc] initWithName:@"Request Signature"
                                                        Value:temp.requestSignature]];
    }
    else if ([self.report.type isEqualToString:@"rumproperty"]) {
        request = [NSFetchRequest fetchRequestWithEntityName:@"RUMProperty"];
        request.predicate = pred;
        DemoAppRUMProperty *temp = [[context executeFetchRequest:request
                                                    error:&error] objectAtIndex:0];
        [self.data addObject:[[ReportData alloc] initWithName:@"Report Id"
                                                        Value:temp.reportId]];
        [self.data addObject:[[ReportData alloc] initWithName:@"Property"
                                                        Value:temp.property]];
        [self.data addObject:[[ReportData alloc] initWithName:@"Value"
                                                        Value:temp.value]];
        [self.data addObject:[[ReportData alloc] initWithName:@"Timestamp"
                                                        Value:temp.timestamp]];
        [self.data addObject:[[ReportData alloc] initWithName:@"Request Signature"
                                                        Value:temp.requestSignature]];
    }
    else if ([self.report.type isEqualToString:@"rumslice"]) {
        request = [NSFetchRequest fetchRequestWithEntityName:@"RUMSlice"];
        request.predicate = pred;
        DemoAppRUMSlice *temp = [[context executeFetchRequest:request
                                                        error:&error] objectAtIndex:0];
        [self.data addObject:[[ReportData alloc] initWithName:@"Slice Name"
                                                        Value:temp.name]];
        [self.data addObject:[[ReportData alloc] initWithName:@"Start"
                                                        Value:temp.start]];
        [self.data addObject:[[ReportData alloc] initWithName:@"Timestamp"
                                                        Value:temp.timestamp]];
        [self.data addObject:[[ReportData alloc] initWithName:@"Request Signature"
                                                        Value:temp.requestSignature]];
    }
    else if ([self.report.type isEqualToString:@"networktype"]) {
        request = [NSFetchRequest fetchRequestWithEntityName:@"NetworkType"];
        request.predicate = pred;
        NetworkType *temp = [[context executeFetchRequest:request
                                                           error:&error] objectAtIndex:0];
        [self.data addObject:[[ReportData alloc] initWithName:@"Type"
                                                        Value:temp.type]];
        [self.data addObject:[[ReportData alloc] initWithName:@"Sub-Type"
                                                        Value:temp.subType]];
        [self.data addObject:[[ReportData alloc] initWithName:@"Request Signature"
                                                        Value:temp.requestSignature]];
    }
    else if ([self.report.type isEqualToString:@"remoteprobe"]) {
        request = [NSFetchRequest fetchRequestWithEntityName:@"RemoteProbe"];
        request.predicate = pred;
        RemoteProbe *temp = [[context executeFetchRequest:request
                                                           error:&error] objectAtIndex:0];
        [self.data addObject:[[ReportData alloc] initWithName:@"Provider Zone Id"
                                                        Value:temp.providerZoneId]];
        [self.data addObject:[[ReportData alloc] initWithName:@"Provider Customer Id"
                                                        Value:temp.providerCustomerId]];
        [self.data addObject:[[ReportData alloc] initWithName:@"Provider Id"
                                                        Value:temp.providerId]];
        [self.data addObject:[[ReportData alloc] initWithName:@"Probe Type Number"
                                                        Value:temp.probeTypeNum]];
        [self.data addObject:[[ReportData alloc] initWithName:@"Response Code"
                                                        Value:temp.responseCode]];
        [self.data addObject:[[ReportData alloc] initWithName:@"Measurement"
                                                        Value:temp.measurement]];
        [self.data addObject:[[ReportData alloc] initWithName:@"Request Signature"
                                                        Value:temp.requestSignature]];
    }
    else {
        NSLog(@"Unexpected report type: %@", self.report.type);
        exit(1);
    }
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

-(NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [self.data count];
}

-(UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [self.tableView dequeueReusableCellWithIdentifier:@"Cell" forIndexPath:indexPath];
    ReportData *data = [self.data objectAtIndex:indexPath.row];
    cell.textLabel.text = data.name;
    cell.detailTextLabel.text = [NSString stringWithFormat:@"%@", data.value];
    return cell;
}

-(NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
    return self.report.reportTypeString;
}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [self.tableView cellForRowAtIndexPath:indexPath];
    //NSLog(@"%@", cell.detailTextLabel.text);
    UIAlertView *view = [[UIAlertView alloc] initWithTitle:cell.textLabel.text
                                                   message:cell.detailTextLabel.text
                                                  delegate:nil
                                         cancelButtonTitle:@"Ok"
                                         otherButtonTitles:nil];
    [view show];
}

@end
