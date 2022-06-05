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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.*;
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
public class HibernateOneToManyBenchmark extends BenchmarkBase {

    @State(Scope.Benchmark)
    public static class BenchmarkState extends AbstractSpringBootBenchmarkLifecycle {


        @Autowired
        private TeacherRepository teacherRepository;

        @Override
        public void preTrialSetUp() throws Exception {
            super.preTrialSetUp();
            teacherRepository = configurableApplicationContext.getBean(TeacherRepository.class);
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

    private static Logger logger = LoggerFactory.getLogger(HibernateOneToManyBenchmark.class);

    @Benchmark
    public void measureUnidirectionalOneToMany(BenchmarkState benchmarkState) {
        Teacher teacher = new Teacher();
        teacher.setName(RandomStringUtils.randomAlphabetic(10));

        List<Student> studentList = Lists.newArrayList();
        // prepare studentList
        for(int i = 0; i < numberOfEntities; i++) {
            Student student = new Student();
            student.setName(RandomStringUtils.randomAlphabetic(10));
            studentList.add(student);
        }

        for(Student student: studentList) {
            teacher.get_1_OneToManyStudents().add(student);
        }
        benchmarkState.teacherRepository.save(teacher);

        assert benchmarkState.teacherRepository.findById(teacher.getId()).get().get_1_OneToManyStudents().size() == numberOfEntities;


        for(Student student: studentList) {
            teacher.get_1_OneToManyStudents().remove(student);
        }
        benchmarkState.teacherRepository.save(teacher);

        assert benchmarkState.teacherRepository.findById(teacher.getId()).get().get_1_OneToManyStudents().size() == 0;
    }

    @Benchmark
    public void measureUnidirectionalOneToManyAndJoinColumn(BenchmarkState benchmarkState) {
        Teacher teacher = new Teacher();
        teacher.setName(RandomStringUtils.randomAlphabetic(10));

        List<Student> studentList = Lists.newArrayList();
        // prepare studentList
        for(int i = 0; i < numberOfEntities; i++) {
            Student student = new Student();
            student.setName(RandomStringUtils.randomAlphabetic(10));
            studentList.add(student);
        }

        for(Student student: studentList) {
            teacher.get_2_OneToManyStudents().add(student);
        }
        benchmarkState.teacherRepository.save(teacher);

        assert benchmarkState.teacherRepository.findById(teacher.getId()).get().get_2_OneToManyStudents().size() == numberOfEntities;


        for(Student student: studentList) {
            teacher.get_2_OneToManyStudents().remove(student);
        }
        benchmarkState.teacherRepository.save(teacher);

        assert benchmarkState.teacherRepository.findById(teacher.getId()).get().get_2_OneToManyStudents().size() == 0;
    }

    @Benchmark
    public void measureBidirectionalOneToManyAndJoinColumn(BenchmarkState benchmarkState) {
        Teacher teacher = new Teacher();
        teacher.setName(RandomStringUtils.randomAlphabetic(10));

        List<Student> studentList = Lists.newArrayList();
        // prepare studentList
        for(int i = 0; i < numberOfEntities; i++) {
            Student student = new Student();
            student.setName(RandomStringUtils.randomAlphabetic(10));
            studentList.add(student);
        }

        for(Student student: studentList) {
            teacher.addStudent(student);
        }
        benchmarkState.teacherRepository.save(teacher);

        logger.debug("get_3_OneToManyStudents 3 [{}]", benchmarkState.teacherRepository.findById(teacher.getId()).get().get_3_OneToManyStudents().size());

        assert benchmarkState.teacherRepository.findById(teacher.getId()).get().get_3_OneToManyStudents().size() == numberOfEntities;


        for(Student student: studentList) {
            teacher.removeStudent(student);
        }
        benchmarkState.teacherRepository.save(teacher);

        assert benchmarkState.teacherRepository.findById(teacher.getId()).get().get_3_OneToManyStudents().size() == 0;
    }

    @Benchmark
    public void measureUnidirectionalOneToManyAndOrderedColumn(BenchmarkState benchmarkState) {
        Teacher teacher = new Teacher();
        teacher.setName(RandomStringUtils.randomAlphabetic(10));

        List<Student> studentList = Lists.newArrayList();
        // prepare studentList
        for(int i = 0; i < numberOfEntities; i++) {
            Student student = new Student();
            student.setName(RandomStringUtils.randomAlphabetic(10));
            studentList.add(student);
        }

        for(Student student: studentList) {
            teacher.get_4_OneToManyStudents().add(student);
        }
        benchmarkState.teacherRepository.save(teacher);

        assert benchmarkState.teacherRepository.findById(teacher.getId()).get().get_4_OneToManyStudents().size() == numberOfEntities;


        for(Student student: studentList) {
            teacher.get_4_OneToManyStudents().remove(student);
        }
        benchmarkState.teacherRepository.save(teacher);

        assert benchmarkState.teacherRepository.findById(teacher.getId()).get().get_4_OneToManyStudents().size() == 0;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(HibernateOneToManyBenchmark.class.getSimpleName())
                .resultFormat(ResultFormatType.JSON)
                .result("HibernateOneToManyBenchmark-result.json")
                .build();

        new Runner(opt).run();
    }


}

@Entity(name = "Student")
@Table(name = "student")
@Data
class Student {

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
    @JoinColumn(name = "teacher_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Teacher teacher;
}


@Entity(name = "Teacher")
@Table(name = "teacher")
@Data
class Teacher {

    @EqualsAndHashCode.Exclude
    @Id
    @GenericGenerator(name = "idGenerator", strategy = "uuid")
    @GeneratedValue(generator = "idGenerator")
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    // Unidirectional @OneToMany
    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            /*
             * fetch = FetchType.LAZY
             * for better performance
             */
            fetch = FetchType.EAGER
    )
    private Set<Student> _1_OneToManyStudents = Sets.newHashSet();

    // Unidirectional @OneToMany with @JoinColumn
    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            /*
             * fetch = FetchType.LAZY
             * for better performance
             */
            fetch = FetchType.EAGER
    )
    @JoinColumn(name = "student_id")
    private Set<Student> _2_OneToManyStudents = Sets.newHashSet();

    //Bidirectional @OneToMany
    @OneToMany(
            mappedBy = "teacher",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    private Set<Student> _3_OneToManyStudents = Sets.newHashSet();

    public void addStudent(Student student) {
        this._3_OneToManyStudents.add(student);
        student.setTeacher(this);
    }

    public void removeStudent(Student student) {
        this._3_OneToManyStudents.remove(student);
        student.setTeacher(null);
    }

    // Unidirectional @OneToMany with @OrderColumn
    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            /*
             * fetch = FetchType.LAZY
             * for better performance
             */
            fetch = FetchType.EAGER
    )
    @OrderColumn(name = "index")
    private Set<Student> _4_OneToManyStudents = Sets.newHashSet();
}

interface StudentRepository extends CrudRepository<Student, String> {

}

interface TeacherRepository extends CrudRepository<Teacher, String> {

}