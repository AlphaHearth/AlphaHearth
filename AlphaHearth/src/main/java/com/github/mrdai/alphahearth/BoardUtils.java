package com.github.mrdai.alphahearth;

import info.hearthsim.brazier.game.*;
import info.hearthsim.brazier.game.minions.Minion;
import info.hearthsim.brazier.game.weapons.AttackTool;
import info.hearthsim.brazier.game.weapons.Weapon;

public abstract class BoardUtils {
    public static boolean compareHero(Hero heroA, Hero heroB) {
        if (heroA.getCurrentHp() != heroB.getCurrentHp())
            return false;
        if (heroA.getCurrentArmor() != heroB.getCurrentArmor())
            return false;
        if (heroA.isImmune() != heroB.isImmune())
            return false;
        if (heroA.getMaxHp() != heroB.getMaxHp())
            return false;

        HeroPower powerA = heroA.getHeroPower();
        HeroPower powerB = heroB.getHeroPower();
        if (powerA.isPlayable() != powerB.isPlayable())
            return false;
        if (powerA.getManaCost() != powerB.getManaCost())
            return false;
        if (!powerA.getPowerDef().getName().equals(powerB.getPowerDef().getName()))
            return false;

        AttackTool attackA = heroA.getAttackTool();
        AttackTool attackB = heroB.getAttackTool();
        if (attackA.canAttackWith() != attackB.canAttackWith())
            return false;
        if (attackA.getAttack() != attackB.getAttack())
            return false;
        if (attackA.getMaxAttackCount() != attackB.getMaxAttackCount())
            return false;

        return true;
    }

    public static boolean compareWeapon(Weapon weaponA, Weapon weaponB) {
        if (weaponA == null && weaponB != null || weaponB == null && weaponA != null)
            return false;
        if (weaponA == null && weaponB == null)
            return true;
        if (weaponA.getAttack() != weaponB.getAttack())
            return false;
        if (weaponA.getDurability() != weaponB.getDurability())
            return false;
        return true;
    }

    public static boolean compareBoardSide(BoardSide boardA, BoardSide boardB) {
        if (boardA.getMinionCount() != boardB.getMinionCount())
            return false;
        int totalAttackA = 0, totalAttackB = 0, totalHealthA = 0, totalHealthB = 0;
        for (Minion minion : boardA.getAllMinions()) {
            AttackTool attack = minion.getAttackTool();
            totalAttackA += attack.getAttack() * attack.getMaxAttackCount();
            totalHealthA += minion.getBody().getCurrentHp();
        }
        for (Minion minion : boardB.getAllMinions()) {
            AttackTool attack = minion.getAttackTool();
            totalAttackB+= attack.getAttack() * attack.getMaxAttackCount();
            totalHealthB += minion.getBody().getCurrentHp();
        }

        return totalAttackA == totalAttackB && totalHealthA == totalHealthB;
    }

    public static boolean compareSecrets(SecretContainer secretsA, SecretContainer secretsB) {
        if (secretsA.getSecrets().size() != secretsB.getSecrets().size())
            return false;
        for (int i = 0; i < secretsA.getSecrets().size(); i++)
            if (!secretsA.getSecrets().get(i).getSecretId().getName().equals(secretsB.getSecrets().get(i).getSecretId().getName()))
                return false;
        return true;
    }

    public static boolean compareMana(ManaResource manaA, ManaResource manaB) {
        if (manaA.getManaCrystals() != manaB.getManaCrystals())
            return false;
        if (manaA.getNextTurnOverload() != manaB.getNextTurnOverload())
            return false;
        return true;
    }

}
