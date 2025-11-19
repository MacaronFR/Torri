#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface OpenFile : NSObject

@property (class, nonatomic, readonly) OpenFile *shared;
@property (nonatomic, strong) NSString *text;
@property (nonatomic, copy) void (^onResult)(NSArray<NSURL *> *urls);
@property (nonatomic, copy) void (^onResultSave)();
@property (nonatomic, copy) void (^setOpenFileBrowser)(BOOL value);
@property (nonatomic, copy) void (^setOpenFileExport)(BOOL value);

- (instancetype)init;

@end

NS_ASSUME_NONNULL_END
