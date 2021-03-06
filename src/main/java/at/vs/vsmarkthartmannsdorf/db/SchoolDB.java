package at.vs.vsmarkthartmannsdorf.db;

import at.vs.vsmarkthartmannsdorf.data.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import lombok.Data;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Data
public class SchoolDB {
    private static SchoolDB instance;

    private ObservableList<Teacher> teachers;
    private ObservableList<SchoolClass> schoolClasses;
    private ObservableList<Timetable> timetables;
    private ObservableList<TeacherSubject> teacherSubjects;
    private ObservableList<TeacherTimetable> teacherTimetables;

    private ObservableList<TeacherAbsence> teacherAbsences;
    private GridPane printTimetables;
    private ArrayList<Subjectobject> subjects;


    private SchoolDB() {
        teachers = FXCollections.observableArrayList();
        schoolClasses = FXCollections.observableArrayList();
        timetables = FXCollections.observableArrayList();
        teacherSubjects = FXCollections.observableArrayList();
        teacherTimetables = FXCollections.observableArrayList();
        teacherAbsences = FXCollections.observableArrayList();
        printTimetables = new GridPane();
        subjects = new ArrayList<Subjectobject>();

        for (Teacher teacher : teachers) {
            for (Subjectobject subject : subjects) {
                teacherSubjects.add(new TeacherSubject(teacher.getId(), subject));
            }
        }

    }

    public static SchoolDB getInstance() {
        if (instance == null) {
            instance = new SchoolDB();
        }
        return instance;
    }

    public void addTeacher(Teacher teacher) {
        teachers.add(teacher);
        for (Subjectobject subject : teacher.getSubjects()) {
            teacherSubjects.add(new TeacherSubject(teacher.getId(), subject));
            teacherTimetables.add(new TeacherTimetable(teacher.getId()));
        }
    }

    public void setTeacher(List<Teacher> teachers) {
        this.teachers = FXCollections.observableArrayList();
        this.teacherSubjects = FXCollections.observableArrayList();
        for (Teacher teacher : teachers) {
            addTeacher(teacher);
        }
    }

    public void addSchoolClass(SchoolClass schoolClass) {
        schoolClasses.add(schoolClass);
        addTimetable(new Timetable(schoolClass, Week.A));
    }

    public void removeSchoolClass(SchoolClass schoolClass) {
        schoolClasses.remove(schoolClass);

        List<Timetable> timetablesToRemove = getTimetablesFromClass(schoolClass);
        timetablesToRemove.forEach(t -> timetables.remove(t));
    }

    public void setTimetables(List<Timetable> timetables){
        this.timetables = FXCollections.observableArrayList(timetables);
    }

    private void addTimetable(Timetable timetable) {
        timetables.add(timetable);
    }

    public ObservableList<Teacher> getTeachers() {
        return teachers;
    }

    public ObservableList<SchoolClass> getSchoolClasses() {
        return schoolClasses;
    }

    public ObservableList<Timetable> getTimetables() {
        return timetables;
    }

    public ObservableList<TeacherSubject> getTeacherSubjects() {
        return teacherSubjects;
    }

    public void removeTeacher(Teacher teacher) {
        teachers.remove(teacher);
        teacherTimetables.remove(findTeacherTimetableByID(teacher.getId()));
    }

    public void setSchoolClasses(List<SchoolClass> schoolClasses) {
        this.schoolClasses = FXCollections.observableArrayList();
        this.schoolClasses.addAll(schoolClasses);
    }

    public Optional<Timetable> findTimetableByClass(SchoolClass schoolClass, Week week) {
        return timetables.stream().filter(t -> t.getSchoolClass().equals(schoolClass) && t.getWeek().equals(week)).findFirst();
    }

    public void addSubject(Day day, int hour, Lesson lesson, Timetable timetable) {
        getTimetables().get(getTimetables().indexOf(timetable)).addSubject(day, hour, lesson);
    }

    public List<TeacherSubject> getTeacherBySubject(Subjectobject subject) {
        return SchoolDB.getInstance().getTeacherSubjects().stream().filter(teacherSubject ->
                teacherSubject.getSubject().equals(subject)).collect(Collectors.toList());
    }

