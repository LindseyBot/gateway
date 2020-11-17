package net.notfab.lindsey.core.service;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.notfab.lindsey.core.framework.models.PlayList;
import net.notfab.lindsey.core.framework.models.PlayListCursor;
import net.notfab.lindsey.core.framework.models.PlayListSecurity;
import net.notfab.lindsey.core.framework.models.Song;
import net.notfab.lindsey.core.framework.options.Option;
import net.notfab.lindsey.core.framework.options.OptionManager;
import net.notfab.lindsey.core.repositories.mongo.PlaylistRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class PlayListService {

    private final PlaylistRepository repository;
    private final OptionManager options;

    public PlayListService(PlaylistRepository repository, OptionManager options) {
        this.repository = repository;
        this.options = options;
    }

    public Optional<PlayList> findActive(Guild guild) {
        Option option = options.find("playlist");
        String uuid = options.get(option, guild);
        if (uuid == null) {
            return Optional.empty();
        }
        return repository.findById(uuid);
    }

    public void setActive(Guild guild, String uuid) {
        Option option = options.find("playlist");
        options.set(option, guild, uuid);
    }

    public PlayList create(User owner, String name) {
        long count = repository.countAllByOwner(owner.getIdLong());
        if (count == 2) {
            return null;
        }
        PlayList playList = new PlayList();
        playList.setId(UUID.randomUUID().toString());
        playList.setName(name);
        playList.setOwner(owner.getIdLong());
        playList.setShuffle(false);
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
        return playList.getCurators().contains(user.getIdLong());
    }

    public Song findNextSong(PlayList playList) {
        if (playList.getSongs().isEmpty()) {
            return null;
        }
        PlayListCursor cursor = playList.getCursor();
        if (cursor == null) {
            cursor = new PlayListCursor();
            cursor.setPosition(-1);
        }
        // TODO: Shuffle mode
        int position = cursor.getPosition() + 1;
        if (position > playList.getSongs().size()) {
            position = 0;
        }
        return playList.getSongs().get(position);
    }

    public boolean updateCursor(PlayList playList, Song song) {
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
        playList.setCursor(cursor);
        repository.save(playList);
        return true;
    }

}
