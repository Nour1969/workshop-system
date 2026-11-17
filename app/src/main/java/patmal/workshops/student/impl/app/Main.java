package patmal.workshops.student.impl.app;

import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Team;
import patmal.workshops.api.model.Workshop;
import patmal.workshops.student.impl.logic.WorkshopSystemImpl;

import java.util.Collections;

public class Main {
    public static void main(String[] args) {
        WorkshopSystemImpl system = new WorkshopSystemImpl();

        Instructor instructor = system.createInstructor(
                "I-001", "Alex Morgan", "alex.morgan@example.com");
        Student student = system.createStudent(
                "S-001", "Jamie Lee", "jamie.lee@example.com");
        Workshop workshop = system.createWorkshop(
                "Java Basics", "An introduction to Java programming.");

        system.associateWorkshopWithInstructor(instructor, workshop);
        Team team = system.createTeam("Java Learners", workshop,
                Collections.singletonList(student.getId()));

        System.out.println("Instructor: " + instructor.getName()
                + " (" + instructor.getEmail() + ")");
        System.out.println("Student: " + student.getName()
                + " (" + student.getEmail() + ")");
        System.out.println("Workshop: " + workshop.getName()
                + " - " + workshop.getDescription());
        System.out.println("Workshop instructor: "
                + workshop.getInstructor().getName());
        System.out.println("Student team: " + team.getName()
                + " (workshop: " + team.getWorkshop().getName() + ")");
    }
}
