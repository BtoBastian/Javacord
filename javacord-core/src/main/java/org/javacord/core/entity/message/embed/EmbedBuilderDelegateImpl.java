package org.javacord.core.entity.message.embed;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EditableEmbedField;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.embed.EmbedField;
import org.javacord.api.entity.message.embed.internal.EmbedBuilderDelegate;
import org.javacord.api.entity.user.User;
import org.javacord.core.util.FileContainer;
import org.javacord.core.util.io.FileUtils;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * The implementation of {@link EmbedBuilderDelegate}.
 */
public class EmbedBuilderDelegateImpl implements EmbedBuilderDelegate {

    // General embed stuff
    private String title = null;
    private String description = null;
    private String url = null;
    private Instant timestamp = null;
    private Color color = null;

    // Footer
    private String footerText = null;
    private String footerIconUrl = null;
    private FileContainer footerIconContainer = null;

    // Image
    private String imageUrl = null;
    private FileContainer imageContainer = null;

    // Author
    private String authorName = null;
    private String authorUrl = null;
    private String authorIconUrl = null;
    private FileContainer authorIconContainer = null;

    // Thumbnail
    private String thumbnailUrl = null;
    private FileContainer thumbnailContainer = null;

    // Fields
    private final List<EmbedFieldImpl> fields = new ArrayList<>();

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void setTimestampToNow() {
        this.timestamp = Instant.now();
    }

    @Override
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public void setFooter(String text) {
        footerText = text;
        footerIconUrl = null;
        footerIconContainer = null;
    }

    @Override
    public void setFooter(String text, String iconUrl) {
        footerText = text;
        footerIconUrl = iconUrl;
        footerIconContainer = null;
    }

    @Override
    public void setFooter(String text, Icon icon) {
        footerText = text;
        footerIconUrl = icon.getUrl().toString();
        footerIconContainer = null;
    }

    @Override
    public void setFooter(String text, File icon) {
        footerText = text;
        footerIconUrl = null;
        if (icon == null) {
            footerIconContainer = null;
        } else {
            footerIconContainer = new FileContainer(icon);
            footerIconContainer.setFileTypeOrName(UUID.randomUUID().toString() + "." + FileUtils.getExtension(icon));
        }
    }

    @Override
    public void setFooter(String text, InputStream icon) {
        setFooter(text, icon, "png");
    }

    @Override
    public void setFooter(String text, InputStream icon, String fileType) {
        footerText = text;
        footerIconUrl = null;
        if (icon == null) {
            footerIconContainer = null;
        } else {
            footerIconContainer = new FileContainer(icon, fileType);
            footerIconContainer.setFileTypeOrName(UUID.randomUUID().toString() + "." + fileType);
        }
    }

    @Override
    public void setFooter(String text, byte[] icon) {
        setFooter(text, icon, "png");
    }

    @Override
    public void setFooter(String text, byte[] icon, String fileType) {
        footerText = text;
        footerIconUrl = null;
        if (icon == null) {
            footerIconContainer = null;
        } else {
            footerIconContainer = new FileContainer(icon, fileType);
            footerIconContainer.setFileTypeOrName(UUID.randomUUID().toString() + "." + fileType);
        }
    }

    @Override
    public void setFooter(String text, BufferedImage icon) {
        setFooter(text, icon, "png");
    }

    @Override
    public void setFooter(String text, BufferedImage icon, String fileType) {
        footerText = text;
        footerIconUrl = null;
        if (icon == null) {
            footerIconContainer = null;
        } else {
            footerIconContainer = new FileContainer(icon, fileType);
            footerIconContainer.setFileTypeOrName(UUID.randomUUID().toString() + "." + fileType);
        }
    }

    @Override
    public void setImage(String url) {
        imageUrl = url;
        imageContainer = null;
    }

    @Override
    public void setImage(Icon image) {
        imageUrl = image.getUrl().toString();
        imageContainer = null;
    }

