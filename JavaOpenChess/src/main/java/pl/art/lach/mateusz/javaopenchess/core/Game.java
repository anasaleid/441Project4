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
package pl.art.lach.mateusz.javaopenchess.core;

import org.apache.log4j.Logger;
import pl.art.lach.mateusz.javaopenchess.core.ai.AI;
import pl.art.lach.mateusz.javaopenchess.core.ai.AIFactory;
import pl.art.lach.mateusz.javaopenchess.core.data_transfer.DataExporter;
import pl.art.lach.mateusz.javaopenchess.core.data_transfer.DataTransferFactory;
import pl.art.lach.mateusz.javaopenchess.core.data_transfer.TransferFormat;
import pl.art.lach.mateusz.javaopenchess.core.moves.Move;
import pl.art.lach.mateusz.javaopenchess.core.moves.MovesHistory;
import pl.art.lach.mateusz.javaopenchess.core.pieces.Piece;
import pl.art.lach.mateusz.javaopenchess.core.pieces.PieceFactory;
import pl.art.lach.mateusz.javaopenchess.core.pieces.implementation.King;
import pl.art.lach.mateusz.javaopenchess.core.players.Player;
import pl.art.lach.mateusz.javaopenchess.core.players.PlayerType;
import pl.art.lach.mateusz.javaopenchess.core.players.implementation.ComputerPlayer;
import pl.art.lach.mateusz.javaopenchess.core.players.implementation.HumanPlayer;
import pl.art.lach.mateusz.javaopenchess.utils.GameTypes;
import pl.art.lach.mateusz.javaopenchess.utils.Settings;
import pl.art.lach.mateusz.javaopenchess.utils.SettingsFactory;

import javax.swing.*;



/** 
 * Class responsible for the starts of new games, loading games,
 * saving it, and for ending it.
 * This class is also responsible for appoing player with have
 * a move at the moment
 * 
 * @author: Mateusz  Lach ( matlak, msl )
 * @author: Damian Marciniak
 */
public class Game
{
    
    private static final Logger LOG = Logger.getLogger(Game.class);
    
    private DataExporter fenExporter = DataTransferFactory.getExporterInstance(TransferFormat.FEN);
    
    private static final String FEN_PREFIX_NAME = "FEN: ";
    
    private static final int PADDING = 5;
    
    /**
     * Settings object of the current game
     */
    protected Settings settings;
 
    /**
     * if chessboard is blocked - true, false otherwise
     */
    private boolean blockedChessboard;
    
    /**
     * chessboard data object
     */    
    protected Chessboard chessboard;

    /**
     * Currently active player object
     */
    protected Player activePlayer;
    
    /**
     * Game clock object
     */
    protected GameClock gameClock;


    /**
     * History of moves object
     */       
    protected MovesHistory moves;

    
    protected JTabbedPane tabPane;
    
    protected JTextField fenState;

    
    private AI ai = null;
    
    private boolean isEndOfGame = false;

    public Game(Boolean firstMove)
    {
        init(firstMove);
    }

    protected final void init(boolean firstMove)
    {
        this.settings = SettingsFactory.getInstance();
        settings.setGameType(GameTypes.LOCAL);
        if(firstMove == true){
            Player user = new HumanPlayer("Player", Colors.WHITE);
            Player computer = new ComputerPlayer(" Computer", Colors.BLACK);
            settings.setPlayerWhite(computer);
            settings.setPlayerBlack(user);
        }
        else {
            Player user = new HumanPlayer("Player", Colors.BLACK);
            Player computer = new ComputerPlayer(" Computer", Colors.WHITE);
            settings.setPlayerWhite(user);
            settings.setPlayerBlack(computer);
        }

        this.moves = new MovesHistory(this);
        this.chessboard = new Chessboard(this.getSettings(), this.moves);
        this.ai = AIFactory.getAI(1);
        
        this.setBlockedChessboard(false);
    }




    /** 
     * Method to Start new game
     */
    public void newGame()
    {
        getChessboard().setPieces4NewGame(
            getSettings().getPlayerWhite(),
            getSettings().getPlayerBlack()
        );

        activePlayer = getSettings().getPlayerWhite();
        if (activePlayer.getPlayerType() != PlayerType.LOCAL_USER)
        {
            this.setBlockedChessboard(true);
        }
    }
    


