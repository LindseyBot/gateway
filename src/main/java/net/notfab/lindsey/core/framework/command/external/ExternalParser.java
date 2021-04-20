package net.notfab.lindsey.core.framework.command.external;

import lombok.Data;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.notfab.lindsey.shared.entities.commands.CommandData;
import net.notfab.lindsey.shared.entities.commands.CommandOption;

import java.util.*;

@Data
public class ExternalParser {

    public static CommandData run(StringBuilder path, List<CommandOption> optList, Deque<String> args,
                                  Member member, GuildMessageReceivedEvent event) throws BadArgumentException {
        CommandData data = new CommandData();
        data.setCommandName(path.toString());
        data.setGuild(FakeBuilder.toFake(event.getGuild()));
        data.setMember(FakeBuilder.toFake(member));
        data.setChannel(FakeBuilder.toFake(event.getChannel()));
        if (optList.isEmpty()) {
            data.setOptions(new HashMap<>());
            return data;
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
        data.setOptions(opts);
        return data;
    }


}
