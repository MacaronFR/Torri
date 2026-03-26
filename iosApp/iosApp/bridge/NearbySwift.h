#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface MessageSwift : NSObject
@property (nonatomic, strong, readonly) NSString *id;
@property (nonatomic, strong, readonly) NSData *message;

@end

@interface NearbySwift : NSObject

@property (nonatomic, copy) void (^onDiscoverDevice)(NSString *endpointID, NSString *endpointName);
@property (nonatomic, copy) void (^onDeviceLost)(NSString *endpointID);
@property (nonatomic, copy) void (^onConnectionStateChange)(NSString *state, NSString *endpointID);
@property (nonatomic, copy) BOOL (^onReceiveData)(NSData *data, NSString *endpointID);

- (instancetype)init;
- (void)startAdvertisingWithStar:(BOOL)star;
- (void)stopAdvertising;
- (void)startDiscoveryWithStar:(BOOL)star;
- (void)stopDiscovery;
- (void)connectWithEndpointId:(NSString *)endpointId;
- (void)disconnectWithEndpointId:(NSString *)endpointId;
- (void)sendDataWithData:(NSData *)data endpointId:(NSString *)endpointId;
- (void)receiveDataWithCompletionHandler:(void (^)(MessageSwift * _Nullable data, NSError * _Nullable error))completion;

@end

NS_ASSUME_NONNULL_END
