package model;

public class Musica {

    private int ID;
    private String nome;
    private String localizacao;
    private String album;
    private String ano;
    private String artista;
    private String estilo;

    public Musica(int ID, String nome, String localizacao, String album, String artista, String estilo, String ano) {
        this.ID = ID;
        this.nome = nome;
        this.localizacao = localizacao;
        this.album = album;
        this.artista = artista;
        this.estilo = estilo;
        this.ano = ano;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtista() {
        return artista;
    }

    public void setArtista(String artista) {
        this.artista = artista;
    }

    public String getEstilo() {
        return estilo;
    }

    public void setEstilo(String estilo) {
        this.estilo = estilo;
    }

    public String getAno() {
        return ano;
    }

    public void setAno(String ano) {
        this.ano = ano;
    }
}
