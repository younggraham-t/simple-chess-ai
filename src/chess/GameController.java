package chess;
import chess_ai.ChessAI;
import chess_ai.ai_board.AIBoard;
import chess_ai.ai_board.MoveGenerator;
import chessboard.ChessBoard;
import chessboard.Move;
import chessboard.Player;
import piece.Coordinate;
import piece.King;
import piece.Piece;
import java.util.*;
import java.util.List;

import javax.swing.*;
import java.awt.*;

/**
 * Created by wangyiyi on 2/25/15.
 */

/**
 * Game Controller
 */
public class GameController {
    protected ChessBoard board;  // chess board
    protected GameView game_view; // game view
    protected Piece chosen_piece;  // the piece that is chosen by player
    protected boolean game_start; // game already starts?
    protected String player1_name; // player1 name
    protected String player2_name; // player2 name
    protected int player1_score; // player1 score
    protected int player2_score; // player2 score
    protected String message;    // game message
//    Stack<String> chessboard_history_log; // used to save move history
    protected ChessAI chessAI;
    protected int AIDepth;
    
    /**
     * Constructor: initialize game controller
     * @param board
     * @param game_view
     */
    public GameController(ChessBoard board, GameView game_view){
        this.board = board;      // bind chessboard to current game controller
        this.game_view = game_view; // bind game view to current game controller
        this.chosen_piece = null; // no piece is chosen by player yet
        this.game_start = false;  // game is not started yet, need to click start button.
        this.player1_name = "WHITE"; // player1 name
        this.player2_name = "BLACK"; // player2 name
        this.player1_score = 0; // player1 score
        this.player2_score = 0; // player2 score
        this.message = "Press Start button to start the game"; // game message
        //this.chessboard_history_log = null; // no piece move history yet
        this.chessAI = new ChessAI(false);
    }
    
    public void doOneStep() {

    }


    /**
     * Check whether player's king is in check
     * @param player check this player's king
     * @return true if king is in check.
     */
    public boolean playersKingIsInCheck(Player player){
        AIBoard aiBoard = new AIBoard();
		aiBoard.LoadPosition(board.toString());
        return new MoveGenerator().isInCheck(aiBoard);
    }



    /**
     * Game is over
     * @param status
     */
    public void gameIsOver(String status){
        if(this.game_start == false) // game already over
            return;
        Player current_player = this.getPlayerForThisTurn(); // get current player
        this.game_start = false; // game not started now.
        if (status.equals("checkmate")){ // checkmate
            this.message = "Checkmate! "  + (current_player == Player.WHITE ? this.player2_name : player1_name) + " Win!!"; // reset message
            // update player score
            if (current_player == Player.WHITE){
                this.player2_score++;
            }
            else{
                this.player1_score++;
            }
        }
        else{ // stalemate
            this.message = "Stalemate!"; // reset message
        }

        // redraw menu
        this.game_view.menu_view.drawMenu(this.player1_score, this.player2_score, this.message);
    }

 
    /**
     *
     * @return Player for this turn.
     */
    public Player getPlayerForThisTurn(){
        return board.getPlayerForThisTurn();
    }
    

    
    public void redrawCanvas(JPanel panel) {
    	 
        this.message = (this.playersKingIsInCheck(this.getPlayerForThisTurn()) ? "Check! " : "") +     // show king in check
                (this.getPlayerForThisTurn() == Player.WHITE ? this.player1_name : this.player2_name) + "'s turn"; // show which player's turn
        this.updateMessage(this.message);
        panel.repaint();
    }
    

    
    /**
     * Check user mouse click, and update gui.
     * @param g2d
     * @param clicked_x_coord
     * @param clicked_y_coord
     */
    
    /*
     * Graham Young added AI move after a player move
     * 11/11/22
     *  completely refactored to use makeAMove method 11/13/22
     */
    public Move checkUserClick(Graphics2D g2d, double clicked_x_coord, double clicked_y_coord){
        if(this.game_start == false){ // game is not started yet. so we don't need to check user mouse click.
            return null;
        }
        int x, y;
        Piece p;
        boolean moveHappened = false;
        Move move = null;
        /*
         * check mouse click.
         */
        if(clicked_x_coord >= 0 && clicked_y_coord >= 0) {     // valid click scope
            x = (int) (clicked_x_coord / 64);                  // convert to left-bottom chess board coordinate system
            y = 8 - 1 - (int) (clicked_y_coord / 64);
            p = this.board.getPieceAtCoordinate(x, y);
            //moveHappened = makeAMove(this.chosen_piece, x,y);
            /*
             *  Now we clicked a spot/piece
             *
             */
            if (p != null) { // player clicked a piece; show its possible moves
                if(p.getPlayer() == this.getPlayerForThisTurn()) { // player clicked his/her own piece
                    this.chosen_piece = p;       // save as chosen_piece
                    AIBoard aiBoard = new AIBoard();
            		aiBoard.LoadPosition(board.toString());
            		List<chess_ai.ai_board.Move> legalMovesFromGenerator = new MoveGenerator().generateMoves(aiBoard);
            		List<Move> legalMoves = new ArrayList<>();
            		for(chess_ai.ai_board.Move m : legalMovesFromGenerator) {
            			legalMoves.add(m.getChessBoardMove());
            		}
            		ArrayList<Coordinate> legalMovesForPiece = new ArrayList<>();
            		for (Move m : legalMoves) {
            			if(m.getPiece().equals(p.getLocation())) {
            				legalMovesForPiece.add(m.getMoveTo());
            			}
            		}
                    this.game_view.chessboard_view.drawPossibleMovesForPiece(g2d, legalMovesForPiece); // draw possible moves
                }
            }
            if (this.chosen_piece != null) {
            	move = new Move(this.chosen_piece.getLocation(), new Coordinate(x, y));
            	moveHappened = board.makeAMove(move);
            	
            }
        }
        if (moveHappened) 
        return move;
        else return null;
        
    }
    
