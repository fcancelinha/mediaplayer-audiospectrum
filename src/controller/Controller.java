package controller;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.*;
import view.Gradients;
import services.MetaFiles;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Controller{


    // [ USER ]
    @FXML private PasswordField txtPassword, txtRegisterPassword1, txtRegisterPassword2,txtAlterPasswordOld, txtAlterPasswordNew, txtAlterPasswordRepeat;
    @FXML private TextField txtUsername, txtRegisterUsername, txtRegisterSecret, txtRecoverSecret, txtRecoverUsername;
    @FXML private Hyperlink loginLink, registerLink, backRecoverLogin, recoverLink;
    @FXML private Label lblWarnings, lblRegisterWarnings, lblAlterWarnings;
    private final UtilizadorD user = new UtilizadorD();

    // [ MEDIA PLAYER ]
    private MediaPlayer mediaPlayer;
    private int index = 1;
    private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("mm:ss");

    // [ MEDIA CONTROLS ]
    @FXML private Slider progress, volume;
    @FXML private ToggleButton TGrepeat, TGshuffle, TGplayPause, TGmute, darkMode;
    @FXML private ImageView imagePlayPause, muteImage, miniCoverArt;
    @FXML private Label lbl_songName, lbl_musicProgress, lblArtist, lblAlbum, lblplaylistWarning, miniSongName, miniAlbumName, lblRecoverWarnings;

    // [ PLAYLIST ]
    private PlaylistD pd = new PlaylistD();
    private MusicaD md = new MusicaD();
    private String selectedPlaylist;
    
    // [ INTERFACE ]
    @FXML private Button BTopen, BTclose, mediaControlsDown, BTMediaPlayer, BTplaylist, BTlogout, BTsettings;
    @FXML private AnchorPane drag, paneMediaControls, aDrawer, movableMediaControls;
    @FXML private Pane panePlaylist, paneUser, paneMediaPlayer, paneRegistration, paneSettings, recoverPassword;
    @FXML private TextField txtplaylistName;
    @FXML private Circle circularImage, innerCircle;
    @FXML private AreaChart<String, Number> spectrum;
    @FXML private HBox miniArea;
    @FXML private TabPane tabpaneplaylists = new TabPane();
    @FXML private Accordion settingsAccordion;
    private String style = null;
    private final RotateTransition rotate = new RotateTransition();
    private Stage stage;
    private Image image, play, pause, mute, unmute;
    private ListView<HBoxCell> dynamicListSelection; //para interagir com a listview dinâmica


    @FXML public void initialize() {

        draggable();
        navigation();
        logStatus();

        aDrawer.setTranslateX(-64);

        image = new Image(getClass().getResourceAsStream("/img/NA.png"));
        pause = new Image(getClass().getResourceAsStream("/img/pause.png"));
        play = new Image(getClass().getResourceAsStream("/img/play-button.png"));
        mute = new Image(getClass().getResourceAsStream("/img/mute.png"));
        unmute = new Image(getClass().getResourceAsStream("/img/speaker.png"));
    }

    //[ USERS ]
    @FXML private void checkLogin() {

        TextField[] tf = {txtUsername, txtPassword};

        if(checkConditions(tf, lblWarnings, 3)){
            if(user.checkLogin(txtUsername.getText(), txtPassword.getText()))
                logStatus();
            else
                lblWarnings.setText(user.getError());
        }

    }

    @FXML private void registerUser(){

        TextField[] tf = {txtRegisterUsername, txtRegisterSecret, txtRegisterPassword1, txtRegisterPassword2 };

        if(checkConditions(tf, lblRegisterWarnings, 11)){
            if(user.registerUser(txtRegisterUsername.getText().trim(), txtRegisterPassword1.getText(), txtRegisterSecret.getText()))
                paneUser.toFront();
            else
                lblRegisterWarnings.setText(user.getError());
        }
    }


    @FXML private void alterPassword(){

        TextField[] tf = {txtAlterPasswordOld, txtAlterPasswordNew, txtAlterPasswordRepeat};

        if( checkConditions(tf, lblAlterWarnings, 8)){
            if(user.alterPassword(txtAlterPasswordOld.getText(), txtAlterPasswordNew.getText()))
                lblAlterWarnings.setText("Password Altered with success");
            else
                lblAlterWarnings.setText(user.getError());
        }
    }

    @FXML private void deleteAccount(){ //Alert window //melhorar este método
        user.deleteUser();
        logStatus();
    }

    @FXML private void recoverPassword(){
        TextField[] tf = {txtRecoverUsername, txtRecoverSecret};

        if(checkConditions(tf, lblRecoverWarnings, 10)){
            user.recoverPassword(txtRecoverUsername.getText(), txtRecoverSecret.getText());
            lblRecoverWarnings.setText(user.getError());
        }

    }

    private boolean checkConditions(TextField[] tf, Label warnings, int cut){

        for (TextField textField : tf) {
            if (textField.getText().isEmpty()) {
                warnings.setText(textField.getId().substring(cut) + " field missing");
                    return false;
            } else if (tf.length > 2 && !tf[tf.length - 2].getText().equals(tf[tf.length - 1].getText())) {
                warnings.setText("Passwords must match");
                    return false;
            }
        }
        return true;
    }

    /**
     * Check if the a user has logged in and hides or shows controls according to it
     * presentPlaylist() will set off a domino effect of methods that construct the tabs which act
     * as playlists and each tab will have a listview with an observable list of musics
     * resetMediaPlayer() will reset all the media controls and graphics
     */
    private void logStatus(){

        if(user.isUserLoggedIn()){
            presentPlaylist();
            paneMediaPlayer.toFront();
        }else{
            paneUser.toFront();
            user.userReset();
            resetMediaPlayer(mediaLoaded());
        }

        aDrawer.setTranslateX(-64);
        BTclose.setVisible(false);
        BTopen.setVisible(user.isUserLoggedIn());
        aDrawer.setVisible(user.isUserLoggedIn());
        paneMediaControls.setVisible(user.isUserLoggedIn());
        user.setUserLoggedIn(false);
    }

    // [ INTERFACE ]

    /**
     * initializes 2 listeners on mouse click and drag so that the window(scene)
     * coords(x,y) are set as the result of the coords at click minus the
     * coords when drag is finished which transports the window to the end of the drag event.
     */
    private void draggable(){

        drag.setOnMousePressed(pressEvent ->
                drag.setOnMouseDragged(dragEvent -> {
                    stage = (Stage)((Node)dragEvent.getSource()).getScene().getWindow();
                    stage.setX(dragEvent.getScreenX() - pressEvent.getSceneX());
                    stage.setY(dragEvent.getScreenY() - pressEvent.getSceneY());
                }));
    }


    @FXML private void minimizeApp(MouseEvent event) {
        ((Stage)((Button)event.getSource()).getScene().getWindow()).setIconified(true);
    }


    @FXML private void closeApp() {
        Platform.exit();
    }


    private void navigation() {

        BTMediaPlayer.setOnMouseClicked(e -> paneMediaPlayer.toFront());
        BTsettings.setOnMouseClicked(e -> paneSettings.toFront());
        BTplaylist.setOnMouseClicked(e -> panePlaylist.toFront());
        BTlogout.setOnMouseClicked(e -> logStatus());
        loginLink.setOnMouseClicked(e -> paneUser.toFront());
        registerLink.setOnMouseClicked(e -> paneRegistration.toFront());
        backRecoverLogin.setOnMouseClicked(e -> paneUser.toFront());
        recoverLink.setOnMouseClicked(e -> recoverPassword.toFront());
    }

    /**
     * Simple transition that opens or closes the left anchorpane
     * which holds the navigation controls depending on which button is clicked.
     * when the slide animation is finished depending on the button
     * the opposite one will get hidden.
     * @param event source of the event(either BTopen or BTclose)
     */
    @FXML private void drawer(MouseEvent event) { //Side Drawer

        TranslateTransition slide = new TranslateTransition();
        slide.setDuration(Duration.seconds(0.2));
        slide.setNode(aDrawer);

        slide.setToX(event.getSource() == BTopen ? 0 : -64);
        aDrawer.setTranslateX(event.getSource() == BTopen ? -64 : 0);
        slide.play();

        slide.setOnFinished((ActionEvent e) -> {
            BTopen.setVisible(event.getSource() != BTopen);
            BTclose.setVisible(event.getSource() == BTopen);
        });
    }

    /**
     *  Same as above but without the need to hide the button since it exists
     *  in the mini area and the translation is done vertically
     * @param event event source of the event(either mediaControlsDown or up)
     */
    @FXML private void mediaDrawerDown(MouseEvent event) { //Media Drawer

        TranslateTransition slide2 = new TranslateTransition();
        slide2.setDuration(Duration.seconds(0.2));
        slide2.setNode(movableMediaControls);

        slide2.setToY(event.getSource() == mediaControlsDown ? 64 : 0);
        movableMediaControls.setTranslateY(event.getSource() == mediaControlsDown ? 64 : 0);
        slide2.play();
    }


    /**
     * checks if the textfield for the playlist name is empty
     * selectedplaylist holds the ID of that playlist for later use when inserting musics
     * lblplaylistWarnings will get updated depending on the outcome of pd.addPlaylists
     * this is different from the else since it gets the SQL error from the method.
     * presentPlaylist will set off the domino of constructing controls and elements needed
     * after it the newly created tab will get selected and brought into user view.
     */
    // [ PLAYLISTS ]
    @FXML private void addPlaylist() {

        if(!txtplaylistName.getText().trim().isEmpty()){
            selectedPlaylist = txtplaylistName.getText();
            lblplaylistWarning.setText(pd.addPlaylists(selectedPlaylist, user.getUser().getId()));
            presentPlaylist();
            tabpaneplaylists.getSelectionModel().selectLast();
            txtplaylistName.clear();
        }else
            lblplaylistWarning.setText("You need to add a name to your Playlist");
    }

    /**
     * ltp will hold all the constructed tabs with content
     * //LOAD
     * for each playlist object present in the pd list after firing the sql proc retrievePlaylists
     * retrieving all playlists associated with a user, the lambda forEach interface
     * is going to populate ltp with constructed tabs using the playlist object returned from retrievePlaylits
     * to give the tab its name and id.
     *
     * //DELIVER
     * after ltp is fully loaded with tabs, it will populate the Tabpane with each tab through the method
     * constructTabPane, after which it selected the first tab for user view.
     */
    private void presentPlaylist(){

        List<Tab> ltp = new ArrayList<>();

        //RESET
        pd.clearAll();

        //LOAD
        pd.retrievePlaylists(user.getUser().getId()).forEach(playlist -> ltp.add(constructTabs(playlist)));

        //DELIVER
        constructTabPane(ltp);
    }

    /**
     * checks for existing tabs first
     * md will hold all the Musica objects
     * mf is used to access the file dialog and metadata handler
     * mf will digest a list of files and add the details of metadata of each fail into a new Musica object (title, artist, album, etc..)
     * after which present playlist will refresh the tabpanes with the information (new tabs)
     * md.saveAll will fire all the SQL procs that will save all the details into the Database which will
     * then be used by presentPlaylist() to retrieve musics and set the content of the tabs with it.
     *
     * This method needs to be improved since my initial idea was to refresh only one tab pane and not all
     * has some performance impact the more playlists exist upon login.
     */
    //[ MUSICS ]
    @FXML private void addMusic()  {

        if(tabpaneplaylists.getTabs().size() < 1){
            lblplaylistWarning.setText("add a playlist first");
            return;
        }
        lblplaylistWarning.setText("");

        MusicaD md = new MusicaD(); //LOAD
        MetaFiles mf = new MetaFiles(); //METADATA

        try {
            mf.musicFiles(this.stage, md);
            md.saveAll(selectedPlaylist, user.getUser().getId());
            presentPlaylist();
        } catch (NullPointerException x){
            lblplaylistWarning.setText("No music was selected or saved");
        }
    }

    /**
     *
     * mediaLoaded() checks if there is a music already playing and disposes it
     * Official oracle documentation mentions the need to always create a new mediaplayer object for each file
     * initializeMusicUtils will set or reset cover image, progress bar listeners, animations etc..
     * on ready the mediaplayer will play the music check if it should mute the sound or not
     * and on end of media checks if repeat is selected or not, if yes it will return to the beginning of the song
     * thus effectively repeating the song,
     * otherwise it will dispose the previous music and using recursion play the next song in a natural way
     * if shuffle is selected, the readyPlayer is going to receive a random index within the current md list size
     * and play a random song, otherwise it will receive current index +1.
     * index is always set to above values to maintain the correct index for other controls
     *
     * NullPointerException returns index to the beginning of the list if for some reason shuffle or other factors
     * go off the current musica list size.
     *
     * MediaException occurs when a song is no longer present in that directory which then fires a method which fires
     * a sql proc deleting that music from the database and in a transparent way to the user plays the next song
     *
     * @param i used only to enable recursion
     */
    // [ MEDIA PLAYER ]
    private void readyPlayer(int i){

        if(mediaLoaded()) mediaPlayer.dispose();

        try {
            mediaPlayer = new MediaPlayer(new Media(new File(md.getMusicas().get(i).getLocalizacao()).toURI().toString()));

            initializeMusicUtils(md.getMusicas().get(i));
            mediaPlayer.setOnReady(() -> {
                mediaPlayer.setAudioSpectrumListener(new AudioSpectrum());
                mediaPlayer.setMute(TGmute.isSelected());
                mediaPlayer.play();
                mediaPlayer.setOnEndOfMedia(() -> {
                    if(TGrepeat.isSelected()) {
                        mediaPlayer.seek(Duration.ZERO);
                    }else{
                        mediaPlayer.dispose();
                        readyPlayer(index = TGshuffle.isSelected() ? randomAccessMusic() : i + 1); //AUTOPLAY [ON]
                    }
                });
            });

        }catch (NullPointerException | IndexOutOfBoundsException e){
            readyPlayer(index = 1);
        }catch (MediaException na){
            fileNotFound(i);
        }
    }


    /**
     * Plays next song while deleting the current song if the directory does not exist
     * also deletes the listview cell which contains that music using the dynamicListSelection global variable
     * @param i
     */
    private void fileNotFound(int i){
        readyPlayer(i + 1);
        dynamicListSelection.getItems().remove(i);
        md.deleteMusic(selectedPlaylist, md.getMusicas().get(i), user.getUser().getId());
    }

    /**
     * uses an area chart to visually interpret sound frequencies, implements AudiuSpectrumListen to be used by the mediaplayer
     * the values are updated according to the parameter INTERVAL
     * Usage of platform.run later to initiate an asynchronous task so that it is consistently updated after each music
     * each new instantiation defines a set number of X rows in the chart to accomodate about 128 frequency bands with
     * a sound frequency threshold of -120 sensitivity
     * the Y value of the chart is updated constantly via a runnable function using the magnitude values for the frequency bands
     */
    // AUDIO SPECTRUM
    private class AudioSpectrum implements AudioSpectrumListener{

        private XYChart.Series<String, Number> series1;
        private XYChart.Data[] series1Data;

        public AudioSpectrum(){
            initializeChart();
        }

        @Override
        public void spectrumDataUpdate(double timestamp, double duration, float[] magnitudes, float[] phases) {

            Platform.runLater(() ->{
                for (int i = 0; i < series1Data.length; i++)
                    series1Data[i].setYValue(magnitudes[i] - mediaPlayer.getAudioSpectrumThreshold());
            });

            spectrum.getData().clear();
            spectrum.getData().add(series1);
            if(style != null)
                spectrum.lookup(".default-color0.chart-series-area-fill").setStyle("-fx-fill: " + style.substring(21));
        }


        private void initializeChart(){

            //BANDS 128 Default //FREQ -60 Default // INTERVAL 0.1 Default
            final int BANDS = 128;
            final int FREQUENCY = -120;
            final double INTERVAL = 0.08;
            final int DATADISPLAY = 128;

            mediaPlayer.setAudioSpectrumNumBands(BANDS);
            mediaPlayer.setAudioSpectrumThreshold(FREQUENCY);
            mediaPlayer.setAudioSpectrumInterval(INTERVAL);

            series1 = new XYChart.Series<>();
            series1Data = new XYChart.Data[DATADISPLAY];

            for (int i = 0; i < series1Data.length; i++) {
                series1Data[i] = new XYChart.Data<>(Integer.toString(i), DATADISPLAY);
                    series1.getData().add(series1Data[i]);
            }
        }
    }

    private void initializeMusicUtils(Musica musica){

        getCover();
        musicProgress();
        cdSpin();
        details(musica.getNome(), musica.getArtista(), musica.getAlbum());
    }

    /**
     * main driver of the spinning animation int he mediaplayer pane
     */
    private void cdSpin(){

        if(mediaPlayer.getStatus() == MediaPlayer.Status.DISPOSED || rotate.getStatus() == Animation.Status.RUNNING){
            rotate.stop();
            return;
        }

        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.setFromAngle(0);
        rotate.setToAngle(360);
        rotate.setDuration(Duration.seconds(10));
        rotate.setNode(circularImage);

        rotate.play();
    }

    /**
     * provides the logic for the shuffle
     * @return returns an integer within the range of the current playlist
     */
    private int randomAccessMusic(){  //R.A.M eheheheh
        Random random = new Random();
        return random.nextInt(md.getMusicas().size()); //exclusive
    }

    /**
     * simple boolean mediaplayer state checker
     * @return
     */
    private boolean mediaLoaded(){
        return mediaPlayer != null;
    }

    /**
     * Data conversion for the slider timer
     * @param time
     * @return
     */
    private String convertTime(int time){
        return LocalTime.MIN.plusSeconds(time).format(FORMATTER);
    }

    /**
     * takes care of the selected cell, the current time and the update of the slider
     */
    private void musicProgress(){

        dynamicListSelection.getSelectionModel().select(index);

        mediaPlayer.setVolume(volume.getValue() / 100);

        mediaPlayer.setOnPlaying(() ->{
            imagePlayPause.setImage(pause);
                TGplayPause.setSelected(true);
                    rotate.play();
        });

        mediaPlayer.setOnPaused(() ->{
            imagePlayPause.setImage(play);
                TGplayPause.setSelected(false);
                    rotate.pause();
        });

        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            progress.setValue((newValue.toSeconds() / mediaPlayer.getTotalDuration().toSeconds()) * 100);
                lbl_musicProgress.setText(convertTime((int)(mediaPlayer.getCurrentTime().toSeconds())));
        });
    }


    private void details(String songname, String artist, String album){

        lbl_songName.setText(songname);
        lblArtist.setText(artist);
        lblAlbum.setText(album);
        miniSongName.setText(songname);
        miniAlbumName.setText(album);
    }

    /**
     * using the Media class it extracts the album cover image for the loaded song
     * using a map consisting of a key and a value associated with that key
     * if it's "image" the graphic controls load that Image
     */
    private void getCover(){

        circularImage.setFill(new ImagePattern(image));
            miniCoverArt.setImage(image);

        mediaPlayer.getMedia().getMetadata().addListener((MapChangeListener<String, Object>) map -> {
            if(map.getKey().equals("image")){
                circularImage.setFill(new ImagePattern((Image) map.getValueAdded()));
                    miniCoverArt.setImage((Image) map.getValueAdded());
            }
        });
    }

    // [ MEDIA CONTROLS ]
    @FXML private void setVolume() {
        if(mediaLoaded())
            mediaPlayer.setVolume(volume.getValue() / 100);
    }
    
    @FXML private void setProgress() {

        if(mediaLoaded()){
            mediaPlayer.seek(Duration.seconds((progress.getValue() / 100) * mediaPlayer.getTotalDuration().toSeconds()));
                rotate.jumpTo(Duration.seconds(progress.getValue()));
        }
    }

    @FXML private void nextMusic() {
        if(mediaLoaded())
            readyPlayer(index = TGshuffle.isSelected() ? randomAccessMusic() : ++index);
    }


    @FXML private void previousMusic() {
        if(mediaLoaded())
            readyPlayer(index = TGshuffle.isSelected() ? randomAccessMusic() : --index);
    }

    @FXML private void setShuffle(){
        if(mediaLoaded())
            TGshuffle.setStyle(TGshuffle.isSelected() && mediaLoaded() ? Gradients.SELECT.getStyle() : Gradients.TRANSPARENT.getStyle());
    }

    @FXML private void setRepeat() {
        if(mediaLoaded())
            TGrepeat.setStyle(TGrepeat.isSelected() && mediaLoaded() ? Gradients.SELECT.getStyle() : Gradients.TRANSPARENT.getStyle());
    }

    @FXML private void setMute() {

        if(mediaLoaded()){
            mediaPlayer.setMute(TGmute.isSelected());
                muteImage.setImage(TGmute.isSelected() ? mute : unmute);
        }
    }

    @FXML private void playPause(){

            if(mediaLoaded()){
                if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING){
                    mediaPlayer.pause();
                        imagePlayPause.setImage(pause);
                }
                else if(mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED || mediaPlayer.getStatus() == MediaPlayer.Status.DISPOSED){
                    mediaPlayer.play();
                        imagePlayPause.setImage(play);
                }
            }else if(dynamicListSelection != null){
                readyPlayer(dynamicListSelection.getSelectionModel().getSelectedIndex());
            }
    }

    @FXML private void openGithub() throws URISyntaxException, IOException {
        Desktop.getDesktop().browse(new URI("https://github.com/fcancelinha"));
    }

    private void constructTabPane(List<Tab> list){

        tabpaneplaylists.getTabs().clear();
        tabpaneplaylists.setTabMinWidth(90);
        tabpaneplaylists.getTabs().addAll(list);
        tabpaneplaylists.getStyleClass().add("floating");
        tabpaneplaylists.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
        tabpaneplaylists.setOnMouseClicked(click -> selectedPlaylist = tabpaneplaylists.getSelectionModel().getSelectedItem().getId());
    }


    private Tab constructTabs(Playlist playlist){

        Tab tb = new Tab();
        MusicaD musicaD = new MusicaD();

        String tabStyle =  style == null ? Gradients.BLOOD.getStyle() : style;

        tb.setId(playlist.getNome());
        tb.setText(playlist.getNome());
        tb.setStyle(tabStyle);

        tb.setOnCloseRequest(request -> showWarningWindow(request, playlist));

        musicaD.musicReadyList(playlist, user.getUser().getId());
        tb.setContent(createContent(playlist, musicaD));

        return tb;
    }


    private void showWarningWindow(Event event, Playlist playlist){

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Playlist deletion confirmation");
        alert.setHeaderText("");
        alert.setContentText("You're about to delete this playlist, are you sure?");

       alert.showAndWait().ifPresent(buttonType -> {
           if(buttonType == ButtonType.OK)
               pd.deletePlaylist(playlist, user.getUser().getId());
           else{
               event.consume();
           }
       });
    }


    private ListView<HBoxCell> createContent(Playlist playlist, MusicaD musicaD) {

        ListView<HBoxCell> listView = new ListView<>(); //Object Listview
        List<HBoxCell> HBoxlist = new ArrayList<>(); //Collection List
        ObservableList<HBoxCell> oblHBoxlist = FXCollections.observableList(HBoxlist);

        HBoxlist.add(new HBoxCell(Gradients.DARKMODE.getStyle() + "-fx-text-fill: white;"));

        musicaD.forEach(musica -> {
            if(musica != null)
                HBoxlist.add(new HBoxCell(musica, listView, playlist));
        });

        setListViewControl(listView, musicaD);
        listView.setItems(oblHBoxlist);

        return listView;
    }


    private void setListViewControl(ListView<HBoxCell> listView, MusicaD musicList){

        listView.setOnMouseClicked(click -> {

            md.setMusicas(musicList.getMusicas());
            dynamicListSelection = listView;
            index = listView.getSelectionModel().getSelectedIndex();

            if(click.getClickCount() == 2 && listView.getSelectionModel().getSelectedIndex() > 0){
                readyPlayer(index);
            }
        });
    }


    private class HBoxCell extends HBox {

        public HBoxCell(Musica musica, ListView<HBoxCell> listview, Playlist playlist){

            this.setAlignment(Pos.CENTER);
            this.getChildren().addAll(new LabelTemplate(musica.getNome(), 350, Pos.CENTER_LEFT, ""),
                    delMusicButton(playlist, listview, musica),
                    new LabelTemplate(musica.getArtista(), 300, Pos.CENTER, ""),
                    new LabelTemplate(musica.getAlbum(), 325, Pos.CENTER, ""),
                    new LabelTemplate(musica.getEstilo(), 120, Pos.CENTER, ""),
                    new LabelTemplate(musica.getAno(), 120, Pos.CENTER, ""));
        }

        public HBoxCell(String style){

            Label title = new LabelTemplate("Title", 350, Pos.CENTER, style);
            Label deletion = new LabelTemplate("#", 25, Pos.CENTER, style);
            Label artist = new LabelTemplate("Artist", 300, Pos.CENTER, style);
            Label album = new LabelTemplate("Album", 325, Pos.CENTER, style);
            Label genre = new LabelTemplate("Genre", 125, Pos.CENTER, style);
            Label year = new LabelTemplate("Year", 125, Pos.CENTER, style);

            this.getChildren().addAll(title,deletion, artist, album, genre, year);
        }

        public class LabelTemplate extends Label {

            public LabelTemplate(String text, int size, Pos position, String style){

                this.setText(text);
                this.setMinWidth(size);
                this.setAlignment(position);
                this.setTextAlignment(TextAlignment.CENTER);
                this.setContentDisplay(ContentDisplay.CENTER);
                this.setStyle(style);
            }
        }
    }

    private Button delMusicButton(Playlist playlist, ListView<HBoxCell> listView, Musica musica) {

        Button delete = new Button();
        ImageView deleteImage = new ImageView();

        deleteImage.setImage(new Image(getClass().getResourceAsStream("/img/trash.png")));
        deleteImage.setFitHeight(15);
        deleteImage.setFitWidth(15);

        delete.setGraphic(deleteImage);
        delete.setCursor(Cursor.HAND);
        delete.setText("");
        delete.setAlignment(Pos.CENTER);
        delete.setStyle(Gradients.TRANSPARENT.getStyle());

        delete.setOnMouseClicked(click -> {

           if(mediaLoaded())
             resetMediaPlayer(md.getMusicas().get(index) == musica);

                  listView.getItems().remove(delete.getParent());
                  md.getMusicas().remove(musica);
                  md.deleteMusic(playlist.getNome(), musica, user.getUser().getId());
             });


        return delete;
    }


    private void resetMediaPlayer(boolean musicIsSelected){

        if(musicIsSelected){
            mediaPlayer.dispose();
            spectrum.getData().clear();
            circularImage.setFill(Color.TRANSPARENT);
            rotate.stop();
            details("Select a music", "", "");
            progress.setValue(0.0);
            TGplayPause.setSelected(false);
            imagePlayPause.setImage(play);
            TGmute.setSelected(false);
            TGshuffle.setSelected(false);
            lbl_musicProgress.setText("00:00");
            miniCoverArt.setImage(null);
            TGmute.setSelected(false);
            movableMediaControls.setTranslateY(0);
        }
    }

    // [ COLORS & DARKMODE ]

    @FXML private void changeStyle(MouseEvent event){

        Node[] anchors = { drag, movableMediaControls, aDrawer, miniArea};

        style = Gradients.valueOf(((Button) event.getSource()).getId()).getStyle();

        for(Node node : anchors) node.setStyle(node.getStyle() + style);
        tabpaneplaylists.getTabs().forEach(tab -> tab.setStyle(style));
    }

    @FXML private void darkMode(){

        darkMode.setStyle(darkMode.isSelected() ? Gradients.SELECT.getStyle() : Gradients.TRANSPARENT.getStyle());

        Node[] nodes = {paneRegistration, paneUser, panePlaylist, paneSettings, paneMediaPlayer, lblAlbum, lblArtist, lbl_songName, lblplaylistWarning, recoverPassword};

        settingsAccordion.getPanes().forEach(pane -> pane.setStyle(darkMode.isSelected() ? "-fx-text-fill: White;" : "-fx-text-fill: Black"));

        for(Node node: nodes){
            if(node.getId().startsWith("lbl"))
                node.setStyle(darkMode.isSelected() ? "-fx-text-fill: White;" : "-fx-text-fill: Black");
            else
                node.setStyle(darkMode.isSelected() ? Gradients.DARKMODE.getStyle() : Gradients.LIGHMODE.getStyle());
        }


    }







}
