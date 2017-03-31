package net.mossan.java.reversi.model;

public enum DiscType {
    BLACK {
        @Override
        public DiscType otherDiscType() {
            return DiscType.WHITE;
        }
    },
    WHITE {
        @Override
        public DiscType otherDiscType() {
            return DiscType.BLACK;
        }
    };

    public abstract DiscType otherDiscType();
}
