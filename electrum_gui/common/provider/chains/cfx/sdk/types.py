from typing import NewType

from web3._utils.compat import TypedDict

Drip = NewType('Drip', int)


class SponsorInfo(TypedDict):
    sponsorBalanceForCollateral: int
    sponsorBalanceForGas: int
    sponsorGasBound: int
    sponsorForCollateral: str
    sponsorForGas: str