    @Override
    public void setImage(File image) {
        imageUrl = null;
        if (image == null) {
            imageContainer = null;
        } else {
            imageContainer = new FileContainer(image);
            imageContainer.setFileTypeOrName(UUID.randomUUID().toString() + "." + FileUtils.getExtension(image));
        }
    }

    @Override
    public void setImage(InputStream image) {
        setImage(image, "png");
    }

    @Override
    public void setImage(InputStream image, String fileType) {
        imageUrl = null;
        if (image == null) {
            imageContainer = null;
        } else {
            imageContainer = new FileContainer(image, fileType);
            imageContainer.setFileTypeOrName(UUID.randomUUID().toString() + "." + fileType);
        }
    }

    @Override
    public void setImage(byte[] image) {
        setImage(image, "png");
    }

    @Override
    public void setImage(byte[] image, String fileType) {
        imageUrl = null;
        if (image == null) {
            imageContainer = null;
        } else {
            imageContainer = new FileContainer(image, fileType);
            imageContainer.setFileTypeOrName(UUID.randomUUID().toString() + "." + fileType);
        }
    }

    @Override
    public void setImage(BufferedImage image) {
        setImage(image, "png");
    }

    @Override
    public void setImage(BufferedImage image, String fileType) {
        imageUrl = null;
        if (image == null) {
            imageContainer = null;
        } else {
            imageContainer = new FileContainer(image, fileType);
            imageContainer.setFileTypeOrName(UUID.randomUUID().toString() + "." + fileType);
        }
    }

    @Override
    public void setAuthor(MessageAuthor author) {
        authorName = author.getDisplayName();
        authorUrl = null;
        authorIconUrl = author.getAvatar().getUrl().toString();
        authorIconContainer = null;
    }

    @Override
    public void setAuthor(User author) {
        authorName = author.getName();
        authorUrl = null;
        authorIconUrl = author.getAvatar().getUrl().toString();
        authorIconContainer = null;
    }

    @Override
    public void setAuthor(String name) {
        authorName = name;
        authorUrl = null;
        authorIconUrl = null;
        authorIconContainer = null;
    }

    @Override
    public void setAuthor(String name, String url, String iconUrl) {
        authorName = name;
        authorUrl = url;
        authorIconUrl = iconUrl;
        authorIconContainer = null;
    }

    @Override
    public void setAuthor(String name, String url, Icon icon) {
        authorName = name;
        authorUrl = url;
        authorIconUrl = icon.getUrl().toString();
        authorIconContainer = null;
    }

    @Override
    public void setAuthor(String name, String url, File icon) {
        authorName = name;
        authorUrl = url;
        authorIconUrl = null;
        if (icon == null) {
            authorIconContainer = null;
        } else {
            authorIconContainer = new FileContainer(icon);
            authorIconContainer.setFileTypeOrName(UUID.randomUUID().toString() + "." + FileUtils.getExtension(icon));
        }
    }

    @Override
    public void setAuthor(String name, String url, InputStream icon) {
        setAuthor(name, url, icon, "png");
    }

    @Override
    public void setAuthor(String name, String url, InputStream icon, String fileType) {
        authorName = name;
        authorUrl = url;
        authorIconUrl = null;
        if (icon == null) {
            authorIconContainer = null;
        } else {
            authorIconContainer = new FileContainer(icon, fileType);
            authorIconContainer.setFileTypeOrName(UUID.randomUUID().toString() + "." + fileType);
        }
    }

    @Override
    public void setAuthor(String name, String url, byte[] icon) {
        setAuthor(name, url, icon, "png");
    }

    @Override
    public void setAuthor(String name, String url, byte[] icon, String fileType) {
        authorName = name;
        authorUrl = url;
        authorIconUrl = null;
        if (icon == null) {
            authorIconContainer = null;
        } else {
            authorIconContainer = new FileContainer(icon, fileType);
            authorIconContainer.setFileTypeOrName(UUID.randomUUID().toString() + "." + fileType);
        }
    }

