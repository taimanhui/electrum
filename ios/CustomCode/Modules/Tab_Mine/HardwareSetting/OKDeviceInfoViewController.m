//
//  OKDeviceInfoViewController.m
//  OneKey
//
//  Created by liuzj on 08/01/2021.
//  Copyright © 2021 Onekey. All rights reserved.
//

#import "OKDeviceInfoViewController.h"
#import "OKDeviceModel.h"

@interface OKDeviceInfoViewController()
@property (weak, nonatomic) IBOutlet UILabel *modelNameLabel;
@property (weak, nonatomic) IBOutlet UIView *nameView;
@property (weak, nonatomic) IBOutlet UILabel *nameLabel;
@property (weak, nonatomic) IBOutlet UILabel *deviceIDLabel;
@property (weak, nonatomic) IBOutlet UILabel *bluetoothNameLabel;
@property (weak, nonatomic) IBOutlet UILabel *sysVersionLabel;
@property (weak, nonatomic) IBOutlet UILabel *bluetoothVersionLabel;
@property (weak, nonatomic) IBOutlet UILabel *deviceID;
@property (weak, nonatomic) IBOutlet UILabel *bluetoothName;
@property (weak, nonatomic) IBOutlet UILabel *sysVersion;
@property (weak, nonatomic) IBOutlet UILabel *bluetoothVersion;

@end

@implementation OKDeviceInfoViewController
//static const NSUInteger backgroundColor = 0xF5F6F7;

+ (instancetype)controllerWithStoryboard {
    return [[UIStoryboard storyboardWithName:@"HardwareSetting" bundle:nil] instantiateViewControllerWithIdentifier:@"OKDeviceInfoViewController"];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(changeName)];
    [self.nameView addGestureRecognizer:tap];
    self.title = MyLocalizedString(@"hardwareWallet.info", nil);
    [self setupUI];
}

- (void)setupUI {
    OKDeviceInfoModel *deviceInfo = self.deviceModel.deviceInfo;
    self.deviceIDLabel.text = MyLocalizedString(@"hardwareWallet.info.id", nil);
    self.bluetoothNameLabel.text = MyLocalizedString(@"hardwareWallet.info.buletoothName", nil);
    self.sysVersionLabel.text = MyLocalizedString(@"hardwareWallet.info.sysVersion", nil);
    self.bluetoothVersionLabel.text = MyLocalizedString(@"hardwareWallet.info.buletoothVersion", nil);

    self.nameLabel.text = deviceInfo.label;
    self.deviceID.text = deviceInfo.device_id;
    self.bluetoothName.text = deviceInfo.ble_name;
    self.sysVersion.text = deviceInfo.deviceSysVersion;
    self.bluetoothVersion.text = deviceInfo.ble_ver;

}

- (void)changeName {
    UIAlertController *alertVc = [UIAlertController alertControllerWithTitle:@"" message:nil preferredStyle:UIAlertControllerStyleAlert];
    [alertVc addTextFieldWithConfigurationHandler:^(UITextField * _Nonnull textField) {
        textField.placeholder = self.nameLabel.text;
    }];
    UIAlertAction *action1 = [UIAlertAction actionWithTitle:@"OK" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        self.nameLabel.text = [[alertVc textFields] objectAtIndex:0].text;
    }];
    UIAlertAction *action2 = [UIAlertAction actionWithTitle:@"Cancel" style:UIAlertActionStyleCancel handler:nil];

    [alertVc addAction:action2];
    [alertVc addAction:action1];
    [self presentViewController:alertVc animated:YES completion:nil];
}
@end
