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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

import static com.github.mcfongtw.spring.boot.AbstractSpringBootBenchmark.NUMBER_OF_ENTITIES;

public class HibernateOneToManyBenchmark {

    @State(Scope.Benchmark)
    public static class ExecutionPlan extends AbstractSpringBootBenchmark.AbstractSpringBootExecutionPlan {


        @Autowired
        private TeacherRepository teacherRepository;

        @Setup(Level.Trial)
        @Override
        public void preTrialSetUp() throws Exception {
            super.preTrialSetUp();
            teacherRepository = configurableApplicationContext.getBean(TeacherRepository.class);
        }

        @TearDown(Level.Trial)
        @Override
        public void preTrialTearDown() throws Exception {
            super.preTrialTearDown();
        }

    }

    private static Logger logger = LoggerFactory.getLogger(HibernateOneToManyBenchmark.class);

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Measurement(iterations=10)
    public void measureUnidirectionalOneToMany(ExecutionPlan executionPlan) {
        Teacher teacher = new Teacher();
        teacher.setName(RandomStringUtils.randomAlphabetic(10));

        List<Student> studentList = Lists.newArrayList();
        // prepare studentList
        for(int i = 0; i < NUMBER_OF_ENTITIES; i++) {
            Student student = new Student();
            student.setName(RandomStringUtils.randomAlphabetic(10));
            studentList.add(student);
        }

        for(Student student: studentList) {
            teacher.get_1_OneToManyStudents().add(student);
        }
        executionPlan.teacherRepository.save(teacher);

        assert executionPlan.teacherRepository.findById(teacher.getId()).get().get_1_OneToManyStudents().size() == NUMBER_OF_ENTITIES;


        for(Student student: studentList) {
            teacher.get_1_OneToManyStudents().remove(student);
        }
        executionPlan.teacherRepository.save(teacher);

        assert executionPlan.teacherRepository.findById(teacher.getId()).get().get_1_OneToManyStudents().size() == 0;
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Measurement(iterations=10)
    public void measureUnidirectionalOneToManyAndJoinColumn(ExecutionPlan executionPlan) {
        Teacher teacher = new Teacher();
        teacher.setName(RandomStringUtils.randomAlphabetic(10));

        List<Student> studentList = Lists.newArrayList();
        // prepare studentList
        for(int i = 0; i < NUMBER_OF_ENTITIES; i++) {
            Student student = new Student();
            student.setName(RandomStringUtils.randomAlphabetic(10));
            studentList.add(student);
        }

        for(Student student: studentList) {
            teacher.get_2_OneToManyStudents().add(student);
        }
        executionPlan.teacherRepository.save(teacher);

        assert executionPlan.teacherRepository.findById(teacher.getId()).get().get_2_OneToManyStudents().size() == NUMBER_OF_ENTITIES;


        for(Student student: studentList) {
            teacher.get_2_OneToManyStudents().remove(student);
        }
        executionPlan.teacherRepository.save(teacher);

        assert executionPlan.teacherRepository.findById(teacher.getId()).get().get_2_OneToManyStudents().size() == 0;
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Measurement(iterations=10)
    public void measureBidirectionalOneToManyAndJoinColumn(ExecutionPlan executionPlan) {
        Teacher teacher = new Teacher();
        teacher.setName(RandomStringUtils.randomAlphabetic(10));

        List<Student> studentList = Lists.newArrayList();
        // prepare studentList
        for(int i = 0; i < NUMBER_OF_ENTITIES; i++) {
            Student student = new Student();
            student.setName(RandomStringUtils.randomAlphabetic(10));
            studentList.add(student);
        }

        for(Student student: studentList) {
            teacher.addStudent(student);
        }
        executionPlan.teacherRepository.save(teacher);

        logger.debug("get_3_OneToManyStudents 3 [{}]", executionPlan.teacherRepository.findById(teacher.getId()).get().get_3_OneToManyStudents().size());

        assert executionPlan.teacherRepository.findById(teacher.getId()).get().get_3_OneToManyStudents().size() == NUMBER_OF_ENTITIES;


        for(Student student: studentList) {
            teacher.removeStudent(student);
        }
        executionPlan.teacherRepository.save(teacher);

        assert executionPlan.teacherRepository.findById(teacher.getId()).get().get_3_OneToManyStudents().size() == 0;
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Measurement(iterations=10)
    public void measureUnidirectionalOneToManyAndOrderedColumn(ExecutionPlan executionPlan) {
        Teacher teacher = new Teacher();
        teacher.setName(RandomStringUtils.randomAlphabetic(10));

        List<Student> studentList = Lists.newArrayList();
        // prepare studentList
        for(int i = 0; i < NUMBER_OF_ENTITIES; i++) {
            Student student = new Student();
            student.setName(RandomStringUtils.randomAlphabetic(10));
            studentList.add(student);
        }

        for(Student student: studentList) {
            teacher.get_4_OneToManyStudents().add(student);
        }
        executionPlan.teacherRepository.save(teacher);

        assert executionPlan.teacherRepository.findById(teacher.getId()).get().get_4_OneToManyStudents().size() == NUMBER_OF_ENTITIES;


        for(Student student: studentList) {
            teacher.get_4_OneToManyStudents().remove(student);
        }
        executionPlan.teacherRepository.save(teacher);

        assert executionPlan.teacherRepository.findById(teacher.getId()).get().get_4_OneToManyStudents().size() == 0;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(HibernateOneToManyBenchmark.class.getSimpleName())
                .forks(1)
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