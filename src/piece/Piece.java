package piece;

import chessboard.ChessBoard;
import chessboard.Move;
import chessboard.Player;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangyiyi on 2/12/15.
 *
 * Piece Class
 */
public abstract class Piece {
    @Override
	public String toString() {
		return "Piece [piece_name=" + piece_name + ", x=" + x_coordinate + ", y=" + y_coordinate
				+ ", player=" + player + "]";
	}

	protected char piece_name; // name of the piece: king, rook, bishop, queen, knight, pawn.

    // Assume left bottom corner is (0, 0)
    protected int x_coordinate;  // piece x coordinate
    protected int y_coordinate;  // piece y coordinate

    protected Player player;     // white and black
    protected ChessBoard board;  // the current chessboard object

    protected String piece_image_path; // piece image path
    
    protected boolean hasMoved = false;
    protected int value;

    /**
     * Initialize a piece object
     * @param piece_name set the piece name
     * @param board      the chess board where we put this piece
     * @param player     the player id
     */
    public Piece(char piece_name, ChessBoard board, Player player){
        this.piece_name = piece_name;             // set the piece name
        this.x_coordinate = -1;                   // init coordinate to -1, which means the piece is not put to chess board yet
        this.y_coordinate = -1;
        this.board = board;                       // set the board
        this.player = player;                     // set player id
        this.board.addPieceToList(this);          // add piece to piece list
    }

    /**
     * Set the coordinate of piece on chess board
     * @param x         the x coordinate to put the piece
     * @param y         the y coordinate to put the piece
     * @return if can put the piece at that coordinate, return true; otherwise return true.
     */
    public boolean setCoordinate(int x, int y){
        if(x < 0 || x >= this.board.getWidth() || y < 0 || y >= this.board.getHeight() || this.board.getPieceAtCoordinate(x, y) != null){ // invalid coordinate
            return false;
        }
        if(this.x_coordinate != -1 && this.y_coordinate != -1) {  // piece is not just initialized.
            this.removeSelf(); // remove self from current coordinate
            //this.hasMoved = true;
        }
        // set coordinate
        this.x_coordinate = x;
        this.y_coordinate = y;
        // save this piece to board
        this.board.setPieceAtCoordinate(this, x, y);
        return true;
    }

    /**
     * Piece removes itself from chess board
     */
    public void removeSelf(){
        this.board.removePiece(this);
        
        this.x_coordinate = -1; // clear coordinate
        this.y_coordinate = -1;
        
    }
    
    public int getValue() {
    	return value;
    }

    /**
     * ChessBoarder getter
     * @return board
     */
    public ChessBoard getChessBoard(){
        return this.board;
    }

    /**
     * Setter, set player
     * @param player
     */
    public void setPlayer(Player player){
        this.player = player;
    }
    
    public void moved() {
    	this.hasMoved = true;
    }
    
    public boolean hasMoved() {
    	return hasMoved;
    }

    /**
     * Getter, return player
     * @return player
     */
    public Player getPlayer(){
        return this.player;
    }

    /**
     * Getter, return the x coordinate of this piece
     * @return x coordinate of this piece
     */
    public int getX_coordinate(){
        return this.x_coordinate;
    }

    /**
     * Getter, return the y coordinate of this piece
     * @return y coordinate of this piece
     */
    public int getY_coordinate(){
        return  this.y_coordinate;
    }
    
    public Coordinate getLocation() {
    	return new Coordinate(this.x_coordinate, this.y_coordinate);
    }

    /**
     * Getter, return the piece name of this piece
     * @return piece name of this piece
     */
    public char getPiece_name(){
        return this.piece_name;
    }

    /**
     * Setter, set the piece image path of this piece
     * @param piece_image_path
     */
    public void setPiece_image_path(String piece_image_path){
        this.piece_image_path = piece_image_path;
    }

