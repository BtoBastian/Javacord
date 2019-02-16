package org.javacord.core.entity.message.embed;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.message.embed.BaseEmbedAuthor;
import org.javacord.api.entity.message.embed.BaseEmbedField;
import org.javacord.api.entity.message.embed.BaseEmbedFooter;
import org.javacord.api.entity.message.embed.BaseEmbedImage;
import org.javacord.api.entity.message.embed.BaseEmbedMember;
import org.javacord.api.entity.message.embed.BaseEmbedThumbnail;
import org.javacord.api.entity.message.embed.draft.EmbedDraft;
import org.javacord.api.entity.message.embed.draft.EmbedDraftAuthor;
import org.javacord.api.entity.message.embed.draft.EmbedDraftFooter;
import org.javacord.api.entity.message.embed.draft.EmbedDraftImage;
import org.javacord.api.entity.message.embed.draft.EmbedDraftThumbnail;
import org.javacord.api.entity.message.embed.internal.EmbedBuilderDelegate;
import org.javacord.core.entity.message.embed.draft.EmbedDraftImpl;
import org.javacord.core.util.FileContainer;

/**
 * The implementation of {@link EmbedBuilderDelegate}.
 */
public class EmbedBuilderDelegateImpl implements EmbedBuilderDelegate {

    // Fields
    protected final List<BaseEmbedField> fields;
    // General embed stuff
    protected String title = null;
    protected String description = null;
    protected String url = null;
    protected Instant timestamp = null;
    protected Color color = null;
    // Author
    protected BaseEmbedMember<?, ? extends EmbedDraftAuthor, ?> author = null;
    protected String authorName = null;
    protected String authorUrl = null;
    protected String authorIconUrl = null;
    protected FileContainer authorIconContainer = null;
    // Thumbnail
    protected BaseEmbedMember<?, ? extends EmbedDraftThumbnail, ?> thumbnail = null;
    protected String thumbnailUrl = null;
    protected FileContainer thumbnailContainer = null;
    // Image
    protected BaseEmbedMember<?, ? extends EmbedDraftImage, ?> image = null;
    protected String imageUrl = null;
    protected FileContainer imageContainer = null;
    // Footer
    protected BaseEmbedMember<?, ? extends EmbedDraftFooter, ?> footer = null;
    protected String footerText = null;
    protected String footerIconUrl = null;
    protected FileContainer footerIconContainer = null;

    public EmbedBuilderDelegateImpl() {
        fields = new ArrayList<>();
    }

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
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public void setAuthor(String name, String url, String iconUrl) {
        this.authorName = name;
        this.authorUrl = url;
        this.authorIconUrl = iconUrl;
    }

    @Override
    public void setAuthor(String name, String url, Object icon, String fileType) {
        setAuthor(name, url, null);
        this.authorIconContainer = createFileContainer(icon, fileType);
    }

    @Override
    public <T extends BaseEmbedAuthor & BaseEmbedMember<?, ? extends EmbedDraftAuthor, ?>> void setAuthor(T author) {
        this.author = author;

        this.authorName = null;
        this.authorUrl = null;
        this.authorIconUrl = null;
        this.authorIconContainer = null;
    }

    @Override
    public void setThumbnail(String url) {
        this.thumbnailUrl = url;
    }

    @Override
    public void setThumbnail(Object image, String fileType) {
        this.thumbnailContainer = createFileContainer(image, fileType);
    }

    @Override
    public <T extends BaseEmbedThumbnail & BaseEmbedMember<?, ? extends EmbedDraftThumbnail, ?>> void setThumbnail(
            T thumbnail
    ) {
        this.thumbnail = thumbnail;

        this.thumbnailUrl = null;
        this.thumbnailContainer = null;
    }

    @Override
    public void setImage(String url) {
        this.imageUrl = url;
    }

    @Override
    public void setImage(Object image, String fileType) {
        this.imageContainer = createFileContainer(image, fileType);
    }

    @Override
    public <T extends BaseEmbedImage & BaseEmbedMember<?, ? extends EmbedDraftImage, ?>> void setImage(T image) {
        this.image = image;

        this.imageUrl = null;
        this.imageContainer = null;
    }

    @Override
    public void setFooter(String text, String iconUrl) {
        this.footerText = text;
        this.footerIconUrl = iconUrl;
    }

    @Override
    public void setFooter(String text, Object icon, String fileType) {
        setFooter(text, null);
        this.footerIconContainer = createFileContainer(icon, fileType);
    }

    @Override
    public <T extends BaseEmbedFooter & BaseEmbedMember<?, ? extends EmbedDraftFooter, ?>> void setFooter(T footer) {
        this.footer = footer;

        this.footerText = null;
        this.footerIconUrl = null;
        this.footerIconContainer = null;
    }

    @Override
    public void addField(String name, String value, boolean inline) {
        fields.add(new PreliminaryField(name, value, inline));
    }

    @Override
    public void addField(BaseEmbedField field) {
        fields.add(field);
    }

    @Override
    public EmbedDraft build() {
        return new EmbedDraftImpl(
                title,
                description,
                url,
                timestamp,
                color,

                author, authorName, authorUrl, authorIconUrl, authorIconContainer,
                thumbnail, thumbnailUrl, thumbnailContainer,
                image, imageUrl, imageContainer,
                footer, footerText, footerIconUrl, footerIconContainer,
                fields
        );
    }

    private FileContainer createFileContainer(Object image, String fileType) {
        if (image == null) {
            return null;
        }

        FileContainer container;

        if (image instanceof Icon) {
            container = new FileContainer((Icon) image);
        } else if (image instanceof File) {
            container = new FileContainer((File) image);
        } else if (image instanceof InputStream) {
            container = new FileContainer((InputStream) image, fileType);
        } else if (image instanceof BufferedImage) {
            container = new FileContainer((BufferedImage) image, fileType);
        } else if (image.getClass() == byte[].class) {
            container = new FileContainer((byte[]) image, fileType);
        } else {
            throw new AssertionError();
        }

        container.setFileTypeOrName(UUID.randomUUID().toString() + "." + fileType);

        return container;
    }

    private class PreliminaryField implements BaseEmbedField {
        private final String name;
        private final String value;
        private final boolean inline;

        private PreliminaryField(String name, String value, boolean inline) {
            this.name = name;
            this.value = value;
            this.inline = inline;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public boolean isInline() {
            return inline;
        }

        @Override
        public String getName() {
            return name;
        }
    }

}
