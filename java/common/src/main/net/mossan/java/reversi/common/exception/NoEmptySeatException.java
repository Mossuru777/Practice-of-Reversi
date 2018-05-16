package net.mossan.java.reversi.common.exception;

public class NoEmptySeatException extends Exception {
    public NoEmptySeatException() {
        super("There is no empty seat.");
    }
}
