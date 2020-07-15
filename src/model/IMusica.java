package model;

import model.Musica;
import model.Playlist;

import java.sql.Connection;

public interface IMusica {

    void saveAll(String playlistName, int user);
    void saveMusics(Connection conn, Musica music, String playlistName, int user);
    void saveAlbum(Connection conn, Musica musica);
    void saveArtist(Connection conn,  Musica musica);
    void saveStyle(Connection conn,  Musica musica);
    void retrieveMusics(Connection conn,  Playlist playlist, int userID);
    void deleteMusic(String playlistName, Musica musica, int IDuser);

}
