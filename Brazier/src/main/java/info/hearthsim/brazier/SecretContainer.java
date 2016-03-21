package info.hearthsim.brazier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jtrim.utils.ExceptionHelper;

/**
 * Container for {@link Secret}, which can be used as the container of the active in-game secrets
 * of a certain player.
 */
public final class SecretContainer implements PlayerProperty {
    public static final int MAX_SECRETS = 5;

    private final Player owner;
    private final List<Secret> secrets;
    private final List<Secret> secretsView;

    public SecretContainer(Player owner) {
        ExceptionHelper.checkNotNullArgument(owner, "owner");

        this.owner = owner;
        this.secrets = new ArrayList<>(MAX_SECRETS);
        this.secretsView = Collections.unmodifiableList(secrets);
    }

    /**
     * Returns a copy of this {@code SecretContainer} with the given new owner.
     */
    public SecretContainer copyFor(Player newOwner) {
        SecretContainer result = new SecretContainer(newOwner);
        for (Secret secret : secrets)
            result.addSecret(secret.copyFor(newOwner.getGame(), newOwner));
        return result;
    }

    public List<Secret> getSecrets() {
        return secretsView;
    }

    public boolean isFull() {
        return secrets.size() >= MAX_SECRETS;
    }

    @Override
    public Player getOwner() {
        return owner;
    }

    public Secret findById(EntityName secretId) {
        ExceptionHelper.checkNotNullArgument(secretId, "secretId");

        for (Secret secret: secrets) {
            if (secretId.equals(secret.getSecretId())) {
                return secret;
            }
        }
        return null;
    }

    public Secret findById(EntityId secretId) {
        ExceptionHelper.checkNotNullArgument(secretId, "secretId");

        for (Secret secret: secrets) {
            if (secretId == secret.getEntityId()) {
                return secret;
            }
        }
        return null;
    }

    public void addSecret(Secret secret) {
        ExceptionHelper.checkNotNullArgument(secret, "secret");

        if (isFull())
            return;

        secrets.add(secret);
        secret.activate();
    }

    public void stealActivatedSecret(SecretContainer other, Secret secret) {
        other.removeSecretLeaveActive(secret);
        if (isFull()) {
            secret.deactivate();
            return;
        }

        secret.setOwner(owner);
        secrets.add(secret);
    }

    private void removeSecretLeaveActive(Secret secret) {
        ExceptionHelper.checkNotNullArgument(secret, "secret");

        int secretCount = secrets.size();
        for (int i = 0; i < secretCount; i++) {
            if (secrets.get(i) == secret) {
                secrets.remove(i);
                return;
            }
        }
    }

    public void removeSecret(Secret secret) {
        ExceptionHelper.checkNotNullArgument(secret, "secret");
        // TODO: Show secret to the opponent

        removeSecretLeaveActive(secret);
        secret.deactivate();
    }

    public void removeAllSecrets() {
        if (secrets.isEmpty())
            return;

        for (Secret secret: secrets)
            secret.deactivate();

        secrets.clear();


        // TODO: Show secrets to the opponent
    }

    public boolean hasSecret() {
        return !secrets.isEmpty();
    }
}
