package org.javacord.api.entity.message.embed.internal;

import org.javacord.api.entity.Icon;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EditableEmbedField;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.embed.EmbedField;
import org.javacord.api.entity.user.User;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.time.Instant;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * This class is internally used by the {@link EmbedBuilder} to created embeds.
 * You usually don't want to interact with this object.
 */
public interface EmbedBuilderDelegate {

    /**
     * Sets the title of the embed.
     *
     * @param title The title of the embed.
     */
    void setTitle(String title);

    /**
     * Sets the description of the embed.
     *
     * @param description The description of the embed.
     */
    void setDescription(String description);

    /**
     * Sets the url of the embed.
     *
     * @param url The url of the embed.
     */
    void setUrl(String url);

    /**
     * Sets the current time as timestamp of the embed.
     */
    void setTimestampToNow();

    /**
     * Sets the timestamp of the embed.
     *
     * @param timestamp The timestamp to set.
     */
    void setTimestamp(Instant timestamp);

    /**
     * Sets the color of the embed.
     *
     * @param color The color of the embed.
     */
    void setColor(Color color);

    /**
     * Sets the footer of the embed.
     *
     * @param text The text of the footer.
     */
    void setFooter(String text);

    /**
     * Sets the footer of the embed.
     *
     * @param text The text of the footer.
     * @param iconUrl The url of the footer's icon.
     */
    void setFooter(String text, String iconUrl);

    /**
     * Sets the footer of the embed.
     *
     * @param text The text of the footer.
     * @param icon The footer's icon.
     */
    void setFooter(String text, Icon icon);

    /**
     * Sets the footer of the embed.
     *
     * @param text The text of the footer.
     * @param icon The footer's icon.
     */
    void setFooter(String text, File icon);

    /**
     * Sets the footer of the embed.
     * This method assumes the file type is "png"!
     *
     * @param text The text of the footer.
     * @param icon The footer's icon.
     */
    void setFooter(String text, InputStream icon);

    /**
     * Sets the footer of the embed.
     *
     * @param text The text of the footer.
     * @param icon The footer's icon.
     * @param fileType The type of the file, e.g. "png" or "gif".
     */
    void setFooter(String text, InputStream icon, String fileType);

    /**
     * Sets the footer of the embed.
     * This method assumes the file type is "png"!
     *
     * @param text The text of the footer.
     * @param icon The footer's icon.
     */
    void setFooter(String text, byte[] icon);

    /**
     * Sets the footer of the embed.
     *
     * @param text The text of the footer.
     * @param icon The footer's icon.
     * @param fileType The type of the file, e.g. "png" or "gif".
     */
    void setFooter(String text, byte[] icon, String fileType);

    /**
     * Sets the footer of the embed.
     * This method assumes the file type is "png"!
     *
     * @param text The text of the footer.
     * @param icon The footer's icon.
     */
    void setFooter(String text, BufferedImage icon);

    /**
     * Sets the footer of the embed.
     *
     * @param text The text of the footer.
     * @param icon The footer's icon.
     * @param fileType The type of the file, e.g. "png" or "gif".
     */
    void setFooter(String text, BufferedImage icon, String fileType);

    /**
     * Sets the image of the embed.
     *
     * @param url The url of the image.
     */
    void setImage(String url);

    /**
     * Sets the image of the embed.
     *
     * @param image The image.
     */
    void setImage(Icon image);

    /**
     * Sets the image of the embed.
     *
     * @param image The image.
     */
    void setImage(File image);

    /**
     * Sets the image of the embed.
     * This method assumes the file type is "png"!
     *
     * @param image The image.
     */
    void setImage(InputStream image);

    /**
     * Sets the image of the embed.
     *
     * @param image The image.
     * @param fileType The type of the file, e.g. "png" or "gif".
     */
    void setImage(InputStream image, String fileType);

    /**
     * Sets the image of the embed.
     * This method assumes the file type is "png"!
     *
     * @param image The image.
     */
    void setImage(byte[] image);

    /**
     * Sets the image of the embed.
     *
     * @param image The image.
     * @param fileType The type of the file, e.g. "png" or "gif".
     */
    void setImage(byte[] image, String fileType);

    /**
     * Sets the image of the embed.
     * This method assumes the file type is "png"!
     *
     * @param image The image.
     */
    void setImage(BufferedImage image);

    /**
     * Sets the image of the embed.
     *
     * @param image The image.
     * @param fileType The type of the file, e.g. "png" or "gif".
     */
    void setImage(BufferedImage image, String fileType);

    /**
     * Sets the author of the embed.
     *
     * @param author The message author which should be used as author.
     */
    void setAuthor(MessageAuthor author);

    /**
     * Sets the author of the embed.
     *
     * @param author The user which should be used as author.
     */
    void setAuthor(User author);

    /**
     * Sets the author of the embed.
     *
     * @param name The name of the author.
     */
    void setAuthor(String name);

    /**
     * Sets the author of the embed.
     *
     * @param name The name of the author.
     * @param url The url of the author.
     * @param iconUrl The url of the author's icon.
     */
    void setAuthor(String name, String url, String iconUrl);

