//
//  ScaryBugsDetailViewController.h
//  ScaryBugs
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "RateView.h"

@class ScaryBugDoc;

@interface ScaryBugsDetailViewController : UIViewController <UISplitViewControllerDelegate, UITextFieldDelegate, RateViewDelegate, UIImagePickerControllerDelegate, UINavigationControllerDelegate>

@property (strong, nonatomic) ScaryBugDoc* detailItem;
@property (strong, nonatomic) IBOutlet UILabel *detailDescriptionLabel;
@property (weak, nonatomic) IBOutlet UITextField *titleField;
@property (weak, nonatomic) IBOutlet UIImageView *imageView;
@property (weak, nonatomic) IBOutlet RateView *rateView;
@property (strong, nonatomic) UIImagePickerController *picker;

- (IBAction)addPictureTapped:(id)sender;
- (IBAction)titleFieldTextChanged:(id)sender;

@end