    /**
     * Method to end game
     * @param message what to show player(s)
     *     at end of the game (for example "draw", "black wins" etc.)
     */
    public void endGame(String message)
    {
        this.setBlockedChessboard(true);
        this.isEndOfGame = true;
        LOG.debug(message);
    }

    /**
     * Method to swich active players after move
     */
    public void switchActive()
    {
        if (activePlayer == getSettings().getPlayerWhite())
        {
            activePlayer = getSettings().getPlayerBlack();
        }
        else
        {
            activePlayer = getSettings().getPlayerWhite();
        }
    }

    /**
     * Method of getting accualy active player
     *  @return  player The player which have a move
     */
    public Player getActivePlayer()
    {
        return this.activePlayer;
    }
    
    public void setActivePlayer(Player player)
    {
        this.activePlayer = player;
    }

    /**
     * Method to go to next move (checks if game is LOCAL/NETWORK etc.)
     */
    public void nextMove()
    {
        switchActive();
        LOG.debug(String.format(
            "next move, active player: %s, color: %s, type: %s",
            activePlayer.getName(),
            activePlayer.getColor().name(),
            activePlayer.getPlayerType().name()
        ));
        
        if (activePlayer.getPlayerType() == PlayerType.LOCAL_USER)
        {
            this.setBlockedChessboard(false);
        }
        else if (activePlayer.getPlayerType() == PlayerType.NETWORK_USER
                || activePlayer.getPlayerType() == PlayerType.COMPUTER)
        {
            this.setBlockedChessboard(true);
        }
    }

    /** 
     * Method to simulate Move to check
     *   if it's correct etc. (usable for NETWORK game).
     * @param beginX from which X (on chessboard) move starts
     * @param beginY from which Y (on chessboard) move starts
     * @param endX   to   which X (on chessboard) move go
     * @param endY   to   which Y (on chessboard) move go
     * @param promoted promoted string
     * @return boolean true if move OK, false otherwise.
     * */
    public boolean simulateMove(int beginX, int beginY, int endX,
            int endY, String promoted)
    {
        try 
        {
            Square begin = getChessboard().getSquare(beginX, beginY);
            Square end   = getChessboard().getSquare(endX, endY);
            getChessboard().select(begin);
            Piece activePiece = getChessboard().getActiveSquare().getPiece();
            if (activePiece.getAllMoves().contains(end)) //move
            {
                getChessboard().move(begin, end);
                if (null != promoted && !"".equals(promoted))
                {
                    Piece promotedPiece = PieceFactory.getPiece(
                            getChessboard(), 
                            end.getPiece().getPlayer().getColor(),
                            promoted,
                            activePlayer);
                    end.setPiece(promotedPiece);
                }
            }
            else
            {
                LOG.debug(String.format(
                    "Bad move: beginX: %s beginY: %s endX: %s endY: %s",
                    beginX, beginY, endX, endY
                ));
                return false;
            }
            getChessboard().unselect();
            nextMove();

            return true;
            
        } 
        catch (StringIndexOutOfBoundsException
                | ArrayIndexOutOfBoundsException
                | NullPointerException exc) 
        {
            LOG.error("simulateMove error: ", exc);
            return false;
        }    
    }
    
    public String printboard(){
        String board = "";
        for (Square[] s: getChessboard().getSquares()) {
            for(Square s1 : s){
                if(s1.getPiece() == null){
                    board = String.format("%s, ", board);
                }
                else {
                    board = String.format("%s%s, ", board, s1.getPiece().getSymbol());
                }
            }
            board = String.format("%s\n", board);
        }
        return board;
    }
    
    
    public Move move(int x, int y)
            throws ArrayIndexOutOfBoundsException
    {
        Move cpuMove = null;
        try
        {
            Square sq = getChessboard().getSquare(x, y);

            if (cannotInvokeMoveAction(sq))
            {
                return cpuMove;
            }
            if (isSelectAction(sq))
            {
                selectSquare(sq);
            }
            else if (getChessboard().getActiveSquare() == sq) //unselect
            {
                getChessboard().unselect();
            }
            else if (canInvokeMoveAction(sq)) //move
            {
                if (getSettings().getGameType() == GameTypes.LOCAL)
                {
                    getChessboard().move(getChessboard().getActiveSquare(), sq);
                }

                getChessboard().unselect();
                
                //switch player
                this.nextMove();
                
                //checkmate or stalemate
                King king;
                if (this.activePlayer == getSettings().getPlayerWhite())
                {
                    king = getChessboard().getKingWhite();
                }
                else
                {
                    king = getChessboard().getKingBlack();
                }
                
                switch (king.isCheckmatedOrStalemated())
                {
                    case 1:
                        this.endGame(String.format(
                            "Checkmate! %s player lose!",
                            king.getPlayer().getColor().toString()
                        ));
                        break;
                    case 2:
                        this.endGame("Stalemate! Draw!");
                        break;
                }
            }
            if (canDoComputerMove())
            {
                cpuMove = doComputerMove();
            }

        }
        catch(NullPointerException exc)
        {
            LOG.error("NullPointerException: " + exc.getMessage(), exc);
        }
        return cpuMove;
    }

