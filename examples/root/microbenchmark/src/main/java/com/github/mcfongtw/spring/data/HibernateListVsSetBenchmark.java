package com.github.mcfongtw.spring.data;

import com.github.mcfongtw.spring.boot.AbstractSpringBootBenchmark;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.*;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static com.github.mcfongtw.spring.boot.AbstractSpringBootBenchmark.*;

public class HibernateListVsSetBenchmark {

    @State(Scope.Benchmark)
    public static class ExecutionPlan extends AbstractSpringBootBenchmark.AbstractSpringBootExecutionPlan {

        @Autowired
        private EmployerRepository employerRepository;

        @Setup(Level.Trial)
        @Override
        public void preTrialSetUp() throws Exception {
            super.preTrialSetUp();

            employerRepository = configurableApplicationContext.getBean(EmployerRepository.class);
        }

        @TearDown(Level.Trial)
        @Override
        public void preTrialTearDown() throws Exception {
            super.preTrialTearDown();
        }
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Measurement(iterations = DEFAULT_NUMBER_OF_ITERATIONS)
    public void measureOneToManyWithSet(ExecutionPlan executionPlan) {
        Employer employer = new Employer();
        employer.setName(RandomStringUtils.randomAlphabetic(10));

        List<Employee> employeeList = Lists.newArrayList();
        // prepare employeeList
        for(int i = 0; i < numberOfEntities; i++) {
            Employee employee = new Employee();
            employee.setName(RandomStringUtils.randomAlphabetic(10));
            employeeList.add(employee);
        }

        for(Employee employee: employeeList) {
            employer.addEmployeeToSet(employee);
        }

        executionPlan.employerRepository.save(employer);

        assert executionPlan.employerRepository.findById(employer.getId()).get().getSetOfEmployeees().size() == numberOfEntities;


        for(Employee employee: employeeList) {
            employer.removeEmployeeFromSet(employee);
        }

        executionPlan.employerRepository.save(employer);

        assert executionPlan.employerRepository.findById(employer.getId()).get().getSetOfEmployeees().size() == 0;

    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Measurement(iterations = DEFAULT_NUMBER_OF_ITERATIONS)
    public void measureOneToManyWithList(ExecutionPlan executionPlan) {
        Employer employer = new Employer();
        employer.setName(RandomStringUtils.randomAlphabetic(10));

        List<Employee> employeeList = Lists.newArrayList();
        // prepare employeeList
        for(int i = 0; i < numberOfEntities; i++) {
            Employee employee = new Employee();
            employee.setName(RandomStringUtils.randomAlphabetic(10));
            employeeList.add(employee);
        }

        for(Employee employee: employeeList) {
            employer.addEmployeeToList(employee);
        }

        executionPlan.employerRepository.save(employer);

        assert executionPlan.employerRepository.findById(employer.getId()).get().getListOfEmployeees().size() == numberOfEntities;


        for(Employee employee: employeeList) {
            employer.removeEmployeeFromList(employee);
        }

        executionPlan.employerRepository.save(employer);

        assert executionPlan.employerRepository.findById(employer.getId()).get().getListOfEmployeees().size() == 0;
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Measurement(iterations = DEFAULT_NUMBER_OF_ITERATIONS)
    public void measureSortWithList(ExecutionPlan executionPlan) {
        Employer employer = new Employer();
        employer.setName(RandomStringUtils.randomAlphabetic(10));

        List<Employee> employeeList = Lists.newArrayList();
        // prepare employeeList
        for(int i = 0; i < numberOfEntities; i++) {
            Employee employee = new Employee();
            employee.setName(RandomStringUtils.randomAlphabetic(10));
            employeeList.add(employee);
        }

        for(Employee employee: employeeList) {
            employer.addEmployeeToList(employee);
        }

        executionPlan.employerRepository.save(employer);

        assert executionPlan.employerRepository.findById(employer.getId()).get().getListOfEmployeees().size() == numberOfEntities;

        employer.getListOfEmployeees().sort(Comparator.comparing(Employee::getName));
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Measurement(iterations = DEFAULT_NUMBER_OF_ITERATIONS)
    public void measureSortWithTreeSet(ExecutionPlan executionPlan) {
        Employer employer = new Employer();
        employer.setName(RandomStringUtils.randomAlphabetic(10));

        List<Employee> employeeList = Lists.newArrayList();
        // prepare employeeList
        for(int i = 0; i < numberOfEntities; i++) {
            Employee employee = new Employee();
            employee.setName(RandomStringUtils.randomAlphabetic(10));
            employeeList.add(employee);
        }

        for(Employee employee: employeeList) {
            employer.addEmployeeToSet(employee);
        }

        executionPlan.employerRepository.save(employer);

        assert executionPlan.employerRepository.findById(employer.getId()).get().getListOfEmployeees().size() == numberOfEntities;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(HibernateListVsSetBenchmark.class.getSimpleName())
                .warmupIterations(numberOfWarmUpIterations)
                .forks(1)
                .resultFormat(ResultFormatType.JSON)
                .result("HibernateListVsSetBenchmark-result.json")
                .build();

        new Runner(opt).run();
    }
}


@Entity(name = "Employee")
@Table(name = "employee")
@Data
class Employee {

    @EqualsAndHashCode.Exclude
    @Id
    @GenericGenerator(name = "idGenerator", strategy = "uuid")
    @GeneratedValue(generator = "idGenerator")
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    //Bidirectional @OneToMany
    /*
     * fetch = FetchType.LAZY
     * for better performance
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Employer employer;
}


@Entity(name = "Employer")
@Table(name = "employer")
@Data
class Employer {

    @EqualsAndHashCode.Exclude
    @Id
    @GenericGenerator(name = "idGenerator", strategy = "uuid")
    @GeneratedValue(generator = "idGenerator")
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    // Bidirectional @OneToMany
    @OneToMany(
            mappedBy = "employer",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            /*
             * fetch = FetchType.LAZY
             * for better performance
             */
            fetch = FetchType.EAGER
    )
    private Set<Employee> setOfEmployeees = Sets.newTreeSet(Comparator.comparing(Employee::getName));

    public void addEmployeeToSet(Employee employee) {
        setOfEmployeees.add(employee);
        employee.setEmployer(this);
    }

    public void removeEmployeeFromSet(Employee employee) {
        setOfEmployeees.remove(employee);
        employee.setEmployer(null);
    }

    // Bidirectional @OneToMany
    @OneToMany(
            mappedBy = "employer",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            /*
             * fetch = FetchType.LAZY
             * for better performance
             */
            fetch = FetchType.EAGER
    )
    private List<Employee> listOfEmployeees = Lists.newArrayList();

    public void addEmployeeToList(Employee employee) {
        listOfEmployeees.add(employee);
        employee.setEmployer(this);
    }

    public void removeEmployeeFromList(Employee employee) {
        listOfEmployeees.remove(employee);
        employee.setEmployer(null);
    }
}

interface EmployeeRepository extends CrudRepository<Employee, String> {

}

interface EmployerRepository extends CrudRepository<Employer, String> {

}