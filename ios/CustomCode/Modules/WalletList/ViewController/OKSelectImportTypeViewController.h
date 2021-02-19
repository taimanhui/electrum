//
//  OKSelectImportTypeViewController.h
//  OneKey
//
//  Created by xiaoliang on 2020/10/16.
//  Copyright © 2020 OneKey. All rights reserved..
//

#import "BaseViewController.h"

NS_ASSUME_NONNULL_BEGIN

@interface OKSelectImportTypeViewController : BaseViewController
@property (nonatomic,copy)NSString *coinType;
+ (instancetype)selectImportTypeViewController;
@end

NS_ASSUME_NONNULL_END
