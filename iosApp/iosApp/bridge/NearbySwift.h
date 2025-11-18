#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface NearbySwift : NSObject

@property (nonatomic, copy) void (^onDiscoverDevice)(NSString *endpointID, NSString *endpointName);
@property (nonatomic, copy) void (^onDeviceLost)(NSString *endpointID);
@property (nonatomic, copy) void (^onConnectionStateChange)(NSString *state, NSString *endpointID);

- (instancetype)init;
- (void)startAdvertising;
- (void)stopAdvertising;
- (void)startDiscovery;
- (void)stopDiscovery;
- (void)connectWithEndpointId:(NSString *)endpointId;
- (void)disconnectWithEndpointId:(NSString *)endpointId;
- (void)sendDataWithData:(NSData *)data endpointId:(NSString *)endpointId;
- (void)receiveDataWithCompletionHandler:(void (^)(NSData * _Nullable data, NSError * _Nullable error))completion;

@end

NS_ASSUME_NONNULL_END
