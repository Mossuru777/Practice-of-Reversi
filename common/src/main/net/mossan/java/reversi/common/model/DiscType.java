package net.mossan.java.reversi.common.model;

import org.jetbrains.annotations.Nullable;

import java.awt.*;

public enum DiscType {
    BLACK(0) {
        @Override
        public DiscType otherDiscType() {
            return DiscType.WHITE;
        }
    },
    WHITE(1) {
        @Override
        public DiscType otherDiscType() {
            return DiscType.BLACK;
        }
    };

    private final int index;

    DiscType(final int index) {
        this.index = index;
    }

    public static DiscType fromInt(@Nullable Integer i) {
        if (i == null || i < 0 || i > 1) {
            return null;
        }
        if (i == 0) {
            return DiscType.BLACK;
        } else {
            return DiscType.WHITE;
        }
    }

    public abstract DiscType otherDiscType();

    public int getInt() {
        return this.index;
    }

    public Color getColor() {
        return this == DiscType.BLACK ? Color.black : Color.white;
    }
}
