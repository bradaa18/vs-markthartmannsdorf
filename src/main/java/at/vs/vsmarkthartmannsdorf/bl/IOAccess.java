package at.vs.vsmarkthartmannsdorf.bl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import at.vs.vsmarkthartmannsdorf.data.*;
import at.vs.vsmarkthartmannsdorf.db.SchoolDB;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

// 25.11.2021 Simon: create "storeClassFiles", "readClassFiles" Functions
// 02.12.2021 Simon: fixed JSON /read and /write
// 04.12.2021 Simon: add "storeTeacherFiles", "readTeacherFiles" Functions
// 09.12.2021 Simon: add "storeTimetableFiles", "readTimetableFiles" Functions

//TODO: IOAccess.class.getClassLoader().getResourceAsStream("");
//TODO: Fix Problem with storage in JAR
public class IOAccess {

    private static final File FILE_CLASS = Paths.get("", "data", "class.json").toFile();
    private static final File FILE_TEACHER = Paths.get("", "data", "teacher.json").toFile();
    private static final File FILE_TIMETABLE = Paths.get("", "data", "timetable.json").toFile();
    private static final File FILE_ABSENCE = Paths.get("", "data", "absence.json").toFile();
    private static final File FILE_TEACHER_TIMETABLE = Paths.get("", "data", "teacherTimetable.json").toFile();
    private static final File FILE_SUBJECT_TIMETABLE = Paths.get("", "data", "subjects.json").toFile();


    public static synchronized boolean storeClassFiles(List<SchoolClass> schoolClassList) {
        try {
            FILE_CLASS.getParentFile().mkdirs();
            ObjectMapper om = new ObjectMapper();
            String jsonStr = om.writerWithDefaultPrettyPrinter().writeValueAsString(schoolClassList);

            FileWriter fileWriter = new FileWriter(IOAccess.FILE_CLASS.getAbsolutePath(), StandardCharsets.UTF_8);
            fileWriter.write(jsonStr);
            fileWriter.close();
            System.out.println("FileWrite wrote in \"" + IOAccess.FILE_CLASS.getName() + "\".");

            return true; //successfully wrote data

        } catch (IOException e) {
            System.out.println("Failed to write in the File: \"" + IOAccess.FILE_CLASS.getName() + "\".");
            e.printStackTrace();
            return false; //not successfully wrote data
        }
    }

