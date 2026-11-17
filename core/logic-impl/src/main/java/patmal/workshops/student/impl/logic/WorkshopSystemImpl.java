package patmal.workshops.student.impl.logic;

import patmal.workshops.api.logic.WorkshopSystem;
import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Team;
import patmal.workshops.api.model.Workshop;
import patmal.workshops.student.impl.model.InstructorImpl;
import patmal.workshops.student.impl.model.StudentImpl;
import patmal.workshops.student.impl.model.TeamImpl;
import patmal.workshops.student.impl.model.WorkshopImpl;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;

public class WorkshopSystemImpl implements WorkshopSystem {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*@"
                    + "(?=.{1,253}$)(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+"
                    + "[A-Za-z]{2,63}$");

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
        requireEmail(email, "Instructor email");
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
        requireEmail(email, "Student email");
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

    @Override
    public void exportInstructorsDataToJson(File file) {
        List<Map<String, Object>> records = new ArrayList<>();
        for (Instructor instructor : instructors.values()) {
            List<Map<String, Object>> instructorWorkshops = new ArrayList<>();
            for (Workshop workshop : instructor.getWorkshops()) {
                instructorWorkshops.add(workshopForInstructorJson(workshop));
            }
            Map<String, Object> record = new LinkedHashMap<>();
            record.put("id", instructor.getId());
            record.put("name", instructor.getName());
            record.put("email", instructor.getEmail());
            record.put("workshops", instructorWorkshops);
            records.add(record);
        }
        writeJson(file, records);
    }

    @Override
    public void exportStudentsDataToJson(File file) {
        List<Map<String, Object>> records = new ArrayList<>();
        for (Student student : students.values()) {
            Team team = findTeamForStudent(student);
            Map<String, Object> record = studentJson(student);
            record.put("team", team == null ? null : teamSummaryJson(team));
            records.add(record);
        }
        writeJson(file, records);
    }

    @Override
    public void exportTeamsDataToJson(File file) {
        List<Map<String, Object>> records = new ArrayList<>();
        for (Team team : teams.values()) {
            records.add(teamJson(team));
        }
        writeJson(file, records);
    }

    private Map<String, Object> workshopForInstructorJson(Workshop workshop) {
        List<Map<String, Object>> workshopTeams = new ArrayList<>();
        for (Team team : teams.values()) {
            if (team.getWorkshop() == workshop) {
                Map<String, Object> teamRecord = new LinkedHashMap<>();
                teamRecord.put("id", team.getId());
                teamRecord.put("name", team.getName());
                List<Map<String, Object>> teamStudents = new ArrayList<>();
                for (Student student : team.getStudents()) {
                    teamStudents.add(studentJson(student));
                }
                teamRecord.put("students", teamStudents);
                workshopTeams.add(teamRecord);
            }
        }

        Map<String, Object> record = new LinkedHashMap<>();
        record.put("id", workshop.getId());
        record.put("name", workshop.getName());
        record.put("description", workshop.getDescription());
        record.put("teams", workshopTeams);
        return record;
    }

    private Map<String, Object> teamSummaryJson(Team team) {
        List<String> studentIds = new ArrayList<>();
        for (Student student : team.getStudents()) {
            studentIds.add(student.getId());
        }

        Map<String, Object> record = new LinkedHashMap<>();
        record.put("id", team.getId());
        record.put("name", team.getName());
        record.put("studentIds", studentIds);
        record.put("workshop", workshopJson(team.getWorkshop()));
        return record;
    }

    private Map<String, Object> teamJson(Team team) {
        List<Map<String, Object>> teamStudents = new ArrayList<>();
        for (Student student : team.getStudents()) {
            teamStudents.add(studentJson(student));
        }

        Map<String, Object> record = new LinkedHashMap<>();
        record.put("id", team.getId());
        record.put("name", team.getName());
        record.put("students", teamStudents);
        record.put("workshop", workshopJson(team.getWorkshop()));
        return record;
    }

    private static Map<String, Object> workshopJson(Workshop workshop) {
        Map<String, Object> record = new LinkedHashMap<>();
        if (workshop == null) {
            return record;
        }
        record.put("id", workshop.getId());
        record.put("name", workshop.getName());
        record.put("description", workshop.getDescription());
        Instructor instructor = workshop.getInstructor();
        record.put("instructor", instructor == null ? null : instructorJson(instructor));
        return record;
    }

    private static Map<String, Object> instructorJson(Instructor instructor) {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("id", instructor.getId());
        record.put("name", instructor.getName());
        record.put("email", instructor.getEmail());
        return record;
    }

    private static Map<String, Object> studentJson(Student student) {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("id", student.getId());
        record.put("name", student.getName());
        record.put("email", student.getEmail());
        return record;
    }

    private Team findTeamForStudent(Student student) {
        Integer teamId = studentTeamIds.get(student.getId());
        return teamId == null ? null : teams.get(teamId);
    }

    private static void writeJson(File file, Object value) {
        try {
            Files.writeString(file.toPath(), toJson(value), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to export JSON to " + file, e);
        }
    }

    private static String toJson(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            return jsonString((String) value);
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Map<?, ?>) {
            List<String> fields = new ArrayList<>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                if (!(entry.getKey() instanceof String)) {
                    throw new IllegalArgumentException("JSON object keys must be strings");
                }
                fields.add(jsonString((String) entry.getKey()) + ":" + toJson(entry.getValue()));
            }
            return "{" + String.join(",", fields) + "}";
        }
        if (value instanceof Iterable<?>) {
            List<String> elements = new ArrayList<>();
            for (Object element : (Iterable<?>) value) {
                elements.add(toJson(element));
            }
            return "[" + String.join(",", elements) + "]";
        }
        throw new IllegalArgumentException("Unsupported JSON value type: "
                + value.getClass().getName());
    }

    private static String jsonString(String text) {
        StringBuilder escaped = new StringBuilder("\"");
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            switch (character) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
            }
        }
        return escaped.append('"').toString();
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

    private static void requireEmail(String email, String label) {
        if (email == null || email.length() > 254 || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException(label + " must be a valid email address");
        }
    }

    private static <K, V> List<V> snapshot(Map<K, V> values) {
        return Collections.unmodifiableList(new ArrayList<>(values.values()));
    }
}