    public boolean switchLessons(Day sourceDay, Day targetDay, int sourceHour, int targetHour, Timetable timetable) {
        Lesson sourceTeacherLesson = timetable.getSubjects().get(sourceDay).get(sourceHour);
        Lesson targetTeacherLesson = timetable.getSubjects().get(targetDay).get(targetHour);

        boolean switchable = true;

        List<TeacherSubject> teachers = sourceTeacherLesson.getTeacher();
        List<TeacherSubject> teachers2 = targetTeacherLesson.getTeacher();

        for (TeacherSubject teacherSubject: teachers){
            int teacherID = teacherSubject.getTeacherId();
            Optional<TeacherTimetable> teacherTimetable = findTeacherTimetableByID(teacherID);
            if (teacherTimetable.isPresent()){
                if (checkIfTeacherIsBlocked(teacherID, targetDay, targetHour, timetable.getWeek())){
                    switchable = false;
                    break;
                }
                if (!teacherTimetable.get().getWeeklySubjects().get(timetable.getWeek()).get(targetDay).get(targetHour).isEmpty()){
                    switchable = false;
                    break;
                }
            }
        }

        for (TeacherSubject teacherSubject: teachers2){
            int teacherID = teacherSubject.getTeacherId();
            Optional<TeacherTimetable> teacherTimetable = findTeacherTimetableByID(teacherID);
            if (teacherTimetable.isPresent()){
                if (checkIfTeacherIsBlocked(teacherID, sourceDay, sourceHour, timetable.getWeek())){
                    switchable = false;
                    break;
                }
                if (!teacherTimetable.get().getWeeklySubjects().get(timetable.getWeek()).get(sourceDay).get(sourceHour).isEmpty()){
                    switchable = false;
                    break;
                }
            }
        }

        if (switchable){
            for (TeacherSubject teacherSubject: teachers){
                removeSubjectFromTeacherTimetable(teacherSubject.getTeacherId(), sourceDay, sourceHour, timetable.getWeek());
                addSubjectToTeacherTimetable(teacherSubject.getTeacherId(), targetDay, targetHour, timetable.getWeek(),
                        new TeacherLesson(teacherSubject.getSubject(), timetable.getSchoolClass().getId()));
            }

            timetables.stream()
                    .filter(t -> t.getSchoolClass()
                            .equals(timetable.getSchoolClass())
                            && t.getWeek().equals(timetable.getWeek())).findFirst().get().addSubject(sourceDay, sourceHour, targetTeacherLesson);
            timetables.stream()
                    .filter(t -> t.getSchoolClass()
                            .equals(timetable.getSchoolClass())
                            && t.getWeek().equals(timetable.getWeek())).findFirst().get().addSubject(targetDay, targetHour, sourceTeacherLesson);
            return true;
        }
        return false;
    }

    public void removeSubject(Day day, int hour, Timetable timetable){
        List<TeacherSubject> teacherSubjects = timetable.getSubjects().get(day).get(hour).getTeacher();
        teacherSubjects.forEach(teacherSubject -> {
           findTeacherTimetableByID(teacherSubject.getTeacherId()).get().addSubject(day, hour, new TeacherLesson(), timetable.getWeek());
        });
        timetables.stream().filter(t -> t.getSchoolClass().equals(timetable.getSchoolClass())
                && t.getWeek().equals(timetable.getWeek())).findFirst().get().removeSubject(day, hour);
    }

    public void addTeacherToLesson(Day day, int hour, Timetable timetable, TeacherSubject teacherSubject){
        if (!timetable.getSubjects().get(day).get(hour).getTeacher().contains(teacherSubject)) {
            timetables.stream().filter(t -> t.getSchoolClass().equals(timetable.getSchoolClass())
                            && t.getWeek().equals(timetable.getWeek()))
                    .findFirst()
                    .get()
                    .getSubjects()
                    .get(day)
                    .get(hour)
                    .addTeacher(teacherSubject);
            addSubjectToTeacherTimetable(teacherSubject.getTeacherId(), day, hour, timetable.getWeek(), new TeacherLesson(
                    teacherSubject.getSubject(), timetable.getSchoolClass().getId()));
        }
    }

    public void addSubjectToTeacherTimetable(int id, Day day, int hour, Week week, TeacherLesson teacherLesson){
        findTeacherTimetableByID(id).get().addSubject(day, hour, teacherLesson, week);
    }

    public void removeSubjectFromTeacherTimetable(int id, Day day, int hour, Week week){
        findTeacherTimetableByID(id).get().addSubject(day, hour, null, week);
    }