    public static synchronized List<SchoolClass> readClassFiles() {
        List<SchoolClass> schoolClassList = new ArrayList<>();
        if (!new File(FILE_CLASS.getAbsolutePath()).exists()) {
            return new ArrayList<>();
        }
        try {
            String result = Files.readString(Paths.get(FILE_CLASS.getAbsolutePath()), StandardCharsets.UTF_8);

            if (result.isEmpty()) {
                return new ArrayList<>();
            }

            ObjectMapper om = new ObjectMapper();
            SchoolClass[] schoolClasses = om.readValue(result, SchoolClass[].class);



           Arrays.stream(schoolClasses).forEach(schoolClass -> {
               Optional<Teacher> teacherLink = SchoolDB.getInstance().getTeachers().stream().filter(teacher -> teacher
                       .getAbbreviation()
                       .equals(SchoolDB.getInstance().getTeacherByID(teacher.getId()).get().getAbbreviation()))
                       .findFirst();

               if (teacherLink.isPresent()){
                   int id = teacherLink.get().getId();
                   schoolClass.setTeacherID(id);
               }
            });

            schoolClassList = Arrays.asList(schoolClasses);
            System.out.println("FileWrite read in \"" + IOAccess.FILE_CLASS.getName() + "\".");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return schoolClassList;
    }

    public static synchronized boolean storeTeacherFiles(List<Teacher> teacherList) {
        try {
            FILE_TEACHER.getParentFile().mkdirs();


            ObjectMapper om = new ObjectMapper();
            String jsonStr = om.writerWithDefaultPrettyPrinter().writeValueAsString(teacherList);

            FileWriter fileWriter = new FileWriter(IOAccess.FILE_TEACHER.getAbsolutePath(), StandardCharsets.UTF_8);
            fileWriter.write(jsonStr);
            fileWriter.close();
            System.out.println("FileWrite wrote in \"" + IOAccess.FILE_TEACHER.getName() + "\".");

            return true; //successfully wrote data

        } catch (IOException e) {
            System.out.println("Failed to write in the File: \"" + IOAccess.FILE_TEACHER.getName() + "\".");
            e.printStackTrace();
            return false; //not successfully wrote data
        }
    }

    public static synchronized List<Teacher> readTeacherFiles() {
        List<Teacher> teacherList = new ArrayList<>();
        if (!new File(FILE_TEACHER.getAbsolutePath()).exists()) {
            return new ArrayList<>();
        }
        try {
            String result = Files.readString(Paths.get(FILE_TEACHER.getAbsolutePath()), StandardCharsets.UTF_8);

            if (result.isEmpty()) {
                return new ArrayList<>();
            }

            ObjectMapper om = new ObjectMapper();
            Teacher[] teachers = om.readValue(result, Teacher[].class);

            teacherList = Arrays.asList(teachers);
            System.out.println("FileWrite read in \"" + IOAccess.FILE_TEACHER.getName() + "\".");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return teacherList;
    }

    public static synchronized boolean storeTimetableFiles() {
        try {
            FILE_TIMETABLE.getParentFile().mkdirs();

            ObjectMapper om = new ObjectMapper();
            String jsonStr = om.writerWithDefaultPrettyPrinter().writeValueAsString(SchoolDB.getInstance().getTimetables());

            FileWriter fileWriter = new FileWriter(IOAccess.FILE_TIMETABLE.getAbsolutePath(), StandardCharsets.UTF_8);
            fileWriter.write(jsonStr);
            fileWriter.close();
            System.out.println("FileWrite wrote in \"" + IOAccess.FILE_TIMETABLE.getName() + "\".");

            return true; //successfully wrote data

        } catch (IOException e) {
            System.out.println("Failed to write in the File: \"" + IOAccess.FILE_TIMETABLE.getName() + "\".");
            e.printStackTrace();
            return false; //not successfully wrote data
        }
    }

    public static synchronized List<Timetable> readTimetableFiles() {
        List<Timetable> timetableList = new ArrayList<>();

        if (!new File(FILE_TIMETABLE.getAbsolutePath()).exists()) {
            return new ArrayList<>();
        }
        try {
            String result = Files.readString(Paths.get(FILE_TIMETABLE.getAbsolutePath()), StandardCharsets.UTF_8);

            if (result.isEmpty()) {
                return new ArrayList<>();
            }

            ObjectMapper om = new ObjectMapper();
            Timetable[] timetables = om.readValue(result, Timetable[].class);

            SchoolDB.getInstance().setTimetables(Arrays.asList(timetables));
            System.out.println("FileWrite read in \"" + IOAccess.FILE_TIMETABLE.getName() + "\".");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return timetableList;
    }

    public static synchronized boolean storeAbsenceFiles() {
        try {
            FILE_ABSENCE.getParentFile().mkdirs();
            ObjectMapper om = new ObjectMapper();
            String jsonStr = om.writerWithDefaultPrettyPrinter().writeValueAsString(SchoolDB.getInstance().getTeacherAbsences());

            FileWriter fileWriter = new FileWriter(IOAccess.FILE_ABSENCE.getAbsolutePath(), StandardCharsets.UTF_8);
            fileWriter.write(jsonStr);
            fileWriter.close();
            System.out.println("FileWrite wrote in \"" + IOAccess.FILE_ABSENCE.getName() + "\".");

            return true; //successfully wrote data

        } catch (IOException e) {
            System.out.println("Failed to write in the File: \"" + IOAccess.FILE_ABSENCE.getName() + "\".");
            e.printStackTrace();
            return false; //not successfully wrote data
        }
    }

    public static synchronized void readAbsenceFiles() {
        List<TeacherAbsence> teacherAbsences = new ArrayList<>();
        if (!new File(FILE_ABSENCE.getAbsolutePath()).exists()) {
            return;
        }
        try {
            String result = Files.readString(Paths.get(FILE_ABSENCE.getAbsolutePath()), StandardCharsets.UTF_8);

            if (result.isEmpty()) {
                return;
            }

            ObjectMapper om = new ObjectMapper();
            TeacherAbsence[] teacherAbsence = om.readValue(result, TeacherAbsence[].class);

            teacherAbsences = Arrays.asList(teacherAbsence);
            System.out.println("FileWrite read in \"" + IOAccess.FILE_ABSENCE.getName() + "\".");

        } catch (IOException e) {
            e.printStackTrace();
        }
        SchoolDB.getInstance().setTeacherAbsences(FXCollections.observableArrayList(teacherAbsences));
    }

    public static synchronized boolean storeTeacherTimetableFiles() {
        try {
            FILE_TEACHER_TIMETABLE.getParentFile().mkdirs();
            ObjectMapper om = new ObjectMapper();
            String jsonStr = om.writerWithDefaultPrettyPrinter().writeValueAsString(SchoolDB.getInstance().getTeacherTimetables());

            FileWriter fileWriter = new FileWriter(IOAccess.FILE_TEACHER_TIMETABLE.getAbsolutePath(), StandardCharsets.UTF_8);
            fileWriter.write(jsonStr);
            fileWriter.close();
            System.out.println("FileWrite wrote in \"" + IOAccess.FILE_TEACHER_TIMETABLE.getName() + "\".");

            return true; //successfully wrote data

        } catch (IOException e) {
            System.out.println("Failed to write in the File: \"" + IOAccess.FILE_TEACHER_TIMETABLE.getName() + "\".");
            e.printStackTrace();
            return false; //not successfully wrote data
        }
    }

    public static synchronized void readTeacherTimetableFiles() {
        List<TeacherTimetable> teacherTimetable = new ArrayList<>();
        if (!new File(FILE_TEACHER_TIMETABLE.getAbsolutePath()).exists()) {
            return;
        }
        try {
            String result = Files.readString(Paths.get(FILE_TEACHER_TIMETABLE.getAbsolutePath()), StandardCharsets.UTF_8);

            if (result.isEmpty()) {
                return;
            }

            ObjectMapper om = new ObjectMapper();
            TeacherTimetable[] teacherTimetables = om.readValue(result, TeacherTimetable[].class);

            teacherTimetable = Arrays.asList(teacherTimetables);
            System.out.println("FileWrite read in \"" + IOAccess.FILE_TEACHER_TIMETABLE.getName() + "\".");

        } catch (IOException e) {
            e.printStackTrace();
        }
        SchoolDB.getInstance().setTeacherTimetables(FXCollections.observableArrayList(teacherTimetable));
    }
    public static synchronized void readSubjectFiles() {
        List<Subjectobject> subjectobject = new ArrayList<>();
        if (!new File(FILE_SUBJECT_TIMETABLE.getAbsolutePath()).exists()) {
            return;
        }
        try {
            String result = Files.readString(Paths.get(FILE_SUBJECT_TIMETABLE.getAbsolutePath()), StandardCharsets.UTF_8);

            if (result.isEmpty()) {
                return;
            }

            ObjectMapper om = new ObjectMapper();
            Subjectobject[] subjectobjects = om.readValue(result, Subjectobject[].class);

            subjectobject = Arrays.asList(subjectobjects);
            System.out.println("FileWrite read in \"" + IOAccess.FILE_SUBJECT_TIMETABLE.getName() + "\".");

        } catch (IOException e) {
            e.printStackTrace();
        }
        SchoolDB.getInstance().setSubjects(FXCollections.observableArrayList(subjectobject));
    }

        public static synchronized boolean storeSubjectFiles() {
            try {
                FILE_SUBJECT_TIMETABLE.getParentFile().mkdirs();
                ObjectMapper om = new ObjectMapper();
                String jsonStr = om.writerWithDefaultPrettyPrinter().writeValueAsString(SchoolDB.getInstance().getSubjects());

                FileWriter fileWriter = new FileWriter(IOAccess.FILE_SUBJECT_TIMETABLE.getAbsolutePath(), StandardCharsets.UTF_8);
                fileWriter.write(jsonStr);
                fileWriter.close();
                System.out.println("FileWrite wrote in \"" + IOAccess.FILE_SUBJECT_TIMETABLE.getName() + "\".");

                return true; //successfully wrote data

            } catch (IOException e) {
                System.out.println("Failed to write in the File: \"" + IOAccess.FILE_SUBJECT_TIMETABLE.getName() + "\".");
                e.printStackTrace();
                return false; //not successfully wrote data
            }
        }


}
