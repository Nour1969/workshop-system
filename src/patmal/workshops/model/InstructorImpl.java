package patmal.workshops.model;

import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Workshop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InstructorImpl implements Instructor {
    private String id;
    private String name;
    private String email;
    private final List<Workshop> workshops = new ArrayList<>();

    public InstructorImpl() {
    }

    public InstructorImpl(String id, String name, String email, List<Workshop> workshops) {
        this.id = id;
        this.name = name;
        this.email = email;
        setWorkshops(workshops);
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
    public List<Workshop> getWorkshops() {
        return Collections.unmodifiableList(workshops);
    }

    public void setWorkshops(List<Workshop> workshops) {
        List<Workshop> newWorkshops = new ArrayList<>();
        if (workshops != null) {
            for (Workshop workshop : workshops) {
                if (workshop != null && !newWorkshops.contains(workshop)) {
                    newWorkshops.add(workshop);
                }
            }
        }

        for (Workshop workshop : new ArrayList<>(this.workshops)) {
            if (!newWorkshops.contains(workshop)) {
                removeWorkshop(workshop);
            }
        }
        for (Workshop workshop : newWorkshops) {
            addWorkshop(workshop);
        }
    }

    public void addWorkshop(Workshop workshop) {
        if (workshop == null) {
            return;
        }
        if (workshop instanceof WorkshopImpl) {
            ((WorkshopImpl) workshop).setInstructor(this);
        } else {
            addWorkshopReference(workshop);
        }
    }

    public void removeWorkshop(Workshop workshop) {
        if (workshop == null) {
            return;
        }
        if (workshop instanceof WorkshopImpl
                && ((WorkshopImpl) workshop).getInstructor() == this) {
            ((WorkshopImpl) workshop).setInstructor(null);
        } else {
            removeWorkshopReference(workshop);
        }
    }

    void addWorkshopReference(Workshop workshop) {
        if (!workshops.contains(workshop)) {
            workshops.add(workshop);
        }
    }

    void removeWorkshopReference(Workshop workshop) {
        workshops.remove(workshop);
    }
}
