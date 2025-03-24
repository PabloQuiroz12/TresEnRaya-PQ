package edu.upb.tresenraya.logic;

import lombok.Getter;

@Getter
public class TicTacToe {

    public enum Mark {
        EMPTY, X, O
    }

    private static class Cell {

        boolean marked;
        Mark mark;

        public Cell() {
            this.marked = false;
            this.mark = Mark.EMPTY;
        }

        public Mark getMark() {
            return this.mark;
        }

    }

    private final Cell[][] board;

    public TicTacToe() {
        board = new Cell[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = new Cell();
            }
        }
    }

    public boolean markCell(int row, int col, Mark mark) {
        if (row < 0 || row >= 3 || col < 0 || col >= 3 || board[row][col].marked) {
            return false;
        }
        board[row][col].marked = true;
        board[row][col].mark = mark;
        return true;
    }

    public void printBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                System.out.print(board[i][j].mark == Mark.EMPTY ? "-" : board[i][j].mark);
                System.out.print(" ");
            }
            System.out.println();
        }
    }

    public Mark getCellMark(int row, int col) {
        if (row < 0 || row >= 3 || col < 0 || col >= 3) {
            throw new IllegalArgumentException("Invalid cell position");
        }
        return board[row][col].mark;
    }

    public Mark getWinner() {
        for (int i = 0; i < 3; i++) {
            if (board[i][0].mark == board[i][1].mark && board[i][1].mark == board[i][2].mark
                    && board[i][0].mark != Mark.EMPTY) {
                return board[i][0].mark;
            }

            if (board[0][i].mark == board[1][i].mark && board[1][i].mark == board[2][i].mark
                    && board[0][i].mark != Mark.EMPTY) {
                return board[0][i].mark;
            }
        }

        if (board[0][0].mark == board[1][1].mark && board[1][1].mark == board[2][2].mark
                && board[0][0].mark != Mark.EMPTY) {
            return board[0][0].mark;
        }

        if (board[0][2].mark == board[1][1].mark && board[1][1].mark == board[2][0].mark
                && board[0][2].mark != Mark.EMPTY) {
            return board[0][2].mark;
        }

        return Mark.EMPTY;
    }

    public boolean isBoardFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j].getMark() == Mark.EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void main(String[] args) {
        TicTacToe game = new TicTacToe();
        game.markCell(0, 0, Mark.X);
        game.markCell(1, 1, Mark.O);
        game.markCell(2, 2, Mark.X);
        game.printBoard();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                System.out.println(game.getCellMark(i, j));
            }
        }
    }
}
