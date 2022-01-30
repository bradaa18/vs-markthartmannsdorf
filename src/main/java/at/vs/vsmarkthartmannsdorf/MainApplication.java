package at.vs.vsmarkthartmannsdorf;

import at.vs.vsmarkthartmannsdorf.bl.IOAccess;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApplication extends Application {

    MainController controller;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("demo/start.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 600);
        stage.setTitle("Stundenplaner");
        stage.setScene(scene);
        stage.show();

        controller = fxmlLoader.getController();
        controller.setTeachers(IOAccess.readTeacherFiles());
        controller.setClasses(IOAccess.readClassFiles());
        // controller.loadAbsence();
        // controller.setTeachers(IOAccess.readTeacherFiles());
    }

    @Override
    public void stop() throws Exception {
        System.out.println("CLOSED WINDOW");

        IOAccess.storeTeacherFiles(controller.getTeacher());
        // IOAccess.storeClassFiles(controller.getClasses());
        // IOAccess.storeTimetableFiles(controller.getTimetables());

        //IOAccess_Excel.createExcelFile(controller.getTeacher(), controller.getClasses());
    }
}
