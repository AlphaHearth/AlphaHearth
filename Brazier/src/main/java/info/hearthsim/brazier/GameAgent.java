package info.hearthsim.brazier;

import info.hearthsim.brazier.actions.GameAction;
import info.hearthsim.brazier.actions.PlayTargetRequest;
import info.hearthsim.brazier.actions.undo.UndoAction;
import info.hearthsim.brazier.cards.Card;

import java.util.Optional;

import info.hearthsim.brazier.actions.undo.UndoableResult;
import org.jtrim.utils.ExceptionHelper;

/**
 * {@code GameAgent} acts as an agent of a given {@link Game},
 * providing limited methods for client to use.
 */
public class GameAgent {
    private Game game;

    /**
     * Creates a {@code GameAgent} with the given {@code Game}, and
     * set {@code Player1} as the starting player.
     *
     * @param game the given {@code Game}
     */
    public GameAgent(Game game) {
        this(game, game.getPlayer1().getPlayerId());
    }

    /**
     * Creates a {@code GameAgent} with the given {@code Game} and
     * specific starting player.
     *
     * @param game the given {@code Game}
     * @param startingPlayer the {@link PlayerId} of the starting player
     */
    public GameAgent(Game game, PlayerId startingPlayer) {
        ExceptionHelper.checkNotNullArgument(game, "game");
        this.game = game;
        this.game.setCurrentPlayerId(startingPlayer);
    }

    public Game getGame() {
        return game;
    }

    /**
     * Ends the current turn.
     */
    public UndoAction endTurn() {
        return doGameAction(Game::endTurn);
    }

    /**
     * Sets the given player to the current player.
     *
     * @param currentPlayerId the {@code PlayerId} of the given player
     */
    public UndoAction setCurrentPlayerId(PlayerId currentPlayerId) {
        return game.setCurrentPlayerId(currentPlayerId);
    }

    public Player getCurrentPlayer() {
        return game.getCurrentPlayer();
    }

    public PlayerId getCurrentPlayerId() {
        return getCurrentPlayer().getPlayerId();
    }

    /**
     * Executes the given {@link GameAction}.
     * @param gameAction the given {@link GameAction}.
     */
    public UndoAction doGameAction(GameAction gameAction) {
        ExceptionHelper.checkNotNullArgument(gameAction, "gameAction");

        UndoAction action = gameAction.alterGame(game);
        UndoAction deathResults = game.endPhase();
        return () -> {
            deathResults.undo();
            action.undo();
        };
    }

    /**
     * Designates the given attacker to attack the specific target.
     *
     * @param attacker the {@link TargetId} of the attacker.
     * @param defender the {@link TargetId} of the target.
     */
    public UndoAction attack(TargetId attacker, TargetId defender) {
        // TODO: Check if the action is actually a valid move.
        return doGameAction((currentGame) -> currentGame.attack(attacker, defender));
    }

    /**
     * Plays the designated player's hero power towards the given target.
     */
    public UndoAction playHeroPower(PlayTargetRequest targetRequest) {
        return doGameAction((currentGame) -> {
            Player castingPlayer = currentGame.getPlayer(targetRequest.getCastingPlayerId());
            Character target = currentGame.findTarget(targetRequest.getTargetId());

            HeroPower selectedPower = castingPlayer.getHero().getHeroPower();
            return selectedPower.play(currentGame, Optional.ofNullable(target));
        });
    }

    /**
     * Plays the designated player's card towards the given target.
     */
    public UndoAction playCard(int cardIndex, PlayTargetRequest playTarget) {
        // TODO: Check if the action is actually a valid move.
        return doGameAction((currentGame) -> {
            Player player = currentGame.getPlayer(playTarget.getCastingPlayerId());
            Hand hand = player.getHand();
            int manaCost = hand.getCard(cardIndex).getActiveManaCost();

            UndoableResult<Card> cardRef = hand.removeAtIndex(cardIndex);
            if (cardRef == null) {
                return UndoAction.DO_NOTHING;
            }

            UndoAction playUndo = player.playCard(cardRef.getResult(), manaCost, playTarget);
            return () -> {
                playUndo.undo();
                cardRef.undo();
            };
        });
    }

    public void setGame(Game game) {
        this.game = game;
    }
}