    public void removeTeacherFromLesson(Day day, int hour, Timetable timetable, TeacherSubject teacherSubject){
        timetables.stream().filter(t -> t.getSchoolClass().equals(timetable.getSchoolClass()))
                .findFirst()
                .get()
                .getSubjects()
                .get(day)
                .get(hour)
                .removeTeacher(teacherSubject);
        findTeacherTimetableByID(teacherSubject.getTeacherId()).get().addSubject(day, hour, new TeacherLesson(), timetable.getWeek());
    }

    public boolean checkIfTeacherContainsInLesson(Day day, int hour, Timetable timetable, TeacherSubject teacherSubject){
        return timetables.stream()
                .filter(t -> t.getSchoolClass().equals(timetable.getSchoolClass()))
                .findFirst()
                .get()
                .getSubjects()
                .get(day)
                .get(hour)
                .getTeacher()
                .contains(teacherSubject);
    }

    public List<Timetable> getTimetablesFromClass(SchoolClass schoolClass){
        return timetables.stream()
                .filter(t -> t.getSchoolClass().equals(schoolClass)).collect(Collectors.toList());
    }

    public List<Week> getWeeksFromSchoolClass(SchoolClass schoolClass){
        List<Week> weeks = new ArrayList<>();
        for (Timetable timetable: getTimetablesFromClass(schoolClass)){
            weeks.add(timetable.getWeek());
        }
        return weeks;
    }

    public void addWeekToTimetable(SchoolClass schoolClass){
        List<Week> weeks = getWeeksFromSchoolClass(schoolClass);
        Week lastWeek = weeks.get(weeks.size() - 1);

        List<Week> availableWeeks = Arrays.asList(Week.values());
        if (!availableWeeks.get(availableWeeks.size() - 1).equals(lastWeek)){
            Timetable timetable = new Timetable(schoolClass, availableWeeks.get(availableWeeks.indexOf(lastWeek) + 1));
            addTimetable(timetable);
        }
    }

    public void removeWeekFromTimetable(SchoolClass schoolClass, Week week){
        Timetable timetable = timetables.stream().filter(t -> t.getSchoolClass().equals(schoolClass) && t.getWeek().equals(week))
                .findFirst()
                .get();

        timetable.getSubjects().forEach((day, integerLessonHashMap) -> {
            integerLessonHashMap.forEach((hour, lesson) -> {
                lesson.getTeacher().forEach(teacherSubject -> {
                    removeTeacherFromLesson(day, hour, timetable, teacherSubject);
                });
            });
        });
        timetables.remove(timetable);
    }


    public void setNewTeacherAbsence (TeacherAbsence teacherAbsence) {
        /*Optional<TeacherAbsence> oldTeacherAbsence = teacherAbsences
                .stream()
                .filter(tA -> tA.getTeacherID() == teacherAbsence.getTeacherID())
                .findFirst();

        if (oldTeacherAbsence.isPresent()) {

            int index = teacherAbsences.indexOf(oldTeacherAbsence.get());
            teacherAbsences.add(index, teacherAbsence);
            teacherAbsences.remove(oldTeacherAbsence.get());
        } else {*/
            teacherAbsences.add(teacherAbsence);
        /*}*/
    }

    public int getLastTeacherID(){
        List<Integer> ids = teachers.stream().map(Teacher::getId).toList();
        if (!ids.isEmpty()){
            return Collections.max(ids) + 1;
        }else{
            return 0;
        }
    }

    public int getLastSchoolClassID(){
        List<Integer> ids = schoolClasses.stream().map(SchoolClass::getId).toList();
        if (!ids.isEmpty()){
            return Collections.max(ids) + 1;
        }else{
            return 0;
        }
    }

    public Optional<Teacher> getTeacherByID(int id){
        return teachers.stream().filter(t -> t.getId() == id).findFirst();
    }

    public boolean isTeacherAbsence (Teacher teacher) {
        //return teacherAbsences.stream().anyMatch(teacherAbsence -> teacherAbsence.getTeacherID() == teacher.getId());

        /*Optional<LocalDate> latestTeacherAbsenceDate = teacherAbsences
                .stream()
                .filter(teacherAbsence -> teacherAbsence.getTeacherID() == teacher.getId())
                .map(TeacherAbsence::getToDate)
                .max(LocalDate::compareTo);

        return latestTeacherAbsenceDate.map(localDate -> localDate.isAfter(LocalDate.now())).orElse(false);*/

        AtomicBoolean isAbsence = new AtomicBoolean(false);
        teacherAbsences.forEach(teacherAbsence -> {
            if (teacherAbsence.getFromDate().isBefore(LocalDate.now()) && teacherAbsence.getToDate().isAfter(LocalDate.now())
            || teacherAbsence.getFromDate().isEqual(LocalDate.now()) || teacherAbsence.getToDate().isEqual(LocalDate.now())){
                isAbsence.set(true);
            }
        });
        return isAbsence.get();



    }

