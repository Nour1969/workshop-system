package patmal.workshops.model;

import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Team;
import patmal.workshops.api.model.Workshop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TeamImpl implements Team {
    private Integer id;
    private String name;
    private final List<Student> students = new ArrayList<>();
    private Workshop workshop;

    public TeamImpl() {
    }

    public TeamImpl(Integer id, String name, List<Student> students, Workshop workshop) {
        this.id = id;
        this.name = name;
        this.workshop = workshop;
        setStudents(students);
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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
    public List<Student> getStudents() {
        return Collections.unmodifiableList(students);
    }

    public void setStudents(List<Student> students) {
        this.students.clear();
        if (students != null) {
            for (Student student : students) {
                addStudent(student);
            }
        }
    }

    public void addStudent(Student student) {
        if (student != null && !students.contains(student)) {
            students.add(student);
        }
    }

    public void removeStudent(Student student) {
        students.remove(student);
    }

    @Override
    public Workshop getWorkshop() {
        return workshop;
    }

    public void setWorkshop(Workshop workshop) {
        this.workshop = workshop;
    }
}
