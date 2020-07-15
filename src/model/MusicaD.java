package model;

import org.jetbrains.annotations.NotNull;
import services.Connector;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class MusicaD implements IMusica, Iterable<Musica> {

    private List<Musica> playlist  = new ArrayList<>();
    private List<Musica> musicas  = new LinkedList<>();

    public MusicaD(){

    }

    @Override
    public void saveAll(String playlistName, int user) {

        try(Connection conn = new Connector().getConnection()) {

            playlist.forEach(musica -> {

                saveArtist(conn, musica);
                saveAlbum(conn, musica);
                saveStyle(conn, musica);
                saveMusics(conn, musica, playlistName, user);
            });
        } catch (SQLException x) {
            System.out.println(x);
        }
        playlist.clear();
    }


    public void musicReadyList(Playlist playlist, int user){
        musicas.clear();

        try(Connection conn = new Connector().getConnection()) {

          retrieveMusics(conn, playlist, user);

        } catch (SQLException x) {
            x.printStackTrace();
        }
    }

    @Override //Add error to output and send it out
    public void saveMusics(Connection conn, Musica musica, String playlistName, int user) {

        String output;

        try(CallableStatement cstmt = conn.prepareCall("{call usp_saveMusic(?,?,?,?,?,?,?,?)}")){

            cstmt.setString("nome", musica.getNome());
            cstmt.setString("localizacao", musica.getLocalizacao());
            cstmt.setString("album", musica.getAlbum());
            cstmt.setString("artista", musica.getArtista());
            cstmt.setString("estilo", musica.getEstilo());
            cstmt.setString("playlistID", playlistName);
            cstmt.setInt("IDutilizador", user);
            cstmt.registerOutParameter("output", Types.NVARCHAR);
            cstmt.execute();

            output = cstmt.getString("output");
            System.out.println(output);

        } catch (SQLException s) {
            System.out.println(s);
        }
    }


    @Override
    public void saveAlbum(Connection conn, Musica musica){

        try(CallableStatement cstmt = conn.prepareCall("{call usp_AddAlbum(?,?)}")) {

            cstmt.setString("album", musica.getAlbum());
            cstmt.setString("ano", musica.getAno());
            cstmt.execute();

        } catch (SQLException x) {
            x.printStackTrace();
        }
    }

    @Override
    public void saveArtist(Connection conn,  Musica musica){
        
        try(CallableStatement cstmt = conn.prepareCall("{call usp_saveArtist(?)}")){

            cstmt.setString("artist", musica.getArtista());
            cstmt.execute();

        } catch (SQLException x) {
            x.printStackTrace();
        }
    }

    @Override
    public void saveStyle(Connection conn, Musica musica){

        try (CallableStatement cstmt = conn.prepareCall("{call usp_AddEstilo(?)}")){

            cstmt.setString("estilo", musica.getEstilo());
            cstmt.execute();

        } catch (SQLException x) {
            x.printStackTrace();
        }
    }


    @Override
    public void retrieveMusics(Connection conn, Playlist playlist, int userID) {

        ResultSet rs = null;

        try (CallableStatement cstmt = conn.prepareCall("{call usp_retrieveMusics(?,?)}")){

            cstmt.setString("playlistID", playlist.getNome());
            cstmt.setInt("IDuser", userID);
            boolean results = cstmt.execute(); // SAFEGUARD caso não haja músicas

            rs = cstmt.getResultSet();
            musicas.add(null);

            if(results) {
                while (rs.next()) {
                    musicas.add(new Musica(rs.getInt(7),
                                           rs.getString(1),
                                           rs.getString(2),
                                           rs.getString(3),
                                           rs.getString(4),
                                           rs.getString(5),
                                           rs.getString(6)));
                }
            }
        } catch (SQLException x) {
            x.printStackTrace();
        }finally{
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void deleteMusic(String playlistName, Musica musica, int IDuser) {

        try(Connection conn = new Connector().getConnection();
            CallableStatement cstmt = conn.prepareCall("{call  usp_DeleteMusic(?,?,?)}")){

            cstmt.setString("playlistID", playlistName);
            cstmt.setInt("musicaID", musica.getID());
            cstmt.setInt("IDuser", IDuser);
            cstmt.execute();

        } catch (SQLException x) {
            x.printStackTrace();
        }
    }


    @NotNull
    @Override
    public Iterator<Musica> iterator() {
        return musicas.iterator();
    }

    // GETTERS & SETTERS

    public List<Musica> getMusicas() {
        return musicas;
    }

    public List<Musica> getPlaylist() {
        return playlist;
    }

    public void setMusicas(List<Musica> musicas) {
        this.musicas = musicas;
    }


}
