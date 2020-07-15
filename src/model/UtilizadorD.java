package model;


import services.Connector;
import java.sql.*;

public class UtilizadorD implements IUtilizador{

    private Utilizador user;
    private String error;
    private boolean userLoggedIn;

    public UtilizadorD(){
        user = new Utilizador();
    }

    public void userReset(){
        user.setId(0);
        user.setUsername(null);
    }

    @Override
    public boolean checkLogin(String username, String password){
        error = null;
        ResultSet rs = null;

        try(Connection conn = new Connector().getConnection();
            CallableStatement cstmt = conn.prepareCall("{call checkLogin(?,?,?)}")){

            cstmt.setString("username", username);
            cstmt.setString("password", password);
            cstmt.registerOutParameter("output", Types.NVARCHAR);

            boolean results = cstmt.execute(); // SAFEGUARD caso não haja username
            rs = cstmt.getResultSet();

            if(results){
                while(rs.next()){
                    user.setId(rs.getInt(1));
                    user.setUsername(rs.getString(2));
                    userLoggedIn = true; //can be in here because it will only occur once
                }
            }else
                error = cstmt.getString("output");

        } catch (SQLException s) {
            s.printStackTrace();
        }finally {
            try {
                if(rs != null)
                    rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return error == null;
    }

    @Override
    public boolean registerUser(String username, String password, String secret){

        error = null;

        try(Connection conn = new Connector().getConnection();
            CallableStatement cstmt = conn.prepareCall("{call registerUser(?,?,?,?)}")){

            cstmt.setString("username", username);
            cstmt.setString("password", password);
            cstmt.setString("secret", secret);
            cstmt.registerOutParameter("output", Types.NVARCHAR);
            cstmt.execute();

            error = cstmt.getString("output");

        } catch (SQLException s) {
            s.printStackTrace();
        }

        return error == null;
    }

    @Override
    public boolean alterPassword(String oldPassword, String newPassword){

        error = null;

        try(Connection conn = new Connector().getConnection();
            CallableStatement cstmt =conn.prepareCall("{call alterPassword(?,?,?,?)}")){

            cstmt.setString("username", user.getUsername());
            cstmt.setString("oldPassword", oldPassword);
            cstmt.setString("newPassword", newPassword);
            cstmt.registerOutParameter("output", Types.NVARCHAR);
            cstmt.execute();

            error = cstmt.getString("output");

        } catch (SQLException s) {
            s.printStackTrace();
        }

        return error == null;
    }

    @Override
    public void deleteUser(){

        try(Connection conn = new Connector().getConnection();
            CallableStatement cstmt = conn.prepareCall("{call deleteUser(?)}")){

            cstmt.setString("username", user.getUsername());
            cstmt.execute();
            userLoggedIn = false;
        } catch (SQLException s) {
            s.printStackTrace();
        }
    }

    @Override
    public void recoverPassword(String username, String secret){

        error = null;

        try(Connection conn = new Connector().getConnection();
            CallableStatement cstmt = conn.prepareCall("{call recoverPassword(?,?,?)}")){

            cstmt.setString("username", username);
            cstmt.setString("secret", secret);
            cstmt.registerOutParameter("output", Types.NVARCHAR);
            cstmt.execute();

            error = cstmt.getString("output");

        } catch (SQLException s) {
            s.printStackTrace();
        }
    }


    public Utilizador getUser() {
        return user;
    }

    public String getError() {
        return error;
    }

    public boolean isUserLoggedIn() {
        return userLoggedIn;
    }

    public void setUserLoggedIn(boolean userLoggedIn) {
        this.userLoggedIn = userLoggedIn;
    }
}
