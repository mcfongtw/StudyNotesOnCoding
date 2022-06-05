package com.github.mcfongtw.spring.data;

import com.github.mcfongtw.BenchmarkBase;
import com.github.mcfongtw.spring.boot.AbstractSpringBootBenchmarkLifecycle;
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
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput})
@OutputTimeUnit(TimeUnit.SECONDS)
@Measurement(iterations = 20)
@Warmup(iterations = 10)
@Fork(3)
@Threads(1)
public class HibernateOneToOneBenchmark extends BenchmarkBase {

    @State(Scope.Benchmark)
    public static class BenchmarkState extends AbstractSpringBootBenchmarkLifecycle {

        @Autowired
        private ManRepository manRepository;

        @Autowired
        private WomanRepository womanRepository;

        @Autowired
        private MaleRepository maleRepository;

        @Autowired
        private FemaleRepository femaleRepository;

        @Override
        public void preTrialSetUp() throws Exception {
            super.preTrialSetUp();
            manRepository = configurableApplicationContext.getBean(ManRepository.class);
            womanRepository = configurableApplicationContext.getBean(WomanRepository.class);
            maleRepository = configurableApplicationContext.getBean(MaleRepository.class);
            femaleRepository = configurableApplicationContext.getBean(FemaleRepository.class);
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
    public void measureBidirectionalOneToOne(BenchmarkState benchmarkState) {
        Man man = new Man();
        man.setName(RandomStringUtils.randomAlphabetic(10));

        Woman woman = new Woman();
        woman.setName(RandomStringUtils.randomAlphabetic(10));

        man.setWoman(woman);
        woman.setMan(man);

        benchmarkState.manRepository.save(man);


        assert benchmarkState.manRepository.findById(man.getId()).get().getWoman().getName() == woman.getName();
        assert benchmarkState.womanRepository.findById(woman.getId()).get().getMan().getName() == man.getName();

        benchmarkState.womanRepository.delete(woman);

        assert benchmarkState.manRepository.findById(man.getId()).isPresent() == false;
    }

    @Benchmark
    public void measureUniDirectionalOneToOneWithMapsId(BenchmarkState benchmarkState) {
        Male male = new Male();
        male.setName(RandomStringUtils.randomAlphabetic(10));

        Female female = new Female();
        female.setName(RandomStringUtils.randomAlphabetic(10));

        male.setFemale(female);

        benchmarkState.maleRepository.save(male);


        assert benchmarkState.maleRepository.findById(male.getId()).get().getFemale().getName() == female.getName();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(HibernateOneToOneBenchmark.class.getSimpleName())
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
            cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH},
            fetch = FetchType.EAGER,
            orphanRemoval = true
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Woman woman;
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
    @OneToOne(
            fetch = FetchType.EAGER,
            cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}
    )
    @JoinColumn(name = "man_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Man man;
}


interface ManRepository extends CrudRepository<Man, String> {

}

interface WomanRepository extends CrudRepository<Woman, String> {

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

    @OneToOne
    @MapsId
    private Female female;
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

interface MaleRepository extends CrudRepository<Male, Long> {

}

interface FemaleRepository extends CrudRepository<Female, Long> {

}

////////////////////////////////////////////////

