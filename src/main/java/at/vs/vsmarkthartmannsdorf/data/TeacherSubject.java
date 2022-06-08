package at.vs.vsmarkthartmannsdorf.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherSubject {
    private int teacherId;
    private Subjectobject subject;
}
