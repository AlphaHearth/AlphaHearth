package info.hearthsim.brazier;

import info.hearthsim.brazier.actions.PlayTargetRequest;
import info.hearthsim.brazier.db.CardDescr;
import info.hearthsim.brazier.db.HearthStoneDb;
import info.hearthsim.brazier.game.*;
import info.hearthsim.brazier.game.cards.Card;
import info.hearthsim.brazier.game.cards.CardName;
import info.hearthsim.brazier.game.minions.MinionName;
import info.hearthsim.brazier.game.weapons.WeaponName;
import info.hearthsim.brazier.ui.GamePlayPanel;
import info.hearthsim.brazier.ui.UiUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HearthStoneEmulator {
    private static final Random RNG = new Random();

    private static List<CardDescr> getRandomCards(HearthStoneDb db, int count) {
        List<CardDescr> cards = db.getCardDb().getByKeywords(Keywords.COLLECTIBLE);
        List<CardDescr> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            result.add(cards.get(RNG.nextInt(cards.size())));
        }
        return result;
    }

    private static CardDescr getCard(HearthStoneDb db, String cardName) {
        return db.getCardDb().getById(new CardName(cardName));
    }

    private static Card getCard(Player player, String cardName) {
        HearthStoneDb db = player.getGame().getDb();
        return new Card(player, getCard(db, cardName));
    }

    private static void playCard(Player player, String cardName, PlayTargetRequest playTarget) {
        Card card = getCard(player, cardName);
        player.playCard(card, 0, playTarget);
        player.getGame().endPhase();
    }

    private static void setupInitialGame(Game game) {
        HearthStoneDb db = game.getDb();

        Player player1 = game.getPlayer1();
        player1.getHero().setCurrentHp(29);
        player1.getHero().setCurrentArmor(2);
        player1.summonMinion(db.getMinionDb().getById(new MinionName("Sludge Belcher")));
        player1.equipWeapon(db.getWeaponDb().getById(new WeaponName("Fiery War Axe")));
        player1.getHero().getAttackTool().incAttackCount();

        player1.getHand().addCard(getCard(db, "Moonfire"));

        player1.getManaResource().setManaCrystals(7);
        player1.setMana(0);
        player1.getDeck().setCards(getRandomCards(db, 10));

        Player player2 = game.getPlayer2();
        player2.getHero().setCurrentHp(26);
        player2.getHero().setCurrentArmor(0);
        BoardSide board2 = player2.getBoard();
        player2.summonMinion(db.getMinionDb().getById(new MinionName("Grim Patron")));
        board2.getAllMinions().get(0).getBody().damage(player1.getSpellDamage(1));
        board2.getAllMinions().get(1).getBody().damage(player1.getSpellDamage(1));
        board2.getAllMinions().get(2).getBody().damage(player1.getSpellDamage(3));

        player2.getDeck().setCards(getRandomCards(db, 10));

        Hand hand2 = player2.getHand();
        hand2.addCard(getCard(db, "Slam"));
        hand2.addCard(getCard(db, "Fiery War Axe"));
        hand2.addCard(getCard(db, "Death's Bite"));
        hand2.addCard(getCard(db, "Frothing Berserker"));
        hand2.addCard(getCard(db, "Dread Corsair"));
        hand2.addCard(getCard(db, "Dread Corsair"));
        hand2.addCard(getCard(db, "Warsong Commander"));
        hand2.forAllCards((card) -> card.decreaseManaCost(1));

        player2.getDeck().putOnTop(getCard(db, "Whirlwind"));

        game.endPhase();

        player2.startNewTurn();

        player2.getManaResource().setManaCrystals(8);
        player2.setMana(8);

        player1.getHero().setHeroPower(db.getHeroPowerDb().getById(new CardName("Armor Up!")));
        player2.getHero().setHeroPower(db.getHeroPowerDb().getById(new CardName("Armor Up!")));
    }

    private static void setupInitialGame2(Game game) {
        HearthStoneDb db = game.getDb();

        Player player1 = game.getPlayer1();
        player1.getHero().setCurrentHp(23);
        player1.getHero().setCurrentArmor(0);
        player1.summonMinion(db.getMinionDb().getById(new MinionName("Grim Patron")));

        BoardSide board1 = player1.getBoard();
        board1.getAllMinions().get(0).getBody().damage(player1.getBasicDamage(1));
        board1.getAllMinions().get(0).getBody().damage(player1.getBasicDamage(1));
        board1.getAllMinions().get(2).getBody().damage(player1.getBasicDamage(1));

        player1.getHand().addCard(getCard(db, "Moonfire"));
        player1.getDeck().setCards(getRandomCards(db, 10));

        Player player2 = game.getPlayer2();
        player2.getHero().setCurrentHp(26);
        player2.getHero().setCurrentArmor(0);
        player2.summonMinion(db.getMinionDb().getById(new MinionName("Grim Patron")));

        BoardSide board2 = player2.getBoard();
        board2.getAllMinions().get(0).getBody().damage(player1.getBasicDamage(1));
        board2.getAllMinions().get(1).getBody().damage(player1.getBasicDamage(2));
        board2.getAllMinions().get(2).getBody().damage(player1.getBasicDamage(1));
        board2.getAllMinions().get(3).getBody().damage(player1.getBasicDamage(2));
        board2.getAllMinions().get(4).getBody().damage(player1.getBasicDamage(3));
        player2.summonMinion(db.getMinionDb().getById(new MinionName("Treant")));

        player2.getDeck().setCards(getRandomCards(db, 10));

        Hand hand2 = player2.getHand();
        hand2.addCard(getCard(db, "Unstable Ghoul"));
        hand2.addCard(getCard(db, "Emperor Thaurissan"));
        hand2.addCard(getCard(db, "Execute"));
        hand2.addCard(getCard(db, "Grim Patron"));
        hand2.addCard(getCard(db, "Execute"));
        hand2.addCard(getCard(db, "Warsong Commander"));
        hand2.addCard(getCard(db, "Frothing Berserker"));

        player2.getDeck().putOnTop(getCard(db, "Acolyte of Pain"));

        game.endPhase();
        game.setCurrentPlayerId(game.getPlayer2().getPlayerId());
        player2.startNewTurn();

        player1.getManaResource().setManaCrystals(8);
        player1.setMana(0);

        player2.getManaResource().setManaCrystals(8);
        player2.setMana(8);

        player1.getHero().setHeroPower(db.getHeroPowerDb().getById(new CardName("Armor Up!")));
        player2.getHero().setHeroPower(db.getHeroPowerDb().getById(new CardName("Armor Up!")));
    }

    private static void setupInitialGame3(Game game) {
        HearthStoneDb db = game.getDb();

        Player player1 = game.getPlayer1();
        game.setCurrentPlayerId(player1.getPlayerId());

        player1.getHero().setHeroPower(db.getHeroPowerDb().getById(new CardName("Reinforce")));

        player1.summonMinion(db.getMinionDb().getById(new MinionName("Tirion Fordring")));
        player1.summonMinion(db.getMinionDb().getById(new MinionName("Tirion Fordring")));
        player1.summonMinion(db.getMinionDb().getById(new MinionName("Tirion Fordring")));

        player1.getHand().addCard(getCard(db, "Pyroblast"));
        player1.getDeck().setCards(getRandomCards(db, 10));

        Player player2 = game.getPlayer2();
        player2.getHero().setHeroPower(db.getHeroPowerDb().getById(new CardName("Dagger Mastery")));

        playCard(player2, "The Coin",
                new PlayTargetRequest(player2.getPlayerId()));
        playCard(player2, "Defias Ringleader",
                new PlayTargetRequest(player2.getPlayerId(), 0, null));
        playCard(player2, "SI:7 Agent",
                new PlayTargetRequest(player2.getPlayerId(), 0, player2.getBoard().getAllMinions().get(0).getEntityId()));
        playCard(player2, "Ancient Mage",
                new PlayTargetRequest(player2.getPlayerId(), 1, null));
        player2.summonMinion(db.getMinionDb().getById(new MinionName("Sylvanas Windrunner")));

        player2.getDeck().setCards(getRandomCards(db, 10));

        Hand hand2 = player2.getHand();
        hand2.addCard(getCard(db, "Shadowstep"));
        hand2.addCard(getCard(db, "Shadowstep"));
        hand2.addCard(getCard(db, "Fan of Knives"));
        hand2.addCard(getCard(db, "Deadly Poison"));
        hand2.addCard(getCard(db, "Deadly Poison"));
        hand2.addCard(getCard(db, "Blade Flurry"));
        hand2.addCard(getCard(db, "Blade Flurry"));
        hand2.addCard(getCard(db, "Elven Archer"));
        hand2.addCard(getCard(db, "Preparation"));

        player2.getDeck().putOnTop(getCard(db, "Emperor Cobra"));
        player2.getDeck().putOnTop(getCard(db, "Headcrack"));

        player1.getHero().setCurrentHp(30);
        player1.getHero().setCurrentArmor(0);

        player2.getHero().setCurrentHp(1);
        player2.getHero().setCurrentArmor(0);

        game.endTurn();

        player1.getManaResource().setManaCrystals(10);
        player1.setMana(0);

        player2.getManaResource().setManaCrystals(10);
        player2.setMana(10);
    }

    public static void main(String[] args) throws Throwable {
        UiUtils.useLookAndFeel("Nimbus");

        HearthStoneDb db = HearthStoneDb.readDefault();

        SwingUtilities.invokeLater(() -> {
            PlayerId player1 = new PlayerId("Player1");
            PlayerId player2 = new PlayerId("Player2");

            Game game = new Game(db, player1, player2);
            setupInitialGame3(game);

            GamePlayPanel gamePlayPanel = new GamePlayPanel(game, player2);

            JFrame mainFrame = new JFrame("HearthStone Emulator");
            mainFrame.getContentPane().setLayout(new GridLayout(1, 1));
            mainFrame.getContentPane().add(gamePlayPanel);

            mainFrame.pack();
            mainFrame.setLocationRelativeTo(null);
            mainFrame.setVisible(true);
            mainFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        });
    }
}
