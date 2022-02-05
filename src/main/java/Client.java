import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;
import java.util.Date;

public class Client {

    public static void main(String[] args) {
        DiscordClient client = DiscordClient.create("TOKEN"); // Replace TOKEN by your discord client token

        Mono<Void> login = client.withGateway((GatewayDiscordClient gateway) -> {
            Mono<Void> printOnLogin = gateway.on(ReadyEvent.class, event ->
                    Mono.fromRunnable(() -> {
                        final User self = event.getSelf();
                        System.out.printf("Logged in as %s#%s%n", self.getUsername(), self.getDiscriminator());
                    }))
                    .then();

            Mono<Void> handlePingCommand = gateway.on(MessageCreateEvent.class, event -> {
                Message message = event.getMessage();

                if (message.getContent().equalsIgnoreCase("!ping")) {
                    return message.getChannel()
                            .flatMap(channel -> {
                                Date date = new Date();
                                return channel.createMessage("Pong `" + (date.getTime() - message.getTimestamp().toEpochMilli()) + "ms`");
                            });
                }

                return Mono.empty();
            }).then();

            return printOnLogin.and(handlePingCommand);
        });


        login.block();
    }

}