    /**
     * Getter, return piece image path of this piece
     * @return image path of this piece
     */
    public String getPiece_image_path(){
        return this.piece_image_path;
    }
//
//    /**
//     * Given coordinate (x, y), check whether the piece can move there.
//     *
//     * If the piece can move to that coordinate, save (x, y) to coords.
//     *
//     * If the piece cannot move anywhere further after reach that coordinate, like there is an another piece there, which blocks the way => return true; otherwise return false
//     * @param coords  the coordinates array list
//     * @param x       the x coordinate to check
//     * @param y       the y coordinate to check
//     * @return true if the piece at that coordinate is opponent's piece or player's piece, thus it will block the way.
//     */
//    public boolean addToCoordinatesIfValid(ArrayList<Coordinate> coords, int x, int y){
//        if (x < 0 || y < 0 || x >= this.board.getWidth() || y >= this.board.getHeight()) // invalid coordinate
//            return false;
//        
//        if(this.board.getPieceAtCoordinate(x, y) == null){     // the square is not occupied by any piece
//            coords.add(new Coordinate(x, y));
//            return false;
//        }
//        else if(this.board.getPieceAtCoordinate(x, y).player != this.player) {  // meet opponent's piece
//            coords.add(new Coordinate(x, y));
//            return true;
//        }
//        else  // meet player's own piece
//            return true;
//    }
//
//    /**
//     *
//     * Check whether this is a suicide move.
//     *
//     * Suppose piece moves to (move_to_x, move_to_y) coordinate, then check if the king is in check.
//     *
//     * @param move_to_x:   the x coordinate we want to move our piece to
//     * @param move_to_y:   the y coordinate we want to move our piece to
//     * @return return true if this move will cause king being checked.
//     */
//    public boolean isSuicideMove(int move_to_x, int move_to_y){
//
//        Player current_player = this.player;
//        int current_x_coord = this.x_coordinate ;
//        int current_y_coord = this.y_coordinate;
//        boolean is_suicide = false;
//
//        Piece king = (current_player == Player.WHITE ? this.board.getKing1() : this.board.getKing2());  // get king
//
//        Piece remove_piece = this.board.getPieceAtCoordinate(move_to_x, move_to_y); // get piece that need to be removed
//
//        ArrayList<Piece> opponent_pieces = (current_player == Player.WHITE ? this.board.getBlack_pieces() : this.board.getWhite_pieces()); // get opponent's pieces
//
//        if(remove_piece != null) {
//            remove_piece.removeSelf();  // remove self temporarily
//        }
//        if(this.piece_name == 'p')  // if it is pawn, we don't want to change its first_time_move flag
//            ((Pawn)this).setCoordinateWithoutChangingFirstTimeMoveFlag(move_to_x, move_to_y);
//        else
//            this.setCoordinate(move_to_x, move_to_y); // move p to that coordinate;
//
//        for(Piece opponent_piece : opponent_pieces){
//            if(opponent_piece.getX_coordinate() == -1 || opponent_piece.getY_coordinate() == -1) // invalid coordinate
//                continue;
//            ArrayList<Coordinate> coords = opponent_piece.getPossibleMoveCoordinate();
//            for(Coordinate coord : coords){
//                if(coord.getX() == king.getX_coordinate() && coord.getY() == king.getY_coordinate()){ // will go to king's coord
//                    is_suicide = true;
//                    break;
//                }
//            }
//        }
//        // restore remove_piece and p
//        if(this.piece_name == 'p')
//            ((Pawn)this).setCoordinateWithoutChangingFirstTimeMoveFlag(current_x_coord, current_y_coord);
//        else
//            this.setCoordinate(current_x_coord, current_y_coord);
//        if(remove_piece != null) {
//            if (remove_piece.getPiece_name() == 'p')
//                ((Pawn)remove_piece).setCoordinateWithoutChangingFirstTimeMoveFlag(move_to_x, move_to_y);
//            else
//                remove_piece.setCoordinate(move_to_x, move_to_y);
//        }
//        // System.out.println("Is suicide: " + is_suicide);
//        return is_suicide;
//    }
//    
//    public ArrayList<Move> getLegalMoves() {
//    	if(this.getX_coordinate() == -1 || this.getY_coordinate() == -1) {
//    		return new ArrayList<>();
//    	}
//    	ArrayList<Coordinate> coords = getPossibleMoveCoordinate();
//    	ArrayList<Move> output = new ArrayList<>();
//    	for (Coordinate coord : coords) {
//            if (!this.isSuicideMove(coord.getX(), coord.getY())){ // it is a suicide move, therefore player cannot make this move.
//                output.add(new Move(this.getLocation(), coord));
//            }
//    	}
//    	return output;
//    }
//    
//    /**
//     * Get possible move coordinates for this piece
//     *
//     * As this function is implemented in each subclass, it will return null.
//     *
//     * @return ArrayList<Coordinate> Object that contains all possible move coordinates.
//     */
//    public abstract ArrayList<Coordinate> getPossibleMoveCoordinate();
}
