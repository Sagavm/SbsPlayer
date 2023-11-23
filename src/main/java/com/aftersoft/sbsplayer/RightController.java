package com.aftersoft.sbsplayer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.util.Duration;

public class RightController implements Initializable {
    @FXML
    private MediaView rightVideo;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Slider volume;
    @FXML
    private StackPane rStackPane;
    @FXML
    Label timeLabel;
    @FXML
    ImageView bGImage;
    private double rightStageWidth;
    private double rightStageHeight;
    private double videoWdivH;
    private double videoHdivW;
    public static boolean videoBinded;
    Timeline controlTimer;
    public static executer autoPlay, unbindMediaPlayer, hideBGImage;
    public static boolean stretch = true, sbs = true;
    DoubleProperty progress = new SimpleDoubleProperty(0.0);

    public void Play(){
        if (App.fileLoaded){
            PreparePlayer();
            //Hide BG image.
            App.bGVisible = false;
            hideBGImage.execute();
            LeftController.hideBGImage.execute();
            App.mediaPlayer.play();
        } 
    }

    public void Pause(){
        if (App.fileLoaded){
            App.mediaPlayer.pause();
        }
    }

public void PreparePlayer(){
        //Bind the video duration with progress bar. Do it just one.
        //ShowControls(true);
        if (!videoBinded){
            rightVideo.setMediaPlayer(App.mediaPlayer);
            progress.bind(Bindings.createDoubleBinding(() ->
                App.mediaPlayer.getCurrentTime().toMillis() / App.mediaPlayer.getTotalDuration().toMillis(),
                App.mediaPlayer.currentTimeProperty(), App.mediaPlayer.totalDurationProperty()));
            progressBar.progressProperty().bind(progress);
            //Show the time on the label
            App.mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                // Actualizar el tiempo transcurrido
                timeLabel.setText(FormatDuration(newValue));
            });
            videoBinded = true;
            //Start with the control hidden.
            ShowControls(false);
            //Hide and show progressBar
            SetProgressAndVolumeBarBehavior();
            //Vinculate the slider with volume.
            App.mediaPlayer.setVolume(volume.getValue());
            volume.valueProperty().addListener((observable, oldValue, newValue) -> {
                App.mediaPlayer.setVolume(newValue.doubleValue());
            });
        }
        //Check the video Options.
        if (stretch){
            videoWdivH = (float)App.mediaPlayer.getMedia().getWidth()/(float)App.mediaPlayer.getMedia().getHeight();
            videoHdivW = (float)App.mediaPlayer.getMedia().getHeight()/(float)App.mediaPlayer.getMedia().getWidth();
        }
        if (sbs && !stretch){
                videoWdivH = (float)App.mediaPlayer.getMedia().getWidth()/2.0/(float)App.mediaPlayer.getMedia().getHeight();
                videoHdivW = (float)App.mediaPlayer.getMedia().getHeight()/(float)App.mediaPlayer.getMedia().getWidth()/2.0;
        }
        if(!sbs && !stretch){
            videoWdivH = (float)App.mediaPlayer.getMedia().getWidth()/(float)App.mediaPlayer.getMedia().getHeight()/2.0;
            videoHdivW = (float)App.mediaPlayer.getMedia().getHeight()/2.0/(float)App.mediaPlayer.getMedia().getWidth();
        }
        rightStageWidth = App.rightStage.widthProperty().getValue();
        rightStageHeight = App.rightStage.heightProperty().getValue();
        SetVideoAspectRatio(rightStageWidth, rightStageHeight);
    }
    private String FormatDuration(Duration duration) {
        //Time elapsed.
        int hours = (int)duration.toMinutes()/60;
        int minutes = (int)duration.toMinutes()%60;
        int seconds = (int)duration.toSeconds()%60;
        //Total time
        int totalHours = (int)App.mediaPlayer.totalDurationProperty().getValue().toMinutes()/60;
        int totalMinutes =(int)App.mediaPlayer.totalDurationProperty().getValue().toMinutes()%60;
        int totalSeconds = (int)App.mediaPlayer.totalDurationProperty().getValue().toSeconds()%60;
        
        //String totalTime = String.format("%02d:%02d", App.mediaPlayer.totalDurationProperty().getValue().toMinutes(),App.mediaPlayer.totalDurationProperty().getValue().toSeconds()); 
        return String.format("%02d:%02d:%02d/%02d:%02d:%02d",hours, minutes, seconds, totalHours,totalMinutes,totalSeconds); // + "/" + totalTime;
    }

    public void ShowControls(boolean show){
            //Start with the control hidden.
            progressBar.setVisible(show);
            volume.setVisible(show);
            timeLabel.setVisible(show);
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        timeLabel.setMouseTransparent(true);
        bGImage.setMouseTransparent(true);
        rStackPane.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
        rightVideo.setOnContextMenuRequested(event -> App.contextMenu.show(rightVideo, event.getScreenX(), event.getScreenY()));                 
        rightVideo.setPreserveRatio(false); //Unable preserving ratio
        rightVideo.setOnMouseClicked(event -> {
            //FullScreen with double click.
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                App.SetFullScreen(App.rightStage, App.leftStage);      
            }
            //Play with click.
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                if (App.ready || App.paused || App.stopped){
                    Play();
                    //In case of contextMenu created.
                    App.contextMenu.hide();
                }
                if (App.playing){
                    Pause();
                    App.contextMenu.hide();
                }
            }
        });
        
        //Resize the video when resizing the stage.
        App.rightStage.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (App.mediaPlayer != null) {
                //Calculate the right aspect ratio.
                rightStageWidth = (double)newValue;
                SetVideoAspectRatio(rightStageWidth, rightStageHeight);
            }else{
                rightVideo.setFitWidth((double)newValue);
            }
            //Resize background image.
            resizeBgImage();
            //bGImage.setFitWidth(App.rightStage.widthProperty().getValue());
        });
        App.rightStage.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (App.mediaPlayer != null) {
                rightStageHeight = (double)newValue;
                SetVideoAspectRatio(rightStageWidth, rightStageHeight);
            }else{
                rightVideo.setFitHeight((double)newValue);
            }
            //Resize background image.
            resizeBgImage();
            //bGImage.setFitHeight(App.rightStage.heightProperty().getValue());
        });
        //Change video position with Click on progressBar
        progressBar.setOnMouseClicked(event -> {
        double progressClick = event.getX();
        //Get total duration of video.
        double totalDuration = App.mediaPlayer.getTotalDuration().toMillis();
        // Calculate the corresponding time.
        double newTime = (progressClick / progressBar.getWidth()) * totalDuration;
        //Set new position.
        App.mediaPlayer.seek(new javafx.util.Duration(newTime));
        });
        //Start with the progressBar and volume Hidden.
        ShowControls(false);
        SetHideControlsTimer(1000);
        //Assign action to play to the executer; in this case, for autoplaying the video when opening.
        autoPlay = () -> Play();
        unbindMediaPlayer = () -> rightVideo.setMediaPlayer(null);
        hideBGImage = () -> {
            if (App.bGVisible){
                bGImage.setVisible(true);
            }else{
                bGImage.setVisible(false);
            }
        };
    }

    public void resizeBgImage(){
        if (App.rightStage.getHeight() >= App.rightStage.getWidth()*9.0/16.0){
            bGImage.setFitWidth(App.rightStage.getWidth());
            bGImage.setFitHeight(App.rightStage.getWidth()*9.0/16.0);
        }
        //2: Supose that the videoH is the same of the stageH
        if (App.rightStage.getWidth() >= App.rightStage.getHeight()*16.0/9.0){
            bGImage.setFitWidth(App.rightStage.getHeight()*16.0/9.0); 
            bGImage.setFitHeight(App.rightStage.getHeight());
        }
    }
    //This control the visible property of the controls.
    public void SetProgressAndVolumeBarBehavior(){
            App.rightScene.setOnMouseMoved(event ->{
                controlTimer.stop();
                rightVideo.setCursor(Cursor.DEFAULT);
                ShowControls(true);
                controlTimer.play();
            });

            volume.setOnMouseEntered(event ->{
                controlTimer.stop();
                ShowControls(true);
            });

            progressBar.setOnMouseEntered(event ->{
                controlTimer.stop();
                ShowControls(true);
            });              
    }
    public void SetHideControlsTimer(int milliseconds){
        Duration duration = Duration.millis(milliseconds);
        KeyFrame keyFrame = new KeyFrame(duration, event -> {
            ShowControls(false);
            //Hide the mouse.
            rightVideo.setCursor(Cursor.NONE);
            controlTimer.stop();
        });
        controlTimer = new Timeline(keyFrame);
        controlTimer.setCycleCount(1); 
    }

    //This is for create an static variable to whatever function of this class.
    @FunctionalInterface
    interface executer {
        void execute();
    }

    public void SetVideoAspectRatio(double stageWidth, double stageHeight){
        if (sbs){
            //1: Supose that the videoW is the same of the stageW
            if (stageHeight >= stageWidth*videoHdivW){
                rightVideo.setFitWidth(stageWidth);
                rightVideo.setFitHeight(stageWidth*videoHdivW);     
            }
            //2: Supose that the videoH is the same of the stageH
            if (stageWidth >= stageHeight*videoWdivH){
                rightVideo.setFitWidth(stageHeight*videoWdivH); 
                rightVideo.setFitHeight(stageHeight);
            }
            //Cut the screen at half.
            rightVideo.setViewport(new Rectangle2D((float)App.mediaPlayer.getMedia().getWidth()/2.0, 0, (float)App.mediaPlayer.getMedia().getWidth()/2.0, (float)App.mediaPlayer.getMedia().getHeight()));  //Rectangle size and size of the cutted part; Crop the image
        }else{
            //1: Supose that the videoW is the same of the stageW
            if (stageWidth >= stageHeight*videoWdivH){
                rightVideo.setFitHeight(stageHeight);
                rightVideo.setFitWidth(stageHeight*videoWdivH);     
            }
            //2: Supose that the videoH is the same of the stageH
            if (stageHeight >= stageWidth*videoHdivW){
                rightVideo.setFitHeight(stageWidth*videoHdivW); 
                rightVideo.setFitWidth(stageWidth);
            }
            //Cut the screen at half.
            rightVideo.setViewport(new Rectangle2D(0, (float)App.mediaPlayer.getMedia().getHeight()/2.0, (float)App.mediaPlayer.getMedia().getWidth(), (float)App.mediaPlayer.getMedia().getHeight()/2.0));  //Rectangle size and size of the cutted part; Crop the image
        }
        //Set new progress width.
        progressBar.setPrefWidth(rightVideo.getFitWidth()*0.7);
        //Set new volume Height
        volume.setPrefHeight(rightVideo.getFitHeight()*0.2);
        volume.setTranslateX(rightVideo.getFitWidth()*(-0.37));
    }
}