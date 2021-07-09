package org.javacord.core.entity.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.ActionRowBuilder;
import org.javacord.api.entity.message.component.ComponentType;
import org.javacord.api.entity.message.component.HighLevelComponent;
import org.javacord.api.entity.message.component.LowLevelComponent;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.internal.MessageBuilderDelegate;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.webhook.IncomingWebhook;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.message.component.ActionRowImpl;
import org.javacord.core.entity.message.embed.EmbedBuilderDelegateImpl;
import org.javacord.core.entity.message.mention.AllowedMentionsImpl;
import org.javacord.core.entity.user.Member;
import org.javacord.core.util.FileContainer;
import org.javacord.core.util.logging.LoggerUtil;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The implementation of {@link MessageBuilderDelegate}.
 */
public class MessageBuilderDelegateImpl implements MessageBuilderDelegate {

    /**
     * The logger of this class.
     */
    private static final Logger logger = LoggerUtil.getLogger(MessageBuilderDelegateImpl.class);

    /**
     * The string builder used to create the message.
     */
    protected final StringBuilder strBuilder = new StringBuilder();

    /**
     * The list of embeds of the message.
     */
    protected List<EmbedBuilder> embeds = new ArrayList<>();

    /**
     * If the message should be text to speech or not.
     */
    protected boolean tts = false;

    /**
     * The nonce of the message.
     */
    protected String nonce = null;

    /**
     * A list with all attachments which should be added to the message.
     */
    protected final List<FileContainer> attachments = new ArrayList<>();

    /**
     * A list with all the components which should be added to the message.
     */
    protected final List<HighLevelComponent> components = new ArrayList<>();

    /**
     * The MentionsBuilder used to control mention behavior.
     */
    protected AllowedMentions allowedMentions = null;

    /**
     * The message to reply to.
     */
    protected Long replyingTo = null;


    @Override
    public void addComponents(HighLevelComponent... highLevelComponents) {
        this.components.addAll(Arrays.asList(highLevelComponents));
    }

    @Override
    public void addActionRow(LowLevelComponent... lowLevelComponents) {
        this.addComponents(ActionRow.of(lowLevelComponents));
    }

    @Override
    public void appendCode(String language, String code) {
        strBuilder
            .append("\n")
            .append(MessageDecoration.CODE_LONG.getPrefix())
            .append(language)
            .append("\n")
            .append(code)
            .append(MessageDecoration.CODE_LONG.getSuffix());
    }

    @Override
    public void append(String message, MessageDecoration... decorations) {
        for (MessageDecoration decoration : decorations) {
            strBuilder.append(decoration.getPrefix());
        }
        strBuilder.append(message);
        for (int i = decorations.length - 1; i >= 0; i--) {
            strBuilder.append(decorations[i].getSuffix());
        }
    }

    @Override
    public void append(Mentionable entity) {
        strBuilder.append(entity.getMentionTag());
    }

    @Override
    public void append(Object object) {
        strBuilder.append(object);
    }

    @Override
    public void appendNewLine() {
        strBuilder.append("\n");
    }

    @Override
    public void setContent(String content) {
        strBuilder.setLength(0);
        strBuilder.append(content);
    }

    @Override
    public void addEmbed(EmbedBuilder embed) {
        if (embed != null) {
            embeds.add(embed);
        }
    }

    /**
     * Fill the builder's values with a message.
     *
     * @param message The message to copy.
     */
    @Override
    public void copy(Message message) {
        this.getStringBuilder().append(message.getContent());

        if (!message.getEmbeds().isEmpty()) {
            this.addEmbed(message.getEmbeds().get(0).toBuilder());
        }

        for (MessageAttachment attachment : message.getAttachments()) {
            // Since spoiler status is encoded in the file name, it is copied automatically.
            this.addAttachment(attachment.getUrl());
        }

        for (HighLevelComponent component : message.getComponents()) {
            if (component.getType() == ComponentType.ACTION_ROW) {
                ActionRowBuilder builder = new ActionRowBuilder();
                builder.copy((ActionRow) component);
                this.addComponents(builder.build());
            }
        }
    }

    @Override
    public void removeAllEmbeds() {
        embeds.clear();
    }

    @Override
    public void addEmbeds(EmbedBuilder... embeds) {
        this.embeds.addAll(Arrays.asList(embeds));
    }

    @Override
    public void removeEmbed(EmbedBuilder embed) {
        this.embeds.remove(embed);
    }

    @Override
    public void removeEmbeds(EmbedBuilder... embeds) {
        this.embeds.removeAll(Arrays.asList(embeds));
    }

    @Override
    public void removeComponent(int index) {
        components.remove(index);
    }

    @Override
    public void removeComponent(HighLevelComponent component) {
        components.remove(component);
    }

