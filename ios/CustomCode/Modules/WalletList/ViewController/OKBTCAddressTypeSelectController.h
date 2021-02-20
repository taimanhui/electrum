//
//  OKBTCAddressTypeSelectController.h
//  OneKey
//
//  Created by liuzhijie on 2021/2/18.
//  Copyright © 2021 Onekey. All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface OKBTCAddressTypeSelectController : UIViewController
@property (nonatomic, copy) void(^callback)(OKBTCAddressType type);
+ (instancetype)viewControllerWithStoryboard;
@end

NS_ASSUME_NONNULL_END
