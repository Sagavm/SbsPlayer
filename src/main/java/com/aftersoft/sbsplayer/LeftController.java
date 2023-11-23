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

public class LeftController implements Initializable {
    @FXML
    private MediaView leftVideo;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Slider volume;
    @FXML
    StackPane lStackPane;
    @FXML
    Label timeLabel;
    @FXML
    ImageView bGImage;
    private double leftStageWidth;
    private double leftStageHeight;
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
            //Hide BgImage
            App.bGVisible = false;
            hideBGImage.execute();
            RightController.hideBGImage.execute();
            App.mediaPlayer.play();
        }
    }

    public void Pause(){
        if (App.fileLoaded){
            App.mediaPlayer.pause();
        }
    }
    public void PreparePlayer(){
        leftVideo.setMediaPlayer(App.mediaPlayer);
        //Bind the video duration with progress bar. Do it just one.
        ShowControls(true);
        if (!videoBinded){
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
        leftStageWidth = App.leftStage.widthProperty().getValue();
        leftStageHeight = App.leftStage.heightProperty().getValue();
        SetVideoAspectRatio(leftStageWidth, leftStageHeight);
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
        lStackPane.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
        leftVideo.setOnContextMenuRequested(event -> App.contextMenu.show(leftVideo, event.getScreenX(), event.getScreenY()));                 
        leftVideo.setPreserveRatio(false); //Unable preserving ratio
        leftVideo.setOnMouseClicked(event -> {
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
        App.leftStage.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (App.mediaPlayer != null) {
                //Calculate the left aspect ratio.
                leftStageWidth = (double)newValue;
                SetVideoAspectRatio(leftStageWidth, leftStageHeight);
            }else{
                leftVideo.setFitWidth((double)newValue);
            }
            //Resize background image.
            resizeBgImage();
        });
        App.leftStage.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (App.mediaPlayer != null) {
                leftStageHeight = (double)newValue;
                SetVideoAspectRatio(leftStageWidth, leftStageHeight);
            }else{
                leftVideo.setFitHeight((double)newValue);
            }
            //Resize background image.
            resizeBgImage();
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
        unbindMediaPlayer = () -> leftVideo.setMediaPlayer(null);
        hideBGImage = () -> {
            if (App.bGVisible){
                bGImage.setVisible(true);
            }else{
                bGImage.setVisible(false);
            }
        };
    }
    public void resizeBgImage(){
        if (App.leftStage.getHeight() >= App.leftStage.getWidth()*9.0/16.0){
            bGImage.setFitWidth(App.leftStage.getWidth());
            bGImage.setFitHeight(App.leftStage.getWidth()*9.0/16.0);
        }
        //2: Supose that the videoH is the same of the stageH
        if (App.leftStage.getWidth() >= App.leftStage.getHeight()*16.0/9.0){
            bGImage.setFitWidth(App.leftStage.getHeight()*16.0/9.0); 
            bGImage.setFitHeight(App.leftStage.getHeight());
        }
    }
    //This controls the visible property of the controls.
    public void SetProgressAndVolumeBarBehavior(){
            App.leftScene.setOnMouseMoved(event ->{
                controlTimer.stop();
                leftVideo.setCursor(Cursor.DEFAULT);
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
            leftVideo.setCursor(Cursor.NONE);
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
                leftVideo.setFitWidth(stageWidth);
                leftVideo.setFitHeight(stageWidth*videoHdivW);
            }
            //2: Supose that the videoH is the same of the stageH
            if (stageWidth >= stageHeight*videoWdivH){
                leftVideo.setFitWidth(stageHeight*videoWdivH); 
                leftVideo.setFitHeight(stageHeight);
            }
            //Cut the screen at half.
            leftVideo.setViewport(new Rectangle2D(0, 0, (float)App.mediaPlayer.getMedia().getWidth()/2.0, (float)App.mediaPlayer.getMedia().getHeight()));  //Rectangle size and size of the cutted part; Crop the image
        }else{
            //1: Supose that the videoW is the same of the stageW
            if (stageWidth >= stageHeight*videoWdivH){
                leftVideo.setFitHeight(stageHeight);
                leftVideo.setFitWidth(stageHeight*videoWdivH);     
            }
            //2: Supose that the videoH is the same of the stageH
            if (stageHeight >= stageWidth*videoHdivW){
                leftVideo.setFitHeight(stageWidth*videoHdivW); 
                leftVideo.setFitWidth(stageWidth);
            }
            //Cut the screen at half.
            leftVideo.setViewport(new Rectangle2D(0, 0, (float)App.mediaPlayer.getMedia().getWidth(), (float)App.mediaPlayer.getMedia().getHeight()/2.0));  //Rectangle size and size of the cutted part; Crop the image
        }
        //Set new progress width.
        progressBar.setPrefWidth(leftVideo.getFitWidth()*0.7);
        //Set new volume Height
        volume.setPrefHeight(leftVideo.getFitHeight()*0.2);
        volume.setTranslateX(leftVideo.getFitWidth()*(-0.37));
    }
}
