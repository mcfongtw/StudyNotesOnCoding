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
public class HibernateManyToManyBenchmark extends BenchmarkBase {

    @State(Scope.Benchmark)
    public static class BenchmarkState extends AbstractSpringBootBenchmarkLifecycle {

        @Autowired
        private BoyRepository boyRepository;

        @Autowired
        private GirlRepository girlRepository;

        @Override
        public void preTrialSetUp() throws Exception {
            super.preTrialSetUp();
            boyRepository = configurableApplicationContext.getBean(BoyRepository.class);
            girlRepository = configurableApplicationContext.getBean(GirlRepository.class);
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
    public void measureUnidirecitonalManyToMany(BenchmarkState benchmarkState) throws Exception {
        List<Girl> girls = Lists.newArrayList();

        numberOfEntities = 128;

        for(int i = 0; i < numberOfEntities; i++) {
            Girl girl = new Girl();
            girl.setName(RandomStringUtils.randomAlphabetic(10));
            girls.add(girl);
        }

        Boy boy = new Boy();
        boy.setName(RandomStringUtils.randomAlphabetic(10));

        for(Girl girl: girls) {
            boy.getSetOfGirls_1().add(girl);
            benchmarkState.boyRepository.save(boy);
        }


        assert benchmarkState.boyRepository.findById(boy.getId()).get().getName() == boy.getName();
        assert benchmarkState.boyRepository.findById(boy.getId()).get().getSetOfGirls_1().size() == numberOfEntities;

        for(Girl girl: girls) {
            boy.getSetOfGirls_1().remove(girl);
        }

        //FIXME: getGirl() != null
//        assert benchmarkState.girlRepository.findById(girl.getId()).get() == null;
    }

    @Benchmark
    public void measureBidirectionalManyToMany(BenchmarkState benchmarkState) throws Exception {
        List<Girl> girls = Lists.newArrayList();

        numberOfEntities = 128;

        for(int i = 0; i < numberOfEntities; i++) {
            Girl girl = new Girl();
            girl.setName(RandomStringUtils.randomAlphabetic(10));
            girls.add(girl);
        }

        Boy boy = new Boy();
        boy.setName(RandomStringUtils.randomAlphabetic(10));


        for(Girl girl: girls) {
            boy.addGirlToSet(girl);
            benchmarkState.boyRepository.save(boy);
        }

        assert benchmarkState.boyRepository.findById(boy.getId()).get().getName() == boy.getName();
        assert benchmarkState.boyRepository.findById(boy.getId()).get().getSetOfGirls_2().size() == numberOfEntities;

        for(Girl girl: girls) {
            boy.removeGirlFromSet(girl);
        }

        //FIXME: getGirl() != null
//        assert benchmarkState.girlRepository.findById(girl.getId()).get() == null;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(HibernateManyToManyBenchmark.class.getSimpleName())
                .resultFormat(ResultFormatType.JSON)
                .result("HibernateManyToManyBenchmark-result.json")
                .build();

        new Runner(opt).run();
    }

}


@Entity(name = "Boy")
@Table(name = "boy")
@Data
class Boy {

    @EqualsAndHashCode.Exclude
    @Id
    @GenericGenerator(name = "idGenerator", strategy = "uuid")
    @GeneratedValue(generator = "idGenerator")
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    //Unidirectional @ManyToMany
    @ManyToMany(
            cascade = {CascadeType.MERGE, CascadeType.PERSIST},
            /*
             * fetch = FetchType.LAZY
             * for better performance
             */
            fetch = FetchType.EAGER
    )
    @JoinTable(name = "boy_girl_set_1"
            , joinColumns = @JoinColumn(name = "boy_id")
            , inverseJoinColumns = @JoinColumn(name = "girl_id")
    )
    private Set<Girl> setOfGirls_1 = Sets.newHashSet();


    //Bidirectional @ManyToMany
    @ManyToMany(
            cascade = {CascadeType.MERGE, CascadeType.PERSIST},
            /*
             * fetch = FetchType.LAZY
             * for better performance
             */
            fetch = FetchType.EAGER
            )
    @JoinTable(name = "boy_girl_set_2"
            , joinColumns = @JoinColumn(name = "boy_id")
            , inverseJoinColumns = @JoinColumn(name = "girl_id")
    )
    private Set<Girl> setOfGirls_2 = Sets.newHashSet();

    public void addGirlToSet(Girl girl) {
        setOfGirls_2.add(girl);
        girl.getSetOfBoys().add(this);
    }

    public void removeGirlFromSet(Girl girl) {
        setOfGirls_2.remove(girl);
        girl.getSetOfBoys().remove(this);
    }
}

@Entity(name = "Girl")
@Table(name = "girl")
@Data
class Girl {

    @EqualsAndHashCode.Exclude
    @Id
    @GenericGenerator(name = "idGenerator", strategy = "uuid")
    @GeneratedValue(generator = "idGenerator")
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    //Bidirectional @ManyToMany
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(mappedBy = "setOfGirls_2")
    private Set<Boy> setOfBoys = Sets.newHashSet();
}

interface BoyRepository extends CrudRepository<Boy, String> {

}

interface GirlRepository extends CrudRepository<Girl, String> {

}