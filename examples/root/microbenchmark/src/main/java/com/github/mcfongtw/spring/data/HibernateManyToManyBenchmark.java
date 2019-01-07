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
import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

import static com.github.mcfongtw.spring.boot.AbstractSpringBootBenchmark.NUMBER_OF_ENTITIES;

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
    @Measurement(iterations=10)
    public void measureManyToManyWithList(HibernateManyToManyBenchmark.ExecutionPlan executionPlan) throws Exception {
        List<Girl> girls = Lists.newArrayList();

        NUMBER_OF_ENTITIES = 32;

        for(int i = 0; i < NUMBER_OF_ENTITIES; i++) {
            Girl girl = new Girl();
            girl.setName(RandomStringUtils.randomAlphabetic(10));
            girls.add(girl);
        }

        Boy boy = new Boy();
        boy.setName(RandomStringUtils.randomAlphabetic(10));

        for(Girl girl: girls) {
            boy.addGirlToList(girl);
            executionPlan.boyRepository.save(boy);
        }


        assert executionPlan.boyRepository.findById(boy.getId()).get().getName() == boy.getName();
        assert executionPlan.boyRepository.findById(boy.getId()).get().getListOfGirls().size() == NUMBER_OF_ENTITIES;

        for(Girl girl: girls) {
            boy.removeGirlFromList(girl);
        }

        //FIXME: getGirl() != null
//        assert executionPlan.girlRepository.findById(girl.getId()).get() == null;
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Measurement(iterations=10)
    public void measureManyToManyWithSet(HibernateManyToManyBenchmark.ExecutionPlan executionPlan) throws Exception {
        List<Girl> girls = Lists.newArrayList();

        NUMBER_OF_ENTITIES = 128;

        for(int i = 0; i < NUMBER_OF_ENTITIES; i++) {
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
        assert executionPlan.boyRepository.findById(boy.getId()).get().getSetOfGirls().size() == NUMBER_OF_ENTITIES;

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

    @ManyToMany(
            cascade = {CascadeType.MERGE, CascadeType.PERSIST},
            fetch = FetchType.EAGER
    )
    @JoinTable(name = "boy_girl_list"
            , joinColumns = @JoinColumn(name = "boy_id")
            , inverseJoinColumns = @JoinColumn(name = "girl_id")
    )
    private List<Girl> listOfGirls = Lists.newArrayList();

    public void addGirlToList(Girl girl) {
        listOfGirls.add(girl);
        girl.getListOfBoys().add(this);
    }

    public void removeGirlFromList(Girl girl) {
        listOfGirls.remove(girl);
        girl.getListOfBoys().remove(this);
    }

    @ManyToMany(
            cascade = {CascadeType.MERGE, CascadeType.PERSIST},
            fetch = FetchType.EAGER
            )
    @JoinTable(name = "boy_girl_set"
            , joinColumns = @JoinColumn(name = "boy_id")
            , inverseJoinColumns = @JoinColumn(name = "girl_id")
    )
    private Set<Girl> setOfGirls = Sets.newHashSet();

    public void addGirlToSet(Girl girl) {
        setOfGirls.add(girl);
        girl.getSetOfBoys().add(this);
    }

    public void removeGirlFromSet(Girl girl) {
        setOfGirls.remove(girl);
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

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(mappedBy = "listOfGirls")
    private List<Boy> listOfBoys = Lists.newArrayList();

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(mappedBy = "setOfGirls")
    private Set<Boy> setOfBoys = Sets.newHashSet();
}

interface BoyRepository extends JpaRepository<Boy, String> {

}

interface GirlRepository extends JpaRepository<Girl, String> {

}