package unit_test;

import chessboard.ChessBoard;
import chessboard.Player;
import org.junit.Test;
import piece.*;

import static org.junit.Assert.*;

public class PieceTest {

    /**
     * Test player id
     * player id could only be 1 or 2; otherwise throw exception
     * @throws Exception
     */
    @Test
    public void testGetPlayer() throws Exception {
        ChessBoard board = new ChessBoard(8, 8);  // create chess board

        Piece p1 = new King(board, Player.WHITE);            // check player id valid
        assertEquals(Player.WHITE, p1.getPlayer());

        Piece p2 = new King(board, Player.BLACK);            // check player id valid
        assertEquals(Player.BLACK, p2.getPlayer());
    }

    /**
     * Test whether piece name is correct
     * @throws Exception
     */
    @Test
    public void testGetPiece_name() throws Exception {
        ChessBoard board = new ChessBoard(8, 8);  // create chess board

        Piece p;

        p = new King(board, Player.WHITE);
        assertEquals("king", p.getPiece_name());

        p = new Bishop(board, Player.WHITE);
        assertEquals("bishop", p.getPiece_name());

        p = new Knight(board, Player.WHITE);
        assertEquals("knight", p.getPiece_name());

        p = new Pawn(board, Player.WHITE);
        assertEquals("pawn", p.getPiece_name());

        p = new Queen(board, Player.WHITE);
        assertEquals("queen", p.getPiece_name());

        p = new Rook(board, Player.WHITE);
        assertEquals("rook", p.getPiece_name());
    }
    /**
     * Test whether the piece image path is correct
     */
    @Test
    public void testGetPiece_image_path() throws Exception {
        ChessBoard board = new ChessBoard(8, 8);  // create chess board

        Piece p;

        // test player 1
        p = new King(board, Player.WHITE);
        assertEquals("assets/white_king.png", p.getPiece_image_path());

        p = new Bishop(board, Player.WHITE);
        assertEquals("assets/white_bishop.png", p.getPiece_image_path());

        p = new Knight(board, Player.WHITE);
        assertEquals("assets/white_knight.png", p.getPiece_image_path());

        p = new Pawn(board, Player.WHITE);
        assertEquals("assets/white_pawn.png", p.getPiece_image_path());

        p = new Queen(board, Player.WHITE);
        assertEquals( "assets/white_queen.png", p.getPiece_image_path());

        p = new Rook(board, Player.WHITE);
        assertEquals("assets/white_rook.png", p.getPiece_image_path());


        // test player 2
        p = new King(board, Player.BLACK);
        assertEquals("assets/black_king.png", p.getPiece_image_path());

        p = new Bishop(board, Player.BLACK);
        assertEquals("assets/black_bishop.png", p.getPiece_image_path());

        p = new Knight(board, Player.BLACK);
        assertEquals("assets/black_knight.png", p.getPiece_image_path());

        p = new Pawn(board, Player.BLACK);
        assertEquals("assets/black_pawn.png", p.getPiece_image_path());

        p = new Queen(board, Player.BLACK);
        assertEquals("assets/black_queen.png", p.getPiece_image_path());

        p = new Rook(board, Player.BLACK);
        assertEquals("assets/black_rook.png", p.getPiece_image_path());
    }

    /**
     * Test when call setCoordinate on a invalid coordinate, will false be returned ?
     * @throws Exception
     */
    @Test
    public void testSetCoordinate() throws Exception{
        ChessBoard board = new ChessBoard(8, 8);

        Piece p;
        boolean result;

        p = new King(board, Player.WHITE);

        /*      1 |   2          | 3
         *    -----------------------
         *        |              |
         *     4  | valid area   | 5
         *        |              |
         *    -----------------------
         *     6  |   7          | 8
         */

        // check case 1
        result = p.setCoordinate(-1, 9); // outside the boundary;
        assertEquals(false, result);

        // check case 2
        result = p.setCoordinate(1, 8); // outside the boundary;
        assertEquals(false, result);

        // check case 3
        result = p.setCoordinate(10, 10); // outside the boundary;
        assertEquals(false, result);

        // check case 4
        result = p.setCoordinate(-1, 0); // outside the boundary;
        assertEquals(false, result);

        // check case 5
        result = p.setCoordinate(9, 4); // outside the boundary;
        assertEquals(false, result);

        // check case 6
        result = p.setCoordinate(-1, -1); // outside the boundary;
        assertEquals(false, result);

        // check case 7
        result = p.setCoordinate(4, -1); // outside the boundary;
        assertEquals(false, result);

        // check case 8
        result = p.setCoordinate(9, -4); // outside the boundary;
        assertEquals(false, result);


        // check valid area
        result = p.setCoordinate(0, 0); // inside valid area
        assertEquals(true, result);

        result = p.setCoordinate(7, 7); // inside valid area
        assertEquals(true, result);

        result = p.setCoordinate(6, 3); // inside valid area
        assertEquals(true, result);

        result = p.setCoordinate(2, 2); // inside valid area
        assertEquals(true, result);


        // create new piece, test whether we can put it at p's location
        Piece p2 = new King(board, Player.BLACK);
        result = p2.setCoordinate(2, 2);  // this should return false
        assertEquals(false, result);

        // put p2 in a valid place
        result = p2.setCoordinate(2, 3);
        assertEquals(true, result);

        result = p2.setCoordinate(0, 0);
        assertEquals(true, result);
    }

    /**
     * test whether a piece can remove itself correctly
     * @throws Exception
     */
    @Test
    public void testRemoveSelf() throws Exception {
        ChessBoard board = new ChessBoard(8, 8);

        Piece p = new King(board, Player.WHITE);
        p.setCoordinate(0, 0);

        p.removeSelf();      // piece removes itself from the chess board
        assertEquals(-1, p.getX_coordinate());  // coordinate should be reset to -1
        assertEquals(-1, p.getY_coordinate());

        /* the piece should be found on board after we remove it */
        boolean find = false;
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                if (board.getPieceAtCoordinate(i, j) == p){
                    find = true;
                    break;
                }
            }
        }
        assertEquals(false, find); // piece should not be found from the chess board
    }


}