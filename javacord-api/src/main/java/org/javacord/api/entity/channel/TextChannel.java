package org.javacord.api.entity.channel;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.webhook.Webhook;
import org.javacord.api.listener.channel.TextChannelAttachableListenerManager;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.util.NonThrowingAutoCloseable;
import org.javacord.api.util.cache.MessageCache;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.api.util.logging.ExceptionLogger;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This class represents a text channel.
 */
public interface TextChannel extends Channel, Messageable, TextChannelAttachableListenerManager {

    /**
     * Displays the "xyz is typing..." message.
     * The message automatically disappears after 10 seconds or after sending a message.
     * No ratelimit retries are done at all.
     * If the ratelimit is reached already, then the returned future will complete exceptionally right on the first try
     * with a {@code RatelimitException}.
     *
     * @return A future to tell us if the action was successful.
     * @see #typeContinuously()
     * @see #typeContinuously(Consumer)
     */
    CompletableFuture<Void> type();

    /**
     * Displays the "xyz is typing..." message continuously, starting immediately.
     * The message is continuously displayed if not quit using the returned {@code AutoCloseable}.
     * Sending a message will make the message go away shortly, but it will return immediately if not cancelled using
     * the {@code AutoCloseable}. This can be used in a try-with-resources block like
     * <code>try (NonThrowingAutoCloseable typingIndicator = textChannel.typeContinuously())
     * { /* do lengthy stuff &#42;/ } sendReply();</code>.
     *
     * <p>The typing indicator will immediately be shown. To delay the display of the first typing indicator, use
     * {@link #typeContinuouslyAfter(long, TimeUnit)}. This can be useful if the task you do can be finished in very
     * short time which could cause the typing indicator and the response message being sent at the same time and the
     * typing indicator could be shown for 10 seconds even if the message was sent already.
     *
     * <p>Any occurring exceptions including ratelimit exceptions are suppressed. If you want to handle exceptions, use
     * {@link #typeContinuously(Consumer)} or {@link #typeContinuouslyAfter(long, TimeUnit, Consumer)}.
     *
     * @return An auto-closable to stop sending the typing indicator.
     * @see #type()
     * @see #typeContinuously(Consumer)
     * @see #typeContinuouslyAfter(long, TimeUnit)
     * @see #typeContinuouslyAfter(long, TimeUnit, Consumer)
     */
    default NonThrowingAutoCloseable typeContinuously() {
        return typeContinuouslyAfter(0, TimeUnit.NANOSECONDS, null);
    }

    /**
     * Displays the "xyz is typing..." message continuously, starting immediately.
     * The message is continuously displayed if not quit using the returned {@code AutoCloseable}.
     * Sending a message will make the message go away shortly, but it will return immediately if not cancelled using
     * the {@code AutoCloseable}. This can be used in a try-with-resources block like
     * <code>try (NonThrowingAutoCloseable typingIndicator =
     * textChannel.typeContinuously(ExceptionLogger.getConsumer(RatelimitException.class)))
     * { /* do lengthy stuff &#42;/ } sendReply();</code>.
     *
     * <p>The typing indicator will immediately be shown. To delay the display of the first typing indicator, use
     * {@link #typeContinuouslyAfter(long, TimeUnit)}. This can be useful if the task you do can be finished in very
     * short time which could cause the typing indicator and the response message being sent at the same time and the
     * typing indicator could be shown for 10 seconds even if the message was sent already.
     *
     * <p>Any occurring exceptions including ratelimit exceptions are given to the provided {@code exceptionHandler} or
     * ignored if it is {@code null}.
     *
     * @param exceptionHandler The handler that exceptions are given to.
     * @return An auto-closable to stop sending the typing indicator.
     * @see #type()
     * @see #typeContinuously()
     * @see #typeContinuouslyAfter(long, TimeUnit)
     * @see #typeContinuouslyAfter(long, TimeUnit, Consumer)
     */
    default NonThrowingAutoCloseable typeContinuously(Consumer<Throwable> exceptionHandler) {
        return typeContinuouslyAfter(0, TimeUnit.NANOSECONDS, exceptionHandler);
    }

    /**
     * Displays the "xyz is typing..." message continuously, starting delayed.
     * The message is continuously displayed if not quit using the returned {@code AutoCloseable}.
     * Sending a message will make the message go away shortly, but it will return immediately if not cancelled using
     * the {@code AutoCloseable}. This can be used in a try-with-resources block like
     * <code>try (NonThrowingAutoCloseable typingIndicator =
     * textChannel.typeContinuouslyAfter(500, TimeUnit.MILLISECONDS)) { /* do lengthy stuff &#42;/ }
     * sendReply();</code>.
     *
     * <p>The typing indicator will be shown delayed. This can be useful if the task you do can be finished in very
     * short time which could cause the typing indicator and the response message being sent at the same time and the
     * typing indicator could be shown for 10 seconds even if the message was sent already. With the delay this is
     * compensated, because if the returned {@code AutoCloseable} is closed before the delay is over, no typing
     * indicator will be sent at all.
     *
     * <p>Any occurring exceptions including ratelimit exceptions are suppressed. If you want to handle exceptions, use
     * {@link #typeContinuously(Consumer)} or {@link #typeContinuouslyAfter(long, TimeUnit, Consumer)}.
     *
     * @param delay The delay to wait until the first typing indicator is sent.
     * @param timeUnit The time unit of the delay value.
     * @return An auto-closable to stop sending the typing indicator.
     * @see #type()
     * @see #typeContinuously()
     * @see #typeContinuously(Consumer)
     * @see #typeContinuouslyAfter(long, TimeUnit, Consumer)
     */
    default NonThrowingAutoCloseable typeContinuouslyAfter(long delay, TimeUnit timeUnit) {
        return typeContinuouslyAfter(delay, timeUnit, null);
    }

