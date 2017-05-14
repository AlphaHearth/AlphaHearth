package info.hearthsim.brazier;

import info.hearthsim.brazier.actions.GameAction;
import info.hearthsim.brazier.actions.PlayTargetRequest;
import info.hearthsim.brazier.game.Character;
import info.hearthsim.brazier.game.*;
import info.hearthsim.brazier.game.cards.Card;
import org.jtrim.utils.ExceptionHelper;

import java.util.Optional;

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
        ExceptionHelper.checkNotNullArgument(game, "game");
        this.game = game;
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

    /**
     * Ends the current turn.
     */
    public void endTurn() {
        doGameAction(Game::endTurn);
    }

    /**
     * Sets the given player to the current player.
     *
     * @param currentPlayerId the {@code PlayerId} of the given player
     */
    public void setCurrentPlayerId(PlayerId currentPlayerId) {
        game.setCurrentPlayerId(currentPlayerId);
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
    public void doGameAction(GameAction gameAction) {
        ExceptionHelper.checkNotNullArgument(gameAction, "gameAction");

        gameAction.apply(game);
        game.endPhase();
    }

    /**
     * Designates the given attacker to attack the specific target.
     *
     * @param attacker the {@link EntityId} of the attacker.
     * @param defender the {@link EntityId} of the target.
     */
    public void attack(EntityId attacker, EntityId defender) {
        // TODO: Check if the action is actually a valid move.
        doGameAction((currentGame) -> currentGame.attack(attacker, defender));
    }

    /**
     * Plays the designated player's hero power towards the given target.
     */
    public void playHeroPower(PlayTargetRequest targetRequest) {
        doGameAction((currentGame) -> {
            Player castingPlayer = currentGame.getPlayer(targetRequest.getCastingPlayerId());
            Character target = currentGame.getCharacter(targetRequest.getEntityId());

            HeroPower selectedPower = castingPlayer.getHero().getHeroPower();
            selectedPower.play(Optional.ofNullable(target));
        });
    }

    /**
     * Plays the designated player's card towards the given target.
     */
    public void playCard(int cardIndex, PlayTargetRequest playTarget) {
        // TODO: Check if the action is actually a valid move.
        doGameAction((currentGame) -> {
            Player player = currentGame.getPlayer(playTarget.getCastingPlayerId());
            Hand hand = player.getHand();
            int manaCost = hand.getCard(cardIndex).getActiveManaCost();

            Card card = hand.removeAtIndex(cardIndex);
            if (card == null)
                return;

            player.playCard(card, manaCost, playTarget);
        });
    }

    public void setGame(Game game) {
        this.game = game;
    }
    public Game getGame() {
        return game;
    }
}
