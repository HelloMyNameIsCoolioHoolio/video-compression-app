package hellofx;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Path;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
public class Main extends Application {
    private Boolean paused = false;
    private double targetSize;
    private MediaPlayer player;
    private MediaView viewer;
    private Media media;
    @Override
    public void start(Stage primaryStage) throws Exception{
        FileChooser test = new FileChooser();
        TextField path_input = new TextField();
        path_input.setMaxSize(100, 200);
        path_input.setText(System.getProperty("user.dir"));
        TextField size_input = new TextField();
        size_input.setMaxSize(100, 200);
        size_input.setText("Enter size of compressed video (measured in Kilobytes)");
        Label label = new Label("Insert MP4 file, convert any other vid format to MP4");
        Button button = new Button("Open Dialogue");
        Runtime rt = Runtime.getRuntime();
        VBox v = new VBox(15,path_input, size_input, label,button);
        Scene scene = new Scene(v, 1420, 700);
        EventHandler<ActionEvent> event
        = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e)  {
                File file = test.showOpenDialog(primaryStage);
                size_input.deselect();
                if(file != null && (file.getName().endsWith(".mp4"))) {
                    try {
                        media = new Media(file.toURI().toURL().toString());
                        player = new MediaPlayer(media);
                        viewer = new MediaView(player);
                        player.setOnReady(() -> {
                            String target_size_string = size_input.getText();
                            if(target_size_string != "" || target_size_string != null || target_size_string != "Enter size of compressed video (measured in Kilobytes)") {
                                int targetSize_InBytes = Integer.parseInt(target_size_string) * 8000;
                                targetSize = targetSize_InBytes / media.getDuration().toSeconds();
                                System.out.println(targetSize/8000);
                            }
                            String path_to_output = path_input.getText();
                            System.out.println("Test");
                            java.nio.file.Path p = Paths.get(path_to_output + "\\compressed-" + file.getName());
                            if(Files.exists(p) == true) {
                                try {
                                    Files.delete(p);
                                } catch (Exception ef2) {
                                    label.setText("Error occurred while deleting existent video with same name @ me on discord");
                                }
                            }
                            String[] commands = {"ffmpeg", "-i", file.getAbsolutePath(), "-b", (targetSize/8000) + "k", path_to_output + "\\compressed-" + file.getName()};
                            String full_cmd = "";
                            for(int i = 0; i < commands.length; i++) {
                                full_cmd += commands[i] + " ";
                            }
                            System.out.println(full_cmd);
                            try {
                                ProcessBuilder pb = new ProcessBuilder(commands);
                                pb.redirectErrorStream(true);
                                Process pro = pb.start();
                                BufferedReader in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
                                BufferedReader err = new BufferedReader(new InputStreamReader(pro.getErrorStream()));
                                String temp = null;
                                System.out.println("Passed");
                                while((temp=in.readLine()) != null) {
                                    System.out.println("IN: " + temp);
                                }
                                String temp_ = null;
                                while((temp_=err.readLine()) != null) {
                                    System.out.println("ERR_STREAM: " + temp_);
                                }
                                int code = pro.waitFor();
                                System.out.println(code);
                                File output_ = new File(path_to_output + "\\compressed-" + file.getName());
                                media = new Media(output_.toURI().toURL().toString());
                                player = new MediaPlayer(media);
                                viewer.setMediaPlayer(player);
                                label.setText("Successfully compressed");               
                            } catch (Exception k) {
                                k.printStackTrace();
                            }
                            player.play();
                            size_input.deselect();
                            scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {
                                size_input.deselect();
                                if(key.getCode() == KeyCode.SPACE) {
                                    paused = !paused;
                                    if (paused == true) {
                                        player.pause();
                                    } else {
                                        player.play();
                                    }
                                } else if (key.getCode() == KeyCode.ESCAPE) {
                                    path_input.setDisable(false);
                                    button.setDisable(false);
                                    size_input.setDisable(false);
                                    player.dispose();
                                    v.getChildren().remove(viewer);
                                    label.setText("Insert MP4 file, convert any other vid format to MP4");
                                }
                            });
                            path_input.setDisable(true);
                            button.setDisable(true);
                            size_input.setDisable(true);
                            player.setOnEndOfMedia(() -> {
                                player.dispose();
                                v.getChildren().remove(viewer);
                                button.setDisable(false);
                                label.setText("Insert MP4 file, convert any other vid format to MP4");
                            });
                        });
                        viewer.maxWidth(200);
                        viewer.maxHeight(100);
                        viewer.setPreserveRatio(true);
                        v.getChildren().add(viewer);
                    } catch(MalformedURLException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        };
        button.setOnAction(event);
        v.setAlignment(Pos.CENTER);
        primaryStage.setTitle("Video Compressor");
        primaryStage.setScene(scene);
        primaryStage.show(); 
    }
    public static void main(String[] args) {
        launch(args);
    }
}