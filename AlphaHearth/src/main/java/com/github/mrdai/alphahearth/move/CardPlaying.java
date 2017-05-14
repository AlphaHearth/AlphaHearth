package com.github.mrdai.alphahearth.move;

import com.github.mrdai.alphahearth.Board;
import info.hearthsim.brazier.GameAgent;
import info.hearthsim.brazier.actions.PlayTargetRequest;
import info.hearthsim.brazier.game.*;
import info.hearthsim.brazier.game.Character;
import info.hearthsim.brazier.game.cards.Card;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CardPlaying extends AbstractSingleMove {
    private static final Logger LOG = LoggerFactory.getLogger(CardPlaying.class);

    private final PlayerId playerId;
    private final int cardIndex;
    private final int minionLocation;
    private final boolean isTargetFriendly;
    private final int targetIndex;

    public CardPlaying(Card card) {
        this(card, null);
    }

    public CardPlaying(Card card, Character target) {
        this(card, -1, target);
    }

    public CardPlaying(Card card, int minionLocation) {
        this(card, minionLocation, null);
    }

    public CardPlaying(Card card, int minionLocation, Character target) {
        setConstructPoint();

        if (card.isMinionCard() && minionLocation == -1)
            throw new IllegalArgumentException("Player minion card " + card + " must provide summoning location");
        Player owner = card.getOwner();
        this.playerId = owner.getPlayerId();
        this.cardIndex = owner.getHand().indexOf(card);
        assert cardIndex != -1;
        this.minionLocation = minionLocation;
        if (target == null) {
            isTargetFriendly = false;
            targetIndex = -1;
        } else {
            this.isTargetFriendly = target.getOwner().getPlayerId() == playerId;
            if (target instanceof Hero) {
                this.targetIndex = 8;
            } else {
                BoardSide boardSide = isTargetFriendly ? owner.getBoard() : owner.getOpponent().getBoard();
                this.targetIndex = boardSide.indexOf(target.getEntityId());
            }
        }
    }

    public String toString(Board board) {
        Game game = board.getGame();
        StringBuilder builder = new StringBuilder();
        if (minionLocation == -1) {
            builder.append(game.getPlayer(playerId).getPlayerId()).append(" plays ")
                   .append(game.getPlayer(playerId).getHand().getCard(cardIndex));
            if (targetIndex != -1) {
                builder.append(" with target ");
                String targetName;
                Player targetOwner = isTargetFriendly ? game.getCurrentPlayer() : game.getCurrentOpponent();
                if (targetIndex == 8)
                    targetName = targetOwner.getPlayerId().getName();
                else
                    targetName = targetOwner.getBoard().getMinion(targetIndex).toString();
                builder.append(targetName);
            }
        } else {
            builder.append(game.getPlayer(playerId).getPlayerId()).append(" summons ")
                   .append(game.getPlayer(playerId).getHand().getCard(cardIndex))
                   .append(" on location ").append(minionLocation);
            if (targetIndex != -1) {
                builder.append(" with battle cry target ");
                String targetName;
                Player targetOwner = isTargetFriendly ? game.getCurrentPlayer() : game.getCurrentOpponent();
                if (targetIndex == 8)
                    targetName = targetOwner.getPlayerId().getName();
                else
                    targetName = targetOwner.getBoard().getMinion(targetIndex).toString();
                builder.append(targetName);
            }
        }
        return builder.toString();
    }

    @Override
    public void applyToUnsafe(Board board, boolean logMove) {
        if (logMove)
            LOG.info(toString(board));
        else if (LOG.isTraceEnabled())
            LOG.trace(toString(board));

        GameAgent playAgent = board.playAgent;
        Game game = board.getGame();
        if (targetIndex == -1) {
            playAgent.playCard(cardIndex, new PlayTargetRequest(playerId, minionLocation, null));
        } else {
            Player targetOwner = isTargetFriendly ? game.getPlayer(playerId) : game.getOpponent(playerId);
            if (targetIndex == 8) {
                Hero target = targetOwner.getHero();
                playAgent.playCard(cardIndex, new PlayTargetRequest(playerId, minionLocation, target.getEntityId()));
            } else {
                BoardSide enemyBoardSide = targetOwner.getBoard();
                playAgent.playCard(cardIndex, new PlayTargetRequest(playerId, minionLocation, enemyBoardSide.getMinion(targetIndex).getEntityId()));
            }
        }
    }

    public String toString() {
        return String.format("CardPlaying[PlayerId: %s, cardIndex: %d, minionLocation: %d, isTargetFriendly: %b, target: %s]",
            playerId, cardIndex, minionLocation, isTargetFriendly, targetIndex);
    }
}
