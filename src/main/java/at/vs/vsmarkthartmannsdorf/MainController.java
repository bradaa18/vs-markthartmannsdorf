package at.vs.vsmarkthartmannsdorf;

import at.vs.vsmarkthartmannsdorf.bl.IOAccess_Excel;
import at.vs.vsmarkthartmannsdorf.bl.IOAccess_PDF;
import at.vs.vsmarkthartmannsdorf.data.*;
import com.itextpdf.text.DocumentException;
import at.vs.vsmarkthartmannsdorf.db.SchoolDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;


import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MainController implements Initializable {

    @FXML
    public BorderPane main;
    public HBox teacherBox, timetableBox, timetableDaysBox, classBox, absenceBox, settingsBox, excelImportBox, excelExportBox, helpBox;

    private List<HBox> navbar = Arrays.asList(teacherBox, timetableBox, classBox, absenceBox);

    private ObservableList<Teacher> teachers = FXCollections.observableArrayList();
    private ObservableList<SchoolClass> classes = FXCollections.observableArrayList();

    private TeacherViewController teacherViewController;
    private ClassViewController classViewController;
    private TimetableController timetableViewController;
    private SettingsController settingsController;
    private HelpController helpController;
    private BorderPane teacherView;
    private BorderPane classView;
    private BorderPane timetableView;
    private GridPane absenceView;
    private BorderPane settingsView;
    private BorderPane helpView;

    private boolean classesOpened = false;
    private Stage stage;

    @Override
    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {

            // Load TeacherView
            FXMLLoader teacherLoader = fxmlLoad("demo/teacher.fxml");
            teacherView = teacherLoader.load();
            teacherViewController = teacherLoader.getController();
            teacherViewController.setParent(this);


            //Load ClassView
            FXMLLoader classLoader = fxmlLoad("demo/class.fxml");
            classView = classLoader.load();
            classViewController = classLoader.getController();
            classViewController.setParent(this);

            //Load TimetableView
            FXMLLoader timetableLoader = fxmlLoad("demo/timetable.fxml");
            timetableView = timetableLoader.load();
            timetableViewController = timetableLoader.getController();
            // timetableViewController.setParent(this);

            // Load SettingsView
            FXMLLoader settingsLoader = fxmlLoad("demo/settings.fxml");
            settingsView = settingsLoader.load();
            settingsController = settingsLoader.getController();

            //Load HelpView
            FXMLLoader helpLoader = fxmlLoad("demo/help.fxml");
            helpView = helpLoader.load();
            helpController = helpLoader.getController();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to load fxml
    public FXMLLoader fxmlLoad(String location) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource(location));
        return fxmlLoader;
    }


    @FXML
    public void onClickTeacher() {
        setHighlightedNav(teacherBox);

        teacherViewController.updateTeacher();

        main.setCenter(teacherView);
        main.setBottom(null);
        main.setRight(null);

        teacherViewController.clearRoot();
    }

    @FXML
    public void onClickClasses() {
        setHighlightedNav(classBox);

        main.setCenter(classView);
        main.setBottom(null);
        main.setRight(null);
        classViewController.updateClasses();
        if (classesOpened) {
            classViewController.dismountForm();
            classViewController.addClass();
        } else {
            classViewController.dismountForm();
        }

    }

    @FXML
    public void onClickTimetable() {
        setHighlightedNav(timetableBox);

        timetableViewController.load();
        timetableViewController.getLvTimetables().refresh();

        main.setCenter(timetableView);
        main.setBottom(null);
        main.setRight(null);

    }

    @FXML
    public void onClickAbsence() {
        setHighlightedNav(absenceBox);

        absenceView = new GridPane();

        // FlowPane fp = new FlowPane();
        // fp.setOrientation(Orientation.VERTICAL);

            /*for (int i = 0; i < SchoolDB.getInstance().getTeacherAbsences().size(); i++) {
                FXMLLoader fxmlAbsenceLoader = fxmlLoad("demo/absenceitem.fxml");
                HBox hBox = fxmlAbsenceLoader.load();

                TeacherAbsenceController teacherAbsenceController = fxmlAbsenceLoader.getController();
                teacherAbsenceController.setStartController(this);
                teacherAbsenceController.setData(SchoolDB.getInstance().getTeacherAbsences().get(i));

                if (column == 5) {
                    column = 0;
                    row++;
                }

                // fp.getChildren().add(hBox);
                absenceView.add(hBox, column++, row);
                GridPane.setMargin(hBox, new Insets(5));
            }*/

        final AtomicInteger[] column = {new AtomicInteger(0)};
        AtomicInteger row = new AtomicInteger(1);
        for (Teacher teacher : SchoolDB.getInstance().getTeachers()) {
            FXMLLoader fxmlAbsenceLoader = null;
            try {
                fxmlAbsenceLoader = fxmlLoad("demo/absenceitem.fxml");
                HBox hBox = fxmlAbsenceLoader.load();

                TeacherAbsenceController teacherAbsenceController = fxmlAbsenceLoader.getController();
                teacherAbsenceController.setStartController(this);

                teacherAbsenceController.setData(teacher);

                if (column[0].get() == 5) {
                    column[0].set(0);
                    row.incrementAndGet();
                }

                // fp.getChildren().add(hBox);
                absenceView.add(hBox, column[0].getAndIncrement(), row.get());
                GridPane.setMargin(hBox, new Insets(5));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        main.setCenter(null);

        // ScrollPane sp = new ScrollPane();
        // sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        // sp.setContent(fp);

        main.setCenter(absenceView);
    }

    @FXML
    public void onClickHelp() {
        setHighlightedNav(helpBox);

        //IOAccess_Absence.storeAbsence();

        teacherViewController.updateTeacher();

        main.setCenter(helpView);
        main.setBottom(null);
        main.setRight(null);
    }

    // used to highlight selected field in navbar
    public void setHighlightedNav(HBox hBox) {
        List<HBox> navbar = Arrays.asList(timetableBox, teacherBox, absenceBox, classBox,
                settingsBox, excelImportBox, excelExportBox, helpBox);
        navbar.forEach(hBox1 -> hBox1.setStyle(null));
        hBox.setStyle("-fx-background-color: #518ef0;\n" +
                "    -fx-border-radius: 30;\n" +
                "    -fx-background-radius: 10 10 10 10;");
    }

    public void teacherChangedAbsentStatus(TeacherAbsence teacherAbsence) {

    }

    @FXML
    public void exportAsExcel() {
        IOAccess_Excel.createExcelFile(SchoolDB.getInstance().getTeachers(), SchoolDB.getInstance().getSchoolClasses());

        classes.forEach(schoolClass ->
                System.out.println(SchoolDB.getInstance().getSchoolClasses().stream().filter(schoolClass1 ->
                        schoolClass1.equals(schoolClass)).findFirst().get()));
    }

    @FXML
    public void exportAsPDF() throws DocumentException, IOException {
        FXMLLoader classPDF = fxmlLoad("demo/pdf-dialog.fxml");
        DialogPane pane = classPDF.load();
        PDFController controller = classPDF.getController();

        controller.setClasses(SchoolDB.getInstance().getSchoolClasses());

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setDialogPane(pane);
        dialog.setTitle("Stundenplan zu PDF exportieren");

        Optional<ButtonType> clickedButton = dialog.showAndWait();
        if (clickedButton.get() == ButtonType.OK) {
            System.out.println(controller.classes.getValue());
            if (controller.classes.getValue() != null) {
                IOAccess_PDF.createPDF(controller.classes.getValue());
            }
        }
    }

    @FXML
    public void importFromExcel() {
        try {
            if (IOAccess_Excel.loadFile()) {
                List<Teacher> teacherList = IOAccess_Excel.readFromExcelFileTeacher();
                if (teacherList != null) {
                    setTeachers(teacherList);

                    setClasses(IOAccess_Excel.readFromExcelFileClass(SchoolDB.getInstance().getTeachers(), SchoolDB.getInstance().getSchoolClasses()));

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("vs-martkhartmannsdorf | IMPORTIERT");
                    alert.setHeaderText("Es wurde erfolgreich Importiert!");
                    alert.showAndWait();
                }

            }
        } catch (NullPointerException ignored) {
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void onSettings() {
        settingsController.setStage(stage);
        setHighlightedNav(settingsBox);
        settingsController.update();
        main.setCenter(settingsView);
        main.setBottom(null);
        main.setRight(null);
    }


    public void setTeachers(List<Teacher> teachers) {
        SchoolDB.getInstance().setTeacher(teachers);
    }

    public void setClasses(List<SchoolClass> classes) {
        SchoolDB.getInstance().setSchoolClasses(classes);
        classViewController.setItems(SchoolDB.getInstance().getSchoolClasses());
        //timetableViewController.setItems(this.classes);
    }


    public BorderPane getMain() {
        return main;
    }

    public List<Teacher> getTeacher() {
        return teachers;
    }

    public List<SchoolClass> getClasses() {
        return classes;
    }


    public void setClassesOpened(boolean classesOpened) {
        this.classesOpened = classesOpened;
    }

    public void updateTimetableViews(){
        if(timetableViewController.getController() != null){
            timetableViewController.getController().loadTimetable();
        }
    }
}
