//
//  ScaryBugsMasterViewController.m
//  ScaryBugs
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import "ScaryBugsMasterViewController.h"
#import "ScaryBugsDetailViewController.h"
#import "ScaryBugDoc.h"
#import "ScaryBugData.h"
#import "Radar.h"
#import "RadarVars.h"

@implementation ScaryBugsMasterViewController

@synthesize bugs = _bugs;

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    
    // Report the RUM event
    [Radar reportEvent:RadarEventsViewWillDisappear
              WithTags:RadarTagsMasterViewController | RadarTagsLevelDebug];
}


-(void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self.tableView reloadData];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    // Report the RUM event
    [Radar reportEvent:RadarEventsViewDidAppear
              WithTags:RadarTagsMasterViewController | RadarTagsLevelDebug];
}

- (void)awakeFromNib
{
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        self.clearsSelectionOnViewWillAppear = NO;
        self.contentSizeForViewInPopover = CGSizeMake(320.0, 600.0);
    }
    [super awakeFromNib];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    self.navigationItem.leftBarButtonItem = self.editButtonItem;
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc]
        initWithBarButtonSystemItem:UIBarButtonSystemItemAdd
        target:self
        action:@selector(addTapped:)];

    self.detailViewController = (ScaryBugsDetailViewController *)[[self.splitViewController.viewControllers lastObject] topViewController];
    self.title = @"Scary Bugs";
    
    // Report the RUM event
    [Radar reportEvent:RadarEventsViewDidLoad
              WithTags:RadarTagsMasterViewController | RadarTagsLevelDebug];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)addTapped:(id)sender
{
    // Report the RUM event.  Do this early in the event handler in order to trap any processing
    // that it contains.
    [Radar reportEvent:RadarEventsAddTapped
              WithTags:RadarTagsMasterViewController | RadarTagsLevelDebug];
    
    ScaryBugDoc *newDoc = [[ScaryBugDoc alloc]
                           initWithTitle:@"New Bug"
                           AndRating:0
                           AndThumbImage:nil
                           AndFullImage:nil];
    [_bugs addObject:newDoc];
    NSIndexPath *indexPath = [NSIndexPath indexPathForRow:_bugs.count-1 inSection:0];
    NSArray *indexPaths = [NSArray arrayWithObject:indexPath];
    [self.tableView insertRowsAtIndexPaths:indexPaths withRowAnimation:YES];
    [self.tableView selectRowAtIndexPath:indexPath animated:YES scrollPosition:UITableViewScrollPositionMiddle];
    [self performSegueWithIdentifier:@"MySegue" sender:self];
}

- (NSUInteger)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskAll;
}

- (UIInterfaceOrientation)preferredInterfaceOrientationForPresentation {
    return UIInterfaceOrientationPortrait;
}

#pragma mark - Table View


- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return self.bugs.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"MyBasicCell"];
    ScaryBugDoc *doc = [self.bugs objectAtIndex:indexPath.row];
    cell.textLabel.text = doc.data.title;
    cell.imageView.image = doc.thumbImage;
    return cell;
}

- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the specified item to be editable.
    return YES;
}

- (void)tableView:(UITableView *)tableView
    commitEditingStyle:(UITableViewCellEditingStyle)editingStyle
    forRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        [self.bugs removeObjectAtIndex:indexPath.row];
        [tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath]
            withRowAnimation:UITableViewRowAnimationFade];
    }
}

/*
// Override to support rearranging the table view.
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath
{
}
*/

/*
// Override to support conditional rearranging of the table view.
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the item to be re-orderable.
    return YES;
}
*/

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    ScaryBugsDetailViewController *detailController = segue.destinationViewController;
    ScaryBugDoc *doc = [self.bugs objectAtIndex:self.tableView.indexPathForSelectedRow.row];
    detailController.detailItem = doc;
}



@end
