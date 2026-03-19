package ro.uaic.asli.lab3;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Lab3Homework {

    public interface Profile {
        String getName();
    }

    public enum RelationType {
        FRIEND,
        COLLEAGUE,
        MANAGER,
        EMPLOYER,
        CLIENT,
        PARTNER
    }

    public static abstract class Person implements Profile {
        private final String name;
        private final LocalDate birthDate;
        private final Map<Profile, RelationType> relationships = new HashMap<>();

        protected Person(String name, LocalDate birthDate) {
            this.name = name;
            this.birthDate = birthDate;
        }

        @Override
        public String getName() {
            return name;
        }

        public LocalDate getBirthDate() {
            return birthDate;
        }

        public Map<Profile, RelationType> getRelationships() {
            return relationships;
        }

        public void addRelationship(Profile target, RelationType type) {
            if (target == null || target == this) {
                return;
            }
            relationships.put(target, type);
        }

        public int importance() {
            return relationships.size();
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{name='" + name + "', birthDate=" + birthDate + "}";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Person)) return false;
            Person person = (Person) o;
            return Objects.equals(name, person.name) && Objects.equals(birthDate, person.birthDate);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, birthDate);
        }
    }

    public static final class Programmer extends Person {
        private final String mainLanguage;

        public Programmer(String name, LocalDate birthDate, String mainLanguage) {
            super(name, birthDate);
            this.mainLanguage = mainLanguage;
        }

        public String getMainLanguage() {
            return mainLanguage;
        }

        @Override
        public String toString() {
            return super.toString().replace("}", ", mainLanguage='" + mainLanguage + "'}");
        }
    }

    public static final class Designer extends Person {
        private final String favoriteTool;

        public Designer(String name, LocalDate birthDate, String favoriteTool) {
            super(name, birthDate);
            this.favoriteTool = favoriteTool;
        }

        public String getFavoriteTool() {
            return favoriteTool;
        }

        @Override
        public String toString() {
            return super.toString().replace("}", ", favoriteTool='" + favoriteTool + "'}");
        }
    }

    public static final class Company implements Profile {
        private final String name;
        private final String industry;
        private final Map<Profile, RelationType> relationships = new HashMap<>();
        private final Map<Department, Integer> employeesPerDepartment = new EnumMap<>(Department.class);

        public enum Department {
            ENGINEERING,
            DESIGN,
            SALES,
            HR
        }

        public Company(String name, String industry) {
            this.name = name;
            this.industry = industry;
        }

        @Override
        public String getName() {
            return name;
        }

        public String getIndustry() {
            return industry;
        }

        public Map<Profile, RelationType> getRelationships() {
            return relationships;
        }

        public void addRelationship(Profile target, RelationType type) {
            if (target == null || target == this) {
                return;
            }
            relationships.put(target, type);
        }

        public void setEmployeesInDepartment(Department department, int count) {
            employeesPerDepartment.put(department, count);
        }

        public int importance() {
            return relationships.size();
        }

        @Override
        public String toString() {
            return "Company{name='" + name + "', industry='" + industry + "', relationships=" + relationships.size() + "}";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Company)) return false;
            Company company = (Company) o;
            return Objects.equals(name, company.name) && Objects.equals(industry, company.industry);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, industry);
        }
    }

    public static final class SocialNetwork {
        private final List<Profile> profiles = new ArrayList<>();

        public void addProfile(Profile profile) {
            if (!profiles.contains(profile)) {
                profiles.add(profile);
            }
        }

        public int computeImportance(Profile profile) {
            if (profile instanceof Person person) {
                return person.importance();
            }
            if (profile instanceof Company company) {
                return company.importance();
            }
            return 0;
        }

        public List<Profile> getProfilesOrderedByImportance() {
            List<Profile> result = new ArrayList<>(profiles);
            result.sort((o1, o2) -> {
                int imp1 = computeImportance(o1);
                int imp2 = computeImportance(o2);
                int cmp = Integer.compare(imp2, imp1);
                if (cmp != 0) {
                    return cmp;
                }
                return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
            });
            return result;
        }

        public void printNetwork() {
            List<Profile> ordered = getProfilesOrderedByImportance();
            for (Profile profile : ordered) {
                int importance = computeImportance(profile);
                System.out.println(profile + " | importance=" + importance);
            }
        }
    }

    public static void main(String[] args) {
        Programmer alice = new Programmer("Alice", LocalDate.of(1995, 3, 10), "Java");
        Programmer bob = new Programmer("Bob", LocalDate.of(1990, 7, 21), "Python");
        Designer clara = new Designer("Clara", LocalDate.of(1992, 11, 5), "Figma");

        Company techCorp = new Company("TechCorp", "Software");
        Company designStudio = new Company("DesignStudio", "Creative");

        techCorp.setEmployeesInDepartment(Company.Department.ENGINEERING, 50);
        techCorp.setEmployeesInDepartment(Company.Department.DESIGN, 10);
        designStudio.setEmployeesInDepartment(Company.Department.DESIGN, 25);

        alice.addRelationship(bob, RelationType.FRIEND);
        alice.addRelationship(techCorp, RelationType.EMPLOYER);
        alice.addRelationship(clara, RelationType.COLLEAGUE);

        bob.addRelationship(alice, RelationType.FRIEND);
        bob.addRelationship(techCorp, RelationType.EMPLOYER);

        clara.addRelationship(designStudio, RelationType.EMPLOYER);
        clara.addRelationship(techCorp, RelationType.CLIENT);

        techCorp.addRelationship(alice, RelationType.MANAGER);
        techCorp.addRelationship(bob, RelationType.MANAGER);
        techCorp.addRelationship(designStudio, RelationType.PARTNER);

        designStudio.addRelationship(clara, RelationType.MANAGER);
        designStudio.addRelationship(techCorp, RelationType.PARTNER);

        SocialNetwork network = new SocialNetwork();
        Collections.addAll(network.profiles, alice, bob, clara, techCorp, designStudio);

        network.printNetwork();
    }
}
