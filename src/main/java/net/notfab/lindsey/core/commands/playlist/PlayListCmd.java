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
import net.notfab.lindsey.shared.entities.playlist.Curator;
import net.notfab.lindsey.shared.entities.playlist.PlayList;
import net.notfab.lindsey.shared.entities.profile.server.MusicSettings;
import net.notfab.lindsey.shared.enums.PlayListSecurity;
import net.notfab.lindsey.shared.repositories.sql.CuratorRepository;
import net.notfab.lindsey.shared.repositories.sql.PlayListRepository;
import net.notfab.lindsey.shared.repositories.sql.server.MusicSettingsRepository;
import net.notfab.lindsey.shared.services.PlayListService;
import net.notfab.lindsey.shared.utils.Snowflake;
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
    private Snowflake snowflake;

    @Autowired
    private PlayListRepository repository;

    @Autowired
    private PlayListService service;

    @Autowired
    private MusicSettingsRepository musicSettings;

    @Autowired
    private CuratorRepository curators;

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
                MusicSettings settings = this.musicSettings.findById(member.getGuild().getIdLong())
                    .orElse(new MusicSettings(member.getGuild().getIdLong()));
                if (settings.getActivePlayList() == null) {
                    // None active
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.no_active"));
                    return true;
                }
                Optional<PlayList> oPlayList = this.repository.findById(settings.getActivePlayList());
                if (oPlayList.isEmpty()) {
                    // Deleted
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.no_active"));
                    return true;
                }
                msg.send(channel, this.createDetail(member, oPlayList.get()));
                return true;
            } else if (args[0].equalsIgnoreCase("list")) {
                // list
                List<MessageEmbed> pages = this.createList(member, this.repository.findAllByOwner(member.getUser().getIdLong()));
                if (pages.isEmpty()) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.no_playlist"));
                    return false;
                }
                Menu.create(channel, pages);
                return true;
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("create")) {
                // create <name>
                long count = this.repository.countByOwner(member.getIdLong());
                if (count == 2) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.limit_reached", 2, 2));
                }
                PlayList playList = new PlayList();
                playList.setId(this.snowflake.next());
                playList.setName(args[1]);
                playList.setOwner(member.getIdLong());
                playList.setSecurity(PlayListSecurity.PRIVATE);
                playList.setShuffle(false);
                playList.setLogoUrl("https://i.imgur.com/YcBNYVh.png");
                this.repository.save(playList);
                msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.created", playList.getName()));
                return true;
            } else if (args[0].equalsIgnoreCase("delete")) {
                // delete <name>
                Optional<PlayList> oPlayList = this.repository
                    .findByNameLikeAndOwner("%" + args[1] + "%", member.getUser().getIdLong());
                if (oPlayList.isEmpty()) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.not_found", args[1]));
                    return false;
                }
                this.repository.delete(oPlayList.get());
                msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.deleted", oPlayList.get().getName()));
                return true;
            } else if (args[0].equalsIgnoreCase("show")) {
                // show <name>
                Optional<PlayList> oPlayList = this.repository
                    .findByNameLikeAndOwner("%" + args[1] + "%", member.getUser().getIdLong());
                if (oPlayList.isEmpty()) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.not_found", args[1]));
                    return false;
                }
                msg.send(channel, this.createDetail(member, oPlayList.get()));
                return true;
            } else if (args[0].equalsIgnoreCase("use")) {
                // use <name>
                Optional<PlayList> oPlayList = this.repository.findTopByNameLike("%" + args[1] + "%");
                if (oPlayList.isEmpty()) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.not_found", args[1]));
                    return false;
                }
                if (!service.canRead(oPlayList.get(), member.getUser().getIdLong())) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.locked"));
                    return true;
                }
                MusicSettings settings = this.musicSettings.findById(member.getGuild().getIdLong())
                    .orElse(new MusicSettings(member.getGuild().getIdLong()));
                settings.setActivePlayList(oPlayList.get().getId());
                this.musicSettings.save(settings);
                msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.active", oPlayList.get().getName()));
                return true;
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("shuffle")) {
                // shuffle <name> <true/false>
                Optional<PlayList> oPlayList = this.repository.findTopByNameLike("%" + args[1] + "%");
                if (oPlayList.isEmpty()) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.not_found", args[1]));
                    return true;
                }
                Optional<Boolean> oBoolean = Utils.parseBoolean(args[2]);
                if (oBoolean.isEmpty()) {
                    msg.send(channel, sender(member) + i18n.get(member, "core.not_boolean", args[2]));
                    return false;
                }
                PlayList playList = oPlayList.get();
                if (!service.canModify(playList, member.getUser().getIdLong())) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.locked"));
                    return true;
                }
                playList.setShuffle(oBoolean.get());
                this.repository.save(playList);
                if (oBoolean.get()) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.shuffle_enabled"));
                } else {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.shuffle_disabled"));
                }
                return true;
            } else if (args[0].equalsIgnoreCase("logo")) {
                // logo <name> <url>
                Optional<PlayList> oPlayList = this.repository.findTopByNameLike("%" + args[1] + "%");
                if (oPlayList.isEmpty()) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.not_found", args[1]));
                    return true;
                }
                PlayList playList = oPlayList.get();
                if (!service.canModify(playList, member.getUser().getIdLong())) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.locked"));
                    return true;
                }
                String url = argsToString(args, 2);
                if (!Utils.isImgur(url)) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.not_imgur"));
                    return false;
                }
                playList.setLogoUrl(url);
                this.repository.save(playList);
                msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.logo_updated", playList.getName()));
                return true;
            } else if (args[0].equalsIgnoreCase("security")) {
                // security <name> <security>
                Optional<PlayList> oPlayList = this.repository
                    .findByNameLikeAndOwner("%" + args[1] + "%", member.getUser().getIdLong());
                if (oPlayList.isEmpty()) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.not_found", args[1]));
                    return true;
                }
                Optional<PlayListSecurity> oSecurity = PlayListSecurity.find(args[2]);
                if (oSecurity.isEmpty()) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.not_security"));
                    return false;
                }
                PlayList playList = oPlayList.get();
                playList.setSecurity(oSecurity.get());
                this.repository.save(playList);
                msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.security_updated", playList.getName(),
                    oSecurity.get().name()));
                return true;
            }
        } else if (args.length == 4) {
            Optional<PlayList> oPlayList = this.repository.findByNameLikeAndOwner("%" + args[1] + "%", member.getIdLong());
            if (oPlayList.isEmpty()) {
                msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.not_found", args[1]));
                return true;
            }
            if (args[1].equalsIgnoreCase("curators")) {
                // <name> curators <add/remove> <name>
                Member target = FinderUtil.findMember(argsToString(args, 3), message);
                if (target == null) {
                    msg.send(channel, sender(member) + i18n.get(member, "core.member_nf"));
                    return false;
                }
                Optional<Curator> oCurator = this.curators.findByPlayListAndUserId(oPlayList.get(), target.getIdLong());
                if (args[2].equalsIgnoreCase("add")) {
                    if (oCurator.isEmpty()) {
                        Curator curator = new Curator();
                        curator.setId(snowflake.next());
                        curator.setPlayList(oPlayList.get());
                        curator.setName(target.getUser().getAsTag());
                        curator.setUserId(target.getIdLong());
                        this.curators.save(curator);
                    }
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.curator_added", target.getUser().getAsTag()));
                    return true;
                } else if (args[2].equalsIgnoreCase("remove")) {
                    oCurator.ifPresent(curator -> this.curators.delete(curator));
                    msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.curator_removed", target.getUser().getAsTag()));
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
            .usage("L!pl <list|show|create|use|delete|shuffle|logo|security|name> [name|curators] [true|false|add|remove|security] [user]")
            .permission("commands.playlist")
            .addExample("L!pl list")
            .addExample("L!pl show")
            .addExample("L!pl create edm")
            .addExample("L!pl use edm")
            .addExample("L!pl delete edm")
            .addExample("L!pl shuffle edm true")
            .addExample("L!pl shuffle edm false")
            .addExample("L!pl logo edm <https://i.imgur.com/YcBNYVh.png>")
            .addExample("L!pl security edm PUBLIC")
            .addExample("L!pl edm curators add lindsey")
            .addExample("L!pl edm curators remove lindsey");
        return HelpArticle.of(page);
    }

    private MessageEmbed createDetail(Member member, PlayList playList) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setThumbnail(playList.getLogoUrl());
        builder.setColor(GFXUtils.getAverageColor(member.getUser().getEffectiveAvatarUrl()));
        builder.setTitle(playList.getName());
        builder.addField(i18n.get(member, "commands.playlist.songs"), this.service.size(playList.getId())
            + " - [Link](https://notfab.net/lindsey/playlists/" + playList.getId() + ")", true);
        builder.addField(i18n.get(member, "commands.playlist.security"), playList.getSecurity().name(), true);
        builder.addField(i18n.get(member, "commands.playlist.curators"), String.valueOf(
            this.curators.countByPlayList(playList)
        ), true);
        builder.addField(i18n.get(member, "commands.playlist.shuffle"), String.valueOf(
            playList.isShuffle()
        ), true);
        builder.setFooter(i18n.get(member, "core.request", member.getEffectiveName() + "#" + member.getUser().getDiscriminator()),
            member.getUser().getEffectiveAvatarUrl());
        builder.setTimestamp(Instant.now());
        return builder.build();
    }

    private List<MessageEmbed> createList(Member member, List<PlayList> playLists) {
        return playLists.stream()
            .map(pl -> this.createDetail(member, pl))
            .collect(Collectors.toList());
    }

}
