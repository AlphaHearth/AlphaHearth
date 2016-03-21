package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.*;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.minions.MinionName;
import info.hearthsim.brazier.parsing.NamedArg;
import info.hearthsim.brazier.Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jtrim.utils.ExceptionHelper;

/**
 * Predefined {@link PlayActionRequirement}s.
 */
public final class PlayActionRequirements {
    /**
     * {@code PlayActionRequirement} which tests if the player has played any card in this turn
     * (combo effect will be triggered).
     */
    public static final PlayActionRequirement COMBO = (player) -> player.getCardsPlayedThisTurn() > 0;

    /**
     * {@code PlayActionRequirement} which tests if the player has not played any card in this turn
     * (combo effect will not be triggered).
     */
    public static final PlayActionRequirement NO_COMBO = (player) -> player.getCardsPlayedThisTurn() == 0;

    /**
     * {@code PlayActionRequirement} which tests if the player still have more room on his board side.
     */
    public static final PlayActionRequirement HAS_SPACE_ON_OWN_BOARD = (player) -> !player.getBoard().isFull();

    /**
     * {@code PlayActionRequirement} which tests if the player's opponent has minion on board.
     */
    public static final PlayActionRequirement OPPONENT_BOARD_NOT_EMPTY = opponentBoardIsLarger(0);

    /**
     * {@code PlayActionRequirement} which tests if the player does not have minion on board.
     */
    public static final PlayActionRequirement BOARD_IS_EMPTY = (player) -> {
        Game game = player.getGame();
        return game.getPlayer1().getBoard().getMinionCount() <= 0
                && game.getPlayer2().getBoard().getMinionCount() <= 0 ;
    };

    /**
     * {@code PlayActionRequirement} which tests if the player has minion on board.
     */
    public static final PlayActionRequirement BOARD_IS_NOT_EMPTY = not(BOARD_IS_EMPTY);

    /**
     * {@code PlayActionRequirement} which tests if the player's hand is empty.
     */
    public static final PlayActionRequirement EMPTY_HAND = (player) -> player.getHand().getCardCount() == 0;

    /**
     * {@code PlayActionRequirement} which tests if the player has equipped weapon.
     */
    public static final PlayActionRequirement HAS_WEAPON = (player) -> player.tryGetWeapon() != null;

    /**
     * {@code PlayActionRequirement} which tests if the player does not equip weapon.
     */
    public static final PlayActionRequirement DOESN_HAVE_WEAPON = not(HAS_WEAPON);

    /**
     * Returns a {@code PlayActionRequirement} which tests if there is a card in the player's hand
     * which has all the given {@code Keyword}s.
     */
    public static PlayActionRequirement hasCardInHand(@NamedArg("keywords") Keyword... keywords) {
        ArrayList<Keyword> keywordCopy = new ArrayList<>(Arrays.asList(keywords));
        ExceptionHelper.checkNotNullElements(keywordCopy, "keywords");

        return (Player player) -> {
            Hand hand = player.getHand();
            return hand.findCard((card) -> card.getKeywords().containsAll(keywordCopy)) != null;
        };
    }

    /**
     * Returns a {@code PlayActionRequirement} which tests if there is a minion whose attack is less than
     * the given number and there is enough space on the player's own board to steal that minion.
     */
    public static PlayActionRequirement stealBattleCryNeeds(@NamedArg("maxAttack") int maxAttack) {
        return (Player player) -> {
            BoardSide opponentBoard = player.getOpponent().getBoard();

            boolean hasTarget = opponentBoard
                    .findMinion((minion) -> minion.getAttackTool().getAttack() <= maxAttack) != null;
            if (hasTarget) {
                return player.getBoard().getMinionCount() + 1 < Player.MAX_BOARD_SIZE;
            }
            else {
                return !player.getBoard().isFull();
            }
        };
    }

