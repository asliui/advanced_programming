package ro.uaic.asli.lab3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Demonstrates an object-oriented model with persons and companies
 * that share a common profile and can be ordered by name.
 */
public class Lab3Base {

    public interface Profile {
        String getName();

        String getEmail();
    }

    public static class Person implements Profile, Comparable<Profile> {
        private final String firstName;
        private final String lastName;
        private final String email;

        public Person(String firstName, String lastName, String email) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
        }

        @Override
        public String getName() {
            return firstName + " " + lastName;
        }

        @Override
        public String getEmail() {
            return email;
        }

        @Override
        public int compareTo(Profile other) {
            return String.CASE_INSENSITIVE_ORDER.compare(this.getName(), other.getName());
        }

        @Override
        public String toString() {
            return "Person{name='" + getName() + "', email='" + email + "'}";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Person)) return false;
            Person person = (Person) o;
            return Objects.equals(firstName, person.firstName)
                    && Objects.equals(lastName, person.lastName)
                    && Objects.equals(email, person.email);
        }

        @Override
        public int hashCode() {
            return Objects.hash(firstName, lastName, email);
        }
    }

    public static class Company implements Profile, Comparable<Profile> {
        private final String name;
        private final String email;

        public Company(String name, String email) {
            this.name = name;
            this.email = email;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getEmail() {
            return email;
        }

        @Override
        public int compareTo(Profile other) {
            return String.CASE_INSENSITIVE_ORDER.compare(this.getName(), other.getName());
        }

        @Override
        public String toString() {
            return "Company{name='" + name + "', email='" + email + "'}";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Company)) return false;
            Company company = (Company) o;
            return Objects.equals(name, company.name)
                    && Objects.equals(email, company.email);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, email);
        }
    }

    public static void main(String[] args) {
        List<Profile> profiles = new ArrayList<>();

        profiles.add(new Person("Alice", "Smith", "alice@example.com"));
        profiles.add(new Company("Tech Corp", "contact@techcorp.com"));
        profiles.add(new Person("Bob", "Johnson", "bob@example.com"));
        profiles.add(new Company("Alpha Industries", "info@alpha.com"));

        System.out.println("Before sorting:");
        for (Profile profile : profiles) {
            System.out.println(profile);
        }

        Comparator<Profile> byName =
                (p1, p2) -> String.CASE_INSENSITIVE_ORDER.compare(p1.getName(), p2.getName());
        Collections.sort(profiles, byName);

        System.out.println("\nAfter sorting by name:");
        for (Profile profile : profiles) {
            System.out.println(profile);
        }
    }
}
