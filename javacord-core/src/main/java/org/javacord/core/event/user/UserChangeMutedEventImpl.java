package org.javacord.core.event.user;

import org.javacord.api.event.user.UserChangeMutedEvent;
import org.javacord.core.entity.user.Member;

/**
 * The implementation of {@link UserChangeMutedEvent}.
 */
public class UserChangeMutedEventImpl extends ServerUserEventImpl implements UserChangeMutedEvent {

    private final Member newMember;
    private final Member oldMember;

    /**
     * Creates a new user change muted event.
     *
     * @param newMember The new member.
     * @param oldMember The old member.
     */
    public UserChangeMutedEventImpl(Member newMember, Member oldMember) {
        super(newMember.getUser(), newMember.getServer());
        this.newMember = newMember;
        this.oldMember = oldMember;
    }

    @Override
    public boolean isNewMuted() {
        return newMember.getServer().isMuted(newMember.getUser());
    }

    @Override
    public boolean isOldMuted() {
        return !isNewMuted();
    }
}
