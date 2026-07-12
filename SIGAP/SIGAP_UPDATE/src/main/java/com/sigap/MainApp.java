package com.sigap;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainApp extends Application {

    private static final String SPLASH_FXML = "/com/sigap/view/SplashScreen.fxml";

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(SPLASH_FXML));
        Parent splashRoot = loader.load();

        Scene splashScene = new Scene(splashRoot, 1280, 800);

        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setTitle("SIGAP - Sistem Gerai & Pasar");
        primaryStage.setFullScreenExitHint("");
        primaryStage.setScene(splashScene);
        primaryStage.setResizable(false);
        primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
