package org.javacord.api.listener.channel;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.listener.channel.server.text.ServerTextChannelAttachableListener;
import org.javacord.api.listener.channel.user.PrivateChannelAttachableListener;

/**
 * This is a marker interface for listeners that can be attached to a {@link TextChannel}.
 */
public interface TextChannelAttachableListener
        extends ServerTextChannelAttachableListener, PrivateChannelAttachableListener {
}
