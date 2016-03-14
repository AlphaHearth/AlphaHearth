package info.hearthsim.brazier.ui;

import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.minions.MinionBody;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

@SuppressWarnings("serial")
public class MinionComponent extends javax.swing.JPanel {
    private static final Color DEFAULT_COLOR = Color.ORANGE;
    private static final Color HIGHLIGH_COLOR = Color.GREEN;
    private static final Color FROZEN_COLOR = Color.BLUE;
    private static final Color UNTARGETABLE_COLOR = Color.GRAY;
    private static final Color STEALTH_COLOR = UNTARGETABLE_COLOR.darker();

    private static final int BORDER_WIDTH = 3;

    private static final Border DEFAULT_BORDER = BorderFactory.createEmptyBorder();

    private static final Border TAUNT_BORDER
            = BorderFactory.createLineBorder(Color.BLACK, BORDER_WIDTH);

    private boolean highlighted;
    private Minion minion;

    public MinionComponent(Minion minion) {
        initComponents();

        setMinion(minion);
        setHighlight(false);
    }

    public final void setHighlight(boolean highlighted) {
        this.highlighted = highlighted;
        updateFlagDisplay();
    }

    public final void setMinion(Minion minion) {
        this.minion = minion;

        if (minion != null) {
            String name = minion.getBaseDescr().getDisplayName();
            int attack = minion.getAttackTool().getAttack();
            int maxHp = minion.getBody().getMaxHp();
            int currentHp = minion.getBody().getCurrentHp();

            jNameLabel.setText("<html>" + name + "</html>");
            jMinionAttackLabel.setForeground(Color.BLACK);
            jMinionAttackLabel.setText(Integer.toString(attack));
            jMinionHpLabel.setText(Integer.toString(currentHp));
            jMinionHpLabel.setForeground(currentHp < maxHp ? Color.RED : Color.BLACK);
        }
        else {
            jNameLabel.setText("");
            jMinionAttackLabel.setText("");
            jMinionHpLabel.setText("");
        }

        updateFlagDisplay();
    }

    private Color getRequiredBckgColor() {
        if (highlighted) {
            return HIGHLIGH_COLOR;
        }
        if (minion == null) {
            return DEFAULT_COLOR;
        }

        MinionBody body = minion.getBody();
        if (minion.getProperties().isFrozen()) {
            return FROZEN_COLOR;
        }
        if (body.isStealth()) {
            return STEALTH_COLOR;
        }
        if (!body.isTargetable()) {
            return UNTARGETABLE_COLOR;
        }

        return DEFAULT_COLOR;
    }

    private void updateFlagDisplay() {
        if (minion == null) {
            setBorder(DEFAULT_BORDER);
            jDivineShieldLabel.setVisible(false);
        }
        else {
            MinionBody body = minion.getBody();
            setBorder(body.isTaunt() ? TAUNT_BORDER : DEFAULT_BORDER);
            jDivineShieldLabel.setVisible(minion.getBody().isDivineShield());
        }

        setBackground(getRequiredBckgColor());
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jNameLabel = new javax.swing.JLabel();
        jMinionHpLabel = new javax.swing.JLabel();
        jMinionAttackLabel = new javax.swing.JLabel();
        jDivineShieldLabel = new javax.swing.JLabel();

        jNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jNameLabel.setText("Minion's name");
        jNameLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        jMinionHpLabel.setText("9");

        jMinionAttackLabel.setText("7");

        jDivineShieldLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jDivineShieldLabel.setText("Divine Shield");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jDivineShieldLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jMinionAttackLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jMinionHpLabel)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 44, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jDivineShieldLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jMinionHpLabel)
                    .addComponent(jMinionAttackLabel))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jDivineShieldLabel;
    private javax.swing.JLabel jMinionAttackLabel;
    private javax.swing.JLabel jMinionHpLabel;
    private javax.swing.JLabel jNameLabel;
    // End of variables declaration//GEN-END:variables
}