    /**
     * Displays the "xyz is typing..." message continuously, starting delayed.
     * The message is continuously displayed if not quit using the returned {@code AutoCloseable}.
     * Sending a message will make the message go away shortly, but it will return immediately if not cancelled using
     * the {@code AutoCloseable}. This can be used in a try-with-resources block like
     * <code>try (NonThrowingAutoCloseable typingIndicator = textChannel.typeContinuouslyAfter(500,
     * TimeUnit.MILLISECONDS, ExceptionLogger.getConsumer(RatelimitException.class)))
     * { /* do lengthy stuff &#42;/ } sendReply();</code>.
     *
     * <p>The typing indicator will be shown delayed. This can be useful if the task you do can be finished in very
     * short time which could cause the typing indicator and the response message being sent at the same time and the
     * typing indicator could be shown for 10 seconds even if the message was sent already. With the delay this is
     * compensated, because if the returned {@code AutoCloseable} is closed before the delay is over, no typing
     * indicator will be sent at all.
     *
     * <p>Any occurring exceptions including ratelimit exceptions are given to the provided {@code exceptionHandler} or
     * ignored if it is {@code null}.
     *
     * @param exceptionHandler The handler that exceptions are given to.
     * @param delay The delay to wait until the first typing indicator is sent.
     * @param timeUnit The time unit of the delay value.
     * @return An auto-closable to stop sending the typing indicator.
     * @see #type()
     * @see #typeContinuously()
     * @see #typeContinuously(Consumer)
     * @see #typeContinuouslyAfter(long, TimeUnit)
     */
    default NonThrowingAutoCloseable typeContinuouslyAfter(
            long delay, TimeUnit timeUnit, Consumer<Throwable> exceptionHandler) {
        // the delegate that does the actual type indicator sending and error handling
        Runnable typeRunnable = () -> {
            try {
                CompletableFuture<?> typeFuture = type();
                if (exceptionHandler != null) {
                    typeFuture.exceptionally(throwable -> {
                        exceptionHandler.accept(throwable);
                        return null;
                    });
                }
            } catch (Throwable t) {
                ExceptionLogger.getConsumer().accept(t);
            }
        };

        DiscordApi api = getApi();

        // schedule regular type indicator sending
        Future<?> typingIndicator = api.getThreadPool().getScheduler().scheduleWithFixedDelay(
                typeRunnable, TimeUnit.NANOSECONDS.convert(delay, timeUnit), 8_000_000_000L, TimeUnit.NANOSECONDS);

        // prevent messages from other commands to interrupt the typing indicator too long
        ListenerManager<MessageCreateListener> typingInterruptedListenerManager =
                api.addMessageCreateListener(event -> {
                    if (event.getMessage().getAuthor().isYourself()) {
                        typeRunnable.run();
                    }
                });

        // auto-closable to cancel the continuously typing indicator
        return () -> {
            typingInterruptedListenerManager.remove();
            typingIndicator.cancel(true);
        };
    }

    /**
     * Deletes multiple messages at once.
     * Any message given that is invalid will count towards the minimum and maximum message count
     * (currently 2 and 100 respectively). Additionally, duplicated messages will only be counted once.
     * If a message is older than 2 weeks, the method will fail.
     *
     * @param messages The messages to delete.
     * @return A future to tell us if the deletion was successful.
     */
    default CompletableFuture<Void> bulkDelete(Iterable<Message> messages) {
        return bulkDelete(StreamSupport.stream(messages.spliterator(), false).mapToLong(Message::getId).toArray());
    }

    /**
     * Deletes multiple messages at once.
     * Any message id given that do not exist or are invalid will count towards the minimum and maximum message count
     * (currently 2 and 100 respectively). Additionally, duplicated ids will only be counted once.
     * If a message is older than 2 weeks, the method will fail.
     *
     * @param messageIds The ids of the messages to delete.
     * @return A future to tell us if the deletion was successful.
     */
    CompletableFuture<Void> bulkDelete(long... messageIds);

