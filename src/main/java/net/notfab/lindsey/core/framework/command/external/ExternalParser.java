package net.notfab.lindsey.core.framework.command.external;

import lombok.Data;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.notfab.lindsey.shared.entities.commands.CommandOption;
import net.notfab.lindsey.shared.entities.commands.CommandRequest;

import java.util.*;

@Data
public class ExternalParser {

    public static CommandRequest run(StringBuilder path, List<CommandOption> optList, Deque<String> args,
                                     Member member, GuildMessageReceivedEvent event) throws BadArgumentException {
        CommandRequest request = new CommandRequest();
        request.setCommandName(path.toString());
        request.setGuild(FakeBuilder.toFake(event.getGuild()));
        request.setMember(FakeBuilder.toFake(member));
        request.setChannel(FakeBuilder.toFake(event.getChannel()));
        if (optList.isEmpty()) {
            request.setOptions(new HashMap<>());
            return request;
        }
        Map<String, Object> opts = new HashMap<>();
        for (int i = 0; i < optList.size(); i++) {
            CommandOption option = optList.get(i);
            boolean isLastOption = (optList.size() - 1) == i;

            String argument;
            if (isLastOption && args.size() > 1 && !option.isList()) {
                StringBuilder builder = new StringBuilder();
                do {
                    builder.append(" ").append(args.poll());
                } while (!args.isEmpty());
                argument = builder.substring(1);
            } else {
                argument = args.poll();
            }

            if (argument == null && option.isRequired()) {
                throw new BadArgumentException("core.missing_required", option.getName());
            }

            if (option.isList()) {
                List<Object> objects = new ArrayList<>();
                boolean consuming = true;
                objects.add(OptionParser.parse(option, argument, event));
                do {
                    String arg = args.peek();
                    if (arg == null) {
                        break;
                    }
                    try {
                        objects.add(OptionParser.parse(option, arg, event));
                        args.poll();
                    } catch (BadArgumentException ex) {
                        consuming = false;
                    }
                } while (consuming);
                opts.put(option.getName().toLowerCase(), objects);
            } else {
                opts.put(option.getName().toLowerCase(), OptionParser.parse(option, argument, event));
            }
        }
        request.setOptions(opts);
        return request;
    }


}
