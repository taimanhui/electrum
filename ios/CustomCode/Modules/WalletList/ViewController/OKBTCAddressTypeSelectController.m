//
//  OKBTCAddressTypeSelectController.m
//  OneKey
//
//  Created by liuzhijie on 2021/2/18.
//  Copyright © 2021 Onekey. All rights reserved.
//

#import "OKBTCAddressTypeSelectController.h"

@interface OKBTCAddressTypeSelectController ()
@property (weak, nonatomic) IBOutlet UIView *segwitView;
@property (weak, nonatomic) IBOutlet UIView *nativeSegwitView;
@property (weak, nonatomic) IBOutlet UIView *normalView;
@property (weak, nonatomic) IBOutlet UIView *badge;
@property (weak, nonatomic) IBOutlet UIView *bg;
@end

@implementation OKBTCAddressTypeSelectController

+ (instancetype)viewControllerWithStoryboard {
    return [[UIStoryboard storyboardWithName:@"WalletList" bundle:nil]instantiateViewControllerWithIdentifier:@"OKBTCAddressTypeSelectController"];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.badge.layerRadius = self.badge.height * 0.5;
    [self.bg setLayerRadius:20];
    [self.segwitView addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(segwitTap)]];
    [self.nativeSegwitView addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(nativeSegwitTap)]];
    [self.normalView addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(normalTap)]];
}

- (void)segwitTap {
    [self triggerCallback:OKBTCAddressTypeSegwit];
}

- (void)nativeSegwitTap {
    [self triggerCallback:OKBTCAddressTypeNativeSegwit];
}

- (void)normalTap {
    [self triggerCallback:OKBTCAddressTypeNormal];
}

- (void)triggerCallback:(OKBTCAddressType)type {
    if (self.callback) {
        self.callback(type);
    }
}

@end
