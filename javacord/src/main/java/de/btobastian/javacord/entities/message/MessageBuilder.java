package de.btobastian.javacord.entities.message;

import de.btobastian.javacord.entities.Mentionable;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.channels.ServerTextChannel;
import de.btobastian.javacord.entities.channels.TextChannel;
import de.btobastian.javacord.entities.message.embed.EmbedBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

/**
 * This class can help you to generate messages.
 * It can be helpful, if you work a lot with decorations, etc.
 */
public class MessageBuilder {

    /**
     * The string builder used to create the message.
     */
    private final StringBuilder strBuilder = new StringBuilder();

    /**
     * The embed of the message. Might be <code>null</code>.
     */
    private EmbedBuilder embed = null;

    /**
     * If the message should be text to speech or not.
     */
    private boolean tts = false;

    /**
     * The nonce of the message.
     */
    private String nonce = null;

    /**
     * The stream for the file.
     */
    private InputStream stream = null;

    /**
     * The name of the file.
     */
    private String fileName = null;

    /**
     * Creates a new message builder.
     */
    public MessageBuilder() { }

    /**
     * Creates a message builder from a message.
     *
     * @param message The message to copy.
     * @return A message builder which would produce the same text as the given message.
     */
    public static MessageBuilder fromMessage(Message message) {
        MessageBuilder builder = new MessageBuilder();
        builder.getStringBuilder().append(message.getContent());
        if (!message.getEmbeds().isEmpty()) {
            builder.setEmbed(message.getEmbeds().get(0).toBuilder());
        }
        return builder;
    }

    /**
     * Appends a sting with or without decoration to the message.
     *
     * @param message The string to append.
     * @param decorations The decorations of the string.
     * @return The current instance in order to chain call methods.
     */
    public MessageBuilder append(String message, MessageDecoration... decorations) {
        for (MessageDecoration decoration : decorations) {
            strBuilder.append(decoration.getPrefix());
        }
        strBuilder.append(message);
        for (int i = decorations.length - 1; i >= 0; i--) {
            strBuilder.append(decorations[i].getSuffix());
        }
        return this;
    }

    /**
     * Appends code to the message.
     *
     * @param language The language, e.g. "java".
     * @param code The code.
     * @return The current instance in order to chain call methods.
     */
    public MessageBuilder appendCode(String language, String code) {
        strBuilder
                .append("\n")
                .append(MessageDecoration.CODE_LONG.getPrefix())
                .append(language)
                .append("\n")
                .append(code)
                .append(MessageDecoration.CODE_LONG.getSuffix());
        return this;
    }

    /**
     * Appends a mentionable entity (usually a user or channel) to the message.
     *
     * @param entity The entity to mention.
     * @return The current instance in order to chain call methods.
     */
    public MessageBuilder appendMentionable(Mentionable entity) {
        strBuilder.append(entity.getMentionTag());
        return this;
    }

    /**
     * Appends a user to the message.
     *
     * @param user The user to mention.
     * @return The current instance in order to chain call methods.
     */
    public MessageBuilder appendUser(User user) {
        return appendMentionable(user);
    }

    /**
     * Appends a channel to the message.
     *
     * @param channel The channel to mention.
     * @return The current instance in order to chain call methods.
     */
    public MessageBuilder appendChannel(ServerTextChannel channel) {
        return appendMentionable(channel);
    }

    /**
     * Appends a new line to the message.
     *
     * @return The current instance in order to chain call methods.
     */
    public MessageBuilder appendNewLine() {
        strBuilder.append("\n");
        return this;
    }

    /**
     * Sets the embed of the message.
     *
     * @param embed The embed to set.
     * @return The current instance in order to chain call methods.
     */
    public MessageBuilder setEmbed(EmbedBuilder embed) {
        this.embed = embed;
        return this;
    }


    /**
     * Sets if the message should be text to speech.
     *
     * @param tts Whether the message should be text to speech or not.
     * @return The current instance in order to chain call methods.
     */
    public MessageBuilder setTts(boolean tts) {
        this.tts = tts;
        return this;
    }

    /**
     * Sets the file of the message.
     *
     * @param stream The stream of the file.
     * @param fileName The name of the file.
     * @return The current instance in order to chain call methods.
     */
    public MessageBuilder setFile(InputStream stream, String fileName) {
        this.stream = stream;
        this.fileName = fileName;
        return this;
    }

    /**
     * Sets the file of the message.
     *
     * @param file The file.
     * @return The current instance in order to chain call methods.
     */
    public MessageBuilder setFile(File file) {
        try {
            this.stream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("The provided file couldn't be found!");
        }
        this.fileName = file.getName();
        return this;
    }

    /**
     * Sets the nonce of the message.
     *
     * @param nonce The nonce to set.
     * @return The current instance in order to chain call methods.
     */
    public MessageBuilder setNonce(String nonce) {
        this.nonce = nonce;
        return this;
    }

    /**
     * Gets the {@link StringBuilder} which is used to build the message.
     *
     * @return The StringBuilder which is used to build the message.
     */
    public StringBuilder getStringBuilder() {
        return strBuilder;
    }

    /**
     * Sends the message.
     *
     * @param channel The channel to which the message should be sent.
     * @return The sent message.
     */
    public CompletableFuture<Message> send(TextChannel channel) {
        return channel.sendMessage(toString(), embed, tts, nonce, stream, fileName);
    }

    @Override
    public String toString() {
        return strBuilder.toString();
    }

}
