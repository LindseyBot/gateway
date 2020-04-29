package net.notfab.lindsey.core.commands.moderation;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.framework.command.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class Prune implements Command {
    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
                .name("prune")
                .module(Modules.FUN)
                .permission("commands.prune", "Permission to use the base command")
                .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Bundle bundle) throws Exception {
        if (channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE)) {
            long twoWeeks = ((System.currentTimeMillis() - 1209600000) - 1420070400000L) << 22;
            List<Message> del = new ArrayList<>();
            List<Member> target = new ArrayList<>();
            if (args.length > 0) {
                int i = Integer.parseInt(args[0]) + 1;
                if (i < 2) {
                    //erro <1
                } else if (i > 100) {
                    //erro >100
                } else {
                    Stream.of(args).forEach(u -> {
                        Optional<Member> user = FinderUtil.findMember(u, channel.getGuild());
                        if (user.isPresent()) {
                            target.add(user.get());
                        } else {
                            //user nao existe
                        }
                    });
                    List<Message> history = channel.getHistory().retrievePast(i).complete();
                    if (args.length == 1) {
                        history.forEach(m -> {
                            if (Long.parseLong(m.getId()) > twoWeeks) {
                                del.add(m);
                            }
                        });
                    } else {
                        history.stream().filter(m -> target.contains(m.getAuthor())).forEach(m -> {
                            if (Long.parseLong(m.getId()) > twoWeeks) {
                                del.add(m);
                            }
                        });
                    }
                    channel.deleteMessages(del).queue(d -> {
                        channel.sendMessage(sender(member) + " " + (del.size() - 1) + " Messages deleted. This message will auto-destruct in 5 seconds.").queue(m -> {
                            m.delete().queueAfter(5, TimeUnit.SECONDS, x -> {
                            }, e -> {
                            });
                        });
                    });
                }
            } else {
                //help-me
            }
        } else {
            //bot nao tem permissao
        }
        return false;
    }

}