    /**
     * Sets the author of the embed.
     *
     * @param name The name of the author.
     * @param url The url of the author.
     * @param icon The author's icon.
     */
    void setAuthor(String name, String url, Icon icon);

    /**
     * Sets the author of the embed.
     *
     * @param name The name of the author.
     * @param url The url of the author.
     * @param icon The author's icon.
     */
    void setAuthor(String name, String url, File icon);

    /**
     * Sets the author of the embed.
     * This method assumes the file type is "png"!
     *
     * @param name The name of the author.
     * @param url The url of the author.
     * @param icon The author's icon.
     */
    void setAuthor(String name, String url, InputStream icon);

    /**
     * Sets the author of the embed.
     *
     * @param name The name of the author.
     * @param url The url of the author.
     * @param icon The author's icon.
     * @param fileType The type of the file, e.g. "png" or "gif".
     */
    void setAuthor(String name, String url, InputStream icon, String fileType);

    /**
     * Sets the author of the embed.
     * This method assumes the file type is "png"!
     *
     * @param name The name of the author.
     * @param url The url of the author.
     * @param icon The author's icon.
     */
    void setAuthor(String name, String url, byte[] icon);

    /**
     * Sets the author of the embed.
     *
     * @param name The name of the author.
     * @param url The url of the author.
     * @param icon The author's icon.
     * @param fileType The type of the file, e.g. "png" or "gif".
     */
    void setAuthor(String name, String url, byte[] icon, String fileType);

    /**
     * Sets the author of the embed.
     * This method assumes the file type is "png"!
     *
     * @param name The name of the author.
     * @param url The url of the author.
     * @param icon The author's icon.
     */
    void setAuthor(String name, String url, BufferedImage icon);

    /**
     * Sets the author of the embed.
     *
     * @param name The name of the author.
     * @param url The url of the author.
     * @param icon The author's icon.
     * @param fileType The type of the file, e.g. "png" or "gif".
     */
    void setAuthor(String name, String url, BufferedImage icon, String fileType);

    /**
     * Sets the thumbnail of the embed.
     *
     * @param url The url of the thumbnail.
     */
    void setThumbnail(String url);

    /**
     * Sets the thumbnail of the embed.
     *
     * @param thumbnail The thumbnail.
     */
    void setThumbnail(Icon thumbnail);

    /**
     * Sets the thumbnail of the embed.
     *
     * @param thumbnail The thumbnail.
     */
    void setThumbnail(File thumbnail);

    /**
     * Sets the thumbnail of the embed.
     * This method assumes the file type is "png"!
     *
     * @param thumbnail The thumbnail.
     */
    void setThumbnail(InputStream thumbnail);

    /**
     * Sets the thumbnail of the embed.
     *
     * @param thumbnail The thumbnail.
     * @param fileType The type of the file, e.g. "png" or "gif".
     */
    void setThumbnail(InputStream thumbnail, String fileType);

    /**
     * Sets the thumbnail of the embed.
     * This method assumes the file type is "png"!
     *
     * @param thumbnail The thumbnail.
     */
    void setThumbnail(byte[] thumbnail);

    /**
     * Sets the thumbnail of the embed.
     *
     * @param thumbnail The thumbnail.
     * @param fileType The type of the file, e.g. "png" or "gif".
     */
    void setThumbnail(byte[] thumbnail, String fileType);

    /**
     * Sets the thumbnail of the embed.
     * This method assumes the file type is "png"!
     *
     * @param thumbnail The thumbnail.
     */
    void setThumbnail(BufferedImage thumbnail);

    /**
     * Sets the thumbnail of the embed.
     *
     * @param thumbnail The thumbnail.
     * @param fileType The type of the file, e.g. "png" or "gif".
     */
    void setThumbnail(BufferedImage thumbnail, String fileType);

    /**
     * Adds a field to the embed.
     *
     * @param name The name of the field.
     * @param value The value of the field.
     * @param inline Whether the field should be inline or not.
     */
    void addField(String name, String value, boolean inline);

    /**
     * Updates all fields of the embed that satisfy the given predicate using the given updater.
     *
     * @param predicate The predicate that fields have to satisfy to get updated.
     * @param updater The updater for the fields; the {@code EditableEmbedField} is only valid during the run of the
     *                updater; any try to save it in a variable and reuse it later after this method call will fail
     *                with exceptions.
     */
    void updateFields(Predicate<EmbedField> predicate, Consumer<EditableEmbedField> updater);

    /**
     * Removes all fields of the embed that satisfy the given predicate.
     *
     * @param predicate The predicate that fields have to satisfy to get removed.
     */
    void removeFields(Predicate<EmbedField> predicate);

    /**
     * Checks if this embed requires any attachments.
     *
     * @return Whether the embed requires attachments or not.
     */
    boolean requiresAttachments();

    /**
     * Clones all fields from an existing embed.
     *
     * @param embed An existing embed.
     */
    void fillAll(EmbedBuilder embed);
}