    @Override
    public void removeAllComponents() {
        components.clear();
    }

    @Override
    public void setTts(boolean tts) {
        this.tts = tts;
    }

    @Override
    public void addFile(BufferedImage image, String fileName) {
        addAttachment(image, fileName);
    }

    @Override
    public void addFile(File file) {
        addAttachment(file);
    }

    @Override
    public void addFile(Icon icon) {
        addAttachment(icon);
    }

    @Override
    public void addFile(URL url) {
        addAttachment(url);
    }

    @Override
    public void addFile(byte[] bytes, String fileName) {
        addAttachment(bytes, fileName);
    }

    @Override
    public void addFile(InputStream stream, String fileName) {
        addAttachment(stream, fileName);
    }

    @Override
    public void addFileAsSpoiler(File file) {
        addAttachmentAsSpoiler(file);
    }

    @Override
    public void addFileAsSpoiler(Icon icon) {
        addAttachmentAsSpoiler(icon);
    }

    @Override
    public void addFileAsSpoiler(URL url) {
        addAttachmentAsSpoiler(url);
    }

    @Override
    public void addAttachment(BufferedImage image, String fileName) {
        if (image == null || fileName == null) {
            throw new IllegalArgumentException("image and fileName cannot be null!");
        }
        attachments.add(new FileContainer(image, fileName));
    }

    @Override
    public void addAttachment(File file) {
        if (file == null) {
            throw new IllegalArgumentException("file cannot be null!");
        }
        attachments.add(new FileContainer(file));
    }

    @Override
    public void addAttachment(Icon icon) {
        if (icon == null) {
            throw new IllegalArgumentException("icon cannot be null!");
        }
        attachments.add(new FileContainer(icon));
    }

