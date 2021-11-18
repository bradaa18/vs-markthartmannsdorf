package at.vs.vsmarkthartmannsdorf.data;

import java.util.List;
public class Teacher {
    private String firstname;
    private String surname;
    private String abbreviation;

    private List<Subject> subjects;

    public Teacher(String firstname, String surname, String abbreviation, List<Subject> subjects) {
        this.firstname = firstname;
        this.surname = surname;
        this.abbreviation = abbreviation;
        this.subjects = subjects;
    }

    @Override
    public String toString() {
        return String.format("%s %s", surname.toUpperCase(), firstname);
    }
}
