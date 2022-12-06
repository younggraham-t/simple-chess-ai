package piece;

import chessboard.ChessBoard;
import chessboard.Player;
import java.util.ArrayList;

/**
 * Created by wangyiyi on 2/12/15.
 */
public class Queen extends Piece {
    /**
     * Constructor: initialize Queen Object
     * @param board the board that we are currently using
     * @param player the player that holds the piece
     */
    public Queen(ChessBoard board, Player player){
        super('q', board, player);
        this.value = 9;
        if(player == Player.WHITE){  // White player
            this.piece_image_path = "assets/white_queen.png";
        }
        else{ // Black player
            this.piece_image_path = "assets/black_queen.png";
        }
    }
//
//    /**
//     * Get all possible move coordinates for this queen piece at current coordinate
//     *
//     *        @   @   @
//     *         @  @  @         P: this piece
//     *          @ @ @          @: Possible coordinates to move
//     *        @ @ P @ @
//     *          @ @ @
//     *         @  @  @
//     *        @   @   @
//     *
//     * @return ArrayList<Coordinate> Object that contains all possible move coordinates.
//     */
//    public ArrayList<Coordinate> getPossibleMoveCoordinate() {
//        int current_x_coord = this.x_coordinate;       // get current x coord of pawn
//        int current_y_coord = this.y_coordinate;       // get current y coord of pawn
//        ChessBoard board = this.getChessBoard();            // get chess board
//        ArrayList<Coordinate> coords = new ArrayList<Coordinate>();          // create return ArrayList
//
//        int i, j;
//        // go direction of left top
//        for(i = current_x_coord - 1, j = current_y_coord + 1; i >= 0 && j < board.getHeight(); i--, j++){
//            if(addToCoordinatesIfValid(coords, i, j)) // add to coords if valid; if this return true, then it meets other pieces.
//                break;
//        }
//        // go direction of right top
//        for(i = current_x_coord + 1, j = current_y_coord + 1; i < board.getWidth() && j < board.getHeight(); i++, j++){
//            if(addToCoordinatesIfValid(coords, i, j)) // add to coords if valid; if this return true, then it meets other pieces.
//                break;
//        }
//        // go direction of left bottom
//        for(i = current_x_coord - 1, j = current_y_coord - 1; i >= 0 && j >= 0; i--, j--){
//            if(addToCoordinatesIfValid(coords, i, j)) // add to coords if valid; if this return true, then it meets other pieces.
//                break;
//        }
//        // go direction of right bottom
//        for(i = current_x_coord + 1, j = current_y_coord - 1; i < board.getWidth() && j >= 0; i++, j--){
//            if(addToCoordinatesIfValid(coords, i, j)) // add to coords if valid; if this return true, then it meets other pieces.
//                break;
//        }
//        // check left
//        for(i = current_x_coord - 1; i >= 0; i--){
//            if(addToCoordinatesIfValid(coords, i, current_y_coord)) // add to coords if valid; if this return true, then it meets other pieces.
//                break;
//        }
//        // check right
//        for(i = current_x_coord + 1; i < board.getWidth(); i++){
//            if(addToCoordinatesIfValid(coords, i, current_y_coord)) // add to coords if valid; if this return true, then it meets other pieces.
//                break;
//        }
//        // check above
//        for(i = current_y_coord + 1 ; i < board.getHeight(); i++){
//            if(addToCoordinatesIfValid(coords, current_x_coord, i)) // add to coords if valid; if this return true, then it meets other pieces.
//                break;
//        }
//        // check below
//        for(i = current_y_coord - 1; i >= 0; i--){
//            if(addToCoordinatesIfValid(coords, current_x_coord, i)) // add to coords if valid; if this return true, then it meets other pieces.
//                break;
//        }
//        return  coords;
//    }

}
