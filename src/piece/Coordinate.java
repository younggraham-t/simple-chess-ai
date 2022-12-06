package piece;

import java.util.Comparator;
import java.util.Objects;

import chessboard.Move;

/**
 * Created by wangyiyi on 2/12/15.
 */

/**
 *
 * Coordinate Class
 * This class is used to store x, y coordinate for Piece
 *
 */
public class Coordinate implements Comparable<Coordinate>{
   

	@Override
	public String toString() {
		return "" + convertCoordinateToChessNotation(this);
	}

	// coordinate
    private int x;  // x coordinate
    private int y;  // y coordinate

    /**
     * Constructor: set default x = y = -1
     */
    public Coordinate(){
        this.x = -1;
        this.y = -1;
    }
    
    public Coordinate(String string) {
    	this.x = convertChessNotationToCoordinate(string).getX();
    	this.y = convertChessNotationToCoordinate(string).getY();
    }

    /**
     * Constructor: set x and y
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public Coordinate(int x, int y){
        this.x = x;
        this.y = y;
    }

    /**
     * Getter, return x
     * @return the x coordinate
     */
    public int getX(){
        return this.x;
    }

    /**
     * Setter, set x
     * @param x set value
     */
    public void setX(int x){
        this.x = x;
    }

    /**
     * Getter, return Y
     * @return the y coordinate
     */
    public int getY(){
        return this.y;
    }

    /**
     * Setter, set Y
     * @param y set value
     */
    public void setY(int y){
        this.y = y;
    }
    
    @Override
   	public int hashCode() {
   		return Objects.hash(x, y);
   	}
    /*
     * converts a coordinate to a string that follows chess naming conventions
     * such as e4 or c5 
     */
    /*
     * written by Graham Young
     */
    public static String convertCoordinateToChessNotation(Coordinate c) {
    	char col = (char) ('a'+c.getX());
    	int row = c.getY()+1;
    	return "" + col+row;
    }
    /*
     * written by Graham Young
     */
    public static Coordinate convertChessNotationToCoordinate(String s) {
    	char col = (char) (s.charAt(0)-'a');
    	int row = Integer.parseInt(s.substring(1)) - 1;
    	return new Coordinate(col, row);
    }
    

   	@Override
   	public boolean equals(Object obj) {
   		if (this == obj)
   			return true;
   		if (obj == null)
   			return false;
   		if (getClass() != obj.getClass())
   			return false;
   		Coordinate other = (Coordinate) obj;
   		return x == other.x && y == other.y;
   	}
   	

	@Override
	public int compareTo(Coordinate o) {
		if (this.getX() == o.getX()){ 
			return this.getY() - o.getY();
		}
		return this.getX() - o.getX();
	}
    
}
