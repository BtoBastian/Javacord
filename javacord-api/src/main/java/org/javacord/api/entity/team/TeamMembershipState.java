package org.javacord.api.entity.team;


public enum TeamMembershipState {

    /**
     * User is invited as a team member.
     */
    INVITED(1),

    /**
     * User is accepted as a team member.
     */
    ACCEPTED(2),

    /**
     * An unknown team membership state, most likely due to new team membership states.
     */
    UNKNOWN(-1);

    private final int id;

    /**
     * Class constructor.
     *
     * @param id The id of the membership state
     */
    TeamMembershipState(int id) {
        this.id = id;
    }

    /**
     * Gets the id of the membership state.
     *
     * @return The id of the membership state.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the team membership state by its id.
     *
     * @param id The id of the team membership state.
     * @return The team membership state with the given id.
     */
    public static TeamMembershipState fromId(int id) {
        for (TeamMembershipState membershipState : values()) {
            if (membershipState.getId() == id) {
                return membershipState;
            }
        }
        return UNKNOWN;
    }
}
