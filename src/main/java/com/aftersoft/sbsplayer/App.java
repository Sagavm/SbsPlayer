package com.aftersoft.sbsplayer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.scene.Parent;
import javafx.stage.FileChooser;
import java.io.File;
import javafx.scene.image.Image;


public class App extends Application {
    public static Scene rightScene;
    public static Scene leftScene;
    public static Stage rightStage;
    public static Stage leftStage;
    private double xOffset;
    private double yOffset;
    public static MediaPlayer mediaPlayer;
    public static boolean paused, playing, ready, stopped;
    public App newFile;
    public static double videoWidth;
    public static double videoHeight;
    public static Duration videoDuration;
    public static boolean fileLoaded, bGVisible;
    public static ContextMenu contextMenu = new ContextMenu();
    protected double rXPosition, rYPosition, lXPosition, lYPosition;
    protected boolean rFullScreen, lfullScreen;
    protected Stage tempStage;
    public static File selectedFile;
    public static Media media;

    public static void main(String[] args) throws Exception {
        launch(args);
    }
    @Override
    public void start(Stage stage) throws Exception {
        //Check if there are elements on the contextMenu.
        if (contextMenu.getItems().isEmpty()){
            setContextMenu();
        }
        //create icons
        Image rIcon = new Image("com/aftersoft/sbsplayer/r_icon.jpg");
        Image lIcon = new Image("com/aftersoft/sbsplayer/l_icon.jpg");
        //Create the right stage.
        rightStage = new Stage();
        rightStage.getIcons().add(rIcon);
        rightStage.setTitle("Pantalla Derecha");
        rightStage.initStyle(StageStyle.UNDECORATED);
        //Create the Right Root of elements
        Parent rightRoot = FXMLLoader.load(getClass().getResource("rightWindow.fxml"));
        rightScene = new Scene(rightRoot, 640, 360, Color.BLACK);
        rightStage.setScene(rightScene);
        SetDragging(rightScene,rightStage);
        //Create the left Stage
        leftStage = new Stage();
        leftStage.getIcons().add(lIcon);
        leftStage.setTitle("Pantalla Izquierda");
        leftStage.initStyle(StageStyle.UNDECORATED);
        //Create the Left Root of elements
        Parent leftRoot = FXMLLoader.load(getClass().getResource("leftWindow.fxml"));
        leftScene = new Scene(leftRoot, 640, 360, Color.BLACK);
        leftStage.setScene(leftScene);
        SetDragging(leftScene, leftStage);
        //Exit fullscreen with esc.
        setExitFullScreenWithEscape(rightScene);
        setExitFullScreenWithEscape(leftScene);
        //Show Both Stages.
        leftStage.show();
        rightStage.show();
        leftStage.setX(leftStage.getX()-330.0);
        rightStage.setX(rightStage.getX()+330);
    }