    public void removeAbsenceFromTeacher (Teacher teacher) {
        List<TeacherAbsence> teacherAbsenceList =
                teacherAbsences
                        .stream()
                        .filter(teacherAbsence -> teacherAbsence.getTeacherID() == teacher.getId()).toList();

        teacherAbsenceList.forEach(teacherAbsence -> {
            teacherAbsences.remove(teacherAbsence);
        });

    }

    public void removeAbsence (TeacherAbsence teacherAbsence) {
        teacherAbsences.remove(teacherAbsence);
    }

    public Optional<TeacherTimetable> findTeacherTimetableByID(int id){
        return teacherTimetables.stream().filter(teacherTimetable -> teacherTimetable.getTeacherID() == id).findFirst();
    }

    public ArrayList<Subjectobject> getSubjects() {
        return subjects;
    }
    public void addSubject(String name, double red, double green, double blue){
        subjects.add(new Subjectobject(name, red,green,blue));
    }
    public boolean subjectalreadyexist(String name){
        for(int i=0;i<subjects.size();i++)
            if(subjects.get(i).getName().equals(name))
                return true;
        return false;
    }

    public boolean checkIfTeacherIsBlocked(int teacherId, Day day, int hour, Week week){
        TeacherTimetable teacherTimetable = findTeacherTimetableByID(teacherId).get();
        if (!teacherTimetable.getWeeklySubjects().keySet().contains(week)){
            return false;
        }
        return teacherTimetable.getWeeklySubjects().get(week).get(day).get(hour).isBlocked();
    }

    public Subjectobject getSubjectobjectFromName(String name){
        return SchoolDB.getInstance().getSubjects().stream().filter(subject -> subject.getName().equals(name)).findFirst().get();
    }
    public void setSubjects(ObservableList<Subjectobject> subjects){
        this.subjects = new ArrayList<>(subjects);
    }

    public boolean subjectexistsinanytimetable(Subjectobject subject){
        for(int i=0;i<timetables.size();i++){
            System.out.println(timetables.get(i).getSubjects());
            for(int k=0;k<timetables.get(i).getSubjects().size();k++){
                    try {
                        if(timetables.get(i).getSubjects().get(Day.Montag).get(k).getSubject().getName().equals(subject.getName()))
                            return true;
                        else if(timetables.get(i).getSubjects().get(Day.Dienstag).get(k).getSubject().getName().equals(subject.getName()))
                            return true;
                        else if(timetables.get(i).getSubjects().get(Day.Mittwoch).get(k).getSubject().getName().equals(subject.getName()))
                            return true;
                        else if(timetables.get(i).getSubjects().get(Day.Donnerstag).get(k).getSubject().getName().equals(subject.getName()))
                            return true;
                        else if(timetables.get(i).getSubjects().get(Day.Freitag).get(k).getSubject().getName().equals(subject.getName()))
                            return true;

                    }catch (Exception e){

                    }
            }

        }
        return false;
    }

    public Optional<SchoolClass> findSchoolClassByID(int schoolClassID){
        return schoolClasses.stream().filter(schoolClass -> schoolClass.getId() == schoolClassID).findFirst();
    }

    public void editTeacher(int id, String firstname, String lastname, String abbrevation, List<Subjectobject> subjects){
        getTeachers().stream().filter(teacher -> teacher.getId() == id).findFirst().get().setFirstname(firstname);
        getTeachers().stream().filter(teacher -> teacher.getId() == id).findFirst().get().setSurname(lastname);
        getTeachers().stream().filter(teacher -> teacher.getId() == id).findFirst().get().setAbbreviation(abbrevation);
        getTeachers().stream().filter(teacher -> teacher.getId() == id).findFirst().get().setSubjects(subjects);

        getTeacherSubjects().stream().filter(teacherSubject -> teacherSubject.getTeacherId() == id).collect(Collectors.toList());
        getTeacherSubjects().removeAll(getTeacherSubjects().stream().filter(teacherSubject -> teacherSubject.getTeacherId() == id).collect(Collectors.toList()));
        for(int i=0;i<subjects.size();i++){
            getTeacherSubjects().add(new TeacherSubject(id, subjects.get(i)));
        }
    }
}
