package model;

public interface IUtilizador {

    boolean checkLogin(String username, String password);
    boolean registerUser(String username, String password, String secret);
    boolean alterPassword(String oldPassword, String newPassword);
    void deleteUser();
    void recoverPassword(String username, String secret);

}
