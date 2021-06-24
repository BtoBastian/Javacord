package org.javacord.core.event.user;

import org.javacord.api.event.user.UserChangeDeafenedEvent;
import org.javacord.core.entity.user.Member;

/**
 * The implementation of {@link UserChangeDeafenedEvent}.
 */
public class UserChangeDeafenedEventImpl extends ServerUserEventImpl implements UserChangeDeafenedEvent {

    private final Member newMember;
    private final Member oldMember;

    /**
     * Creates a new user change deafened event.
     *
     * @param newMember The new member.
     * @param oldMember The old member.
     */
    public UserChangeDeafenedEventImpl(Member newMember, Member oldMember) {
        super(newMember.getUser(), newMember.getServer());
        this.newMember = newMember;
        this.oldMember = oldMember;
    }

    @Override
    public boolean isNewDeafened() {
        return newMember.getServer().isDeafened(newMember.getUser());
    }

    @Override
    public boolean isOldDeafened() {
        return !isNewDeafened();
    }
}
