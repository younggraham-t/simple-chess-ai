package unit_test;

import org.junit.Test;

import static org.junit.Assert.*;
import chessboard.ChessBoard;
import chessboard.Player;
import piece.*;

public class ChessBoardTest {

//    /**
//     * Test the judgement of suicide move
//     * @throws Exception
//     */
//    @Test
//    public void testIsSuicideMove() throws Exception {
//        /*
//         *
//         *
//         *     rook2
//         *
//         *
//         *     rook1
//         *     king1
//         *
//         *     case 1: if I move rook1 to left side, then king1 is in check; this is a suicide move
//         *     case 2: if I move rook1 forward, then king1 is not in check; this is not a suicide move
//         */
//        ChessBoard board = new ChessBoard(8, 8);
//        King king1 = new King(board, Player.WHITE);
//        Rook rook1 = new Rook(board, Player.WHITE);
//        Rook rook2 = new Rook(board, Player.BLACK);
//
//        king1.setCoordinate(4, 1);
//        rook1.setCoordinate(4, 2);
//        rook2.setCoordinate(4, 5);
//
//        assertEquals(true, rook1.isSuicideMove(3, 2));  // case 1
//        assertEquals(false, rook1.isSuicideMove(4, 3)); // case 2
//    }

    /*
     * test the judgement of player cannot move statement
     */
    @Test
    public void testPlayerCannotMove() throws Exception {
        ChessBoard board = new ChessBoard(8, 8);

        /* create king */
        King king1 = new King(board, Player.WHITE);
        king1.setCoordinate(0, 0);
        assertEquals(false, board.playerCannotMove(Player.WHITE)); // right now player can move

        /*

                       player2_queen1

         player2_rook1           player1_rook1


         king1         player2_rook2

         in this case player 1 cannot move
         */
        Rook player2_rook1 = new Rook(board, Player.BLACK);
        Rook player2_rook2 = new Rook(board, Player.BLACK);
        Queen player2_queen1 = new Queen(board, Player.BLACK);
        Rook player1_rook1 = new Rook(board, Player.WHITE);

        player2_rook1.setCoordinate(0, 5);
        player2_rook2.setCoordinate(4, 0);
        player2_queen1.setCoordinate(2, 2);
        player1_rook1.setCoordinate(4, 1);
        assertEquals(true, board.playerCannotMove(Player.WHITE));

    }

    /**
     * test the judgement of stalemate
     * @throws Exception
     */
    @Test
    public void testIsStalemate() throws Exception {
        ChessBoard board = new ChessBoard(8, 8);

        /*
         * the case from wikipedia http://en.wikipedia.org/wiki/Stalemate
         */
        King king1 = new King(board, Player.WHITE);
        King king2 = new King(board, Player.BLACK);
        Queen queen2 = new Queen(board, Player.BLACK);

        king1.setCoordinate(7, 7);
        king2.setCoordinate(5, 6);
        queen2.setCoordinate(6, 5);
        assertEquals(true, board.isStalemate(Player.WHITE));

        /*
         * the case that is not stalemate
         */
        king1.setCoordinate(0, 0);
        assertEquals(false, board.isStalemate(Player.WHITE));


        /*
         * test 4 cases in wikipedia
         */
        king1.removeSelf();
        king2.removeSelf();
        queen2.removeSelf();

        // Diagram1
        Pawn pawn1 = new Pawn(board, Player.WHITE);
        king2.setCoordinate(5, 7);
        pawn1.setCoordinate(5, 6);
        king1.setCoordinate(5, 5);
        board.setTurns(1);
        assertEquals(true, board.isStalemate(Player.BLACK));
        pawn1.removeSelf();
        king1.removeSelf();
        king2.removeSelf();

        // Diagram2
        Bishop bishop2 = new Bishop(board, Player.BLACK);
        Rook rook1 = new Rook(board, Player.WHITE);
        king2.setCoordinate(0, 7);
        bishop2.setCoordinate(1, 7);
        king1.setCoordinate(1, 5);
        rook1.setCoordinate(7, 7);
        assertEquals(true, board.isStalemate(Player.BLACK));
        king1.removeSelf();
        king2.removeSelf();
        bishop2.removeSelf();
        rook1.removeSelf();

        // Diagram3
        king2.setCoordinate(0, 0);
        king1.setCoordinate(2, 2);
        rook1.setCoordinate(1, 1);
        assertEquals(true, board.isStalemate(Player.BLACK));
        king1.removeSelf();
        king2.removeSelf();
        rook1.removeSelf();

        // Diagram4
        king2.setCoordinate(0, 0);
        Queen queen1 = new Queen(board, Player.WHITE);
        queen1.setCoordinate(1, 2);
        king1.setCoordinate(6, 4);
        assertEquals(true, board.isStalemate(Player.BLACK));

    }
}