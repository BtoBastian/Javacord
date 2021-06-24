package org.javacord.core.event.user;

import org.javacord.api.event.user.UserChangeSelfDeafenedEvent;
import org.javacord.core.entity.user.Member;

/**
 * The implementation of {@link UserChangeSelfDeafenedEvent}.
 */
public class UserChangeSelfDeafenedEventImpl extends ServerUserEventImpl implements UserChangeSelfDeafenedEvent {

    private final boolean newSelfDeafened;
    private final boolean oldSelfDeafened;

    /**
     * Creates a new user change self deafened event.
     *
     * @param member The member.
     * @param newSelfDeafened The new self deafened status.
     * @param oldSelfDeafened The old self deafened status.
     */
    public UserChangeSelfDeafenedEventImpl(Member member, boolean newSelfDeafened, boolean oldSelfDeafened) {
        super(member.getUser(), member.getServer());
        this.newSelfDeafened = newSelfDeafened;
        this.oldSelfDeafened = oldSelfDeafened;
    }

    @Override
    public boolean isNewSelfDeafened() {
        return newSelfDeafened;
    }

    @Override
    public boolean isOldSelfDeafened() {
        return oldSelfDeafened;
    }
}