    public Move AIMove(Graphics2D g2d) {
    	
    	boolean moveHappened = false;
    	//pass the fen string of the current board to the AI and get its move
    	System.out.println(AIDepth);
    	Move nextAIMove = chessAI.getNextMove(board.toString(), AIDepth);
    	
    	if(nextAIMove != null) {
            moveHappened = board.makeAMove(nextAIMove);
        }
        	
        
    	if(moveHappened) {


        }
    
    	return nextAIMove;
    }
    
    

    /**
     * Update game message
     * @param message
     */
    public void updateMessage(String message){
        // set message
        this.message = message;

        // redraw menu
        this.game_view.menu_view.drawMenu(this.player1_score, this.player2_score, this.message);
    }

    /**
     *
     * Start a new game
     * if game_mode equals 1 => fantasy mode; otherwise classic mode
     * @param game_mode
     */
    public void startNewGame(){
    	   // show game mode selection dialog
        Object[] options = {1, 2, 3, 4, 5};
        int chosen_option = JOptionPane.showOptionDialog(null,
                "Choose an AI Depth (note: higher depth means slower search)",
                "AI Depth",
                JOptionPane.INFORMATION_MESSAGE,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[2]);

        this.AIDepth = (int)chosen_option+1;
        
        ChessBoard new_board = new ChessBoard(8, 8); // create new board;
        this.game_view.chessboard_view.clicked_x_coord = -1;  // reset click x coord
        this.game_view.chessboard_view.clicked_y_coord = -1;  // reset click y coord

        // rebind the chessboard to GameView, GameConstroller
        this.board = new_board;
        this.game_view.board = new_board;
        this.game_view.chessboard_view.board = new_board;
        

        this.board.generateStandardBoard();
        this.game_view.redraw();
    
  

        this.game_start = true; // start game
        this.message = "Have fun in game!!\n" + (this.player1_name) + "'s turn";
        this.game_view.menu_view.drawMenu(this.player1_score, this.player2_score, this.message); // redraw menu for game
    }

    /**
     * Player clicked start button
     */
    /*
     * Graham Young: removed fantasy mode as option
     * 11/11/22
     */
    public void clickedStartButton(){
        if(this.game_start){ // game already started, so this func should do nothing
            JOptionPane.showMessageDialog(null, "Game already started");
            return;
        }
        
        
        this.startNewGame();

    }

    /**
     * Player clicked restart button
     */
    public void clickedRestartButton(){
        if(this.game_start == false){ // game not started yet, cannot restart.
            JOptionPane.showMessageDialog(null, "Game not started");
            return;
        }
        int entry = JOptionPane.showConfirmDialog(null, "Do you want to restart the game?", "Please select", JOptionPane.YES_NO_OPTION);
        if(entry == JOptionPane.NO_OPTION) { // player1 doesn't agree to restart the game
            return;
        }


            this.startNewGame();
        
    }

    /**
     * Player clicked forfeit button
     */
    public void clickedForfeitButton(){
        if(this.game_start == false){ // game not started yet, cannot forfeit.
            JOptionPane.showMessageDialog(null, "Game not started");
            return;
        }
        Player current_player = this.getPlayerForThisTurn(); // get player for the turn
        int entry = JOptionPane.showConfirmDialog(null, (current_player == Player.WHITE ? this.player1_name : this.player2_name) + "! Do you want to give up the game?", "Please select", JOptionPane.YES_NO_OPTION);
        if (entry == JOptionPane.YES_OPTION){ // player wants to forfeit
            this.message = (current_player == Player.WHITE ? this.player2_name : player1_name) + " Win!!"; // reset message
            this.game_start = false; // game not started now.

            // update player score
            if (current_player == Player.WHITE){
                this.player2_score++;
            }
            else{
                this.player1_score++;
            }

            // redraw menu
            this.game_view.menu_view.drawMenu(this.player1_score, this.player2_score, this.message);
        }
    }
    /*
     * written by Graham Young
     */
    public void unMakeMove() {
    	if(board.unMakeMove()) {
        	redrawCanvas(this.game_view);
    	}
    	else {
    		JOptionPane.showMessageDialog(null, "Cannot undo");
    	}

    }

    /**
     * Player clicked undo button
     */
    public void clickedUndoButton(){

    	unMakeMove();

        this.game_start = true; // start game
        this.message = (this.getPlayerForThisTurn() == Player.WHITE ? this.player1_name : this.player2_name) + "'s turn";
        this.game_view.menu_view.drawMenu(this.player1_score, this.player2_score, this.message); // redraw menu for game

        // redraw everything
        this.game_view.redraw();

    }

    /**
     * Update Player Name
     * @param btn
     */
    public void updatePlayerName(JButton btn){
        if(this.game_start == true){
            JOptionPane.showMessageDialog(null, "You cannot change your name during the game");
            return;
        }
        String s = (String)JOptionPane.showInputDialog(null, "Please new name for " + btn.getText());
        if (s != null && s.length() > 0){
            if(btn.getText().equals(this.player1_name)){ // update player1 name
                if(this.player2_name.equals(s)){ // invalid name, cuz player1 and player2 will have the same name
                    JOptionPane.showMessageDialog(null, "Invalid name: " + s);
                    return;
                }
                this.player1_name = s;
            }
            else{ // update player2 name
                if(this.player1_name.equals(s)){ // invalid name, cuz player1 and player2 will have the same name
                    JOptionPane.showMessageDialog(null, "Invalid name: " + s);
                    return;
                }
                this.player2_name = s;
            }
            btn.setText(s); // update player name
        }
    }
}
