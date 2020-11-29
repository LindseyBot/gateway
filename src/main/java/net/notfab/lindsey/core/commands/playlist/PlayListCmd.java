package net.notfab.lindsey.core.commands.playlist;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.core.framework.GFXUtils;
import net.notfab.lindsey.core.framework.Utils;
import net.notfab.lindsey.core.framework.command.*;
import net.notfab.lindsey.core.framework.command.help.HelpArticle;
import net.notfab.lindsey.core.framework.command.help.HelpPage;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import net.notfab.lindsey.core.framework.menu.Menu;
import net.notfab.lindsey.core.framework.models.Curator;
import net.notfab.lindsey.core.framework.models.PlayList;
import net.notfab.lindsey.core.service.PlayListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PlayListCmd implements Command {

    @Autowired
    private Messenger msg;

    @Autowired
    private Translator i18n;

    @Autowired
    private PlayListService service;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("playlist")
            .alias("pl")
            .permission("commands.playlist", "permissions.command")
            .module(Modules.MUSIC)
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (args.length == 0) {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
            return false;
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("show")) {
                // show
                Optional<PlayList> oPlayList = service.findActive(member.getGuild());
                if (oPlayList.isEmpty()) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.no_active"));
                    return true;
                }
                msg.send(channel, this.createDetail(member, oPlayList.get()));
                return true;
            } else if (args[0].equalsIgnoreCase("list")) {
                // list
                List<MessageEmbed> pages = this.createList(member, service.findAllByOwner(member.getUser().getIdLong()));
                Menu.create(channel, pages);
                return true;
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("create")) {
                // create <name>
                PlayList playList = this.service.create(member.getUser(), args[1]);
                if (playList == null) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.limit_reached", 2, 2));
                } else {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.created", playList.getName()));
                }
                return true;
            } else if (args[0].equalsIgnoreCase("delete")) {
                // delete <name>
                PlayList playList = this.service.findByName(member.getUser(), args[1]);
                if (playList == null) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.not_found", args[1]));
                    return false;
                }
                if (playList.getOwner() != member.getUser().getIdLong()) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.locked"));
                } else {
                    this.service.delete(playList);
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.deleted", playList.getName()));
                }
                return true;
            } else if (args[0].equalsIgnoreCase("show")) {
                // show <name>
                PlayList playList = service.findByName(member.getUser(), args[1]);
                if (playList == null) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.not_found", args[1]));
                    return true;
                }
                msg.send(channel, this.createDetail(member, playList));
                return true;
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("shuffle")) {
                // shuffle <name> <true/false>
                PlayList playList = service.findByName(member.getUser(), args[1]);
                if (playList == null) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.not_found", args[1]));
                    return true;
                }
                Optional<Boolean> oBoolean = Utils.parseBoolean(args[2]);
                if (oBoolean.isEmpty()) {
                    msg.send(channel, sender(member) + i18n.get(member, "core.not_boolean", args[2]));
                    return false;
                }
                if (!service.hasPermission(playList, member.getUser())) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.locked"));
                    return true;
                }
                service.setShuffle(playList, oBoolean.get());
                if (oBoolean.get()) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.shuffle_enabled"));
                } else {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.shuffle_disabled"));
                }
                return true;
            } else if (args[0].equalsIgnoreCase("logo")) {
                // logo <name> <url>
                PlayList playList = service.findByName(member.getUser(), args[1]);
                if (playList == null) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.not_found", args[1]));
                    return true;
                }
                if (!service.hasPermission(playList, member.getUser())) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.locked"));
                    return true;
                }
                String url = argsToString(args, 2);
                if (!Utils.isImgur(url)) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.not_imgur"));
                    return false;
                }
                service.setLogo(playList, url);
                msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.logo_updated", playList.getName()));
                return true;
            }
        } else if (args.length == 4) {
            PlayList playList = this.service.findByName(member.getUser(), args[0]);
            if (playList == null) {
                msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.not_found", args[0]));
                return false;
            }
            if (playList.getOwner() != member.getUser().getIdLong()) {
                msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.locked"));
                return true;
            }
            if (args[1].equalsIgnoreCase("curators")) {
                // <name> curators <add/remove> <name>
                Member target = FinderUtil.findMember(argsToString(args, 3), message);
                if (target == null) {
                    msg.send(channel, sender(member) + i18n.get(member, "core.member_nf"));
                    return false;
                }
                if (args[2].equalsIgnoreCase("add")) {
                    service.addCurator(playList, new Curator(target.getIdLong(), target.getUser().getAsTag()));
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.curator_added",
                        target.getUser().getAsTag()));
                    return true;
                } else if (args[2].equalsIgnoreCase("remove")) {
                    service.delCurator(playList, target.getIdLong());
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.curator_removed",
                        target.getUser().getAsTag()));
                    return true;
                }
            }
        }
        HelpArticle article = this.help(member);
        article.send(channel, member, args, msg, i18n);
        return false;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("playlist")
            .text("commands.playlist.description")
            .usage("L!pl <list|show|create|delete|shuffle|logo|name> [name|curators] [true|false|add|remove] [user]")
            .permission("commands.playlist")
            .addExample("L!pl list")
            .addExample("L!pl show")
            .addExample("L!pl create edm")
            .addExample("L!pl delete edm")
            .addExample("L!pl shuffle edm true")
            .addExample("L!pl shuffle edm false")
            .addExample("L!pl logo edm <https://i.imgur.com/YcBNYVh.png>")
            .addExample("L!pl edm curators add lindsey")
            .addExample("L!pl edm curators remove lindsey");
        return HelpArticle.of(page);
    }

    private MessageEmbed createDetail(Member member, PlayList playList) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setThumbnail(playList.getLogoUrl());
        builder.setColor(GFXUtils.getAverageColor(member.getUser().getEffectiveAvatarUrl()));
        builder.setTitle(playList.getName());
        builder.addField("Songs", playList.getSongs().size() + " - [Link](https://notfab.net/lindsey/playlists/" + playList.getId() + ")", true);
        builder.addField("Security", playList.getSecurity().name(), true);
        builder.addField("Curators", String.valueOf(playList.getCurators().size()), true);
        builder.addField("Shuffle", String.valueOf(playList.isShuffle()), true);

        builder.setFooter("Requested By " + member.getUser().getAsTag(), member.getUser().getEffectiveAvatarUrl());
        builder.setTimestamp(Instant.now());
        return builder.build();
    }

    private List<MessageEmbed> createList(Member member, List<PlayList> playLists) {
        return playLists.stream()
            .map(pl -> this.createDetail(member, pl))
            .collect(Collectors.toList());
    }

}
