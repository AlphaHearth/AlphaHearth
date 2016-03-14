package info.hearthsim.brazier;

import info.hearthsim.brazier.actions.PlayTargetRequest;
import info.hearthsim.brazier.actions.undo.UndoAction;
import info.hearthsim.brazier.actions.WorldAction;
import info.hearthsim.brazier.cards.Card;

import java.util.Optional;

import info.hearthsim.brazier.actions.undo.UndoableResult;
import org.jtrim.utils.ExceptionHelper;

/**
 * {@code WorldPlayAgent} acts as an agent of a given {@link World},
 * providing limited methods for client to use.
 */
public class WorldPlayAgent {
    private final World world;

    /**
     * Creates a {@code WorldPlayAgent} with the given {@code World}, and
     * set {@code Player1} as the starting player.
     *
     * @param world the given {@code World}
     */
    public WorldPlayAgent(World world) {
        this(world, world.getPlayer1().getPlayerId());
    }

    /**
     * Creates a {@code WorldPlayAgent} with the given {@code World} and
     * specific starting player.
     *
     * @param world the given {@code World}
     * @param startingPlayer the {@link PlayerId} of the starting player
     */
    public WorldPlayAgent(World world, PlayerId startingPlayer) {
        ExceptionHelper.checkNotNullArgument(world, "world");
        this.world = world;
        this.world.setCurrentPlayerId(startingPlayer);
    }

    public World getWorld() {
        return world;
    }

    /**
     * Ends the current turn.
     */
    public UndoAction endTurn() {
        return doWorldAction(World::endTurn);
    }

    /**
     * Sets the given player to the current player.
     *
     * @param currentPlayerId the {@code PlayerId} of the given player
     */
    public UndoAction setCurrentPlayerId(PlayerId currentPlayerId) {
        return world.setCurrentPlayerId(currentPlayerId);
    }

    public Player getCurrentPlayer() {
        return world.getCurrentPlayer();
    }

    public PlayerId getCurrentPlayerId() {
        return getCurrentPlayer().getPlayerId();
    }

    /**
     * Executes the given {@link WorldAction}.
     * @param worldAction the given {@link WorldAction}.
     */
    public UndoAction doWorldAction(WorldAction worldAction) {
        ExceptionHelper.checkNotNullArgument(worldAction, "worldAction");

        UndoAction action = worldAction.alterWorld(world);
        UndoAction deathResults = world.endPhase();
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
        return doWorldAction((currentWorld) -> currentWorld.attack(attacker, defender));
    }

    /**
     * Plays the designated player's hero power towards the given target.
     */
    public UndoAction playHeroPower(PlayTargetRequest targetRequest) {
        return doWorldAction((currentWorld) -> {
            Player castingPlayer = currentWorld.getPlayer(targetRequest.getCastingPlayerId());
            TargetableCharacter target = currentWorld.findTarget(targetRequest.getTargetId());

            HeroPower selectedPower = castingPlayer.getHero().getHeroPower();
            return selectedPower.play(currentWorld, Optional.ofNullable(target));
        });
    }

    /**
     * Plays the designated player's card towards the given target.
     */
    public UndoAction playCard(int cardIndex, PlayTargetRequest playTarget) {
        // TODO: Check if the action is actually a valid move.
        return doWorldAction((currentWorld) -> {
            Player player = currentWorld.getPlayer(playTarget.getCastingPlayerId());
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
}
