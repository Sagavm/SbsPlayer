module com.aftersoft.sbsplayer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    opens com.aftersoft.sbsplayer to javafx.fxml;
    exports com.aftersoft.sbsplayer;
}