    private boolean canInvokeMoveAction(Square sq)
    {
        Square activeSq = getChessboard().getActiveSquare();
        return activeSq != null
                && activeSq.piece != null
                && activeSq.getPiece().getAllMoves().contains(sq);
    }

    private void selectSquare(Square sq)
    {
        getChessboard().unselect();
        getChessboard().select(sq);
    }

    private boolean isSelectAction(Square sq)
    {
        return sq.piece != null
                && sq.getPiece().getPlayer() == this.activePlayer
                && sq != getChessboard().getActiveSquare();
    }

    private boolean cannotInvokeMoveAction(Square sq)
    {
        return (sq == null && sq.piece == null
                && getChessboard().getActiveSquare() == null)
                || (this.getChessboard().getActiveSquare() == null
                    && sq.piece != null
                    && sq.getPiece().getPlayer() != this.activePlayer
                );
    }



    private boolean canDoComputerMove() 
    {
        return !this.isEndOfGame 
                && this.getSettings().isGameAgainstComputer()
                && this.getActivePlayer().getPlayerType() == PlayerType.COMPUTER
                && null != this.getAi();
    }

    public Move doComputerMove()
    {
        Move move = this.getAi().getMove(this, this.getMoves().getLastMoveFromHistory());
        getChessboard().move(move.getFrom(), move.getTo());
        if (null != move.getPromotedPiece())
        {
            move.getTo().setPiece(move.getPromotedPiece());
        }
        this.nextMove();

        return move;
    }


    /**
     * @return the chessboard
     */
    public Chessboard getChessboard()
    {
        return chessboard;
    }

    /**
     * @return the settings
     */
    public final Settings getSettings() 
    {
        return settings;
    }

    /**
     * @return the blockedChessboard
     */
    public boolean isChessboardBlocked() 
    {
        return isBlockedChessboard();
    }



    /**
     * @return the moves
     */
    public MovesHistory getMoves() 
    {
        return moves;
    }


    /**
     * //TODO: refactor, why I've to change settings object in 2 places!?
     * @param settings the settings to set
     */
    public void setSettings(Settings settings)
    {
        this.settings = settings;
        this.chessboard.setSettings(settings);
    }


    private static long countChessHeight(int height)
    {
        return Math.round(
            (height * 0.80)/Chessboard.NUMBER_OF_SQUARES 
        ) * Chessboard.NUMBER_OF_SQUARES;
    }

    /**
     * @return the ai
     */
    public AI getAi() {
        return ai;
    }

    /**
     * @param ai the ai to set
     */
    public void setAi(AI ai) {
        this.ai = ai;
    }

    /**
     * @return the isEndOfGame
     */
    public boolean isIsEndOfGame() {
        return isEndOfGame;
    }

    /**
     * @param isEndOfGame the isEndOfGame to set
     */
    public void setIsEndOfGame(boolean isEndOfGame) {
        this.isEndOfGame = isEndOfGame;
    }

    /**
     * @return the blockedChessboard
     */
    public boolean isBlockedChessboard()
    {
        return blockedChessboard;
    }

    /**
     * @param blockedChessboard the blockedChessboard to set
     */
    public void setBlockedChessboard(boolean blockedChessboard)
    {
        this.blockedChessboard = blockedChessboard;
    }

    /**
     * @return the fenState
     */
    public JTextField getFenState()
    {
        return fenState;
    }

    /**
     * @param fenState the fenState to set
     */
    public void setFenState(JTextField fenState)
    {
        this.fenState = fenState;
    }
}