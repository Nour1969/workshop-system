package patmal.workshops.logic;

import patmal.workshops.api.logic.WorkshopSystem;
import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Team;
import patmal.workshops.api.model.Workshop;
import patmal.workshops.model.InstructorImpl;
import patmal.workshops.model.StudentImpl;
import patmal.workshops.model.TeamImpl;
import patmal.workshops.model.WorkshopImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WorkshopSystemImpl implements WorkshopSystem {
    private final Map<String, Instructor> instructors = new LinkedHashMap<>();
    private final Map<String, Student> students = new LinkedHashMap<>();
    private final Map<Integer, Workshop> workshops = new LinkedHashMap<>();
    private final Map<Integer, Team> teams = new LinkedHashMap<>();
    private final Map<String, Integer> studentTeamIds = new LinkedHashMap<>();
    private int nextWorkshopId = 1;
    private int nextTeamId = 1;

    public WorkshopSystemImpl() {
    }

    @Override
    public Instructor createInstructor(String id, String name, String email) {
        requireText(id, "Instructor ID");
        requireText(name, "Instructor name");
        requireText(email, "Instructor email");
        if (instructors.containsKey(id)) {
            throw new IllegalArgumentException("An instructor with ID " + id + " already exists");
        }

        Instructor instructor = new InstructorImpl(id, name, email, Collections.emptyList());
        instructors.put(id, instructor);
        return instructor;
    }

    @Override
    public Student createStudent(String id, String name, String email) {
        requireText(id, "Student ID");
        requireText(name, "Student name");
        requireText(email, "Student email");
        if (students.containsKey(id)) {
            throw new IllegalArgumentException("A student with ID " + id + " already exists");
        }

        Student student = new StudentImpl(id, name, email, null, null, Collections.emptyList());
        students.put(id, student);
        return student;
    }

    @Override
    public Workshop createWorkshop(String name, String description) {
        requireText(name, "Workshop name");
        requireText(description, "Workshop description");

        Workshop workshop = new WorkshopImpl(nextWorkshopId, name, description, null);
        workshops.put(nextWorkshopId, workshop);
        nextWorkshopId++;
        return workshop;
    }

    @Override
    public void associateWorkshopWithInstructor(Instructor instructor, Workshop workshop) {
        Instructor registeredInstructor = requireRegisteredInstructor(instructor);
        Workshop registeredWorkshop = requireRegisteredWorkshop(workshop);
        Instructor currentInstructor = registeredWorkshop.getInstructor();
        if (currentInstructor != null && currentInstructor != registeredInstructor) {
            throw new IllegalArgumentException("Workshop " + workshop.getId()
                    + " is already associated with another instructor");
        }

        ((WorkshopImpl) registeredWorkshop).setInstructor(registeredInstructor);
        for (Team team : teams.values()) {
            if (team.getWorkshop() == registeredWorkshop) {
                for (Student student : team.getStudents()) {
                    if (student instanceof StudentImpl) {
                        ((StudentImpl) student).setInstructor(registeredInstructor);
                    }
                }
            }
        }
    }

    @Override
    public Team createTeam(String name, Workshop workshop, List<String> studentIds) {
        requireText(name, "Team name");
        Workshop registeredWorkshop = requireRegisteredWorkshop(workshop);
        if (studentIds == null || studentIds.isEmpty()) {
            throw new IllegalArgumentException("A team must contain at least one student");
        }

        List<Student> teamStudents = new ArrayList<>();
        List<String> uniqueStudentIds = new ArrayList<>();
        for (String studentId : studentIds) {
            requireText(studentId, "Student ID");
            if (uniqueStudentIds.contains(studentId)) {
                throw new IllegalArgumentException("Student " + studentId + " appears more than once");
            }
            Student student = students.get(studentId);
            if (student == null) {
                throw new IllegalArgumentException("No student exists with ID " + studentId);
            }
            if (studentTeamIds.containsKey(studentId)) {
                throw new IllegalArgumentException("Student " + studentId + " already belongs to a team");
            }
            uniqueStudentIds.add(studentId);
            teamStudents.add(student);
        }

        TeamImpl team = new TeamImpl(nextTeamId, name, teamStudents, registeredWorkshop);
        teams.put(nextTeamId, team);
        for (int i = 0; i < teamStudents.size(); i++) {
            Student student = teamStudents.get(i);
            studentTeamIds.put(uniqueStudentIds.get(i), nextTeamId);
            if (student instanceof StudentImpl) {
                StudentImpl studentImpl = (StudentImpl) student;
                studentImpl.setTeamName(name);
                studentImpl.setInstructor(registeredWorkshop.getInstructor());
                List<Student> teamMates = new ArrayList<>(teamStudents);
                teamMates.remove(student);
                studentImpl.setTeamMates(teamMates);
            }
        }
        nextTeamId++;
        return team;
    }

    @Override
    public List<Team> getTeams() {
        return snapshot(teams);
    }

    @Override
    public Team getTeam(Integer id) {
        return teams.get(id);
    }

    @Override
    public List<Student> getStudents() {
        return snapshot(students);
    }

    @Override
    public Student getStudent(String id) {
        return students.get(id);
    }

    @Override
    public List<Workshop> getWorkshops() {
        return snapshot(workshops);
    }

    @Override
    public Workshop getWorkshop(Integer id) {
        return workshops.get(id);
    }

    @Override
    public List<Instructor> getInstructors() {
        return snapshot(instructors);
    }

    @Override
    public Instructor getInstructor(String id) {
        return instructors.get(id);
    }

    @Override
    public List<Student> getFreeStudents() {
        List<Student> freeStudents = new ArrayList<>();
        for (Map.Entry<String, Student> entry : students.entrySet()) {
            if (!studentTeamIds.containsKey(entry.getKey())) {
                freeStudents.add(entry.getValue());
            }
        }
        return Collections.unmodifiableList(freeStudents);
    }

    @Override
    public List<Student> getStudentsOfInstructor(String instructorId) {
        Instructor instructor = instructors.get(instructorId);
        if (instructor == null) {
            return Collections.emptyList();
        }

        List<Student> instructorStudents = new ArrayList<>();
        for (Student student : students.values()) {
            if (student.getInstructor() == instructor) {
                instructorStudents.add(student);
            }
        }
        return Collections.unmodifiableList(instructorStudents);
    }

    @Override
    public List<Student> getStudentsOfWorkshop(Integer workshopId) {
        Workshop workshop = workshops.get(workshopId);
        if (workshop == null) {
            return Collections.emptyList();
        }

        List<Student> workshopStudents = new ArrayList<>();
        for (Team team : teams.values()) {
            if (team.getWorkshop() == workshop) {
                workshopStudents.addAll(team.getStudents());
            }
        }
        return Collections.unmodifiableList(workshopStudents);
    }

    private Instructor requireRegisteredInstructor(Instructor instructor) {
        if (instructor == null || instructors.get(instructor.getId()) != instructor) {
            throw new IllegalArgumentException("Instructor is not registered with this system");
        }
        return instructor;
    }

    private Workshop requireRegisteredWorkshop(Workshop workshop) {
        if (workshop == null || workshops.get(workshop.getId()) != workshop) {
            throw new IllegalArgumentException("Workshop is not registered with this system");
        }
        return workshop;
    }

    private static void requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " must not be null or blank");
        }
    }

    private static <K, V> List<V> snapshot(Map<K, V> values) {
        return Collections.unmodifiableList(new ArrayList<>(values.values()));
    }
}
