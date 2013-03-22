//
//  RadarCommunication.h
//  Radar
//
//  Copyright (c) 2013 Cedexis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CommunicationData.h"

typedef void(^radar_comm_complete_block_t)(NSDictionary *);

@interface RadarCommunication : NSObject

- (NSString *)url;
- (NSDictionary *)toDictionary;

@property id<CommunicationData> data;
@property dispatch_queue_t completionQueue;
@property (nonatomic, copy) radar_comm_complete_block_t completion;
@end