    @Override
    public void addAttachment(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("url cannot be null!");
        }
        attachments.add(new FileContainer(url));
    }

    @Override
    public void addAttachment(byte[] bytes, String fileName) {
        if (bytes == null || fileName == null) {
            throw new IllegalArgumentException("bytes and fileName cannot be null!");
        }
        attachments.add(new FileContainer(bytes, fileName));
    }

    @Override
    public void addAttachment(InputStream stream, String fileName) {
        if (stream == null || fileName == null) {
            throw new IllegalArgumentException("stream and fileName cannot be null!");
        }
        attachments.add(new FileContainer(stream, fileName));
    }

    @Override
    public void addAttachmentAsSpoiler(File file) {
        if (file == null) {
            throw new IllegalArgumentException("file cannot be null!");
        }
        attachments.add(new FileContainer(file, true));
    }

    @Override
    public void addAttachmentAsSpoiler(Icon icon) {
        if (icon == null) {
            throw new IllegalArgumentException("icon cannot be null!");
        }
        attachments.add(new FileContainer(icon, true));
    }

    @Override
    public void addAttachmentAsSpoiler(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("url cannot be null!");
        }
        attachments.add(new FileContainer(url, true));
    }

    @Override
    public void setAllowedMentions(AllowedMentions allowedMentions) {
        if (allowedMentions == null) {
            throw new IllegalArgumentException("mention cannot be null!");
        }
        this.allowedMentions = allowedMentions;
    }

    @Override
    public void replyTo(long messageId) {
        replyingTo = messageId;
    }

    @Override
    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    @Override
    public StringBuilder getStringBuilder() {
        return strBuilder;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Send methods
    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public CompletableFuture<Message> send(User user) {
        return send((Messageable) user);
    }

    @Override
    public CompletableFuture<Message> send(Messageable messageable) {
        if (messageable == null) {
            throw new IllegalStateException("Cannot send message without knowing the receiver");
        }
        if (messageable instanceof TextChannel) {
            return send((TextChannel) messageable);
        } else if (messageable instanceof User) {
            return ((User) messageable).openPrivateChannel().thenCompose(this::send);
        } else if (messageable instanceof Member) {
            return send(((Member) messageable).getUser());
        } else if (messageable instanceof IncomingWebhook) {
            return send((IncomingWebhook) messageable);
        }
        throw new IllegalStateException("Messageable of unknown type");
    }

    @Override
    public CompletableFuture<Message> send(TextChannel channel) {
        ObjectNode body = JsonNodeFactory.instance.objectNode()
                .put("content", toString() == null ? "" : toString())
                .put("tts", tts);
        body.putArray("mentions");

        if (allowedMentions != null) {
            ((AllowedMentionsImpl) allowedMentions).toJsonNode(body.putObject("allowed_mentions"));
        }

        if (embeds.size() != 0) {
            // Discord now supports multiple embeds for Discord bots.
            ArrayNode embedsNode = JsonNodeFactory.instance.objectNode().arrayNode();
            for (int i = 0; i < embeds.size() && i < 10; i++) {
                embedsNode.add(((EmbedBuilderDelegateImpl) embeds.get(i).getDelegate()).toJsonNode());
            }
            body.set("embeds", embedsNode);
        }

        prepareComponents(body);

        if (nonce != null) {
            body.put("nonce", nonce);
        }

        if (replyingTo != null) {
            body.putObject("message_reference").put("message_id", replyingTo);
        }

        RestRequest<Message> request = new RestRequest<Message>(channel.getApi(), RestMethod.POST, RestEndpoint.MESSAGE)
                .setUrlParameters(channel.getIdAsString());
        if (!attachments.isEmpty() || (embeds.size() > 0 && embeds.get(0).requiresAttachments())) {
            CompletableFuture<Message> future = new CompletableFuture<>();
            // We access files etc. so this should be async
            channel.getApi().getThreadPool().getExecutorService().submit(() -> {
                try {
                    List<FileContainer> tempAttachments = new ArrayList<>(attachments);
                    // Add the attachments required for the embeds
                    for (EmbedBuilder embed : embeds) {
                        tempAttachments.addAll(
                                ((EmbedBuilderDelegateImpl) embed.getDelegate()).getRequiredAttachments());
                    }

                    addMultipartBodyToRequest(request, body, tempAttachments, channel.getApi());

                    request.execute(result -> ((DiscordApiImpl) channel.getApi())
                            .getOrCreateMessage(channel, result.getJsonBody()))
                            .whenComplete((message, throwable) -> {
                                if (throwable != null) {
                                    future.completeExceptionally(throwable);
                                } else {
                                    future.complete(message);
                                }
                            });
                } catch (Throwable t) {
                    future.completeExceptionally(t);
                }
            });
            return future;
        } else {
            request.setBody(body);
            return request.execute(result -> ((DiscordApiImpl) channel.getApi())
                .getOrCreateMessage(channel, result.getJsonBody()));
        }
    }

    @Override
    public CompletableFuture<Message> send(IncomingWebhook webhook) {
        return send(webhook.getIdAsString(), webhook.getToken(), null, null, true, webhook.getApi());
    }

    /**
     * Send a message to an incoming webhook.
     *
     * @param webhookId The id of the webhook to send the message to
     * @param webhookToken The token of the webhook to send the message to
     * @param displayName The display name the webhook should use
     * @param avatarUrl The avatar the webhook should use
     * @param wait If the completable future will be completed
     * @param api The api instance needed to send and return the message
     * @return The sent message
     */
    protected CompletableFuture<Message> send(String webhookId, String webhookToken, String displayName, URL avatarUrl,
                                              boolean wait, DiscordApi api) {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        prepareCommonWebhookMessageBodyParts(body);

        if (displayName != null) {
            body.put("username", displayName);
        }
        if (avatarUrl != null) {
            body.put("avatar_url", avatarUrl.toExternalForm());
        }

        if (embeds.size() != 0) {
            ArrayNode embedsNode = JsonNodeFactory.instance.objectNode().arrayNode();
            for (int i = 0; i < embeds.size() && i < 10; i++) {
                embedsNode.add(((EmbedBuilderDelegateImpl) embeds.get(i).getDelegate()).toJsonNode());
            }
            body.set("embeds", embedsNode);
        }

        prepareComponents(body);

        if (strBuilder.length() != 0) {
            body.put("content", strBuilder.toString());
        }

        RestRequest<Message> request =
                    new RestRequest<Message>(api, RestMethod.POST, RestEndpoint.WEBHOOK_SEND)
                    .addQueryParameter("wait", Boolean.toString(wait))
                    .setUrlParameters(webhookId, webhookToken);
        CompletableFuture<Message> future = new CompletableFuture<>();
        if (!attachments.isEmpty() || (embeds.size() > 0 && embeds.get(0).requiresAttachments())) {
            // We access files etc. so this should be async
            api.getThreadPool().getExecutorService().submit(() -> {
                try {
                    List<FileContainer> tempAttachments = new ArrayList<>(attachments);
                    // Add the attachments required for the embeds
                    for (EmbedBuilder embed : embeds) {
                        tempAttachments.addAll(
                                ((EmbedBuilderDelegateImpl) embed.getDelegate()).getRequiredAttachments());
                    }

                    addMultipartBodyToRequest(request, body, tempAttachments, api);

                    executeWebhookRest(request, wait, future, api);
                } catch (Throwable t) {
                    future.completeExceptionally(t);
                }
            });
        } else {
            request.setBody(body);
            executeWebhookRest(request, wait, future, api);
        }
        return future;
    }

    @Override
    public CompletableFuture<Message> sendWithWebhook(DiscordApi api, String webhookId, String webhookToken) {
        return send(webhookId, webhookToken, null, null, true, api);
    }

    /**
     * Method which executes the webhook rest request.
     *
     * @param request The rest request to execute
     * @param wait If discord sends us a response
     * @param future The future to complete
     * @param api The api instance needed to create the message
     */
    private static void executeWebhookRest(RestRequest<Message> request, boolean wait,
                                           CompletableFuture<Message> future, DiscordApi api) {
        CompletableFuture<Message> tmpFuture;
        if (wait) {
            tmpFuture = request.execute(result -> {
                JsonNode body = result.getJsonBody();
                TextChannel channel = api.getTextChannelById(body.get("channel_id").asText()).orElseThrow(() ->
                        new IllegalStateException("Cannot return a message when the channel isn't cached!")
                );
                return ((DiscordApiImpl) api).getOrCreateMessage(channel, body);
            });
        } else {
            tmpFuture = request.execute(result -> null);
        }
        tmpFuture.whenComplete((message, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
            } else {
                future.complete(message);
            }
        });
    }


    @Override
    public CompletableFuture<Message> edit(Message message, boolean updateContent,
                                           boolean updateEmbeds, boolean updateComponents) {
        ObjectNode body = JsonNodeFactory.instance.objectNode();

        if (updateContent) {
            body.put("content", strBuilder.toString());
        }

        if (updateEmbeds) {
            if (embeds.size() != 0) {
                // Discord now supports multiple embeds for Discord bots.
                ArrayNode embedsNode = JsonNodeFactory.instance.objectNode().arrayNode();
                for (int i = 0; i < embeds.size() && i < 10; i++) {
                    embedsNode.add(((EmbedBuilderDelegateImpl) embeds.get(i).getDelegate()).toJsonNode());
                }
                body.set("embeds", embedsNode);
            }
        }

        if (updateComponents) {
            prepareComponents(body);
        }

        return new RestRequest<Message>(message.getApi(), RestMethod.PATCH, RestEndpoint.MESSAGE)
                .setUrlParameters(Long.toUnsignedString(message.getChannel().getId()),
                        Long.toUnsignedString(message.getId()))
                .setBody(body).execute(result -> new MessageImpl((DiscordApiImpl) message.getApi(),
                        message.getApi().getTextChannelById(
                                message.getChannel().getId()).orElseThrow(() ->
                        new IllegalStateException("TextChannel is missing.")), result.getJsonBody()));
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Internal MessageBuilder utility methods
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * Method which creates and adds a MultipartBody to a RestRequest.
     *
     * @param request The RestRequest to add the MultipartBody to
     * @param body The body to use as base for the MultipartBody
     * @param attachments The List of FileContainers to add as attachments
     * @param api The api instance needed to add the attachments
     */
    protected void addMultipartBodyToRequest(RestRequest<?> request, ObjectNode body,
                                             List<FileContainer> attachments, DiscordApi api) {
        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("payload_json", body.toString());

        Collections.reverse(attachments);
        for (int i = 0; i < attachments.size(); i++) {
            byte[] bytes = attachments.get(i).asByteArray(api).join();

            String mediaType = URLConnection
                    .guessContentTypeFromName(attachments.get(i).getFileTypeOrName());
            if (mediaType == null) {
                mediaType = "application/octet-stream";
            }
            multipartBodyBuilder.addFormDataPart("file" + i, attachments.get(i).getFileTypeOrName(),
                    RequestBody.create(MediaType.parse(mediaType), bytes));
        }

        request.setMultipartBody(multipartBodyBuilder.build());
    }

    protected void prepareCommonWebhookMessageBodyParts(ObjectNode body) {
        body.put("tts", this.tts);
        if (strBuilder.length() != 0) {
            body.put("content", strBuilder.toString());
        }
        if (allowedMentions != null) {
            ((AllowedMentionsImpl) allowedMentions).toJsonNode(body.putObject("allowed_mentions"));
        }
        if (embeds.size() != 0) {
            ArrayNode embedsNode = JsonNodeFactory.instance.objectNode().arrayNode();
            for (EmbedBuilder embed : embeds) {
                embedsNode.add(((EmbedBuilderDelegateImpl) embed.getDelegate()).toJsonNode());
            }
            body.set("embeds", embedsNode);
        }
    }

    @Override
    public String toString() {
        return strBuilder.toString();
    }

    protected void prepareComponents(ObjectNode body) {
        prepareComponents(body, false);
    }

    protected void prepareComponents(ObjectNode body, boolean evenIfEmpty) {
        if (evenIfEmpty || components.size() != 0) {
            ArrayNode componentsNode = JsonNodeFactory.instance.objectNode().arrayNode();
            for (int i = 0; i < components.size() && i < 5; i++) {
                ActionRowImpl component = (ActionRowImpl) components.get(i);
                componentsNode.add(component.toJsonNode());
            }
            body.set("components", componentsNode);
        }
    }
}