package net.notfab.lindsey.core.service;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.notfab.lindsey.core.framework.Utils;
import net.notfab.lindsey.core.framework.profile.ProfileManager;
import net.notfab.lindsey.core.repositories.mongo.PlaylistRepository;
import net.notfab.lindsey.shared.entities.playlist.Curator;
import net.notfab.lindsey.shared.entities.playlist.PlayList;
import net.notfab.lindsey.shared.entities.playlist.PlayListCursor;
import net.notfab.lindsey.shared.entities.playlist.Song;
import net.notfab.lindsey.shared.entities.profile.ServerProfile;
import net.notfab.lindsey.shared.enums.PlayListSecurity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class PlayListService {

    private final PlaylistRepository repository;
    private final ProfileManager profiles;

    public PlayListService(PlaylistRepository repository, ProfileManager profiles) {
        this.repository = repository;
        this.profiles = profiles;
    }

    public Optional<PlayList> findActive(Guild guild) {
        return this.findActive(guild.getIdLong());
    }

    public Optional<PlayList> findActive(long guildId) {
        ServerProfile profile = profiles.getGuild(guildId);
        return Optional.ofNullable(profile.getActivePlayList());
    }

    public void setActive(Guild guild, PlayList playList) {
        this.setActive(guild.getIdLong(), playList);
    }

    public void setActive(long guild, PlayList playList) {
        ServerProfile profile = profiles.getGuild(guild);
        profile.setActivePlayList(playList);
        profiles.save(profile);
    }

    public PlayList create(User owner, String name) {
        long count = repository.countAllByOwner(owner.getIdLong());
        if (count == 2) {
            return null;
        }
        PlayList playList = new PlayList();
        playList.setId(UUID.randomUUID().toString());
        playList.setOwner(owner.getIdLong());
        playList.setName(name);
        playList.setShuffle(false);
        playList.setLogoUrl("https://i.imgur.com/YcBNYVh.png");
        playList.setSecurity(PlayListSecurity.PRIVATE);
        return repository.save(playList);
    }

    public PlayList findByName(User owner, String name) {
        return repository.findByOwnerAndNameLike(owner.getIdLong(), name)
            .orElse(null);
    }

    public void delete(PlayList playList) {
        repository.delete(playList);
    }

    public long add(PlayList playList, Song... songs) {
        long before = playList.getSongs().size();
        Stream.of(songs)
            .filter(Objects::nonNull)
            .filter(song -> !playList.getSongs().contains(song))
            .forEach(song -> playList.getSongs().add(song));
        repository.save(playList);
        return playList.getSongs().size() - before;
    }

    public long remove(PlayList playList, String name) {
        long before = playList.getSongs().size();
        Song fake = new Song();
        fake.setUrl(name);
        fake.setName(name);
        fake.setAuthor(name);
        playList.getSongs().removeIf((song) -> song.isSimilar(fake));
        repository.save(playList);
        return before - playList.getSongs().size();
    }

    public Song findSong(PlayList playList, String name) {
        Optional<Integer> oPos = Utils.safeInt(name);
        if (oPos.isPresent()) {
            int pos = oPos.get();
            if (pos > playList.getSongs().size() || pos < 1) {
                return null;
            }
            return playList.getSongs().get(pos - 1);
        }
        Song fake = new Song();
        fake.setUrl(name);
        fake.setName(name);
        fake.setAuthor(name);
        for (Song song : playList.getSongs()) {
            if (song.isSimilar(fake)) {
                return song;
            }
        }
        return null;
    }

    public boolean hasPermission(PlayList playList, User user) {
        if (playList.getOwner() == user.getIdLong()) {
            return true;
        }
        if (playList.getSecurity() == PlayListSecurity.PUBLIC) {
            return true;
        }
        return playList.getCurators().stream()
            .anyMatch(curator -> curator.getId() == user.getIdLong());
    }

    public Song findNextSong(PlayList playList, long guild) {
        if (playList.getSongs().isEmpty()) {
            return null;
        }
        ServerProfile profile = profiles.getGuild(guild);
        PlayListCursor cursor = profile.getCursor();
        if (cursor == null) {
            cursor = new PlayListCursor();
            cursor.setPosition(-1);
        }
        // TODO: Shuffle mode
        int position = cursor.getPosition() + 1;
        if (position >= playList.getSongs().size()) {
            position = 0;
        }
        return playList.getSongs().get(position);
    }

    public boolean updateCursor(PlayList playList, Song song, long guild) {
        PlayListCursor cursor = PlayListCursor.fromSong(song);
        int position = -1;
        for (int i = 0; i < playList.getSongs().size(); i++) {
            Song target = playList.getSongs().get(i);
            if (target.equals(song)) {
                position = i;
                break;
            }
        }
        if (position == -1) {
            return false;
        }
        cursor.setPosition(position);

        ServerProfile profile = profiles.getGuild(guild);
        profile.setCursor(cursor);
        profiles.save(profile);
        return true;
    }

    public void addCurator(PlayList playList, Curator user) {
        if (playList.getCurators().stream()
            .noneMatch(curator -> curator.getId() == user.getId())) {
            playList.getCurators().add(user);
            repository.save(playList);
        }
    }

    public void delCurator(PlayList playList, long userId) {
        playList.getCurators()
            .removeIf(curator -> curator.getId() == userId);
        repository.save(playList);
    }

    public List<PlayList> findAllByOwner(long userId) {
        return repository.findAllByOwner(userId);
    }

    public void setShuffle(PlayList playList, boolean shuffle) {
        playList.setShuffle(shuffle);
        repository.save(playList);
    }

    public void setLogo(PlayList playList, String url) {
        playList.setLogoUrl(url);
        repository.save(playList);
    }

    public void setSecurity(PlayList playList, PlayListSecurity security) {
        playList.setSecurity(security);
        repository.save(playList);
    }

}
