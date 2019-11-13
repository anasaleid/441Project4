package Spring;

import org.json.simple.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import pl.art.lach.mateusz.javaopenchess.JChessApp;

@Controller
public class chessController {
    JChessApp app;

    //firstMove is required since the game needs to know who is white and who is black when creating the game,
    //but start and end are not required since they are only needed if the computer is playing black pieces so it knows
    //the users first move after creating the game. The computer will apply the user's first move and respond with it's moves
    @RequestMapping(value = "/newgame", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public JSONObject receivedNewGame(@RequestParam(name = "firstMove", required = true) boolean firstMove, @RequestParam(name = "start", required = false) String start, @RequestParam(name = "end", required = false) String  end){
        app = new JChessApp();
        JSONObject jObject = null;

        try {
            jObject = app.newGame(firstMove, start, end);
        }
        catch (Exception e){

        }
        return jObject;
    }

    //This just takes the user's moves from the api call as input and returns the computer's move within the jsonobject
    @RequestMapping(value = "/move", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public JSONObject receiveMove(@RequestParam(name = "start", required = true) String start, @RequestParam(name = "end", required = true) String  end){
        JSONObject jObject = null;

        try {
            jObject = app.move(start, end);
        }
        catch (Exception e){

        }
        return  jObject;
    }

    //Quit just changes the state of the game so that you can't make any more moves
    @RequestMapping(value = "/quit", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public JSONObject receiveQuit(){
        JSONObject jObject = null;
        try {
            jObject = app.quit();
        }
        catch (Exception e){

        }
        return  jObject;
    }
}
