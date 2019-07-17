package net.mossan.java.reversi.common.message;

public enum EventType {
    // Response
    ServerState,
    GameState,

    // Request
    requestServerState,
    requestGameState,
    createRoom,
    seating,
    unSeating,
    selectCell
}
