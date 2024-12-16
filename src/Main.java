import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.util.ArrayList;
import java.util.List;

public class Main extends TelegramLongPollingBot {

    private int targetHour=2;
    private int targetMinute=9;

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Override
    public String getBotToken() {
        try {
            return readTokenFromFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotUsername() {
        return "Raffle_unic_bot";
    }

    private static String readTokenFromFile() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("Secret.txt"))) {
            return reader.readLine();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.equalsIgnoreCase("/start")) {
                sendMsg(message,"Здравствуй!");
                SendPhoto sendPhoto = new SendPhoto(); //Объект для отправки фото
                sendPhoto.setChatId(chatId);

                // Загрузка картинки с диска
                File photo = new File("C:\\Users\\Acer\\IdeaProjects\\RaffleBot\\img.png");
                InputFile inputFile = new InputFile(photo);
                sendPhoto.setPhoto(inputFile);

                String caption = "Розыгрыш ничего. Удачи! (Она вам понадобится.)";
                sendPhoto.setCaption(caption);

                try {
                    execute(sendPhoto);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
            if (messageText.equalsIgnoreCase("Розыгрыш")) {
                try {
                    execute(sendInlineKeyBoardMessage(update.getMessage().getChatId()));
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
            if (messageText.equalsIgnoreCase("Количество участников")){
                List<User> users = Work_MySql.getAllUsers(1);
                if (users.isEmpty()){
                    SendMessage message_ans = new SendMessage();
                    message_ans.setChatId(chatId);
                    message_ans.setText("На данный момент в розыгрыше никто не участвует :(");
                    try {
                        execute(message_ans);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }
                else {
                    SendMessage message_ans = new SendMessage();
                    message_ans.setChatId(chatId);
                    message_ans.setText("Количество участников на данный момент: " + users.size());
                    try {
                        execute(message_ans);
                        int kolvo=0;
                        for (User user : users) {
                            SendMessage message_ans_m = new SendMessage();
                            message_ans_m.setChatId(chatId);
                            kolvo = kolvo + 1;
                            message_ans_m.setText(kolvo + ") FirstName: " + user.firstName + ", LastName: " + user.lastName + ", UserName: @" + user.userName);
                            try {
                                execute(message_ans_m);
                            } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }

                        }
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            if (messageText.equalsIgnoreCase("Информация")){
                SendMessage message_ans = new SendMessage();
                message_ans.setChatId(chatId);
                message_ans.setText("Розыгрыш НИЧЕГО! Присоединяйтесь! (Или нет. Все равно ничего не получите.) Мы разыгрываем самую чистую, первозданную форму существования – Ничто! Откажитесь от материальных благ и обретите духовное просветление.(В качестве утешительного приза вы получите… ну, вы поняли.)");
                try {
                    execute(message_ans);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }

                message_ans.setText("Время розыгрыша - " + targetHour + ":" + targetMinute);
                try {
                    execute(message_ans);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }

            }
        }
        else if(update.hasCallbackQuery()){
            String callbackData = update.getCallbackQuery().getData();
            long chatId2 = update.getCallbackQuery().getMessage().getChatId();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();
            String username = update.getCallbackQuery().getFrom().getUserName();
            String firstName = update.getCallbackQuery().getFrom().getFirstName();
            String lastName = update.getCallbackQuery().getFrom().getLastName();
            System.out.println(chatId2);
            System.out.println(username);
            System.out.println(firstName);
            System.out.println(lastName);
            System.out.println(messageId);
            System.out.println();

            try {
                if (callbackData.equals("register")) {
                    int check = Work_MySql.ADDandDEL(chatId2, firstName, lastName, username, 1);

                    if (check == 1){
                        editMessageText(chatId2, messageId, "Вы уже зарегистрированы в розыгрыше. Больше нельзя!");
                    } else if (check != 2){
                        editMessageText(chatId2, messageId, "Вы зарегистрировались в розыгрыше!");
                    }
                } else if (callbackData.equals("decline")) {
                    int check = Work_MySql.ADDandDEL(chatId2, firstName, lastName, username, 2);

                    if (check == 2){
                        editMessageText(chatId2, messageId, "Вас нет в списке участников розыгрыша. Нельзя удалить того, кого нет.");
                    } else if (check != 1){
                        editMessageText(chatId2, messageId, "Вы удалены из участников розыгрыша!");
                    }
                }
            } catch (TelegramApiException e) {
                logger.error("Error registering bot: ", e);
            }
        }
    }

    private void editMessageText(long chatId, int messageId, String text) throws TelegramApiException{
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId(messageId);
        editMessageText.setText(text);
        execute(editMessageText);
    }

    public static SendMessage sendInlineKeyBoardMessage(long chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();

        inlineKeyboardButton1.setText("Хочу");
        inlineKeyboardButton1.setCallbackData("register");
        inlineKeyboardButton2.setText("Не Хочу");
        inlineKeyboardButton2.setCallbackData("decline");

        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButton1);
        keyboardButtonsRow2.add(inlineKeyboardButton2);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        rowList.add(keyboardButtonsRow2);
        inlineKeyboardMarkup.setKeyboard(rowList);

        SendMessage ex = new SendMessage();
        ex.setChatId(chatId);
        ex.setText("Вы хотите участвовать в розыгрыше ничего?");
        ex.setReplyMarkup(inlineKeyboardMarkup);
        return ex;
    }

    public void sendMsg (Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow keyboardFirstRow1 = new KeyboardRow();
        keyboardFirstRow1.add("Розыгрыш");
        keyboard.add(keyboardFirstRow1);

        KeyboardRow keyboardFirstRow2 = new KeyboardRow();
        keyboardFirstRow2.add("Количество участников");
        keyboard.add(keyboardFirstRow2);

        KeyboardRow keyboardFirstRow3 = new KeyboardRow();
        keyboardFirstRow3.add("Информация");
        keyboard.add(keyboardFirstRow3);

        replyKeyboardMarkup.setKeyboard(keyboard);

        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setReplyToMessageId(message.getMessageThreadId());
        sendMessage.setText(text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            logger.error("Error registering bot: ", e);
        }
    }

    private void sendResponse(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Error registering bot: ", e);
        }
    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            botsApi.registerBot(new Main());
        } catch (TelegramApiException e) {
            logger.error("Error registering bot: ", e);
        }
    }

///////////////////////////////////////////////////////////////////////////////////////////////////

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public Main() {
        scheduleTask(targetHour,targetMinute);
    }

    private void scheduleTask(int targetHour, int targetMinute) {
        this.targetHour = targetHour;
        this.targetMinute = targetMinute;

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.HOUR_OF_DAY, targetHour);
        calendar.set(java.util.Calendar.MINUTE, targetMinute);
        calendar.set(java.util.Calendar.SECOND, 0);

        long delay = calendar.getTimeInMillis() - System.currentTimeMillis();

        if (delay < 0) {
            delay += TimeUnit.DAYS.toMillis(1);
        }

        scheduler.scheduleAtFixedRate(this::sendResponseToAll, delay, TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS);

    }
    private void sendResponseToAll() {
        System.out.println("ЕСТЬ");
        List<User> users = Work_MySql.getAllUsers(2);
        if (!users.isEmpty()) {
            User userDetails2 = users.getFirst();
            System.out.println(userDetails2.userName);
            if (Objects.equals(userDetails2.lastName, "null")) {
                userDetails2.lastName = "";
            }
            sendMessageToWinner(userDetails2.chatID, userDetails2.firstName, userDetails2.lastName, userDetails2.userName);
        } else {
            System.out.println("Пользователи не найдены");
        }
    }

    public void sendMessageToWinner(long chatId, String firstName, String lastName, String userName) {
//        SendMessage sendMessage = new SendMessage();
//        sendMessage.setChatId(String.valueOf(chatId));
//        sendMessage.setText(firstName + " " + lastName + " "  +"(" + userName + ")" + " ,Ты победитель!!! Твой приз - огромное ничего!!(Используй её с умом)");

        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);

        File photo = new File("C:\\Users\\Acer\\IdeaProjects\\RaffleBot\\img_1.png");
        InputFile inputFile = new InputFile(photo);
        sendPhoto.setPhoto(inputFile);

        String caption = (firstName + " " + lastName + " "  +"(" + userName + ")" + " ,Ты победитель!!! Твой приз - огромное ничего!!(Используй её с умом)");
        sendPhoto.setCaption(caption);
        try {
            execute(sendPhoto);
            System.out.println("Winner: " + firstName + " " + lastName + " "  +"(" + userName + ")");
        } catch (TelegramApiException e) {
            logger.error("Error sending photo to user: ", e);
        }
    }
}