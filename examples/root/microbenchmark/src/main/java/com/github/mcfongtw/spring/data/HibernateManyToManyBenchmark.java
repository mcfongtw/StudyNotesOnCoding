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
import java.util.List;
import java.util.Set;

import static com.github.mcfongtw.spring.boot.AbstractSpringBootBenchmark.DEFAULT_NUMBER_OF_ITERATIONS;
import static com.github.mcfongtw.spring.boot.AbstractSpringBootBenchmark.numberOfEntities;

public class HibernateManyToManyBenchmark {

    @State(Scope.Benchmark)
    public static class ExecutionPlan extends AbstractSpringBootBenchmark.AbstractSpringBootExecutionPlan {

        @Autowired
        private BoyRepository boyRepository;

        @Autowired
        private GirlRepository girlRepository;

        @Setup(Level.Trial)
        @Override
        public void preTrialSetUp() throws Exception {
            super.preTrialSetUp();
            boyRepository = configurableApplicationContext.getBean(BoyRepository.class);
            girlRepository = configurableApplicationContext.getBean(GirlRepository.class);
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
    public void measureUnidirecitonalManyToMany(ExecutionPlan executionPlan) throws Exception {
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
            executionPlan.boyRepository.save(boy);
        }


        assert executionPlan.boyRepository.findById(boy.getId()).get().getName() == boy.getName();
        assert executionPlan.boyRepository.findById(boy.getId()).get().getSetOfGirls_1().size() == numberOfEntities;

        for(Girl girl: girls) {
            boy.getSetOfGirls_1().remove(girl);
        }

        //FIXME: getGirl() != null
//        assert executionPlan.girlRepository.findById(girl.getId()).get() == null;
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Measurement(iterations = DEFAULT_NUMBER_OF_ITERATIONS)
    public void measureBidirectionalManyToMany(ExecutionPlan executionPlan) throws Exception {
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
            executionPlan.boyRepository.save(boy);
        }

        assert executionPlan.boyRepository.findById(boy.getId()).get().getName() == boy.getName();
        assert executionPlan.boyRepository.findById(boy.getId()).get().getSetOfGirls_2().size() == numberOfEntities;

        for(Girl girl: girls) {
            boy.removeGirlFromSet(girl);
        }

        //FIXME: getGirl() != null
//        assert executionPlan.girlRepository.findById(girl.getId()).get() == null;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(HibernateManyToManyBenchmark.class.getSimpleName())
                .forks(1)
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