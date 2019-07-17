package net.mossan.java.reversi.common.model;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum PlayerType {
    NetworkPlayer(0b1),
    LongestPlaceCPU(0b10),
    RandomPlaceCPU(0b100);

    private final int bit;

    PlayerType(final int bit) {
        this.bit = bit;
    }

    public static PlayerType fromBit(@Nullable Integer bit) {
        if (bit == null) {
            return null;
        } else if (bit == NetworkPlayer.bit) {
            return PlayerType.NetworkPlayer;
        } else if (bit == LongestPlaceCPU.bit) {
            return PlayerType.LongestPlaceCPU;
        } else if (bit == RandomPlaceCPU.bit) {
            return PlayerType.RandomPlaceCPU;
        } else {
            throw new IllegalArgumentException("Unknown bit included: 0b" + Integer.toBinaryString(bit));
        }
    }

    public static PlayerType[] fromBits(int bits) {
        List<PlayerType> types = new ArrayList<>();

        if ((bits & NetworkPlayer.bit) == 1) types.add(NetworkPlayer);
        if ((bits & LongestPlaceCPU.bit) == LongestPlaceCPU.bit) types.add(LongestPlaceCPU);
        if ((bits & RandomPlaceCPU.bit) == RandomPlaceCPU.bit) types.add(RandomPlaceCPU);

        return types.toArray(new PlayerType[0]);
    }

    public static int getBits(PlayerType[] types) {
        return Arrays.stream(types).mapToInt(PlayerType::getBit).sum();
    }

    public int getBit() {
        return this.bit;
    }
}
