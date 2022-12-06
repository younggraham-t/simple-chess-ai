package piece;

import java.awt.Point;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


import chessboard.ChessBoard;
import chessboard.Player;


public class PieceFactory {


	public Piece makePiece(String pieceName, ChessBoard chessBoard, Player player) {
		try {
			Class theClass = Class.forName("piece."+pieceName);
			
			// Find the right Constructor
			Class[] argumentTypes = {chessBoard.getClass(), player.getClass(), };
			Object[] arguments = {chessBoard, player};
			//System.out.println(argumentTypes[1]);
			Constructor theConstructor = theClass.getConstructor(argumentTypes);
			
			return (Piece) theConstructor.newInstance(arguments);
			
		} catch (ClassNotFoundException e) {
			System.err.println("The problem is with the class name.");
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			System.err.println("The problem is with the method name and parameter list");
			e.printStackTrace();
		} catch (SecurityException e) {
			System.err.println("You don't have permission to access that method.");
			e.printStackTrace();
		} catch (InstantiationException e) {
			System.err.println("The problem was in invoking the constructor");
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			System.err.println("You don't have permission to access the constructor.");
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			System.err.println("The problem is with the parameter in the method call.");
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			System.err.println("The method or constructor threw an exception.");
			e.printStackTrace();
		}
		System.out.println("I am unable to create the Piece.  Returning null.");
		return null;
	}

}
