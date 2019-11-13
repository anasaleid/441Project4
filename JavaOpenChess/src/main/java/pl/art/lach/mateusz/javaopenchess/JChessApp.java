/*
#    This program is free software: you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation, either version 3 of the License, or
#    (at your option) any later version.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pl.art.lach.mateusz.javaopenchess;

import Spring.IChessEngine;
import org.json.simple.JSONObject;
import pl.art.lach.mateusz.javaopenchess.core.Game;
import pl.art.lach.mateusz.javaopenchess.core.Square;
import pl.art.lach.mateusz.javaopenchess.core.moves.Move;
import java.util.Scanner;

/**
 * The main class of the application.
 * @author Mateusz  Lach ( matlak, msl )
 * @author Damian Marciniak
 */
public class JChessApp implements IChessEngine {


    private static Game newGame;
     
    public final static String LOG_FILE = "log4j.properties"; 
    
    public final static String MAIN_PACKAGE_NAME = JChessApp.class.getPackage().getName();

     
    /**
     * At startup create and show the main frame of the application.
     */


    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */


    //The board actually takes two integers as input such as 01 instead of A1, so I created this function to
    //take the user's input (A1) and convert it to what the board actually wants (01)
    static String parseMove(String move){
        String[] moves = move.split(" ");
        String firstMove = moves[0];
        String firstCharacter = firstMove.substring(0, firstMove.length()/2);
        String secondCharacter = firstMove.substring(firstMove.length()/2);
        firstCharacter = firstCharacter.toUpperCase();
        int x;
        switch (firstCharacter){
            case "A":
                x = 0;
                break;
            case "B":
                x = 1;
                break;
            case "C":
                x = 2;
                break;
            case "D":
                x = 3;
                break;
            case "E":
                x = 4;
                break;
            case "F":
                x = 5;
                break;
            case "G":
                x = 6;
                break;
            case "H":
                x = 7;
                break;
            default:
                x = 0;
        }
        return x + secondCharacter;
    }

    //This just does the reverse of the method above
    static String reverseParseMove(Square move){
        switch (move.getPozX()){
            case 0:
                return "A" + move.getPozY();
            case 1:
                return "B" + move.getPozY();
            case 2:
                return "C" + move.getPozY();
            case 3:
                return "D" + move.getPozY();
            case 4:
                return "E" + move.getPozY();
            case 5:
                return "F" + move.getPozY();
            case 6:
                return "G" + move.getPozY();
            case 7:
                return "H" + move.getPozY();
            default:
                return "Error" + move.getPozY();
        }
    }

    //Generates a bunch of moves back to back
    //Sometimes this will cause an error due to a random number generator that was used in the original program
    public static void CPUvsCPU(){boolean firstMove = false;
        newGame = new Game(firstMove);
        newGame.newGame();
        int count = 0;
        while(!newGame.isIsEndOfGame()){
            count++;
            newGame.doComputerMove();
            System.out.println(newGame.printboard());

            if(count > 300){
                newGame.setIsEndOfGame(true);
                System.out.println("Over 300 moves were made, ending game in draw");
            }
        }
    }

    //Takes input from standard in in order to play as human player
    //firstMove determines if the CPU goes first
    public static void HumanVsCPU(boolean firstMove) {
        newGame = new Game(firstMove);
        newGame.newGame();

        if (firstMove) {
            Move cpuMove1 = newGame.doComputerMove();
            System.out.println(reverseParseMove(cpuMove1.getFrom()) + ", " + reverseParseMove(cpuMove1.getTo()));
        }

        while (!newGame.isIsEndOfGame()) {
            newGame.printboard();
            System.out.println(newGame.printboard());
            Scanner scan = new Scanner(System.in);
            System.out.println("Input your move: ");
            String move = scan.nextLine();
            String[] move2 = move.split(" ");
            String firstMoveStr = parseMove(move2[0]);
            int x1 = Integer.parseInt(firstMoveStr.substring(0, firstMoveStr.length() / 2));
            int y1 = Integer.parseInt(firstMoveStr.substring(firstMoveStr.length() / 2));
            newGame.move(x1, y1);

            String SecondMove = parseMove(move2[1]);
            int x2 = Integer.parseInt(SecondMove.substring(0, SecondMove.length() / 2));
            int y2 = Integer.parseInt(SecondMove.substring(SecondMove.length() / 2));
            Move cpuMove = newGame.move(x2, y2);
            if (cpuMove != null) {
                System.out.println("CPU MOVE: " + reverseParseMove(cpuMove.getFrom()) + ", " + reverseParseMove(cpuMove.getTo()));
            }
        }
    }
    //I don't need main in here anymore since I am not testing it in intellij anymore. If you'd like to test it in intellij,
    //uncomment this main method and run it, it just work just like before except it will take input from standard in instead
   /* public static void main(String[] args)
    {
        //UNCOMMENT THIS SECTION IF YOU'D LIKE FOR THE COMPUTERS TO PLAY AGAINST EACH OTHER
        CPUvsCPU();


        //UNCOMMENT THIS SECTION IF YOU'D LIKE TO PLAY AGAINST A COMPUTER AS WHITE PIECES
        //HumanVsCPU(false);


        //UNCOMMENT THIS SECTION IF YOU"D LIKE TO PLAY AGAINST A COMPUTER AS BLACK PIECES
        //HumanVsCPU(true);


    }*/