    /**
     * Deletes multiple messages at once.
     * Any message id given that do not exist or are invalid will count towards the minimum and maximum message count
     * (currently 2 and 100 respectively). Additionally, duplicated ids will only be counted once.
     * If a message is older than 2 weeks, the method will fail.
     *
     * @param messageIds The ids of the messages to delete.
     * @return A future to tell us if the deletion was successful.
     */
    default CompletableFuture<Void> bulkDelete(String... messageIds) {
        long[] messageLongIds = Arrays.stream(messageIds).filter(s -> {
            try {
                //noinspection ResultOfMethodCallIgnored
                Long.parseLong(s);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }).mapToLong(Long::parseLong).toArray();
        return bulkDelete(messageLongIds);
    }

    /**
     * Deletes multiple messages at once.
     * Any message given that is invalid will count towards the minimum and maximum message count
     * (currently 2 and 100 respectively). Additionally, duplicated messages will only be counted once.
     * If a message is older than 2 weeks, the method will fail.
     *
     * @param messages The messages to delete.
     * @return A future to tell us if the deletion was successful.
     */
    default CompletableFuture<Void> bulkDelete(Message... messages) {
        return bulkDelete(Arrays.stream(messages).mapToLong(Message::getId).toArray());
    }

    /**
     * Deletes multiple messages at once.
     * This method does not have a size or age restriction.
     * Messages younger than two weeks are sent in batches of 100 messages to the bulk delete API,
     * older messages are deleted with individual delete requests.
     *
     * @param messages The messages to delete.
     * @return A future to tell us if the deletion was successful.
     */
    default CompletableFuture<Void> deleteMessages(Iterable<Message> messages) {
        return deleteMessages(StreamSupport.stream(messages.spliterator(), false).mapToLong(Message::getId).toArray());
    }

    /**
     * Deletes multiple messages at once.
     * This method does not have a size or age restriction.
     * Messages younger than two weeks are sent in batches of 100 messages to the bulk delete API,
     * older messages are deleted with individual delete requests.
     *
     * @param messageIds The ids of the messages to delete.
     * @return A future to tell us if the deletion was successful.
     */
    default CompletableFuture<Void> deleteMessages(long... messageIds) {
        return Message.delete(getApi(), getId(), messageIds);
    }

    /**
     * Deletes multiple messages at once.
     * This method does not have a size or age restriction.
     * Messages younger than two weeks are sent in batches of 100 messages to the bulk delete API,
     * older messages are deleted with individual delete requests.
     *
     * @param messageIds The ids of the messages to delete.
     * @return A future to tell us if the deletion was successful.
     */
    default CompletableFuture<Void> deleteMessages(String... messageIds) {
        long[] messageLongIds = Arrays.stream(messageIds).filter(s -> {
            try {
                //noinspection ResultOfMethodCallIgnored
                Long.parseLong(s);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }).mapToLong(Long::parseLong).toArray();
        return deleteMessages(messageLongIds);
    }

    /**
     * Deletes multiple messages at once.
     * This method does not have a size or age restriction.
     * Messages younger than two weeks are sent in batches of 100 messages to the bulk delete API,
     * older messages are deleted with individual delete requests.
     *
     * @param messages The messages to delete.
     * @return A future to tell us if the deletion was successful.
     */
    default CompletableFuture<Void> deleteMessages(Message... messages) {
        return deleteMessages(Arrays.stream(messages).mapToLong(Message::getId).toArray());
    }

    /**
     * Gets a message by its id.
     *
     * @param id The id of the message.
     * @return The message with the given id.
     */
    CompletableFuture<Message> getMessageById(long id);

    /**
     * Gets a message by its id.
     *
     * @param id The id of the message.
     * @return The message with the given id.
     */
    default CompletableFuture<Message> getMessageById(String id) {
        try {
            return getMessageById(Long.parseLong(id));
        } catch (NumberFormatException e) {
            return getMessageById(-1);
        }
    }

    /**
     * Gets a list with all pinned messages.
     *
     * @return A list with all pinned messages.
     */
    CompletableFuture<MessageSet> getPins();

    /**
     * Gets up to a given amount of messages in this channel from the newer end.
     *
     * @param limit The limit of messages to get.
     * @return The messages.
     * @see #getMessagesAsStream()
     */
    CompletableFuture<MessageSet> getMessages(int limit);

    /**
     * Gets messages in this channel from the newer end until one that meets the given condition is found.
     * If no message matches the condition, an empty set is returned.
     *
     * @param condition The abort condition for when to stop retrieving messages.
     * @return The messages.
     * @see #getMessagesAsStream()
     */
    CompletableFuture<MessageSet> getMessagesUntil(Predicate<Message> condition);

    /**
     * Gets messages in this channel from the newer end while they meet the given condition.
     * If the first message does not match the condition, an empty set is returned.
     *
     * @param condition The condition that has to be met.
     * @return The messages.
     * @see #getMessagesAsStream()
     */
    CompletableFuture<MessageSet> getMessagesWhile(Predicate<Message> condition);

    /**
     * Gets a stream of messages in this channel sorted from newest to oldest.
     *
     * <p>The messages are retrieved in batches synchronously from Discord,
     * so consider not using this method from a listener directly.
     *
     * @return The stream.
     * @see #getMessages(int)
     */
    Stream<Message> getMessagesAsStream();

    /**
     * Gets up to a given amount of messages in this channel before a given message in any channel.
     *
     * @param limit The limit of messages to get.
     * @param before Get messages before the message with this id.
     * @return The messages.
     * @see #getMessagesBeforeAsStream(long)
     */
    CompletableFuture<MessageSet> getMessagesBefore(int limit, long before);

    /**
     * Gets up to a given amount of messages in this channel before a given message in any channel.
     *
     * @param limit The limit of messages to get.
     * @param before Get messages before this message.
     * @return The messages.
     * @see #getMessagesBeforeAsStream(Message)
     */
    default CompletableFuture<MessageSet> getMessagesBefore(int limit, Message before) {
        return getMessagesBefore(limit, before.getId());
    }

    /**
     * Gets messages in this channel before a given message in any channel until one that meets the given condition is
     * found.
     * If no message matches the condition, an empty set is returned.
     *
     * @param condition The abort condition for when to stop retrieving messages.
     * @param before Get messages before the message with this id.
     * @return The messages.
     * @see #getMessagesBeforeAsStream(long)
     */
    CompletableFuture<MessageSet> getMessagesBeforeUntil(Predicate<Message> condition, long before);

    /**
     * Gets messages in this channel before a given message in any channel until one that meets the given condition is
     * found.
     * If no message matches the condition, an empty set is returned.
     *
     * @param condition The abort condition for when to stop retrieving messages.
     * @param before Get messages before this message.
     * @return The messages.
     * @see #getMessagesBeforeAsStream(Message)
     */
    default CompletableFuture<MessageSet> getMessagesBeforeUntil(Predicate<Message> condition, Message before) {
        return getMessagesBeforeUntil(condition, before.getId());
    }


    /**
     * Gets messages in this channel before a given message in any channel while they meet the given condition.
     * If the first message does not match the condition, an empty set is returned.
     *
     * @param condition The condition that has to be met.
     * @param before Get messages before the message with this id.
     * @return The messages.
     * @see #getMessagesBeforeAsStream(long)
     */
    CompletableFuture<MessageSet> getMessagesBeforeWhile(Predicate<Message> condition, long before);

    /**
     * Gets messages in this channel before a given message in any channel while they meet the given condition.
     * If the first message does not match the condition, an empty set is returned.
     *
     * @param condition The condition that has to be met.
     * @param before Get messages before this message.
     * @return The messages.
     * @see #getMessagesBeforeAsStream(Message)
     */
    default CompletableFuture<MessageSet> getMessagesBeforeWhile(Predicate<Message> condition, Message before) {
        return getMessagesBeforeWhile(condition, before.getId());
    }

    /**
     * Gets a stream of messages in this channel before a given message in any channel sorted from newest to oldest.
     *
     * <p>The messages are retrieved in batches synchronously from Discord,
     * so consider not using this method from a listener directly.
     *
     * @param before Get messages before the message with this id.
     * @return The stream.
     * @see #getMessagesBefore(int, long)
     */
    Stream<Message> getMessagesBeforeAsStream(long before);

    /**
     * Gets a stream of messages in this channel before a given message in any channel sorted from newest to oldest.
     *
     * <p>The messages are retrieved in batches synchronously from Discord,
     * so consider not using this method from a listener directly.
     *
     * @param before Get messages before this message.
     * @return The stream.
     * @see #getMessagesBefore(int, Message)
     */
    default Stream<Message> getMessagesBeforeAsStream(Message before) {
        return getMessagesBeforeAsStream(before.getId());
    }

    /**
     * Gets up to a given amount of messages in this channel after a given message in any channel.
     *
     * @param limit The limit of messages to get.
     * @param after Get messages after the message with this id.
     * @return The messages.
     * @see #getMessagesAfterAsStream(long)
     */
    CompletableFuture<MessageSet> getMessagesAfter(int limit, long after);

    /**
     * Gets up to a given amount of messages in this channel after a given message in any channel.
     *
     * @param limit The limit of messages to get.
     * @param after Get messages after this message.
     * @return The messages.
     * @see #getMessagesAfterAsStream(Message)
     */
    default CompletableFuture<MessageSet> getMessagesAfter(int limit, Message after) {
        return getMessagesAfter(limit, after.getId());
    }

    /**
     * Gets messages in this channel after a given message in any channel until one that meets the given condition is
     * found.
     * If no message matches the condition, an empty set is returned.
     *
     * @param condition The abort condition for when to stop retrieving messages.
     * @param after Get messages after the message with this id.
     * @return The messages.
     * @see #getMessagesAfterAsStream(long)
     */
    CompletableFuture<MessageSet> getMessagesAfterUntil(Predicate<Message> condition, long after);

    /**
     * Gets messages in this channel after a given message in any channel until one that meets the given condition is
     * found.
     * If no message matches the condition, an empty set is returned.
     *
     * @param condition The abort condition for when to stop retrieving messages.
     * @param after Get messages after this message.
     * @return The messages.
     * @see #getMessagesAfterAsStream(Message)
     */
    default CompletableFuture<MessageSet> getMessagesAfterUntil(Predicate<Message> condition, Message after) {
        return getMessagesAfterUntil(condition, after.getId());
    }

    /**
     * Gets messages in this channel after a given message in any channel while they meet the given condition.
     * If the first message does not match the condition, an empty set is returned.
     *
     * @param condition The condition that has to be met.
     * @param after Get messages after the message with this id.
     * @return The messages.
     * @see #getMessagesAfterAsStream(long)
     */
    CompletableFuture<MessageSet> getMessagesAfterWhile(Predicate<Message> condition, long after);

    /**
     * Gets messages in this channel after a given message in any channel while they meet the given condition.
     * If the first message does not match the condition, an empty set is returned.
     *
     * @param condition The condition that has to be met.
     * @param after Get messages after this message.
     * @return The messages.
     * @see #getMessagesAfterAsStream(Message)
     */
    default CompletableFuture<MessageSet> getMessagesAfterWhile(Predicate<Message> condition, Message after) {
        return getMessagesAfterWhile(condition, after.getId());
    }

    /**
     * Gets a stream of messages in this channel after a given message in any channel sorted from oldest to newest.
     *
     * <p>The messages are retrieved in batches synchronously from Discord,
     * so consider not using this method from a listener directly.
     *
     * @param after Get messages after the message with this id.
     * @return The messages.
     * @see #getMessagesAfter(int, long)
     */
    Stream<Message> getMessagesAfterAsStream(long after);

    /**
     * Gets a stream of messages in this channel after a given message in any channel sorted from oldest to newest.
     *
     * <p>The messages are retrieved in batches synchronously from Discord,
     * so consider not using this method from a listener directly.
     *
     * @param after Get messages after this message.
     * @return The stream.
     * @see #getMessagesAfter(int, Message)
     */
    default Stream<Message> getMessagesAfterAsStream(Message after) {
        return getMessagesAfterAsStream(after.getId());
    }

    /**
     * Gets up to a given amount of messages in this channel around a given message in any channel.
     * The given message will be part of the result in addition to the messages around if it was sent in this channel
     * and does not count towards the limit.
     * Half of the messages will be older than the given message and half of the messages will be newer.
     * If there aren't enough older or newer messages, the actual amount of messages will be less than the given limit.
     * It's also not guaranteed to be perfectly balanced.
     *
     * @param limit The limit of messages to get.
     * @param around Get messages around the message with this id.
     * @return The messages.
     * @see #getMessagesAroundAsStream(long)
     */
    CompletableFuture<MessageSet> getMessagesAround(int limit, long around);

    /**
     * Gets up to a given amount of messages in this channel around a given message in any channel.
     * The given message will be part of the result in addition to the messages around if it was sent in this channel
     * and does not count towards the limit.
     * Half of the messages will be older than the given message and half of the messages will be newer.
     * If there aren't enough older or newer messages, the actual amount of messages will be less than the given limit.
     * It's also not guaranteed to be perfectly balanced.
     *
     * @param limit The limit of messages to get.
     * @param around Get messages around this message.
     * @return The messages.
     * @see #getMessagesAroundAsStream(Message)
     */
    default CompletableFuture<MessageSet> getMessagesAround(int limit, Message around) {
        return getMessagesAround(limit, around.getId());
    }

    /**
     * Gets messages in this channel around a given message in any channel until one that meets the given condition is
     * found. If no message matches the condition, an empty set is returned.
     * The given message will be part of the result in addition to the messages around if it was sent in this channel
     * and is matched against the condition and will abort retrieval.
     * Half of the messages will be older than the given message and half of the messages will be newer.
     * If there aren't enough older or newer messages, the actual amount of messages will be less than the given limit.
     * It's also not guaranteed to be perfectly balanced.
     *
     * @param condition The abort condition for when to stop retrieving messages.
     * @param around Get messages around the message with this id.
     * @return The messages.
     * @see #getMessagesAroundAsStream(long)
     */
    CompletableFuture<MessageSet> getMessagesAroundUntil(Predicate<Message> condition, long around);

    /**
     * Gets messages in this channel around a given message in any channel until one that meets the given condition is
     * found. If no message matches the condition, an empty set is returned.
     * The given message will be part of the result in addition to the messages around if it was sent in this channel
     * and is matched against the condition and will abort retrieval.
     * Half of the messages will be older than the given message and half of the messages will be newer.
     * If there aren't enough older or newer messages, the actual amount of messages will be less than the given limit.
     * It's also not guaranteed to be perfectly balanced.
     *
     * @param condition The abort condition for when to stop retrieving messages.
     * @param around Get messages around this message.
     * @return The messages.
     * @see #getMessagesAroundAsStream(Message)
     */
    default CompletableFuture<MessageSet> getMessagesAroundUntil(Predicate<Message> condition, Message around) {
        return getMessagesAroundUntil(condition, around.getId());
    }

    /**
     * Gets messages in this channel around a given message in any channel while they meet the given condition.
     * If the first message does not match the condition, an empty set is returned.
     * The given message will be part of the result in addition to the messages around if it was sent in this channel
     * and is matched against the condition and will abort retrieval.
     * Half of the messages will be older than the given message and half of the messages will be newer.
     * If there aren't enough older or newer messages, the actual amount of messages will be less than the given limit.
     * It's also not guaranteed to be perfectly balanced.
     *
     * @param condition The condition that has to be met.
     * @param around Get messages around the message with this id.
     * @return The messages.
     * @see #getMessagesAroundAsStream(long)
     */
    CompletableFuture<MessageSet> getMessagesAroundWhile(Predicate<Message> condition, long around);

    /**
     * Gets messages in this channel around a given message in any channel while they meet the given condition.
     * If the first message does not match the condition, an empty set is returned.
     * The given message will be part of the result in addition to the messages around if it was sent in this channel
     * and is matched against the condition and will abort retrieval.
     * Half of the messages will be older than the given message and half of the messages will be newer.
     * If there aren't enough older or newer messages, the actual amount of messages will be less than the given limit.
     * It's also not guaranteed to be perfectly balanced.
     *
     * @param condition The condition that has to be met.
     * @param around Get messages around this message.
     * @return The messages.
     * @see #getMessagesAroundAsStream(Message)
     */
    default CompletableFuture<MessageSet> getMessagesAroundWhile(Predicate<Message> condition, Message around) {
        return getMessagesAroundWhile(condition, around.getId());
    }

    /**
     * Gets a stream of messages in this channel around a given message in any channel.
     * The first message in the stream will be the given message if it was sent in this channel.
     * After that you will always get an older message and a newer message alternating as long as on both sides
     * messages are available. If only on one side further messages are available, only those are delivered further on.
     * It's not guaranteed to be perfectly balanced.
     *
     * <p>The messages are retrieved in batches synchronously from Discord,
     * so consider not using this method from a listener directly.
     *
     * @param around Get messages around the message with this id.
     * @return The stream.
     * @see #getMessagesAround(int, long)
     */
    Stream<Message> getMessagesAroundAsStream(long around);

    /**
     * Gets a stream of messages in this channel around a given message in any channel.
     * The first message in the stream will be the given message if it was sent in this channel.
     * After that you will always get an older message and a newer message alternating as long as on both sides
     * messages are available. If only on one side further messages are available, only those are delivered further on.
     * It's not guaranteed to be perfectly balanced.
     *
     * <p>The messages are retrieved in batches synchronously from Discord,
     * so consider not using this method from a listener directly.
     *
     * @param around Get messages around this message.
     * @return The stream.
     * @see #getMessagesAround(int, Message)
     */
    default Stream<Message> getMessagesAroundAsStream(Message around) {
        return getMessagesAroundAsStream(around.getId());
    }

    /**
     * Gets all messages in this channel between the first given message in any channel and the second given message in
     * any channel, excluding the boundaries.
     *
     * @param from The id of the start boundary messages.
     * @param to The id of the other boundary messages.
     * @return The messages.
     * @see #getMessagesBetweenAsStream(long, long)
     */
    CompletableFuture<MessageSet> getMessagesBetween(long from, long to);

    /**
     * Gets all messages in this channel between the first given message in any channel and the second given message in
     * any channel, excluding the boundaries.
     *
     * @param from The start boundary messages.
     * @param to The other boundary messages.
     * @return The messages.
     * @see #getMessagesBetweenAsStream(long, long)
     */
    default CompletableFuture<MessageSet> getMessagesBetween(Message from, Message to) {
        return getMessagesBetween(from.getId(), to.getId());
    }

    /**
     * Gets all messages in this channel between the first given message in any channel and the second given message in
     * any channel, excluding the boundaries, until one that meets the given condition is found.
     * If no message matches the condition, an empty set is returned.
     *
     * @param condition The abort condition for when to stop retrieving messages.
     * @param from The id of the start boundary messages.
     * @param to The id of the other boundary messages.
     * @return The messages.
     * @see #getMessagesBetweenAsStream(long, long)
     */
    CompletableFuture<MessageSet> getMessagesBetweenUntil(Predicate<Message> condition, long from, long to);

    /**
     * Gets all messages in this channel between the first given message in any channel and the second given message in
     * any channel, excluding the boundaries, until one that meets the given condition is found.
     * If no message matches the condition, an empty set is returned.
     *
     * @param condition The abort condition for when to stop retrieving messages.
     * @param from The start boundary messages.
     * @param to The other boundary messages.
     * @return The messages.
     * @see #getMessagesBetweenAsStream(long, long)
     */
    default CompletableFuture<MessageSet> getMessagesBetweenUntil(
            Predicate<Message> condition, Message from, Message to) {
        return getMessagesBetweenUntil(condition, from.getId(), to.getId());
    }

    /**
     * Gets all messages in this channel between the first given message in any channel and the second given message in
     * any channel, excluding the boundaries, while they meet the given condition.
     * If the first message does not match the condition, an empty set is returned.
     *
     * @param condition The condition that has to be met.
     * @param from The id of the start boundary messages.
     * @param to The id of the other boundary messages.
     * @return The messages.
     * @see #getMessagesBetweenAsStream(long, long)
     */
    CompletableFuture<MessageSet> getMessagesBetweenWhile(Predicate<Message> condition, long from, long to);

    /**
     * Gets all messages in this channel between the first given message in any channel and the second given message in
     * any channel, excluding the boundaries, while they meet the given condition.
     * If the first message does not match the condition, an empty set is returned.
     *
     * @param condition The condition that has to be met.
     * @param from The start boundary messages.
     * @param to The other boundary messages.
     * @return The messages.
     * @see #getMessagesBetweenAsStream(long, long)
     */
    default CompletableFuture<MessageSet> getMessagesBetweenWhile(
            Predicate<Message> condition, Message from, Message to) {
        return getMessagesBetweenWhile(condition, from.getId(), to.getId());
    }

    /**
     * Gets all messages in this channel between the first given message in any channel and the second given message in
     * any channel, excluding the boundaries, sorted from first given message to the second given message.
     *
     * <p>The messages are retrieved in batches synchronously from Discord,
     * so consider not using this method from a listener directly.
     *
     * @param from The id of the start boundary messages.
     * @param to The id of the other boundary messages.
     * @return The stream.
     * @see #getMessagesBetween(long, long)
     */
    Stream<Message> getMessagesBetweenAsStream(long from, long to);

    /**
     * Gets all messages in this channel between the first given message in any channel and the second given message in
     * any channel, excluding the boundaries, sorted from first given message to the second given message.
     *
     * <p>The messages are retrieved in batches synchronously from Discord,
     * so consider not using this method from a listener directly.
     *
     * @param from The start boundary messages.
     * @param to The other boundary messages.
     * @return The stream.
     * @see #getMessagesBetween(long, long)
     */
    default Stream<Message> getMessagesBetweenAsStream(Message from, Message to) {
        return getMessagesBetweenAsStream(from.getId(), to.getId());
    }

    /**
     * Gets the message cache for the channel.
     *
     * @return The message cache for the channel.
     */
    MessageCache getMessageCache();

    /**
     * Gets a list of all webhooks in this channel.
     *
     * @return A list of all webhooks in this channel.
     */
    CompletableFuture<List<Webhook>> getWebhooks();

    /**
     * Checks if the given user can send messages in this channel.
     * In private chats (private channel or group channel) this always returns <code>true</code> if the user is
     * part of the chat.
     * Please notice, this does not check if a user has blocked private messages!
     *
     * @param user The user to check.
     * @return Whether the given user can write messages or not.
     */
    default boolean canWrite(User user) {
        Optional<PrivateChannel> privateChannel = asPrivateChannel();
        if (privateChannel.isPresent()) {
            return user.isYourself() || privateChannel.get().getRecipient() == user;
        }
        Optional<ServerTextChannel> severTextChannel = asServerTextChannel();
        return !severTextChannel.isPresent()
               || severTextChannel.get().hasPermission(user, PermissionType.ADMINISTRATOR)
               || severTextChannel.get()
                       .hasPermissions(user, PermissionType.READ_MESSAGES, PermissionType.SEND_MESSAGES);
    }

    /**
     * Checks if the user of the connected account can send messages in this channel.
     * In private chats (private channel or group channel) this always returns {@code true} if the user is
     * part of the chat.
     * Please notice, this does not check if a user has blocked private messages!
     *
     * @return Whether the user of the connected account can write messages or not.
     */
    default boolean canYouWrite() {
        return canWrite(getApi().getYourself());
    }

    /**
     * Checks if the given user can use external emojis in this channel.
     * In private chats (private channel or group channel) this always returns <code>true</code> if the user is
     * part of the chat.
     * Please notice, this does not check if a user has blocked private messages!
     * It also doesn't check if the user is even able to send any external emojis (twitch subscription or nitro).
     *
     * @param user The user to check.
     * @return Whether the given user can use external emojis or not.
     */
    default boolean canUseExternalEmojis(User user) {
        if (!canWrite(user)) {
            return false;
        }
        Optional<ServerTextChannel> severTextChannel = asServerTextChannel();
        return !severTextChannel.isPresent()
               || severTextChannel.get().hasAnyPermission(user,
                                                          PermissionType.ADMINISTRATOR,
                                                          PermissionType.USE_EXTERNAL_EMOJIS);
    }

    /**
     * Checks if the user of the connected account can use external emojis in this channel.
     * In private chats (private channel or group channel) this always returns {@code true} if the user is
     * part of the chat.
     * Please notice, this does not check if a user has blocked private messages!
     * It also doesn't check if the user is even able to send any external emojis (twitch subscription or nitro).
     *
     * @return Whether the user of the connected account can use external emojis or not.
     */
    default boolean canYouUseExternalEmojis() {
        return canUseExternalEmojis(getApi().getYourself());
    }

    /**
     * Checks if the given user can use embed links in this channel.
     * In private chats (private channel or group channel) this always returns <code>true</code> if the user is
     * part of the chat.
     * Please notice, this does not check if a user has blocked private messages!
     *
     * @param user The user to check.
     * @return Whether the given user can embed links or not.
     */
    default boolean canEmbedLinks(User user) {
        if (!canWrite(user)) {
            return false;
        }
        Optional<ServerTextChannel> severTextChannel = asServerTextChannel();
        return !severTextChannel.isPresent()
               || severTextChannel.get().hasAnyPermission(user,
                                                          PermissionType.ADMINISTRATOR,
                                                          PermissionType.EMBED_LINKS);
    }

    /**
     * Checks if the user of the connected account can use embed links in this channel.
     * In private chats (private channel or group channel) this always returns {@code true} if the user is
     * part of the chat.
     * Please notice, this does not check if a user has blocked private messages!
     *
     * @return Whether the user of the connected account can embed links or not.
     */
    default boolean canYouEmbedLinks() {
        return canEmbedLinks(getApi().getYourself());
    }

    /**
     * Checks if the given user can read the message history of this channel.
     * In private chats (private channel or group channel) this always returns <code>true</code> if the user is
     * part of the chat.
     *
     * @param user The user to check.
     * @return Whether the given user can read the message history or not.
     */
    default boolean canReadMessageHistory(User user) {
        if (!canSee(user)) {
            return false;
        }
        Optional<ServerTextChannel> severTextChannel = asServerTextChannel();
        return !severTextChannel.isPresent()
               || severTextChannel.get().hasAnyPermission(user,
                                                          PermissionType.ADMINISTRATOR,
                                                          PermissionType.READ_MESSAGE_HISTORY);
    }

    /**
     * Checks if the user of the connected account can read the message history of this channel.
     * In private chats (private channel or group channel) this always returns {@code true} if the user is
     * part of the chat.
     *
     * @return Whether the user of the connected account can read the message history or not.
     */
    default boolean canYouReadMessageHistory() {
        return canReadMessageHistory(getApi().getYourself());
    }

    /**
     * Checks if the given user can use tts (text to speech) in this channel.
     * In private chats (private channel or group channel) this always returns <code>true</code> if the user is
     * part of the chat.
     * Please notice, this does not check if a user has blocked private messages!
     *
     * @param user The user to check.
     * @return Whether the given user can use tts or not.
     */
    default boolean canUseTts(User user) {
        if (!canWrite(user)) {
            return false;
        }
        Optional<ServerTextChannel> severTextChannel = asServerTextChannel();
        return !severTextChannel.isPresent()
               || severTextChannel.get().hasAnyPermission(user,
                                                          PermissionType.ADMINISTRATOR,
                                                          PermissionType.SEND_TTS_MESSAGES);
    }

    /**
     * Checks if the user of the connected account can use tts (text to speech) in this channel.
     * In private chats (private channel or group channel) this always returns {@code true} if the user is
     * part of the chat.
     * Please notice, this does not check if a user has blocked private messages!
     *
     * @return Whether the user of the connected account can use tts or not.
     */
    default boolean canYouUseTts() {
        return canUseTts(getApi().getYourself());
    }

    /**
     * Checks if the given user can attach files in this channel.
     *
     * @param user The user to check.
     * @return Whether the given user can attach files or not.
     */
    default boolean canAttachFiles(User user) {
        Optional<PrivateChannel> privateChannel = asPrivateChannel();
        if (privateChannel.isPresent()) {
            return user.isYourself() || privateChannel.get().getRecipient() == user;
        }
        Optional<ServerTextChannel> severTextChannel = asServerTextChannel();
        return !severTextChannel.isPresent()
               || severTextChannel.get().hasPermission(user, PermissionType.ADMINISTRATOR)
               || (severTextChannel.get().hasPermission(user, PermissionType.ATTACH_FILE)
                   && severTextChannel.get().canWrite(user));
    }

    /**
     * Checks if the user of the connected account can attach files in this channel.
     *
     * @return Whether the user of the connected account can attach files or not.
     */
    default boolean canYouAttachFiles() {
        return canAttachFiles(getApi().getYourself());
    }

    /**
     * Checks if the given user is allowed to add <b>new</b> reactions to messages in this channel.
     *
     * @param user The user to check.
     * @return Whether the given user is allowed to add <b>new</b> reactions to messages in this channel or not.
     */
    default boolean canAddNewReactions(User user) {
        Optional<PrivateChannel> privateChannel = asPrivateChannel();
        if (privateChannel.isPresent()) {
            return user.isYourself() || privateChannel.get().getRecipient() == user;
        }
        Optional<ServerTextChannel> severTextChannel = asServerTextChannel();
        return !severTextChannel.isPresent()
               || severTextChannel.get().hasPermission(user, PermissionType.ADMINISTRATOR)
               || severTextChannel.get().hasPermissions(user,
                                                        PermissionType.READ_MESSAGES,
                                                        PermissionType.READ_MESSAGE_HISTORY,
                                                        PermissionType.ADD_REACTIONS);
    }

    /**
     * Checks if the user of the connected account is allowed to add <b>new</b> reactions to messages in this channel.
     *
     * @return Whether the user of the connected account is allowed to add <b>new</b> reactions to messages in this
     *     channel or not.
     */
    default boolean canYouAddNewReactions() {
        return canAddNewReactions(getApi().getYourself());
    }

    /**
     * Checks if the given user can manage messages (delete or pin them or remove reactions of others) in this channel.
     * In private chats (private channel or group channel) this always returns <code>true</code> if the user is
     * part of the chat.
     *
     * @param user The user to check.
     * @return Whether the given user can manage messages or not.
     */
    default boolean canManageMessages(User user) {
        if (!canSee(user)) {
            return false;
        }
        Optional<ServerTextChannel> severTextChannel = asServerTextChannel();
        return !severTextChannel.isPresent()
               || severTextChannel.get().hasAnyPermission(user,
                                                          PermissionType.ADMINISTRATOR,
                                                          PermissionType.MANAGE_MESSAGES);
    }

    /**
     * Checks if the user of the connected account can manage messages (delete or pin them or remove reactions of
     * others) in this channel.
     * In private chats (private channel or group channel) this always returns {@code true} if the user is
     * part of the chat.
     *
     * @return Whether the user of the connected account can manage messages or not.
     */
    default boolean canYouManageMessages() {
        return canManageMessages(getApi().getYourself());
    }

    /**
     * Checks if the given user can remove reactions of other users in this channel.
     * In private chats (private channel or group channel) this always returns {@code true} if the user is
     * part of the chat.
     * This method just forwards to {@link #canManageMessages(User)} as it is the same permission that is needed.
     *
     * @param user The user to check.
     * @return Whether the given user can remove reactions of others or not.
     */
    default boolean canRemoveReactionsOfOthers(User user) {
        return canManageMessages(user);
    }

    /**
     * Checks if the user of the connected account can remove reactions of other users in this channel.
     * In private chats (private channel or group channel) this always returns {@code true} if the user is
     * part of the chat.
     *
     * @return Whether the user of the connected account can remove reactions of others or not.
     */
    default boolean canYouRemoveReactionsOfOthers() {
        return canRemoveReactionsOfOthers(getApi().getYourself());
    }

    /**
     * Checks if the given user can mention everyone (@everyone) in this channel.
     * In private chats (private channel or group channel) this always returns <code>true</code> if the user is
     * part of the chat.
     *
     * @param user The user to check.
     * @return Whether the given user can mention everyone (@everyone) or not.
     */
    default boolean canMentionEveryone(User user) {
        if (!canSee(user)) {
            return false;
        }
        Optional<ServerTextChannel> severTextChannel = asServerTextChannel();
        return !severTextChannel.isPresent()
               || severTextChannel.get().hasPermission(user, PermissionType.ADMINISTRATOR)
               || (severTextChannel.get().hasPermission(user, PermissionType.MENTION_EVERYONE)
                   && severTextChannel.get().canWrite(user));
    }

    /**
     * Checks if the user of the connected account can mention everyone (@everyone) in this channel.
     * In private chats (private channel or group channel) this always returns {@code true} if the user is
     * part of the chat.
     *
     * @return Whether the user of the connected account can mention everyone (@everyone) or not.
     */
    default boolean canYouMentionEveryone() {
        return canMentionEveryone(getApi().getYourself());
    }

    @Override
    default Optional<? extends TextChannel> getCurrentCachedInstance() {
        return getApi().getTextChannelById(getId());
    }

    @Override
    default CompletableFuture<? extends TextChannel> getLatestInstance() {
        Optional<? extends TextChannel> currentCachedInstance = getCurrentCachedInstance();
        if (currentCachedInstance.isPresent()) {
            return CompletableFuture.completedFuture(currentCachedInstance.get());
        } else {
            CompletableFuture<? extends TextChannel> result = new CompletableFuture<>();
            result.completeExceptionally(new NoSuchElementException());
            return result;
        }
    }

}
