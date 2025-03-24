package edu.upb.tresenraya.logic;

import edu.upb.tresenraya.logic.TicTacToe;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameState {
    private TicTacToe game;
    private String currentPlayer;
    private String opponent;
    private boolean isMyTurn; // Se añadió este campo para indicar si es el turno del jugador

    public GameState(TicTacToe game, String currentPlayer, String opponent, boolean isMyTurn) {
        this.game = game;
        this.currentPlayer = currentPlayer;
        this.opponent = opponent;
        this.isMyTurn = isMyTurn;
    }

    public boolean isMyTurn() {
        return isMyTurn;
    }

    public void setMyTurn(boolean isMyTurn) {
        this.isMyTurn = isMyTurn;
    }
}

