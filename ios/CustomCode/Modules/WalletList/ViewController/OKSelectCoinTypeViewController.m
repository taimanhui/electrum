//
//  OKSelectCoinTypeViewController.m
//  OneKey
//
//  Created by xiaoliang on 2020/10/15.
//  Copyright © 2020 OneKey. All rights reserved..
//

#import "OKSelectCoinTypeViewController.h"
#import "OKSelectCoinTypeTableViewCell.h"
#import "OKSelectCoinTypeTableViewCellModel.h"
#import "OKSetWalletNameViewController.h"
#import "OKSelectImportTypeViewController.h"
#import "OKBTCAddressTypeSelectController.h"

@interface OKSelectCoinTypeViewController ()<UITableViewDelegate,UITableViewDataSource>

@property (weak, nonatomic) IBOutlet UILabel *titleLabel;

@property (weak, nonatomic) IBOutlet UITableView *tableView;

@property (nonatomic,strong)NSArray *coinTypeListArray;

@end

@implementation OKSelectCoinTypeViewController

+ (instancetype)selectCoinTypeViewController
{
    return  [[UIStoryboard storyboardWithName:@"WalletList" bundle:nil]instantiateViewControllerWithIdentifier:@"OKSelectCoinTypeViewController"];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self stupUI];
    self.tableView.tableFooterView = [UIView new];
}

- (void)stupUI
{
    self.titleLabel.text = MyLocalizedString(@"Select the currency", nil);
    if (_addType == OKAddTypeCreateHDDerived) {
        self.title = MyLocalizedString(@"Create a wallet", nil);
    }else if (_addType == OKAddTypeImport){
        self.title = MyLocalizedString(@"Import a single currency wallet", nil);
    }
}
#pragma mark - UITableViewDelegate | UITableViewDataSource
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return self.coinTypeListArray.count;
}
- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 74;
}
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *ID = @"OKSelectCoinTypeTableViewCell";
    OKSelectCoinTypeTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:ID];
    if (cell == nil) {
        cell = [[OKSelectCoinTypeTableViewCell alloc]initWithStyle:UITableViewCellStyleDefault reuseIdentifier:ID];
    }
    NSString *coinType = self.coinTypeListArray[indexPath.row];
    OKSelectCoinTypeTableViewCellModel *model = [OKSelectCoinTypeTableViewCellModel new];
    model.titleString = coinType;
    model.iconName = [NSString stringWithFormat:@"token_%@",[coinType lowercaseString]];
    cell.model = model;
    return cell;
}
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    OKWeakSelf(self)
    NSString *coinType = self.coinTypeListArray[indexPath.row];
    if (_addType == OKAddTypeCreateHDDerived || _addType == OKAddTypeCreateSolo || _addType == OKAddTypeCreateHWDerived) {
        [self getCoin:coinType AddressType:^(OKBTCAddressType btcAddressType) {
            OKSetWalletNameViewController *setWalletNameVc = [OKSetWalletNameViewController setWalletNameViewController];
            setWalletNameVc.addType = weakself.addType;
            setWalletNameVc.coinType = coinType;
            setWalletNameVc.where = weakself.where;
            setWalletNameVc.btcAddressType = btcAddressType;
            [weakself.navigationController pushViewController:setWalletNameVc animated:YES];
        }];
    }else if (_addType == OKAddTypeImport){
        OKSelectImportTypeViewController *selectImportTypeVc = [OKSelectImportTypeViewController selectImportTypeViewController];
        selectImportTypeVc.coinType = coinType;
        [self.navigationController pushViewController:selectImportTypeVc animated:YES];
    }
}
-(NSArray *)coinTypeListArray
{
    return kWalletManager.supportCoinArray;
}

- (void)getCoin:(NSString *)coinType AddressType:(void(^)(OKBTCAddressType btcAddressType))callback {
    if (!callback) { return; }

    coinType = coinType.lowercaseString;
    if (![coinType isEqualToString:@"btc"]) {
        callback(OKBTCAddressTypeNotBTC);
        return;
    }

    OKWeakSelf(self)
    OKBTCAddressTypeSelectController *vc = [OKBTCAddressTypeSelectController viewControllerWithStoryboard];
    vc.callback = ^(OKBTCAddressType type) {
        [weakself.navigationController dismissViewControllerAnimated:YES completion:nil];
        callback(type);
    };
    [self.navigationController presentViewController:vc animated:YES completion:nil];
}
@end
