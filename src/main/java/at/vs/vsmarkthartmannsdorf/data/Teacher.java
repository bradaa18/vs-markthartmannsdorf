package at.vs.vsmarkthartmannsdorf.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Teacher {
    private String firstname;
    private String surname;
    private String abbreviation;
    private List<Subject> subjects;

    @Override
    public String toString() {
        return String.format("%s %s (%s)", surname.toUpperCase(), firstname, abbreviation.toUpperCase());
    }

    public String getFirstname() {
        return firstname;
    }

    public String getSurname() {
        return surname;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }
}
