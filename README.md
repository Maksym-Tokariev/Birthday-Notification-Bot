## Birthday notification bot 
Telegram bot created for reminding group members about the upcoming birthday of one of the members and congratulating the birthday boy. 

## Usage
The bot should be added to the group that you need. Next, the bot will provide you with a link for registration, 
by clicking on it you will enter your date of birth, if you are not already registered, and the bot will add you to the notification system. 
If you are already registered, the bot will add you to the group.
The bot will notify you about the birthday of one of the group members the day before the event, and on the birthday it will congratulate the birthday person.

## The archetype of the project
  When adding a bot to a group, the bot is initialized in the init method of the BotInitializer class, then the bot commands are initialized in the initializeBotCommands method of the TelegramBot class. 
 After that, the update parameter is passed to the onUpdateReceived method of the same class, which calls the MainCommandHandler class with its handleUpdate method. 
 In the handleUpdate method of MainCommandHandler class the update type is defined: when adding a bot to a group, handleChatMessageUpdate is executed; 
 when receiving a message from a user, handleTextMessage is called; when receiving data from a button - handleQuery. 
  The role of the handleChatMessageUpdate method is to generate a link to registration, which contains groupId and groupName for further processing. 
  The handleQuery method is needed exclusively for deleting the group selected by the user. It calls the UserService deleteUserGroup method. 
  The handleTextMessage handles only user commands. It calls BotState to monitor the user's state, CommandHandler to process the command accordingly, and further, if the handler is not null, performs the processing. 
If the handler is null, the BotState prefetch is performed. This is necessary for the /start command, after which the user does not enter any commands, but only provides data. 
 Commands are processed by the handle method of the CommandHandler class, for which each command was mapped to a specific class in the CommandHandlerConfig class.
The following list of commands is available: 
- `/start` - register user
- `/help` - list of commands
- `/mydata` - shows user information
- `/deletedata` - delete information about use
- `/mygroups` - list of user's groups
- `/removegroup` - delete selected group

## Contact Us If you have questions or suggestions, contact us: 
- Email: paranoid063@gmail.com 
- Telegram: @paranoid063

## Install 
```bash
git clone https://github.com/username/project.git](https://github.com/Maksym-Tokariev/Birthday-Notification-Bot.git
cd project
npm install
