package model;

import java.util.List;

public interface IPlaylist{

    String addPlaylists(String playlist, int userID);
    List<Playlist> retrievePlaylists(int userID);
    void deletePlaylist(Playlist playlist, int IDuser);

}
