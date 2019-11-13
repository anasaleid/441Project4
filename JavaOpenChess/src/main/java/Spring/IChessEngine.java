package Spring;

import org.json.simple.JSONObject;

public interface IChessEngine {
    //I made my newgame method take optional parameters start and end.
    //I do this so that if the user who is initiating the game is playing whites, they can send their first move
    //along with the request to start a new game so that the computer has the player's move right away
    JSONObject newGame(boolean firstMove, String start, String end) throws Exception;
    //My move method just sends a start and end
    JSONObject move(String start, String end) throws Exception;
    //Quit doesn't need to take anything in since it just needs to change the game state
    JSONObject quit() throws Exception;
}
