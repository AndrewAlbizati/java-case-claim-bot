package com.github.AndrewAlbizati.commands;

import com.github.AndrewAlbizati.Bot;
import com.github.AndrewAlbizati.enums.PrivilegeLevel;
import com.github.AndrewAlbizati.models.User;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.component.*;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class JoinCommand implements SlashCommandCreateListener {
    private final Bot bot;

    public JoinCommand(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();

        // Ignore other slash commands
        if (!interaction.getCommandName().equalsIgnoreCase("join")) {
            return;
        }

        event.getInteraction().respondWithModal("create-user-modal", "Add User",
                ActionRow.of(TextInput.create(TextInputStyle.SHORT, "firstname_input", "First Name", true)),
                ActionRow.of(TextInput.create(TextInputStyle.SHORT, "lastname_input", "Last Name", true)),
                ActionRow.of(TextInput.create(TextInputStyle.SHORT, "email_input", "Email", true)),
                ActionRow.of(TextInput.create(TextInputStyle.SHORT, "birthday_input", "Birthday (MM/DD/YYYY)", true))
        );

        event.getApi().addModalSubmitListener(submitEvent -> {
            if (!submitEvent.getModalInteraction().getCustomId().equals("create-user-modal")) {
                return;
            }

            List<String> inputs = submitEvent.getModalInteraction().getTextInputValues();

            String firstName = inputs.get(0);
            String lastName = inputs.get(1);
            String email = inputs.get(2);
            String birthday = inputs.get(3);

            int month = Integer.parseInt(birthday.split("/")[0]);
            int day = Integer.parseInt(birthday.split("/")[1]);
            int year = Integer.parseInt(birthday.split("/")[2]);

            User user = new User(
                    interaction.getUser().getId(),
                    firstName,
                    lastName,
                    email,
                    Date.valueOf(LocalDate.of(year, month, day)),
                    Date.valueOf(LocalDate.now()),
                    true,
                    PrivilegeLevel.TECH
            );

            try {
                user.addToDatabase(bot.getConnection());
                submitEvent.getModalInteraction().createImmediateResponder()
                        .setContent("👍")
                        .setFlags(MessageFlag.EPHEMERAL)
                        .respond();
            } catch (SQLException e) {
                e.printStackTrace();
                submitEvent.getModalInteraction().createImmediateResponder()
                        .setContent("Error! Please try again.")
                        .setFlags(MessageFlag.EPHEMERAL)
                        .respond();
            }
        });
    }
}