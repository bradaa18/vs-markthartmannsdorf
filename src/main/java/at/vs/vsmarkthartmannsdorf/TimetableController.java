package at.vs.vsmarkthartmannsdorf;

import at.vs.vsmarkthartmannsdorf.db.SchoolDB;
import at.vs.vsmarkthartmannsdorf.bl.PropertiesLoader;
import at.vs.vsmarkthartmannsdorf.data.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import lombok.Data;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.spreadsheet.Grid;

import java.net.URL;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.zip.InflaterInputStream;

@Data
public class TimetableController implements Initializable {

    @FXML
    public ListView<Timetable> lvTimetables;
    public BorderPane root;
    public Label lblInfo;
    public HBox hbSubjects;

    private boolean isEdit;
    private Timetable visibleTimetable;
    private GridPane timetableView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ((BorderPane) root.getCenter()).getTop().setVisible(false);

        lvTimetables.setItems(SchoolDB.getInstance().getTimetables());
        visibleTimetable = null;

        isEdit = false;

        lblInfo.setStyle("-fx-font-size: 20; -fx-font-weight: bold");

        hbSubjects.setVisible(false);
        timetableView = null;
    }

    public void load() {
        isEdit = false;

        ((VBox) ((BorderPane) root.getCenter()).getCenter()).getChildren().clear();
        ((BorderPane) root.getCenter()).getTop().setVisible(false);

        lvTimetables.setItems(SchoolDB.getInstance().getTimetables());
    }

    public GridPane buildTimetable() {
        timetableView = new GridPane();

        int column = 1;
        int row = 1;

        for (int i = 1; i <= Timetable.MAX_HOURS; i++) {
            timetableView.add(new Label(i + ""), 0, i);
        }
        for (Day day : Day.values()) {
            timetableView.add(new Label(day.name()), column, 0);
            column++;
        }

        column = 1;

        for (Day day : Day.values()) {
            if (column == Day.values().length + 1) {
                column = 1;
            }
            for (int i = 1; i <= Timetable.MAX_HOURS; i++) {
                Lesson lesson = visibleTimetable.getSubjects().get(day).get(i);
                if (row == Timetable.MAX_HOURS + 1) {
                    row = 1;
                }

                VBox vBox = new VBox();

                Color color = null;
                if (lesson.getSubject() != null) {
                    String colorHex = PropertiesLoader.getInstance().getProperties().getProperty(lesson.getSubject().name());
                    color = Color.valueOf(colorHex);

                    vBox.setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
                }

                vBox.setPadding(new Insets(10, 10, 10, 10));

                Label lblSubject;
                Label lblTeacher = new Label("");
                ;

                if (!lesson.getTeacher().isEmpty()) {
                    for (int k = 0; k < lesson.getTeacher().size(); k++) {
                        if (k == 0) {
                            lblTeacher.setText(lesson.getTeacher().get(k).getTeacher().getAbbreviation());
                        }
                        if (k != lesson.getTeacher().size() - 1) {
                            lblTeacher.setText(lblTeacher.getText() + " | " + lesson.getTeacher().get(k).getTeacher().getAbbreviation());
                        }
                    }
                }

                lblSubject = lesson.getSubject() == null ? new Label(" ") : new Label(lesson.getSubject().name());

                if (color != null) {
                    if (color.getBrightness() < 0) {
                        lblTeacher.setStyle("-fx-text-fill: white");
                        lblSubject.setStyle("-fx-text-fill: white");
                    }
                }

                vBox.getChildren().add(lblSubject);
                vBox.getChildren().add(lblTeacher);

                vBox.setOnDragDetected(mouseDragEvent -> {
                    if (isEdit) {
                        Dragboard db = vBox.startDragAndDrop(TransferMode.ANY);
                        db.setDragView(vBox.snapshot(null, null), mouseDragEvent.getX(), mouseDragEvent.getY());
                        ClipboardContent clipboardContent = new ClipboardContent();

                        int sourceColumnIndex = GridPane.getColumnIndex(vBox);
                        int sourceRowIndex = GridPane.getRowIndex(vBox);
                        String content = String.format("%d,%d", sourceColumnIndex, sourceRowIndex);

                        clipboardContent.putString(content);
                        db.setContent(clipboardContent);
                        mouseDragEvent.consume();
                    }
                });

                vBox.setOnDragOver(dragEvent -> {
                    if (isEdit) {
                        if (dragEvent.getGestureSource() != vBox &&
                                dragEvent.getDragboard().hasString()) {
                            dragEvent.acceptTransferModes(TransferMode.MOVE);
                        }

                        dragEvent.consume();
                    }
                });

                int finalRow = row;
                vBox.setOnDragDropped(dragEvent -> {
                    if (isEdit) {
                        Dragboard db = dragEvent.getDragboard();
                        boolean success = false;

                        if (db.hasString()) {
                            if (db.getString().contains(",")){
                                String split[] = db.getString().split(",");
                                int source_column = Integer.parseInt(split[0]);
                                int source_row = Integer.parseInt(split[1]);

                                Node sourceNode = getNodeByRowColumnIndex(source_row, source_column);
                                switchNodes(sourceNode, vBox, timetableView);
                            }else{
                                Subject subject = Subject.valueOf(db.getString());

                                List<MenuItem> items = new ArrayList<>();
                                for (TeacherSubject teacherSubject: SchoolDB.getInstance().getTeacherBySubject(subject)){
                                    MenuItem menuItem = new MenuItem(teacherSubject.getTeacher().getAbbreviation());
                                    menuItem.setOnAction(actionEvent -> {
                                        addSubject(day, finalRow, new Lesson(subject, Arrays.asList(SchoolDB.getInstance()
                                                .getTeacherSubjects()
                                                .stream()
                                                .filter(teacherSubject1 -> teacherSubject1.getTeacher().getAbbreviation().equals(menuItem.getText())).findFirst().get())));
                                    });
                                    items.add(menuItem);
                                }
                                ContextMenu contextMenu = new ContextMenu();
                                contextMenu.getItems().addAll(items);

                                contextMenu.show(timetableView, dragEvent.getScreenX(), dragEvent.getScreenY());
                            }
                            success = true;
                        }
                        dragEvent.setDropCompleted(success);
                        dragEvent.consume();
                    }
                });

                timetableView.add(vBox, column, row);
                row++;
            }
            column++;
        }

        timetableView.setGridLinesVisible(true);
        addStyle(timetableView);

        return timetableView;
    }

    private void switchNodes(Node source, Node target, GridPane pane) {
        int sourceColumnIndex = GridPane.getColumnIndex(source);
        int sourceRowIndex = GridPane.getRowIndex(source);
        int targetColumnIndex = GridPane.getColumnIndex(target);
        int targetRowIndex = GridPane.getRowIndex(target);

        pane.getChildren().removeAll(source, target);

        pane.add(source, targetColumnIndex, targetRowIndex);
        pane.add(target, sourceColumnIndex, sourceRowIndex);

        SchoolDB.getInstance().switchLessons(Day.values()[sourceColumnIndex - 1],
                Day.values()[targetColumnIndex - 1],
                sourceRowIndex, targetRowIndex, visibleTimetable);
        reload();
        setContent();
    }

    public Node getNodeByRowColumnIndex(int row, int column) {
        Node result = null;
        ObservableList<Node> childrens = timetableView.getChildren();

        for (Node node : childrens) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) {
                result = node;
                break;
            }
        }

        return result;
    }

    public void addSubject(Day day, int hour, Lesson lesson) {
        SchoolDB.getInstance().addSubject(day, hour, lesson, visibleTimetable);
        reload();
        setContent();
    }

    public void removeSubject(Day day, int hour){
        SchoolDB.getInstance().removeSubject(day, hour, visibleTimetable);
        reload();
        setContent();
    }

    @FXML
    public void onSelectClass() {
        visibleTimetable = lvTimetables.getSelectionModel().getSelectedItem();
        lblInfo.setText(visibleTimetable.getSchoolClass().getClassname());

        visibleTimetable.addSubject(Day.Montag, 1,
                new Lesson(Subject.Deutsch, Arrays.asList(SchoolDB.getInstance().getTeacherSubjects().get(0))));
        setContent();
    }

    public void addStyle(GridPane timetableView) {
        List<ColumnConstraints> columnConstraintsList = new ArrayList<>();
        List<RowConstraints> rowConstraintsList = new ArrayList<>();


        for (int i = 0; i < timetableView.getColumnCount(); i++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setHalignment(HPos.CENTER);
            if (i == 0) {
                columnConstraints.setPercentWidth(5);
                columnConstraintsList.add(columnConstraints);
            } else {
                columnConstraints.setPercentWidth(50);
                columnConstraintsList.add(columnConstraints);
            }
        }

        RowConstraints rowConstraints = new RowConstraints();
        for (int i = 0; i < timetableView.getRowCount(); i++) {
            rowConstraints.setPercentHeight(50);
            rowConstraintsList.add(rowConstraints);
        }

        timetableView.getColumnConstraints().addAll(columnConstraintsList);
        timetableView.getRowConstraints().addAll(rowConstraintsList);

        GridPane.setVgrow(timetableView, Priority.ALWAYS);
    }

    public void setContent() {
        ((VBox) ((BorderPane) root.getCenter()).getCenter()).getChildren().clear();
        ((BorderPane) root.getCenter()).getTop().setVisible(true);

        ((VBox) ((BorderPane) root.getCenter()).getCenter()).getChildren().add(buildTimetable());
    }

    public void reload() {
        SchoolClass schoolClass = visibleTimetable.getSchoolClass();
        Optional<Timetable> timetable = SchoolDB.getInstance().findTimetableByClass(schoolClass);
        if (timetable.isPresent()) {
            visibleTimetable = timetable.get();
        }
    }

    @FXML
    public void onEditTimetable() {
        if (isEdit == true) {
            isEdit = false;
            hbSubjects.setVisible(false);
            lblInfo.setText(visibleTimetable.getSchoolClass().getClassname());
            hbSubjects.getChildren().clear();
            setContent();
        } else {
            isEdit = true;
            hbSubjects.setVisible(true);
            lblInfo.setText("Bearbeitungsmodus | " + visibleTimetable.getSchoolClass().getClassname());
        }

        if (isEdit) {
            System.out.println("Edit");
            hbSubjects.getChildren().clear();

            GridPane subjects = new GridPane();
            hbSubjects.setPadding(new Insets(10, 10, 10, 10));
            subjects.setAlignment(Pos.BASELINE_CENTER);
            subjects.setHgap(10);
            subjects.setVgap(10);

            int i = 0;
            int j = 0;
            for (Subject subject : Subject.values()) {
                VBox vBox = new VBox();
                vBox.getChildren().add(new Label(subject.name()));
                vBox.setPadding(new Insets(5, 5, 5, 5));

                String colorHex = PropertiesLoader.getInstance().getProperties().getProperty(subject.name());
                Color color = Color.valueOf(colorHex);
                vBox.setBackground(new Background(new BackgroundFill(color, new CornerRadii(5), Insets.EMPTY)));
                vBox.setPadding(new Insets(10, 10, 10, 10));
                vBox.setAlignment(Pos.CENTER);

                if (i == (int) Math.ceil(Subject.values().length / 4.0)) {
                    i = 0;
                    j += 1;
                }

                vBox.setOnDragDetected(mouseDragEvent -> {
                    Dragboard db = vBox.startDragAndDrop(TransferMode.ANY);
                    db.setDragView(vBox.snapshot(null, null), mouseDragEvent.getX(), mouseDragEvent.getY());
                    ClipboardContent clipboardContent = new ClipboardContent();
                    String content = String.format(subject.name());
                    clipboardContent.putString(content);
                    db.setContent(clipboardContent);
                    mouseDragEvent.consume();
                });

                vBox.setOnDragOver(dragEvent -> {
                        if (dragEvent.getGestureSource() != vBox &&
                                dragEvent.getDragboard().hasString()) {
                            dragEvent.acceptTransferModes(TransferMode.MOVE);
                        }

                        dragEvent.consume();
                });

                subjects.add(vBox, i++, j);
            }

            hbSubjects.setOnDragOver(dragEvent -> {
                if (isEdit) {
                    if (dragEvent.getGestureSource() != hbSubjects &&
                            dragEvent.getDragboard().hasString()  && dragEvent.getDragboard().getString().contains(",")) {
                        dragEvent.acceptTransferModes(TransferMode.MOVE);
                    }

                    dragEvent.consume();
                }
            });

            hbSubjects.setOnDragDropped(dragEvent -> {
                Dragboard db = dragEvent.getDragboard();
                boolean success = false;

                if (db.hasString()) {
                    System.out.println(db.getString());
                    if (db.getString().contains(",")){
                        String split[] = db.getString().split(",");
                        int sourceColumn = Integer.parseInt(split[0]);
                        int sourceRow = Integer.parseInt(split[1]);

                        removeSubject(Day.values()[sourceColumn - 1], sourceRow);
                        reload();
                        setContent();
                    }

                    //switchNodes(sourceNode, vBox, timeTableView.get());
                    success = true;
                }
                dragEvent.setDropCompleted(success);
                dragEvent.consume();
            });

            hbSubjects.getChildren().add(subjects);

        }
    }
}
