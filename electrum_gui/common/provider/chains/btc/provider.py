from typing import Dict, Tuple

from electrum import bitcoin, constants
from electrum_gui.common.basic.functional.require import require
from electrum_gui.common.provider.data import AddressValidation, SignedTx, UnsignedTx
from electrum_gui.common.provider.interfaces import ProviderInterface
from electrum_gui.common.secret.interfaces import SignerInterface, VerifierInterface


class BTCProvider(ProviderInterface):
    def verify_address(self, address: str) -> AddressValidation:
        is_valid, encoding = False, None

        if bitcoin.is_segwit_address(address):
            is_valid, encoding = True, "P2WPKH"  # Pay To Witness Public Key Hash
        else:
            try:
                address_type, _ = bitcoin.b58_address_to_hash160(address)
            except Exception as e:
                print(f"Illegal address. error: {e}")
            else:
                if address_type == constants.net.ADDRTYPE_P2SH:
                    is_valid, encoding = True, "P2WPKH-P2SH"  # Pay To Script Hash
                elif address_type == constants.net.ADDRTYPE_P2PKH:
                    is_valid, encoding = True, "P2PKH"  # Pay To Public Key Hash

        return AddressValidation(
            normalized_address=address if is_valid else "",
            display_address=address if is_valid else "",
            is_valid=is_valid,
            encoding=encoding,
        )

    def pubkey_to_address(self, verifier: VerifierInterface, encoding: str = None) -> str:
        require(encoding in ("P2WPKH", "P2WPKH-P2SH", "P2PKH"))
        return bitcoin.pubkey_to_address(encoding.lower(), verifier.get_pubkey(compressed=True).hex())

    def fill_unsigned_tx(self, unsigned_tx: UnsignedTx) -> UnsignedTx:
        raise NotImplementedError()

    def sign_transaction(self, unsigned_tx: UnsignedTx, signers: Dict[str, SignerInterface]) -> SignedTx:
        raise NotImplementedError()

    def get_token_info_by_address(self, token_address: str) -> Tuple[str, str, int]:
        raise NotImplementedError()