    public void setExitFullScreenWithEscape(Scene scene){
        //Set Esc key to exit fullscreen and maximized.
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if(event.getCode()==KeyCode.ESCAPE){
                rightStage.setFullScreen(false);
                leftStage.setMaximized(false);
                leftStage.setFullScreen(false);
                rightStage.setMaximized(false);
            }
        });
    }
    public void setContextMenu(){
        //Now add a file chooser.
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccione un video");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Películas 3D", "*.mp4"));
        //Create the menuitems for the context menu.
        MenuItem openVideo = new MenuItem("Abrir video");
        MenuItem fullScreen = new MenuItem("Pantalla Completa");
        MenuItem stretch = new MenuItem("No estirar el video");
        MenuItem sbs = new MenuItem("Video tipo: Arriba y abajo");
        MenuItem stopVideo = new MenuItem("Detener Video");
        MenuItem close = new MenuItem("Cerrar");
        //When open video, get the file and creeate the media and mediaPlayer.
        openVideo.setOnAction(event -> {
            if (rightStage.isFocused()){
                selectedFile = fileChooser.showOpenDialog(App.rightStage);
            }else{
                selectedFile = fileChooser.showOpenDialog(App.leftStage);
            }
            
            if (selectedFile != null) {
                //Close the last mediaPlayer and create a new one
                CreateMediaPlayer(selectedFile);            
                fileLoaded = true;
            }else{
                if (App.mediaPlayer == null){
                    fileLoaded = false; //Only if not loaded a video.
                }
            }               
        });
        fullScreen.setOnAction(event -> {
            SetFullScreen(App.rightStage, App.leftStage);
        });
        stretch.setOnAction(event -> {
            if (RightController.stretch){
                RightController.stretch = false;
                LeftController.stretch = false;
                RightController.autoPlay.execute();
                LeftController.autoPlay.execute();
                stretch.setText("Estirar el video");
            }else{
                RightController.stretch = true;
                LeftController.stretch = true;
                RightController.autoPlay.execute();
                LeftController.autoPlay.execute();
                stretch.setText("No estirar el video");
            }
        });
        sbs.setOnAction((event)->{
            if (LeftController.sbs){
                LeftController.sbs = false;
                RightController.sbs = false;
                sbs.setText("Video tipo: Lado a lado");
                RightController.autoPlay.execute();
                LeftController.autoPlay.execute();
            }else{
                LeftController.sbs = true;
                RightController.sbs = true;
                sbs.setText("Video tipo: Arriba y abajo");
                RightController.autoPlay.execute();
                LeftController.autoPlay.execute();
            }
        });
        stopVideo.setOnAction((event) -> {
            if (mediaPlayer != null){
                mediaPlayer.stop();
                bGVisible = true;
                RightController.hideBGImage.execute();
                LeftController.hideBGImage.execute();
            }
        });
        close.setOnAction((event) -> {
            if (mediaPlayer != null){
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }
            leftStage.close();
            rightStage.close();
        });
        contextMenu.getItems().addAll(openVideo, fullScreen, stretch, sbs, stopVideo, close);
    }

    public static void SetFullScreen(Stage rStage, Stage lStage){
        if (rStage.isFocused()){
            if (rStage.isFullScreen() ||lStage.isFullScreen()) {
                rStage.setFullScreen(false);
                rStage.setMaximized(false);
                lStage.setMaximized(false);
                lStage.setFullScreen(false);
            }else{
                rStage.setFullScreen(true);
                lStage.setMaximized(true);
            }
        }else{
            if (rStage.isFullScreen() ||lStage.isFullScreen()) {
                rStage.setFullScreen(false);
                rStage.setMaximized(false);
                lStage.setMaximized(false);
                lStage.setFullScreen(false); 
            }else{
                lStage.setFullScreen(true);
                rStage.setMaximized(true);
            }
        }
    }

    private void SetDragging(Scene scene, Stage stage){
        scene.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        scene.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }
    public void CreateMediaPlayer(File selectedFile){
            try {
                if (mediaPlayer != null){ //If there is a previous video.                
                    mediaPlayer.stop();
                    mediaPlayer.dispose();
                    RightController.videoBinded = false;
                    LeftController.videoBinded = false;
                    //Get the last Values.
                    rXPosition = rightStage.getX();
                    rYPosition = rightStage.getY();
                    lXPosition = leftStage.getX();
                    lYPosition = leftStage.getY();
                    rFullScreen = rightStage.isFullScreen();
                    lfullScreen = leftStage.isFullScreen();
                    //Close last stages
                    rightStage.close();
                    leftStage.close();
                    //Create a new Stages and set values.
                    newFile = new App();
                    newFile.start(tempStage);
                    rightStage.setX(rXPosition);
                    rightStage.setY(rYPosition);
                    leftStage.setX(lXPosition);
                    leftStage.setY(lYPosition);
                    if (rFullScreen || lfullScreen){
                        SetFullScreen(rightStage, leftStage);
                    }
                }
                mediaPlayer = new MediaPlayer(new Media(selectedFile.toURI().toString()));
                mediaPlayer.setOnReady(() -> {
                    MediaPlayerState();
                    //Hide background Image on play.
                    bGVisible = false;
                    RightController.autoPlay.execute();
                    LeftController.autoPlay.execute();                      
                });
            } catch (Exception e) {
                // Manejar excepciones aquí
                System.out.println("Ñerda");
                e.printStackTrace();
            }
    }
    public void MediaPlayerState(){
        //Add a listener to the mediaplayer state.
        ready = true;
        mediaPlayer.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == MediaPlayer.Status.PLAYING) {
                playing = true;
                paused = false;
                ready = false;
                stopped = false;
            }else if (newValue == MediaPlayer.Status.PAUSED) {
                playing = false;
                paused = true;
                ready = false; 
                stopped = false;                  
            } else if (newValue == MediaPlayer.Status.READY) {
                playing = false;
                paused = false;
                ready = true; 
                stopped = false;
            }else if (newValue == MediaPlayer.Status.STOPPED){
                playing = false;
                paused = false;
                ready = false; 
                stopped = true;
            }
        });
    }
}