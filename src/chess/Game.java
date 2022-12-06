package chess;

import chessboard.ChessBoard;
import unit_test.PerftTest;

import javax.swing.*;

/**
 * Main Game
 */
public class Game{
    static JFrame frame;
    private ChessBoard board;    // chess board
    private GameController game_controller; // game controller
    private GameView game_view;  // game view
    /**
     * Constructor: init game, set necessary properties.
     * @param board
     * @param game_controller
     * @param game_view
     */
    public Game(ChessBoard board, GameController game_controller, GameView game_view){
        this.board = board;
        this.game_controller = game_controller;
        this.game_view = game_view;

        this.game_view.bindGameController(game_controller); // bind game controller to game view
    }

    public void startGame(){
        this.game_view.initWindow(); // init window and begin to draw GUI
    }

    /**
     * Main function
     * @param args
     */
    public static void main(String [] args){
        // initialize Chessboard(model), game view, and game controller
        ChessBoard board;
        GameView game_view;
        GameController game_controller;
        board = new ChessBoard(ChessBoard.STARTING_FEN); // create standard 8 x 8 chess board.
        game_view = new GameView(board, 64); // initialize game view
        game_controller = new GameController(board, game_view); // initialize game constroller

        // init game
        Game game = new Game(board, game_controller, game_view);

        // start game
        game.startGame();
    }
}