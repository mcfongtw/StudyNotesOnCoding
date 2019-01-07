package com.github.mcfongtw.spring.data;

import com.github.mcfongtw.spring.boot.AbstractSpringBootBenchmark;
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
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;

public class HibernateOneToOneBenchmark {

    private static Logger logger = LoggerFactory.getLogger(HibernateOneToOneBenchmark.class);

    @State(Scope.Benchmark)
    public static class ExecutionPlan extends AbstractSpringBootBenchmark.AbstractSpringBootExecutionPlan {

        @Autowired
        private ManRepository manRepository;

        @Autowired
        private WomanRepository womanRepository;

        @Autowired
        private MaleRepository maleRepository;

        @Autowired
        private FemaleRepository femaleRepository;

        @Setup(Level.Trial)
        @Override
        public void preTrialSetUp() throws Exception {
            super.preTrialSetUp();
            manRepository = configurableApplicationContext.getBean(ManRepository.class);
            womanRepository = configurableApplicationContext.getBean(WomanRepository.class);
            maleRepository = configurableApplicationContext.getBean(MaleRepository.class);
            femaleRepository = configurableApplicationContext.getBean(FemaleRepository.class);

            logger.warn("--------------->ManRepository: [{}]<----------------", manRepository);
        }

        @TearDown(Level.Trial)
        @Override
        public void preTrialTearDown() throws Exception {
            super.preTrialTearDown();
        }

    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Measurement(iterations=10)
    public void measureBidirectionalOneToOne(HibernateOneToOneBenchmark.ExecutionPlan executionPlan) {
        Man man = new Man();
        man.setName(RandomStringUtils.randomAlphabetic(10));

        Woman woman = new Woman();
        woman.setName(RandomStringUtils.randomAlphabetic(10));

        man.setWoman(woman);
        woman.setMan(man);

        executionPlan.manRepository.save(man);


        assert executionPlan.manRepository.findById(man.getId()).get().getWoman().getName() == woman.getName();
        assert executionPlan.womanRepository.findById(woman.getId()).get().getMan().getName() == man.getName();

        executionPlan.womanRepository.delete(woman);
        executionPlan.womanRepository.flush();

//        logger.debug("[{}]", executionPlan.manRepository.findById(man.getId()));
//        logger.debug("[{}]", executionPlan.manRepository.findById(man.getId()).get());
//        logger.debug("[{}]", executionPlan.manRepository.findById(man.getId()).get().getWoman());

        //FIXME: getWoman() != null
//        assert executionPlan.manRepository.findById(man.getId()).get().getWoman() == null;
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Measurement(iterations=10)
    public void measureUniDirectionalOneToOneWithMapsId(HibernateOneToOneBenchmark.ExecutionPlan executionPlan) {
        Male male = new Male();
        male.setName(RandomStringUtils.randomAlphabetic(10));

        Female female = new Female();
        female.setName(RandomStringUtils.randomAlphabetic(10));

        male.setFemale(female);

        executionPlan.maleRepository.save(male);


        assert executionPlan.maleRepository.findById(male.getId()).get().getFemale().getName() == female.getName();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(HibernateOneToOneBenchmark.class.getSimpleName())
                .forks(1)
                .resultFormat(ResultFormatType.JSON)
                .result("HibernateOneToOneBenchmark-result.json")
                .build();

        new Runner(opt).run();
    }

}

@Entity(name = "Man")
@Table(name = "man")
@Data
class Man {

    @EqualsAndHashCode.Exclude
    @Id
    @GenericGenerator(name = "idGenerator", strategy = "uuid")
    @GeneratedValue(generator = "idGenerator")
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    //Bidirectional OneToOne
    @OneToOne(
            mappedBy = "man",
            cascade = {CascadeType.ALL},
            fetch = FetchType.EAGER,
            orphanRemoval = true
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Woman woman;
}

@Transactional
interface ManRepository extends JpaRepository<Man, String> {

    @Override
    Man getOne(String id);

    @Override
    Man save(Man s);

    @Override
    void delete(Man s);
}

@Entity(name = "Woman")
@Table(name = "woman")
@Data
class Woman {

    @EqualsAndHashCode.Exclude
    @Id
    @GenericGenerator(name = "idGenerator", strategy = "uuid")
    @GeneratedValue(generator = "idGenerator")
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    //Bidirectional OneToOne
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "man_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Man man;
}

@Transactional
interface WomanRepository extends JpaRepository<Woman, String> {

    @Override
    Woman getOne(String id);

    @Override
    Woman save(Woman s);

    @Override
    void delete(Woman s);
}

//////////////////////

@Entity(name = "Male")
@Table(name = "male")
@Data
class Male {

    @EqualsAndHashCode.Exclude
    @Id
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToOne()
    @JoinColumn(name = "id")
    @MapsId
    private Female female;
}

@Transactional
interface MaleRepository extends JpaRepository<Male, Long> {
    @Override
    Male getOne(Long id);

    @Override
    Male save(Male s);

    @Override
    void delete(Male s);
}

@Entity(name = "Female")
@Table(name = "female")
@Data
class Female {

    @EqualsAndHashCode.Exclude
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

}

@Transactional
interface FemaleRepository extends JpaRepository<Female, Long> {
    @Override
    Female getOne(Long id);

    @Override
    Female save(Female s);

    @Override
    void delete(Female s);
}
