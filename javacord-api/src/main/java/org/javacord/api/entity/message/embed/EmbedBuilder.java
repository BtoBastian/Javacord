package org.javacord.api.entity.message.embed;

import org.javacord.api.entity.Icon;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.internal.EmbedBuilderDelegate;
import org.javacord.api.entity.user.User;
import org.javacord.api.util.internal.DelegateFactory;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.time.Instant;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * This class is used to create embeds.
 */
public class EmbedBuilder {

    /**
     * Default constructor.
     */
    public EmbedBuilder() {}

    /**
     * The embed delegate used by this instance.
     */
    private final EmbedBuilderDelegate delegate = DelegateFactory.createEmbedBuilderDelegate();

    /**
     * Gets the delegate used by this embed builder internally.
     *
     * @return The delegate used by this embed builder internally.
     */
    public EmbedBuilderDelegate getDelegate() {
        return delegate;
    }

    /**
     *  Creates an EmbedBuilder using fields in an existing embed.
     *
     * @param embed existing embed
     */
    public EmbedBuilder(EmbedBuilder embed) {
        delegate.fillAll(embed);
    }

    /**
     * Sets the title of the embed.
     *
     * @param title The title of the embed.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setTitle(String title) {
        delegate.setTitle(title);
        return this;
    }

    /**
     * Sets the description of the embed.
     *
     * @param description The description of the embed.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setDescription(String description) {
        delegate.setDescription(description);
        return this;
    }

    /**
     * Sets the url of the embed.
     *
     * @param url The url of the embed.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setUrl(String url) {
        delegate.setUrl(url);
        return this;
    }

    /**
     * Sets the current time as timestamp of the embed.
     *
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setTimestampToNow() {
        delegate.setTimestampToNow();
        return this;
    }

    /**
     * Sets the timestamp of the embed.
     *
     * @param timestamp The timestamp to set.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setTimestamp(Instant timestamp) {
        delegate.setTimestamp(timestamp);
        return this;
    }

    /**
     * Sets the color of the embed.
     *
     * @param color The color of the embed.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setColor(Color color) {
        delegate.setColor(color);
        return this;
    }

    /**
     * Sets the footer of the embed.
     *
     * @param text The text of the footer.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setFooter(String text) {
        delegate.setFooter(text);
        return this;
    }

    /**
     * Sets the footer of the embed.
     *
     * @param text The text of the footer.
     * @param iconUrl The url of the footer's icon.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setFooter(String text, String iconUrl) {
        delegate.setFooter(text, iconUrl);
        return this;
    }

    /**
     * Sets the footer of the embed.
     *
     * @param text The text of the footer.
     * @param icon The footer's icon.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setFooter(String text, Icon icon) {
        delegate.setFooter(text, icon);
        return this;
    }

    /**
     * Sets the footer of the embed.
     *
     * @param text The text of the footer.
     * @param icon The footer's icon.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setFooter(String text, File icon) {
        delegate.setFooter(text, icon);
        return this;
    }

    /**
     * Sets the footer of the embed.
     * This method assumes the file type is "png"!
     *
     * @param text The text of the footer.
     * @param icon The footer's icon.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setFooter(String text, InputStream icon) {
        delegate.setFooter(text, icon);
        return this;
    }

    /**
     * Sets the footer of the embed.
     *
     * @param text The text of the footer.
     * @param icon The footer's icon.
     * @param fileType The type of the file, e.g. "png" or "gif".
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setFooter(String text, InputStream icon, String fileType) {
        delegate.setFooter(text, icon, fileType);
        return this;
    }

    /**
     * Sets the footer of the embed.
     * This method assumes the file type is "png"!
     *
     * @param text The text of the footer.
     * @param icon The footer's icon.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setFooter(String text, byte[] icon) {
        delegate.setFooter(text, icon);
        return this;
    }

    /**
     * Sets the footer of the embed.
     *
     * @param text The text of the footer.
     * @param icon The footer's icon.
     * @param fileType The type of the file, e.g. "png" or "gif".
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setFooter(String text, byte[] icon, String fileType) {
        delegate.setFooter(text, icon, fileType);
        return this;
    }

    /**
     * Sets the footer of the embed.
     * This method assumes the file type is "png"!
     *
     * @param text The text of the footer.
     * @param icon The footer's icon.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setFooter(String text, BufferedImage icon) {
        delegate.setFooter(text, icon);
        return this;
    }

    /**
     * Sets the footer of the embed.
     *
     * @param text The text of the footer.
     * @param icon The footer's icon.
     * @param fileType The type of the file, e.g. "png" or "gif".
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setFooter(String text, BufferedImage icon, String fileType) {
        delegate.setFooter(text, icon, fileType);
        return this;
    }

    /**
     * Sets the image of the embed.
     *
     * @param url The url of the image.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setImage(String url) {
        delegate.setImage(url);
        return this;
    }

    /**
     * Sets the image of the embed.
     *
     * @param image The image.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setImage(Icon image) {
        delegate.setImage(image);
        return this;
    }

    /**
     * Sets the image of the embed.
     *
     * @param image The image.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setImage(File image) {
        delegate.setImage(image);
        return this;
    }

    /**
     * Sets the image of the embed.
     * This method assumes the file type is "png"!
     *
     * @param image The image.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setImage(InputStream image) {
        delegate.setImage(image);
        return this;
    }

    /**
     * Sets the image of the embed.
     *
     * @param image The image.
     * @param fileType The type of the file, e.g. "png" or "gif".
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setImage(InputStream image, String fileType) {
        delegate.setImage(image, fileType);
        return this;
    }

    /**
     * Sets the image of the embed.
     * This method assumes the file type is "png"!
     *
     * @param image The image.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setImage(byte[] image) {
        delegate.setImage(image);
        return this;
    }

    /**
     * Sets the image of the embed.
     *
     * @param image The image.
     * @param fileType The type of the file, e.g. "png" or "gif".
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setImage(byte[] image, String fileType) {
        delegate.setImage(image, fileType);
        return this;
    }

    /**
     * Sets the image of the embed.
     * This method assumes the file type is "png"!
     *
     * @param image The image.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setImage(BufferedImage image) {
        delegate.setImage(image);
        return this;
    }

    /**
     * Sets the image of the embed.
     *
     * @param image The image.
     * @param fileType The type of the file, e.g. "png" or "gif".
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setImage(BufferedImage image, String fileType) {
        delegate.setImage(image, fileType);
        return this;
    }

    /**
     * Sets the author of the embed.
     *
     * @param author The message author which should be used as author.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setAuthor(MessageAuthor author) {
        delegate.setAuthor(author);
        return this;
    }

    /**
     * Sets the author of the embed.
     *
     * @param author The user which should be used as author.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setAuthor(User author) {
        delegate.setAuthor(author);
        return this;
    }

    /**
     * Sets the author of the embed.
     *
     * @param name The name of the author.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setAuthor(String name) {
        delegate.setAuthor(name);
        return this;
    }

    /**
     * Sets the author of the embed.
     *
     * @param name The name of the author.
     * @param url The url of the author.
     * @param iconUrl The url of the author's icon.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setAuthor(String name, String url, String iconUrl) {
        delegate.setAuthor(name, url, iconUrl);
        return this;
    }

    /**
     * Sets the author of the embed.
     *
     * @param name The name of the author.
     * @param url The url of the author.
     * @param icon The author's icon.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setAuthor(String name, String url, Icon icon) {
        delegate.setAuthor(name, url, icon);
        return this;
    }

    /**
     * Sets the author of the embed.
     *
     * @param name The name of the author.
     * @param url The url of the author.
     * @param icon The author's icon.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setAuthor(String name, String url, File icon) {
        delegate.setAuthor(name, url, icon);
        return this;
    }

    /**
     * Sets the author of the embed.
     * This method assumes the file type is "png"!
     *
     * @param name The name of the author.
     * @param url The url of the author.
     * @param icon The author's icon.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setAuthor(String name, String url, InputStream icon) {
        delegate.setAuthor(name, url, icon);
        return this;
    }

    /**
     * Sets the author of the embed.
     *
     * @param name The name of the author.
     * @param url The url of the author.
     * @param icon The author's icon.
     * @param fileType The type of the file, e.g. "png" or "gif".
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setAuthor(String name, String url, InputStream icon, String fileType) {
        delegate.setAuthor(name, url, icon, fileType);
        return this;
    }

    /**
     * Sets the author of the embed.
     * This method assumes the file type is "png"!
     *
     * @param name The name of the author.
     * @param url The url of the author.
     * @param icon The author's icon.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setAuthor(String name, String url, byte[] icon) {
        delegate.setAuthor(name, url, icon);
        return this;
    }

    /**
     * Sets the author of the embed.
     *
     * @param name The name of the author.
     * @param url The url of the author.
     * @param icon The author's icon.
     * @param fileType The type of the file, e.g. "png" or "gif".
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setAuthor(String name, String url, byte[] icon, String fileType) {
        delegate.setAuthor(name, url, icon, fileType);
        return this;
    }

    /**
     * Sets the author of the embed.
     * This method assumes the file type is "png"!
     *
     * @param name The name of the author.
     * @param url The url of the author.
     * @param icon The author's icon.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setAuthor(String name, String url, BufferedImage icon) {
        delegate.setAuthor(name, url, icon);
        return this;
    }

    /**
     * Sets the author of the embed.
     *
     * @param name The name of the author.
     * @param url The url of the author.
     * @param icon The author's icon.
     * @param fileType The type of the file, e.g. "png" or "gif".
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setAuthor(String name, String url, BufferedImage icon, String fileType) {
        delegate.setAuthor(name, url, icon, fileType);
        return this;
    }

    /**
     * Sets the thumbnail of the embed.
     *
     * @param url The url of the thumbnail.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setThumbnail(String url) {
        delegate.setThumbnail(url);
        return this;
    }

    /**
     * Sets the thumbnail of the embed.
     *
     * @param thumbnail The thumbnail.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setThumbnail(Icon thumbnail) {
        delegate.setThumbnail(thumbnail);
        return this;
    }

    /**
     * Sets the thumbnail of the embed.
     *
     * @param thumbnail The thumbnail.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setThumbnail(File thumbnail) {
        delegate.setThumbnail(thumbnail);
        return this;
    }

    /**
     * Sets the thumbnail of the embed.
     * This method assumes the file type is "png"!
     *
     * @param thumbnail The thumbnail.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setThumbnail(InputStream thumbnail) {
        delegate.setThumbnail(thumbnail);
        return this;
    }

    /**
     * Sets the thumbnail of the embed.
     *
     * @param thumbnail The thumbnail.
     * @param fileType The type of the file, e.g. "png" or "gif".
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setThumbnail(InputStream thumbnail, String fileType) {
        delegate.setThumbnail(thumbnail, fileType);
        return this;
    }

    /**
     * Sets the thumbnail of the embed.
     * This method assumes the file type is "png"!
     *
     * @param thumbnail The thumbnail.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setThumbnail(byte[] thumbnail) {
        delegate.setThumbnail(thumbnail);
        return this;
    }

    /**
     * Sets the thumbnail of the embed.
     *
     * @param thumbnail The thumbnail.
     * @param fileType The type of the file, e.g. "png" or "gif".
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setThumbnail(byte[] thumbnail, String fileType) {
        delegate.setThumbnail(thumbnail, fileType);
        return this;
    }

    /**
     * Sets the thumbnail of the embed.
     * This method assumes the file type is "png"!
     *
     * @param thumbnail The thumbnail.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setThumbnail(BufferedImage thumbnail) {
        delegate.setThumbnail(thumbnail);
        return this;
    }

    /**
     * Sets the thumbnail of the embed.
     *
     * @param thumbnail The thumbnail.
     * @param fileType The type of the file, e.g. "png" or "gif".
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder setThumbnail(BufferedImage thumbnail, String fileType) {
        delegate.setThumbnail(thumbnail, fileType);
        return this;
    }

    /**
     * Adds an inline field to the embed.
     *
     * @param name The name of the field.
     * @param value The value of the field.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder addInlineField(String name, String value) {
        delegate.addField(name, value, true);
        return this;
    }

    /**
     * Adds a non-inline field to the embed.
     *
     * @param name The name of the field.
     * @param value The value of the field.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder addField(String name, String value) {
        delegate.addField(name, value, false);
        return this;
    }

    /**
     * Adds a field to the embed.
     *
     * @param name The name of the field.
     * @param value The value of the field.
     * @param inline Whether the field should be inline or not.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder addField(String name, String value, boolean inline) {
        delegate.addField(name, value, inline);
        return this;
    }

    /**
     * Updates all fields of the embed that satisfy the given predicate using the given updater.
     *
     * @param predicate The predicate that fields have to satisfy to get updated.
     * @param updater The updater for the fields; the {@code EditableEmbedField} is only valid during the run of the
     *                updater; any try to save it in a variable and reuse it later after this method call will fail
     *                with exceptions.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder updateFields(Predicate<EmbedField> predicate, Consumer<EditableEmbedField> updater) {
        delegate.updateFields(predicate, updater);
        return this;
    }

    /**
     * Updates all fields of the embed using the given updater.
     *
     * @param updater The updater for the fields; the {@code EditableEmbedField} is only valid during the run of the
     *                updater; any try to save it in a variable and reuse it later after this method call will fail
     *                with exceptions.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder updateAllFields(Consumer<EditableEmbedField> updater) {
        delegate.updateFields(field -> true, updater);
        return this;
    }

    /**
     * Removes all fields of the embed that satisfy the given predicate.
     *
     * @param predicate The predicate that fields have to satisfy to get removed.
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder removeFields(Predicate<EmbedField> predicate) {
        delegate.removeFields(predicate);
        return this;
    }

    /**
     * Removes all fields of the embed.
     *
     * @return The current instance in order to chain call methods.
     */
    public EmbedBuilder removeAllFields() {
        delegate.removeFields(field -> true);
        return this;
    }

    /**
     * Checks if this embed requires any attachments.
     *
     * @return Whether the embed requires attachments or not.
     */
    public boolean requiresAttachments() {
        return delegate.requiresAttachments();
    }
}
