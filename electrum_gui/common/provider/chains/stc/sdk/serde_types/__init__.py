# Copyright (c) Facebook, Inc. and its affiliates
# SPDX-License-Identifier: MIT OR Apache-2.0

import ctypes
import typing
from dataclasses import dataclass


class SerializationError(ValueError):
    """Error raised during Serialization"""

    pass


class DeserializationError(ValueError):
    """Error raised during Deserialization"""

    pass


@dataclass(init=False)
class uint128:
    high: ctypes.c_uint64.value
    low: ctypes.c_uint64.value

    def __init__(self, num):
        self.high = ctypes.c_uint64(num >> 64).value
        self.low = ctypes.c_uint64(num & 0xFFFFFFFFFFFFFFFF).value

    def __int__(self):
        return (int(self.high) << 64) | int(self.low)


@dataclass(init=False)
class int128:
    high: ctypes.c_int64.value
    low: ctypes.c_uint64.value

    def __init__(self, num):
        self.high = ctypes.c_int64(num >> 64).value
        self.low = ctypes.c_uint64(num & 0xFFFFFFFFFFFFFFFF).value

    def __int__(self):
        return (int(self.high) << 64) | int(self.low)


@dataclass(init=False)
class uint64:
    value: ctypes.c_uint64.value

    def __init__(self, value):
        self.value = ctypes.c_uint64(value).value

    def __int__(self):
        return self.value


@dataclass(init=False)
class int64:
    value: ctypes.c_int64.value

    def __init__(self, value):
        self.value = ctypes.c_int64(value).value

    def __int__(self):
        return self.value


@dataclass(init=False)
class uint32:
    value: ctypes.c_uint32.value

    def __init__(self, value):
        self.value = ctypes.c_uint32(value).value

    def __int__(self):
        return self.value


@dataclass(init=False)
class int32:
    value: ctypes.c_int32.value

    def __init__(self, value):
        self.value = ctypes.c_int32(value).value

    def __int__(self):
        return self.value


@dataclass(init=False)
class uint16:
    value: ctypes.c_uint16.value

    def __init__(self, value):
        self.value = ctypes.c_uint16(value).value

    def __int__(self):
        return self.value


@dataclass(init=False)
class int16:
    value: ctypes.c_int16.value

    def __init__(self, value):
        self.value = ctypes.c_int16(value).value

    def __int__(self):
        return self.value


@dataclass(init=False)
class uint8:
    value: ctypes.c_uint8.value

    def __init__(self, value):
        self.value = ctypes.c_uint8(value).value

    def __int__(self):
        return self.value


@dataclass(init=False)
class int8:
    value: ctypes.c_int8.value

    def __init__(self, value):
        self.value = ctypes.c_int8(value).value

    def __int__(self):
        return self.value


@dataclass(init=False)
class char:
    value: str

    def __init__(self, s):
        if len(s) != 1:
            raise ValueError("`char` expects a single unicode character")
        self.value = s

    def __str__(self):
        return self.value


unit = typing.Type[None]

bool = bool