    @Override
    public void setAuthor(String name, String url, BufferedImage icon) {
        setAuthor(name, url, icon, "png");
    }

    @Override
    public void setAuthor(String name, String url, BufferedImage icon, String fileType) {
        authorName = name;
        authorUrl = url;
        authorIconUrl = null;
        if (icon == null) {
            authorIconContainer = null;
        } else {
            authorIconContainer = new FileContainer(icon, fileType);
            authorIconContainer.setFileTypeOrName(UUID.randomUUID().toString() + "." + fileType);
        }
    }

    @Override
    public void setThumbnail(String url) {
        thumbnailUrl = url;
        thumbnailContainer = null;
    }

    @Override
    public void setThumbnail(Icon thumbnail) {
        thumbnailUrl = thumbnail.getUrl().toString();
        thumbnailContainer = null;
    }

    @Override
    public void setThumbnail(File thumbnail) {
        thumbnailUrl = null;
        if (thumbnail == null) {
            thumbnailContainer = null;
        } else {
            thumbnailContainer = new FileContainer(thumbnail);
            thumbnailContainer.setFileTypeOrName(
                    UUID.randomUUID().toString() + "." + FileUtils.getExtension(thumbnail));
        }
    }

    @Override
    public void setThumbnail(InputStream thumbnail) {
        setThumbnail(thumbnail, "png");
    }

    @Override
    public void setThumbnail(InputStream thumbnail, String fileType) {
        thumbnailUrl = null;
        if (thumbnail == null) {
            thumbnailContainer = null;
        } else {
            thumbnailContainer = new FileContainer(thumbnail, fileType);
            thumbnailContainer.setFileTypeOrName(UUID.randomUUID().toString() + "." + fileType);
        }
    }

    @Override
    public void setThumbnail(byte[] thumbnail) {
        setThumbnail(thumbnail, "png");
    }

    @Override
    public void setThumbnail(byte[] thumbnail, String fileType) {
        thumbnailUrl = null;
        if (thumbnail == null) {
            thumbnailContainer = null;
        } else {
            thumbnailContainer = new FileContainer(thumbnail, fileType);
            thumbnailContainer.setFileTypeOrName(UUID.randomUUID().toString() + "." + fileType);
        }
    }

    @Override
    public void setThumbnail(BufferedImage thumbnail) {
        setThumbnail(thumbnail, "png");
    }

    @Override
    public void setThumbnail(BufferedImage thumbnail, String fileType) {
        thumbnailUrl = null;
        if (thumbnail == null) {
            thumbnailContainer = null;
        } else {
            thumbnailContainer = new FileContainer(thumbnail, fileType);
            thumbnailContainer.setFileTypeOrName(UUID.randomUUID().toString() + "." + fileType);
        }
    }

    @Override
    public void addField(String name, String value, boolean inline) {
        fields.add(new EmbedFieldImpl(name, value, inline));
    }

    @Override
    public void updateFields(Predicate<EmbedField> predicate, Consumer<EditableEmbedField> updater) {
        fields.stream()
                .filter(predicate)
                .map(EditableEmbedFieldImpl::new)
                .forEach(updater.andThen(field -> ((EditableEmbedFieldImpl) field).clearDelegate()));
    }

    @Override
    public void removeFields(Predicate<EmbedField> predicate) {
        fields.removeIf(predicate);
    }

    @Override
    public boolean requiresAttachments() {
        return footerIconContainer != null
                || imageContainer != null
                || authorIconContainer != null
                || thumbnailContainer != null;
    }


    /**
     * Gets the required attachments for this embed.
     *
     * @return The required attachments for this embed.
     */
    public List<FileContainer> getRequiredAttachments() {
        List<FileContainer> requiredAttachments = new ArrayList<>();
        if (footerIconContainer != null) {
            requiredAttachments.add(footerIconContainer);
        }
        if (imageContainer != null) {
            requiredAttachments.add(imageContainer);
        }
        if (authorIconContainer != null) {
            requiredAttachments.add(authorIconContainer);
        }
        if (thumbnailContainer != null) {
            requiredAttachments.add(thumbnailContainer);
        }
        return requiredAttachments;
    }