    @Override
    public JSONObject newGame(boolean firstMove, String start, String end) throws Exception {
        //Create new game with firstMove as input. firstMove changes who is white and who is black
        newGame = new Game(firstMove);
        newGame.newGame();
        JSONObject obj = new JSONObject();
        if(firstMove)
        {
            //If the computer is first, it will make it's move and return it to the controller
            newGame.setActivePlayer(newGame.getSettings().getPlayerWhite());
            Move cpuMove = newGame.doComputerMove();
            obj.put("start", reverseParseMove(cpuMove.getFrom()));
            obj.put("end", reverseParseMove(cpuMove.getTo()));
            obj.put("board", newGame.printboard());
        }
        else{
            //If the payer is first, the api call will have taken the start and end variables as input and used them
            //to make the first move
            //start is used to select the piece they want
            String firstMoveStr = parseMove(start);
            int x1 = Integer.parseInt(firstMoveStr.substring(0, firstMoveStr.length()/2));
            int y1 = Integer.parseInt(firstMoveStr.substring(firstMoveStr.length()/2));

            newGame.move(x1, y1);
            //end is then used to select the destination
            String SecondMove = parseMove(end);
            int x2 = Integer.parseInt(SecondMove.substring(0, SecondMove.length()/2));
            int y2 = Integer.parseInt(SecondMove.substring(SecondMove.length()/2));

            //Once they player makes their move, the computer will respond with it's own move. That move will be captured
            //here and returned to the controller to be output as a response
            Move cpuMove = newGame.move(x2, y2);
            System.out.println("CPU MOVE: " + reverseParseMove(cpuMove.getFrom()) + ", " + reverseParseMove(cpuMove.getTo()));

            obj.put("start", reverseParseMove(cpuMove.getFrom()));
            obj.put("end", reverseParseMove(cpuMove.getTo()));
            obj.put("board", newGame.printboard());
        }

        return obj;
    }

    //This does the same thing as the else statement in the newGame method.
    //The only other thing it does is that it checks if the game is over, if it is. it will say so.
    @Override
    public JSONObject move(String start, String end) throws Exception {
        JSONObject obj = new JSONObject();
        if(newGame.isIsEndOfGame()){
            obj.put("Quit", "Game already ended");
            return  obj;
        }
        System.out.println(start + ", " + end);
        String firstMove = parseMove(start);
        int x1 = Integer.parseInt(firstMove.substring(0, firstMove.length()/2));
        int y1 = Integer.parseInt(firstMove.substring(firstMove.length()/2));
        System.out.println(x1 + ", " + y1);
        newGame.move(x1, y1);

        String SecondMove = parseMove(end);
        int x2 = Integer.parseInt(SecondMove.substring(0, SecondMove.length()/2));
        int y2 = Integer.parseInt(SecondMove.substring(SecondMove.length()/2));
        System.out.println(x2 + ", " + y2);
        Move cpuMove = newGame.move(x2, y2);
        System.out.println("CPU MOVE: " + reverseParseMove(cpuMove.getFrom()) + ", " + reverseParseMove(cpuMove.getTo()));
        obj.put("start", reverseParseMove(cpuMove.getFrom()));
        obj.put("end", reverseParseMove(cpuMove.getTo()));
        obj.put("board", newGame.printboard());
        return obj;
    }

    //Quit just changes the game state and returns a JSONObject containting a string denoting the game is over
    @Override
    public JSONObject quit() throws Exception {
        newGame.endGame("Quitting Game");
        JSONObject obj = new JSONObject();
        obj.put("Quit", "Game has ended");
        return obj;
    }
}
