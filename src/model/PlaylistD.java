package model;

import org.jetbrains.annotations.NotNull;
import services.Connector;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PlaylistD implements IPlaylist, Iterable<Playlist> {

    private List<Playlist> playGetAll;
    private ResultSet rs;

    public PlaylistD(){
        playGetAll = new ArrayList<>();
        rs = null;
    }

    @Override
    public String addPlaylists(String playlist, int userId){

        String output = null;

        try(Connection conn = new Connector().getConnection();
            CallableStatement cstmt = conn.prepareCall("{call usp_addPlaylist(?,?,?)}")){

            cstmt.setString("nome", playlist);
            cstmt.setInt("idUtilizador", userId);
            cstmt.registerOutParameter("output", Types.NVARCHAR);
            cstmt.execute();

            output = cstmt.getString("output");

        } catch (SQLException s) {
           s.printStackTrace();
        }

        return output;
    }


    @Override
    public List<Playlist> retrievePlaylists(int userID) {

        try(Connection conn = new Connector().getConnection();
            CallableStatement cstmt = conn.prepareCall("{call usp_getPlaylist(?)}")){

            cstmt.setInt("idUtilizador", userID);
            cstmt.execute();

            rs = cstmt.getResultSet();

            while(rs.next())
                playGetAll.add(new Playlist(rs.getString(1), userID)); //adicionar objecto utilizador

        } catch (SQLException s) {
            s.printStackTrace();
        }

        return playGetAll;
    }

    @Override
    public void deletePlaylist(Playlist playlist, int IDuser){

        try(Connection conn = new Connector().getConnection();
            CallableStatement cstmt = conn.prepareCall("{call usp_DeletePlaylist(?, ?)}")){

            cstmt.setString("playlistID", playlist.getNome());
            cstmt.setInt("IDuser", IDuser);
            cstmt.execute();

        } catch (SQLException s) {
            s.printStackTrace();
        }
    }

    @NotNull
    @Override
    public Iterator<Playlist> iterator() {
        return playGetAll.iterator();
    }

    public void clearAll() {
        playGetAll.clear();
    }

    //GETTERS & SETTERS

    public List<Playlist> getPlaylist() {
        return playGetAll;
    }

    public void setPlayGetAll(List<Playlist> playGetAll) {
        this.playGetAll = playGetAll;
    }


}