    /**
     * Gets the embed as a {@link ObjectNode}. This is what is sent to Discord.
     *
     * @return The embed as a ObjectNode.
     */
    public ObjectNode toJsonNode() {
        ObjectNode object = JsonNodeFactory.instance.objectNode();
        return toJsonNode(object);
    }

    /**
     * Adds the json data to the given object node.
     *
     * @param object The object, the data should be added to.
     * @return The provided object with the data of the embed.
     */
    public ObjectNode toJsonNode(ObjectNode object) {
        object.put("type", "rich");
        if (title != null && !title.equals("")) {
            object.put("title", title);
        }
        if (description != null && !description.equals("")) {
            object.put("description", description);
        }
        if (url != null && !url.equals("")) {
            object.put("url", url);
        }
        if (color != null) {
            object.put("color", color.getRGB() & 0xFFFFFF);
        }
        if (timestamp != null) {
            object.put("timestamp", DateTimeFormatter.ISO_INSTANT.format(timestamp));
        }
        if ((footerText != null && !footerText.equals("")) || (footerIconUrl != null && !footerIconUrl.equals(""))) {
            ObjectNode footer = object.putObject("footer");
            if (footerText != null && !footerText.equals("")) {
                footer.put("text", footerText);
            }
            if (footerIconUrl != null && !footerIconUrl.equals("")) {
                footer.put("icon_url", footerIconUrl);
            }
            if (footerIconContainer != null) {
                footer.put("icon_url", "attachment://" + footerIconContainer.getFileTypeOrName());
            }
        }
        if (imageUrl != null && !imageUrl.equals("")) {
            object.putObject("image").put("url", imageUrl);
        }
        if (imageContainer != null) {
            object.putObject("image").put("url", "attachment://" + imageContainer.getFileTypeOrName());
        }
        if (authorName != null && !authorName.equals("")) {
            ObjectNode author = object.putObject("author");
            author.put("name", authorName);
            if (authorUrl != null && !authorUrl.equals("")) {
                author.put("url", authorUrl);
            }
            if (authorIconUrl != null && !authorIconUrl.equals("")) {
                author.put("icon_url", authorIconUrl);
            }
            if (authorIconContainer != null) {
                author.put("icon_url", "attachment://" + authorIconContainer.getFileTypeOrName());
            }
        }
        if (thumbnailUrl != null && !thumbnailUrl.equals("")) {
            object.putObject("thumbnail").put("url", thumbnailUrl);
        }
        if (thumbnailContainer != null) {
            object.putObject("thumbnail").put("url", "attachment://" + thumbnailContainer.getFileTypeOrName());
        }
        if (fields.size() > 0) {
            ArrayNode jsonFields = object.putArray("fields");
            for (EmbedField field : fields) {
                ObjectNode jsonField = jsonFields.addObject();
                jsonField.put("name", field.getName());
                jsonField.put("value", field.getValue());
                jsonField.put("inline", field.isInline());
            }
        }
        return object;
    }

    @Override
    public void fillAll(EmbedBuilder embed) {
        Field[] fields = getClass().getDeclaredFields();

        try {
            for (int i = -1; ++i < fields.length;) {
                fields[i].setAccessible(true);
                internalFiller(fields[i], fields[i].get(embed.getDelegate()));
            }
        }  catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private <T> void internalFiller(Field field, T object) throws IllegalAccessException, NoSuchFieldException {
        if (object instanceof List<?>) {
            fields.addAll((Collection<? extends EmbedFieldImpl>) object);
        } else {
            getClass().getDeclaredField(field.getName()).set(this, object);
        }
    }
}
