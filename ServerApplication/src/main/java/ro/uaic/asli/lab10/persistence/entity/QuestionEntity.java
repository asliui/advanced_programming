package ro.uaic.asli.lab10.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "questions",
        uniqueConstraints = @UniqueConstraint(name = "uk_questions_source_id", columnNames = "source_question_id")
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class QuestionEntity extends AuditableEntity {

    /**
     * Stable id from {@code questions.txt} ordering (Lab 10 {@link ro.uaic.asli.lab10.model.Question#getId()}).
     */
    @Column(name = "source_question_id", nullable = false)
    private int sourceQuestionId;

    @Column(name = "text_body", nullable = false, length = 4000)
    private String text;

    @Column(name = "option_a", nullable = false, length = 2000)
    private String optionA;

    @Column(name = "option_b", nullable = false, length = 2000)
    private String optionB;

    @Column(name = "option_c", nullable = false, length = 2000)
    private String optionC;

    @Column(name = "option_d", nullable = false, length = 2000)
    private String optionD;

    @Column(name = "correct_option", nullable = false, length = 1)
    private String correctOption;

    @ManyToMany(mappedBy = "questions")
    private Set<GameEntity> games = new HashSet<>();

    protected QuestionEntity() {
    }

    public QuestionEntity(
            int sourceQuestionId,
            String text,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
            String correctOption
    ) {
        this.sourceQuestionId = sourceQuestionId;
        this.text = text;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = correctOption;
    }

    public int getSourceQuestionId() {
        return sourceQuestionId;
    }

    public String getText() {
        return text;
    }

    public String getOptionA() {
        return optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public String getCorrectOption() {
        return correctOption;
    }

    public Set<GameEntity> getGames() {
        return games;
    }

    @Override
    public String toString() {
        return "QuestionEntity{id=" + getId() + ", sourceQuestionId=" + sourceQuestionId + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof QuestionEntity that)) {
            return false;
        }
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() == null ? System.identityHashCode(this) : getId().hashCode();
    }
}
