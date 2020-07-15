package services;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Musica;
import model.MusicaD;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MetaFiles {

    //[ METADATA ]
    private final Metadata METADATA = new Metadata();
    private final Parser PARSER = new Mp3Parser();


    public MetaFiles(){
        //inicializar alguma coisa ?
        //drag n drop talvez no futuro
    }


    public void musicFiles(Stage stage, MusicaD md){

        metadataReading(selectFiles(stage), md);
    }


    private List<File> selectFiles(Stage stage) throws NullPointerException {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select musics to save");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Music files .mp3", "*.mp3"));

        return fileChooser.showOpenMultipleDialog(stage);
    }



    private void metadataReading(List<File> files, MusicaD listMusic){

        files.forEach(file ->{
            
            try {

                InputStream input = new FileInputStream(file.getAbsoluteFile().toString().replace('\\', '/'));
                PARSER.parse(input, new DefaultHandler(), METADATA, new ParseContext());

                listMusic.getPlaylist().add(new Musica(0,
                        METADATA.get("title") == null ? file.getName() : METADATA.get("title"),
                        file.getAbsoluteFile().toString().replace('\\', '/') ,
                        METADATA.get("xmpDM:album"),
                        METADATA.get("xmpDM:artist"),
                        METADATA.get("xmpDM:genre"),
                        METADATA.get("xmpDM:releaseDate")));

                input.close();

            } catch (SAXException | IOException | TikaException e) {
                e.printStackTrace();
            }
        });
    }

















}
