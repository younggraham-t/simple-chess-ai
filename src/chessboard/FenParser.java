package chessboard;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;

/*
 * FEN Parser taken and adapted from FEN Parser by: njkevlani
 * at https://github.com/njkevlani/FEN-Parser
 * 
 * 
 * Graham Young: changed piece types to be part of an enum 11/11/22
 */

public class FenParser {
	
	public enum PieceType{
		EMPTY, BR, BN, BB, BQ, BK, BP, WR, WN, WB, WQ, WK, WP
	}
	
//    public static void main(String[] args) throws IOException {
//        InputStream inputStream = System.in;
//        OutputStream outputStream = System.out;
//        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
//        PrintWriter out = new PrintWriter(outputStream);
//        TaskG solver = new TaskG();       
//        String inStr = in.readLine();
//        PieceType board[][] = solver.FEN2Matrix(inStr);
//        for(int i=0;i<8;i++){
//            for(int j=0;j<8;j++)
//                out.print(board[i][j] + " ");
//            out.println();
//        }
//        out.close();
//    }
//    
    
    /*
     * Graham Young: added parseFEN 11/11/22
     */
    public static PieceType[][] parseFEN(String fen) {
    	TaskG solver = new TaskG();
    	return solver.FEN2Matrix(fen);
    }

    static class TaskG {        
        public PieceType[][] FEN2Matrix(String inStr) {
            //code logic
            //bw contains black or white
            //temp2 contains all the rank positions.
            String strg[] = inStr.split(" ");           
            String temp = strg[0];
            String tp = strg[1];
            char bw = tp.charAt(0);                        
            String temp2[] = temp.split("/");
            
            PieceType board[][] = new PieceType[8][8];            
            for(int i=0;i<8;i++){
                String str = temp2[i];
                int k = 0;
                for(int j=0;j<str.length();j++){                   
                    PieceType code = getCode(str.charAt(j));                                                                               
                    
                    if(code == PieceType.EMPTY){
                        int num = Integer.parseInt(""+str.charAt(j));
                        for(int ii=0;ii<num;ii++){
                            board[7-i][k] = PieceType.EMPTY;
                            k++;
                        }                          
                    }                       
                    else{                        
                        board[7-i][k] = code;
                        k++;
                    }
                }                
            }
//            for(PieceType[] l : board) {
//            	for( PieceType p : l) {
//            		System.out.println(p);
//            	}
//            }
            
            return board;                                                
        }       
    }
       
    static PieceType getCode(char t){
        PieceType ans= PieceType.EMPTY;
        switch(t){
            case 'r':
                ans = PieceType.BR;
                break;
            case 'n':
                ans = PieceType.BN;
                break;
            case 'q':
                ans = PieceType.BQ;
                break;
            case 'k':
                ans = PieceType.BK;
                break;
            case 'p':
                ans = PieceType.BP;
                break;
            case 'b':
                ans = PieceType.BB;
                break;
            case 'R':
                ans = PieceType.WR;
                break;
            case 'N':
                ans = PieceType.WN;
                break;
            case 'Q':
                ans = PieceType.WQ;
                break;
            case 'K':
                ans = PieceType.WK;
                break;
            case 'P':
                ans = PieceType.WP;
                break;
            case 'B':
                ans = PieceType.WB;
                break;
        }
        
        return ans;
    }    
}