![](https://javacord.org/img/javacord3_banner.png)
# Javacord [![Latest version](https://shields.javacord.org/github/release/Javacord/Javacord.svg?label=Version&colorB=brightgreen&style=flat-square)](https://github.com/Javacord/Javacord/releases/latest) [![Latest JavaDocs](https://shields.javacord.org/badge/JavaDoc-Latest-yellow.svg?style=flat-square)](https://docs.javacord.org/api/v/latest/) [![Javacord Wiki](https://shields.javacord.org/badge/Wiki-Home-red.svg?style=flat-square)](https://javacord.org/wiki/) [![Javacord Discord server](https://shields.javacord.org/discord/151037561152733184.svg?colorB=%237289DA&label=Discord&style=flat-square)](https://discord.gg/0qJ2jjyneLEgG7y3)

An easy to use multithreaded library for creating Discord bots in Java.

Javacord is a modern library that focuses on simplicity and speed 🚀.
By reducing itself to standard Java classes and features like [`Optional`](https://javacord.org/wiki/essential-knowledge/optionals.html)s and [`CompletableFuture`](https://javacord.org/wiki/essential-knowledge/completable-futures.html)s, it is extremely easy to use for every Java developer, as it does not require you to learn any new frameworks or complex abstractions. 
It has rich [documentation](#-documentation) and an [awesome community on Discord](#-support) that loves to help with any specific problems and questions.

## 🎉 Basic Usage

The following example logs the bot in and replies to every "!ping" message with "Pong!". 

```java
public class MyFirstBot {

    public static void main(String[] args) {
        // Insert your bot's token here
        String token = "your token";

        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();

        // Add a listener which answers with "Pong!" if someone writes "!ping"
        api.addMessageCreateListener(event -> {
            if (event.getMessageContent().equalsIgnoreCase("!ping")) {
                event.getChannel().sendMessage("Pong!");
            }
        });

        // Print the invite url of your bot
        System.out.println("You can invite the bot by using the following url: " + api.createBotInvite());
    }

}
```

<img src="https://javacord.org/img/javacord-readme/ping-pong-white.gif"> 

More sophisticated examples can be found at the [end of the README](#-more-examples). 
You can also check out the [example bot](https://github.com/Javacord/Example-Bot) for a fully functional bot.

### */* Slash commands
First, you need to create the ping pong slash command. Run this code **one single time** to create the command:

```java
public class MyFirstBot {

    public static void main(String[] args) {
        // Insert your bot's token here
        String token = "your token";

        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();

        SlashCommand.with("ping", "A simple ping pong command!").createGlobal(api).join();
    }
}
```

Discord now knows about your command and will offer it in your text channels if you type ``/``.
*Note: Creating a global command may take up to 1 hour to become usable* 

Next, let's see how we can let the bot send answers to this simple slash command:
```java
public class MyFirstBot {

    public static void main(String[] args) {
        // Insert your bot's token here
        String token = "your token";

        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();

        api.addSlashCommandCreateListener(event -> {
            SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
            if (slashCommandInteraction.getCommandName().equals("ping")) {
                slashCommandInteraction.createImmediateResponder()
                    .setContent("Pong!")
                    .setFlags(MessageFlag.EPHEMERAL) // Only visible for the user which invoked the command
                    .respond();
            }
        });
    }
}
```
A more detailed version of how to use slash commands can be found in the [wiki](https://javacord.org/wiki/basic-tutorials/interactions/commands.html) 
## 📦 Download / Installation

The recommended way to get Javacord is to use a build manager, like Gradle or Maven.  
If you are not familiar with build managers, you can follow this [setup guide](#-ide-setup) or download Javacord directly from [GitHub](https://github.com/Javacord/Javacord/releases/latest).

### Javacord Dependency

#### Gradle

```gradle
repositories { mavenCentral() }
dependencies { implementation 'org.javacord:javacord:3.3.2' }
```

#### Maven

```xml
<dependency>
    <groupId>org.javacord</groupId>
    <artifactId>javacord</artifactId>
    <version>3.3.2</version>
    <type>pom</type>
</dependency>
```

#### Sbt

```scala
libraryDependencies ++= Seq("org.javacord" % "javacord" % "3.3.2")
```

### Optional Logger Dependency

Any Log4j-2-compatible logging framework can be used to provide a more sophisticated logging experience
with being able to configure log format, log targets (console, file, database, Discord direct message, ...),
log levels per class, and much more.

For example, Log4j Core in Gradle
```gradle
dependencies { runtimeOnly 'org.apache.logging.log4j:log4j-core:2.11.0' }
```
Take a look at the [logger configuration](https://javacord.org/wiki/basic-tutorials/logger-config.html) wiki article for further information.

## 🔧 IDE Setup

If you have never used Gradle or Maven before, you should take a look at one of the setup tutorials:
* **[IntelliJ & Gradle](https://javacord.org/wiki/getting-started/setup/intellij-gradle.html)** _(recommended)_
* **[IntelliJ & Maven](https://javacord.org/wiki/getting-started/setup/intellij-maven.html)**
* **[Eclipse & Maven](https://javacord.org/wiki/getting-started/setup/eclipse-maven.html)**

## 🤝 Support

Javacord's Discord community is an excellent resource if you have questions about the library.  
* **[The Javacord server](https://discord.gg/0qJ2jjyneLEgG7y3)**

## 📒 Documentation

* The [Javacord wiki](https://javacord.org/wiki/) is a great place to get started.
* Additional documentation can be found in the [JavaDoc](https://docs.javacord.org/api/v/latest/).

## 💡 How to Create a Bot User and Get Its Token 

* **[Creating a Bot User Account](https://javacord.org/wiki/getting-started/creating-a-bot-account.html)**

## 📋 Version Numbers

The version number has a 3-digit format: `major.minor.trivial`
* `major`: Increased extremely rarely to mark a major release (usually a rewrite affecting very huge parts of the library).
* `minor`: Any backward incompatible change to the api.
* `trivial`: A backward compatible change to the **api**. This is usually an important bugfix (or a bunch of smaller ones)
 or a backwards compatible feature addition.
 
## 🔨 Deprecation Policy

A method or class that is marked as deprecated can be removed with the next minor release (but it will usually stay for
several minor releases). A minor release might remove a class or method without having it deprecated, but we will do our
best to deprecate it before removing it. We are unable to guarantee this though, because we might have to remove / replace
something due to changes made by Discord, which we are unable to control. Usually you can expect a deprecated method or
class to stay for at least 6 months before it finally gets removed, but this is not guaranteed.

## 🥇 Large Bots Using Javacord

Javacord is used by many large bots. Here are just a few of them:
* [**Yunite**](https://yunite.xyz/): A bot for Fortnite which runs on over 60,000 servers with over eight million users.
* [**Beemo**](https://beemo.gg/): A bot that prevents raids of many large servers such as [discord.gg/LeagueOfLegends](https://discord.gg/LeagueOfLegends), [discord.gg/VALORANT](https://discord.gg/VALORANT), and many more.

If you own a large bot that uses Javacord, feel free to add it to the list in a pull request!

## 🙌 More Examples 

### Using the MessageBuilder 🛠

<img align="right" src="https://javacord.org/img/javacord-readme/message-builder.png" width="34%">

The following example uses the built-in `MessageBuilder`. It is very useful to construct complex messages with images, code-blocks, embeds, or attachments.

```java
new MessageBuilder()
  .append("Look at these ")
  .append("awesome", MessageDecoration.BOLD, MessageDecoration.UNDERLINE)
  .append(" animal pictures! 😃")
  .appendCode("java", "System.out.println(\"Sweet!\");")
  .addAttachment(new File("C:/Users/JohnDoe/Pictures/kitten.jpg"))
  .addAttachment(new File("C:/Users/JohnDoe/Pictures/puppy.jpg"))
  .setEmbed(new EmbedBuilder()
        .setTitle("WOW")
        .setDescription("Really cool pictures!")
        .setColor(Color.ORANGE))
  .send(channel);
```

### Listeners in Their Own Class 🗃

All the examples use inline listeners for simplicity. For better readability it is also possible to have listeners in their own class:

```java
public class MyListener implements MessageCreateListener {

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        Message message = event.getMessage();
        if (message.getContent().equalsIgnoreCase("!ping")) {
            event.getChannel().sendMessage("Pong!");
        }
    }

}
```
```java
api.addListener(new MyListener());
```

For commands, you have the option of using one of the many existing command frameworks such as
* [**Command Framework**](https://github.com/Vampire/command-framework) by [@Vampire](https://github.com/Vampire)
* [**Sdcf4j**](https://github.com/Bastian/sdcf4j) by [@Bastian](https://github.com/Bastian)

or even write your own!

### Attach Listeners to Objects 📌

You can even attach listeners to objects.
Let's say you have a very sensitive bot.
As soon as someone reacts with a 👎 within the first 30 minutes of message creation, it deletes its own message. 

```java
api.addMessageCreateListener(event -> {
    if (event.getMessageContent().equalsIgnoreCase("!ping")) {
        event.getChannel().sendMessage("Pong!").thenAccept(message -> {
            // Attach a listener directly to the message
            message.addReactionAddListener(reactionEvent -> {
                if (reactionEvent.getEmoji().equalsEmoji("👎")) {
                    reactionEvent.deleteMessage();
                }
            }).removeAfter(30, TimeUnit.MINUTES);
        });
    }
}
```
The result then looks like this:

<img src="https://javacord.org/img/javacord-readme/sensitive-bot-round.gif">

### Creating a Temporary Voice Channel 💣🎧

This example creates a temporary voice channel that gets deleted when the last user leaves it or if nobody joins it within the first 30 seconds after creation.

```java
Server server = ...;
ServerVoiceChannel channel = new ServerVoiceChannelBuilder(server)
        .setName("tmp-channel")
        .setUserlimit(10)
        .create()
        .join();

// Delete the channel if the last user leaves
channel.addServerVoiceChannelMemberLeaveListener(event -> {
    if (event.getChannel().getConnectedUserIds().isEmpty()) {
        event.getChannel().delete();
    }
});

// Delete the channel if no user joined in the first 30 seconds 
api.getThreadPool().getScheduler().schedule(() -> {
    if (channel.getConnectedUserIds().isEmpty()) {
        channel.delete();
    }
}, 30, TimeUnit.SECONDS);
```

> **Note**: You should also make sure to remove the channels on bot shutdown (or startup)

## 📃 License

Javacord is distributed under the [Apache license version 2.0](./LICENSE).
