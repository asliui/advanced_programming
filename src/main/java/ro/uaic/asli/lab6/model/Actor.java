package ro.uaic.asli.lab6.model;

import java.util.Objects;

public final class Actor {
    private int id;
    private String name;

    public Actor() {
    }

    public Actor(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Actor(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Actor{id=" + id + ", name='" + name + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Actor actor)) return false;
        return id != 0 ? id == actor.id : Objects.equals(name, actor.name);
    }

    @Override
    public int hashCode() {
        return id != 0 ? Integer.hashCode(id) : Objects.hashCode(name);
    }
}

