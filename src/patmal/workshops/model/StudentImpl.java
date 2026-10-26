package patmal.workshops.model;

import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Student;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StudentImpl implements Student {
    private String id;
    private String name;
    private String email;
    private String teamName;
    private Instructor instructor;
    private final List<Student> teamMates = new ArrayList<>();

    public StudentImpl() {
    }

    public StudentImpl(String id, String name, String email, String teamName,
                       Instructor instructor, List<Student> teamMates) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.teamName = teamName;
        this.instructor = instructor;
        setTeamMates(teamMates);
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    @Override
    public Instructor getInstructor() {
        return instructor;
    }

    public void setInstructor(Instructor instructor) {
        this.instructor = instructor;
    }

    @Override
    public List<Student> getTeamMates() {
        return Collections.unmodifiableList(teamMates);
    }

    public void setTeamMates(List<Student> teamMates) {
        List<Student> newTeamMates = new ArrayList<>();
        if (teamMates != null) {
            for (Student teamMate : teamMates) {
                if (teamMate != null && teamMate != this && !newTeamMates.contains(teamMate)) {
                    newTeamMates.add(teamMate);
                }
            }
        }

        for (Student oldTeamMate : new ArrayList<>(this.teamMates)) {
            if (!newTeamMates.contains(oldTeamMate)) {
                removeTeamMateReference(oldTeamMate);
            }
        }

        this.teamMates.clear();
        this.teamMates.addAll(newTeamMates);
        for (Student teamMate : this.teamMates) {
            addTeamMateReference(teamMate);
        }
    }

    private void addTeamMateReference(Student teamMate) {
        if (teamMate instanceof StudentImpl) {
            StudentImpl student = (StudentImpl) teamMate;
            if (!student.teamMates.contains(this)) {
                student.teamMates.add(this);
            }
        }
    }

    private void removeTeamMateReference(Student teamMate) {
        if (teamMate instanceof StudentImpl) {
            ((StudentImpl) teamMate).teamMates.remove(this);
        }
    }
}