    /**
     * Returns a {@code PlayActionRequirement} which tests if any of the minions on the player's own board has
     * all of the given {@code Keyword}s.
     */
    public static PlayActionRequirement hasOnOwnBoard(@NamedArg("keywords") Keyword... keywords) {
        ArrayList<Keyword> keywordCopy = new ArrayList<>(Arrays.asList(keywords));
        ExceptionHelper.checkNotNullElements(keywordCopy, "keywords");

        return (Player player) -> {
            return player.getBoard().findMinion((minion) -> minion.getKeywords().containsAll(keywordCopy)) != null;
        };
    }

    /**
     * Returns a {@code PlayActionRequirement} which tests if all of the minions on the player's own board don't have
     * all of the given {@code Keyword}s.
     * <p>
     * The returned {@code PlayActionRequirement} would be a negation of {@link #hasOnOwnBoard(Keyword...)}'s result.
     */
    public static PlayActionRequirement doesntHaveOnOwnBoard(@NamedArg("keywords") Keyword... keywords) {
        return not(hasOnOwnBoard(keywords));
    }

    /**
     * Returns a negation of the given {@code PlayActionRequirement}.
     */
    public static PlayActionRequirement not(@NamedArg("condition") PlayActionRequirement condition) {
        return (player) -> !condition.meetsRequirement(player);
    }

    /**
     * Returns a {@code PlayActionRequirement} which tests if all of the given minions are not on the player's own
     * board.
     */
    public static PlayActionRequirement doesntHaveAllOnOwnBoard(@NamedArg("minions") MinionName[] minions) {
        List<MinionName> minionsCopy = new ArrayList<>(Arrays.asList(minions));
        ExceptionHelper.checkNotNullElements(minionsCopy, "minions");

        return (Player player) -> {
            BoardSide board = player.getBoard();
            Set<MinionName> remaining = new HashSet<>(minionsCopy);
            for (Minion minion: board.getAllMinions()) {
                remaining.remove(minion.getBaseDescr().getId());
            }
            return !remaining.isEmpty();
        };
    }

    /**
     * Returns a {@code PlayActionRequirement} which tests if the number of the opponent's minions
     * on board is larger than the given number.
     */
    public static PlayActionRequirement opponentBoardIsLarger(@NamedArg("minionCount") int minionCount) {
        return (Player player) -> {
            return player.getOpponent().getBoard().getMinionCount() > minionCount;
        };
    }

    /**
     * Returns a {@code PlayActionRequirement} which tests if the opponent's health point is less
     * than the given number.
     */
    public static PlayActionRequirement opponentsHpIsLess(@NamedArg("hp") int hp) {
        return (Player player)
                -> player.getOpponent().getHero().getCurrentHp() < hp;
    }

    /**
     * Returns a {@code PlayActionRequirement} which tests if the player's own health point is less
     * than the given number.
     */
    public static PlayActionRequirement ownHpIsLess(@NamedArg("hp") int hp) {
        return (Player player) -> player.getHero().getCurrentHp() < hp;
    }

    /**
     * Returns a {@code PlayActionRequirement} which tests if the player's own health point is larger
     * than the given number.
     */
    public static PlayActionRequirement ownHpIsMore(@NamedArg("hp") int hp) {
        return (Player player) -> player.getHero().getCurrentHp() > hp;
    }

    /**
     * Returns a {@code PlayActionRequirement} which tests if the player has a given {@code Keyword} flag.
     */
    public static PlayActionRequirement hasPlayerFlag(@NamedArg("flag") Keyword flag) {
        ExceptionHelper.checkNotNullArgument(flag, "flag");
        return (Player player) -> player.getAuraFlags().hasFlag(flag);
    }

    /**
     * Returns a {@code PlayActionRequirement} which tests if the player doesn't have a given {@code Keyword} flag.
     */
    public static PlayActionRequirement doesntHavePlayerFlag(@NamedArg("flag") Keyword flag) {
        ExceptionHelper.checkNotNullArgument(flag, "flag");
        return (Player player) -> !player.getAuraFlags().hasFlag(flag);
    }


    private PlayActionRequirements() {
        throw new AssertionError();
    }
}
