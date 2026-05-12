package ro.uaic.asli.lab10.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
        name = "players",
        uniqueConstraints = @UniqueConstraint(name = "uk_players_name", columnNames = "name")
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class PlayerEntity extends AuditableEntity {

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @OneToMany(mappedBy = "player")
    private Set<ResultEntity> results = new LinkedHashSet<>();

    protected PlayerEntity() {
    }

    public PlayerEntity(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<ResultEntity> getResults() {
        return results;
    }

    @Override
    public String toString() {
        return "PlayerEntity{id=" + getId() + ", name='" + name + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlayerEntity that)) {
            return false;
        }
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() == null ? System.identityHashCode(this) : getId().hashCode();
    }
}
