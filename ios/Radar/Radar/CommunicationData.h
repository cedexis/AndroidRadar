//
//  CommunicationData.h
//  Radar
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol CommunicationData <NSObject>

- (NSString *)hostname;
- (NSArray *)pathParts;
- (NSDictionary *)dictionaryFrom:(NSData *)data;
- (NSString *)queryString;

@end