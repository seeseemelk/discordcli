# DiscordCLI
DiscordCLI is a Discord bot that will allow people to use a Linux command-line from
a text channel of choice.
Note that it is not recommended to use this on a production system or on a virtual
machine that is not well sandboxed.

## Usage
Any message typed into a chosen text channel will be written into the stdin of a program.
Stdout will be posted as messages into the text channel.
A '!' will cause the program to kill the process and start it again.

## Configuration
To configure the bot copy config.ini.example and modify it to suit your needs.