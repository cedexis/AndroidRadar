//
//  ScaryBugsDetailViewController.m
//  ScaryBugs
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import "ScaryBugsDetailViewController.h"
#import "ScaryBugsAppDelegate.h"
#import "ScaryBugDoc.h"
#import "ScaryBugData.h"
#import "UIImageExtras.h"
#import "SVProgressHUD.h"
#import "Radar.h"
#import "RadarVars.h"

@interface ScaryBugsDetailViewController ()
@property (strong, nonatomic) UIPopoverController *masterPopoverController;
- (void)configureView;
@end

@implementation ScaryBugsDetailViewController

@synthesize picker = _picker;

- (ScaryBugsAppDelegate *)appDelegate {
    return (ScaryBugsAppDelegate *)[[UIApplication sharedApplication] delegate];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    
    // Report the RUM event
    [[[self appDelegate] radar] reportEvent:RadarEventsViewWillDisappear
                                   WithTags:RadarTagsDetailViewController | RadarTagsLevelDebug];
    
    // End the slice
    [[[self appDelegate] radar] reportSlice:RadarSlicesDetailView Start:NO];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    // Start a slice to gather all the events that take place during the user's visit to
    // this page
    [[[self appDelegate] radar] reportSlice:RadarSlicesDetailView Start:YES];
    
    // Report the RUM event
    [[[self appDelegate] radar] reportEvent:RadarEventsViewDidAppear
                                   WithTags:RadarTagsDetailViewController | RadarTagsLevelDebug];
}


#pragma mark - Managing the detail item

- (void)setDetailItem:(id)newDetailItem
{
    if (_detailItem != newDetailItem) {
        _detailItem = newDetailItem;
        
        // Update the view.
        [self configureView];
    }

    if (self.masterPopoverController != nil) {
        [self.masterPopoverController dismissPopoverAnimated:YES];
    }        
}

- (void)configureView
{
    self.rateView.notSelectedImage = [UIImage imageNamed:@"shockedface2_empty.png"];
    self.rateView.halfSelectedImage = [UIImage imageNamed:@"shockedface2_half.png"];
    self.rateView.fullSelectedImage = [UIImage imageNamed:@"shockedface2_full.png"];
    self.rateView.editable = YES;
    self.rateView.maxRating = 5;
    self.rateView.delegate = self;
    
    if (self.detailItem) {
        self.titleField.text = self.detailItem.data.title;
        self.rateView.rating = self.detailItem.data.rating;
        self.imageView.image = self.detailItem.fullImage;
    }
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    [self configureView];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Split view

- (void)splitViewController:(UISplitViewController *)splitController
     willHideViewController:(UIViewController *)viewController
          withBarButtonItem:(UIBarButtonItem *)barButtonItem
       forPopoverController:(UIPopoverController *)popoverController
{
    barButtonItem.title = NSLocalizedString(@"Master", @"Master");
    [self.navigationItem setLeftBarButtonItem:barButtonItem animated:YES];
    self.masterPopoverController = popoverController;
}

- (void)splitViewController:(UISplitViewController *)splitController
     willShowViewController:(UIViewController *)viewController
  invalidatingBarButtonItem:(UIBarButtonItem *)barButtonItem
{
    // Called when the view is shown again in the split view, invalidating the button and popover
    // controller.
    [self.navigationItem setLeftBarButtonItem:nil animated:YES];
    self.masterPopoverController = nil;
}

- (IBAction)addPictureTapped:(id)sender {
    if (self.picker == nil) {
        
        // 1) Show status
        [SVProgressHUD showWithStatus:@"Loading picker..."];
        
        // 2) Get a concurrent queue form the system
        dispatch_queue_t concurrentQueue =
        dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
        
        // 3) Load picker in background
        dispatch_async(concurrentQueue, ^{
            
            self.picker = [[UIImagePickerController alloc] init];
            self.picker.delegate = self;
            self.picker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
            self.picker.allowsEditing = NO;
            
            // 4) Present picker in main thread
            dispatch_async(dispatch_get_main_queue(), ^{
                [self.navigationController presentViewController:_picker
                                                        animated:YES
                                                      completion:^(void) {}];
                [SVProgressHUD dismiss];
            });
            
        });
        
    }  else {
        [self.navigationController presentViewController:_picker
                                                animated:YES
                                              completion:^(void) {}];
    }
}

- (IBAction)titleFieldTextChanged:(id)sender {
    self.detailItem.data.title = self.titleField.text;
}

- (NSUInteger)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskAll;
}

- (UIInterfaceOrientation)preferredInterfaceOrientationForPresentation {
    return UIInterfaceOrientationPortrait;
}

#pragma mark UITextFieldDelegate

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];
    return YES;
}

# pragma mark RateViewDelegate

- (void)rateView:(RateView *)rateView ratingDidChange:(float)rating {
    if (self.detailItem.data.rating != rating) {
        self.detailItem.data.rating = rating;
    
        // Report the RUM event
        NSUInteger reportId = [[[self appDelegate] radar]
                               reportEvent:RadarEventsRatingChanged
                                  WithTags:RadarTagsDetailViewController | RadarTagsLevelDebug];
        
        // Report metadata for the RUM event
        [[[self appDelegate] radar]
            reportProperty:RadarPropertiesRating
                     Value:[NSString stringWithFormat:@"%d", (int)rating]
                 ForReport:reportId];
    }
}

# pragma mark UIImagePickerControllerDelegate

- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker {
    [self dismissViewControllerAnimated:YES completion:^(void) {}];
}

- (void)imagePickerController:(UIImagePickerController *)picker
        didFinishPickingMediaWithInfo:(NSDictionary *)info {
    [self dismissViewControllerAnimated:YES completion:^(void){}];
    
    // Report the RUM event
    [[[self appDelegate] radar]
        reportEvent:RadarEventsNewImageSelected
           WithTags:RadarTagsDetailViewController | RadarTagsLevelDebug];
    
    UIImage *fullImage = (UIImage *) [info objectForKey:UIImagePickerControllerOriginalImage];
    
    // 1) Show status
    [SVProgressHUD showWithStatus:@"Resizing image..."];
    
    // 2) Get a concurrent queue form the system
    dispatch_queue_t concurrentQueue =
        dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
    
    // 3) Resize image in background
    dispatch_async(concurrentQueue, ^{
        
        UIImage *thumbImage = [fullImage imageByScalingAndCroppingForSize:CGSizeMake(44, 44)];
        
        // 4) Present image in main thread
        dispatch_async(dispatch_get_main_queue(), ^{
            self.detailItem.fullImage = fullImage;
            self.detailItem.thumbImage = thumbImage;
            self.imageView.image = fullImage;
            [SVProgressHUD dismiss];
            
            // Report the RUM event
            [[[self appDelegate] radar]
                reportEvent:RadarEventsNewImagePresented
                   WithTags:RadarTagsDetailViewController | RadarTagsLevelDebug];
        });
        
    });
}

@end
