package com.github.mcfongtw.spring.data;

import com.github.mcfongtw.BenchmarkBase;
import com.github.mcfongtw.spring.boot.AbstractSpringBootBenchmarkLifecycle;
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
import java.util.concurrent.TimeUnit;

import static com.github.mcfongtw.spring.boot.AbstractSpringBootBenchmarkLifecycle.numberOfEntities;

@BenchmarkMode({Mode.Throughput})
@OutputTimeUnit(TimeUnit.SECONDS)
@Measurement(iterations = 20)
@Warmup(iterations = 10)
@Fork(3)
@Threads(1)
public class HibernateListVsSetBenchmark extends BenchmarkBase {

    @State(Scope.Benchmark)
    public static class BenchmarkState extends AbstractSpringBootBenchmarkLifecycle {

        @Autowired
        private EmployerRepository employerRepository;

        @Override
        public void preTrialSetUp() throws Exception {
            super.preTrialSetUp();

            employerRepository = configurableApplicationContext.getBean(EmployerRepository.class);
        }

        @Setup(Level.Trial)
        @Override
        public void doTrialSetUp() throws Exception {
            super.doTrialSetUp();
        }

        @TearDown(Level.Trial)
        @Override
        public void doTrialTearDown() throws Exception {
            super.doTrialTearDown();
        }

        @Setup(Level.Iteration)
        @Override
        public void doIterationSetup() throws Exception {
            super.doIterationSetup();
        }

        @TearDown(Level.Iteration)
        @Override
        public void doIterationTearDown() throws Exception {
            super.doIterationTearDown();
        }
    }

    @Benchmark
    public void measureOneToManyWithSet(BenchmarkState benchmarkState) {
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

        benchmarkState.employerRepository.save(employer);

        assert benchmarkState.employerRepository.findById(employer.getId()).get().getSetOfEmployeees().size() == numberOfEntities;


        for(Employee employee: employeeList) {
            employer.removeEmployeeFromSet(employee);
        }

        benchmarkState.employerRepository.save(employer);

        assert benchmarkState.employerRepository.findById(employer.getId()).get().getSetOfEmployeees().size() == 0;

    }

    @Benchmark
    public void measureOneToManyWithList(BenchmarkState benchmarkState) {
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

        benchmarkState.employerRepository.save(employer);

        assert benchmarkState.employerRepository.findById(employer.getId()).get().getListOfEmployeees().size() == numberOfEntities;


        for(Employee employee: employeeList) {
            employer.removeEmployeeFromList(employee);
        }

        benchmarkState.employerRepository.save(employer);

        assert benchmarkState.employerRepository.findById(employer.getId()).get().getListOfEmployeees().size() == 0;
    }

    @Benchmark
    public void measureSortWithList(BenchmarkState benchmarkState) {
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

        benchmarkState.employerRepository.save(employer);

        assert benchmarkState.employerRepository.findById(employer.getId()).get().getListOfEmployeees().size() == numberOfEntities;

        employer.getListOfEmployeees().sort(Comparator.comparing(Employee::getName));
    }

    @Benchmark
    public void measureSortWithTreeSet(BenchmarkState benchmarkState) {
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

        benchmarkState.employerRepository.save(employer);

        assert benchmarkState.employerRepository.findById(employer.getId()).get().getListOfEmployeees().size() == numberOfEntities;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(HibernateListVsSetBenchmark.class.getSimpleName())
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