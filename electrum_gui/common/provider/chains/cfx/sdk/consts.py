MAIN_NET_NETWORK_ID = 1029
TEST_NET_NETWORK_ID = 1

MAIN_NET_CHAIN_ID = 1029
TEST_NET_CHAIN_ID = 1


DEFAULT_GAS_PRICE = 1

EARLIEST = "earliest"
LATEST_CHECKPOINT = "latest_checkpoint"
LATEST_CONFIRMED = "latest_confirmed"
LATEST_STATE = "latest_state"
LATEST_MINED = "latest_mined"

DEFAULT_GAS_LIMIT = 21000

# the contract address mapping of sponsor control in different network
SPONSOR_WHITELIST_CONTROL = {
    str(MAIN_NET_CHAIN_ID): "cfx:aaejuaaaaaaaaaaaaaaaaaaaaaaaaaaaaegg2r16ar",
    str(TEST_NET_CHAIN_ID): "cfxtest:aaejuaaaaaaaaaaaaaaaaaaaaaaaaaaaaeprn7v0eh",
}
