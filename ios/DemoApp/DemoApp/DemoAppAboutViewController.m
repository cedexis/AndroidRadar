//
//  DemoAppAboutViewController.m
//  DemoApp
//
//  Created by Jacob Wan on 3/14/13.
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import "DemoAppAboutViewController.h"
#import "DemoAppAppDelegate.h"
#import "RadarVars.h"
#import "Radar.h"

@interface DemoAppAboutViewController ()

@end

@implementation DemoAppAboutViewController

@synthesize webView = _webView;

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

    // Load the Cedexis About page
    NSURL *url = [NSURL fileURLWithPath:[[NSBundle mainBundle] pathForResource:@"about.html" ofType:nil]];
    NSURLRequest *request = [NSURLRequest requestWithURL:url];
    [self.webView loadRequest:request];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (DemoAppAppDelegate *)appDelegate {
    return [[UIApplication sharedApplication] delegate];
}

- (void)webViewDidStartLoad:(UIWebView *)webView {
    DemoAppAppDelegate *del = [self appDelegate];
    
    // Start a RUM slice
    [del.radar reportSlice:RadarSliceAboutView Start:YES];
    
    // RUM event to indicate we're about to start loading the web view
    [del.radar reportEvent:RadarEventsShowAboutViewLoadStart];
}

- (void)webViewDidFinishLoad:(UIWebView *)webView {
    DemoAppAppDelegate *del = [self appDelegate];
    
    // RUM event to indicate that the web view finished loading
    [del.radar reportEvent:RadarEventsShowAboutViewLoadEnd];
    
    // Terminate the RUM slice
    [del.radar reportSlice:RadarSliceAboutView Start:NO];
}

@end
