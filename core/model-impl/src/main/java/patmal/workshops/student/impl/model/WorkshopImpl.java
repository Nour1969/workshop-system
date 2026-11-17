package patmal.workshops.student.impl.model;

import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Workshop;

public class WorkshopImpl implements Workshop {
    private Integer id;
    private String name;
    private String description;
    private Instructor instructor;

    public WorkshopImpl() {
    }

    public WorkshopImpl(Integer id, String name, String description, Instructor instructor) {
        this.id = id;
        this.name = name;
        this.description = description;
        setInstructor(instructor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkshopImpl workshop = (WorkshopImpl) o;
        return id != null && id.equals(workshop.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
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
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Instructor getInstructor() {
        return instructor;
    }

    public void setInstructor(Instructor instructor) {
        if (this.instructor == instructor) {
            return;
        }

        Instructor previousInstructor = this.instructor;
        this.instructor = instructor;
        if (previousInstructor instanceof InstructorImpl) {
            ((InstructorImpl) previousInstructor).removeWorkshopReference(this);
        }
        if (instructor instanceof InstructorImpl) {
            ((InstructorImpl) instructor).addWorkshopReference(this);
        }
    }
}
