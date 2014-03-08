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
    // Start a RUM slice
    [[Radar instance] reportSlice:RadarSliceAboutView Start:YES];
    
    // RUM event to indicate we're about to start loading the web view
    [[Radar instance] reportEvent:RadarEventsShowAboutViewLoadStart];
}

- (void)webViewDidFinishLoad:(UIWebView *)webView {
    // RUM event to indicate that the web view finished loading
    [[Radar instance] reportEvent:RadarEventsShowAboutViewLoadEnd];
    
    // Terminate the RUM slice
    [[Radar instance] reportSlice:RadarSliceAboutView Start:NO];
}

- (BOOL)webView:(UIWebView *)inWeb
shouldStartLoadWithRequest:(NSURLRequest *)inRequest
 navigationType:(UIWebViewNavigationType)inType {
    if (inType == UIWebViewNavigationTypeLinkClicked) {
        [[UIApplication sharedApplication] openURL:[inRequest URL]];
        return NO;
    }
    return YES;
}

@end
