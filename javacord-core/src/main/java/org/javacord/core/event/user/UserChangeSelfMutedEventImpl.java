package org.javacord.core.event.user;

import org.javacord.api.event.user.UserChangeSelfMutedEvent;
import org.javacord.core.entity.user.Member;

/**
 * The implementation of {@link UserChangeSelfMutedEvent}.
 */
public class UserChangeSelfMutedEventImpl extends ServerUserEventImpl implements UserChangeSelfMutedEvent {

    private final boolean newSelfMuted;
    private final boolean oldSelfMuted;

    /**
     * Creates a new user change self muted event.
     *
     * @param member The member.
     * @param newSelfMuted The new self muted status.
     * @param oldSelfMuted The old self muted status.
     */
    public UserChangeSelfMutedEventImpl(Member member, boolean newSelfMuted, boolean oldSelfMuted) {
        super(member.getUser(), member.getServer());
        this.newSelfMuted = newSelfMuted;
        this.oldSelfMuted = oldSelfMuted;
    }

    @Override
    public boolean isNewSelfMuted() {
        return newSelfMuted;
    }

    @Override
    public boolean isOldSelfMuted() {
        return oldSelfMuted;
    }
}
