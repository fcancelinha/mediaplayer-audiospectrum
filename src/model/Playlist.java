package model;

public class Playlist {

    private String nome;
    private int ID_utilizador;

    public Playlist(String nome, int ID_utilizador) {
        this.nome = nome;
        this.ID_utilizador = ID_utilizador;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getID_utilizador() {
        return ID_utilizador;
    }

    public void setID_utilizador(int ID_utilizador) {
        this.ID_utilizador = ID_utilizador;
    }
}
