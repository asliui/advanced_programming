package ro.uaic.asli.lab10.persistence.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import ro.uaic.asli.lab10.persistence.Lab10PersistenceSpringBoot;
import ro.uaic.asli.lab10.persistence.repository.ResultRepository;
import ro.uaic.asli.lab10.persistence.search.ResultSearchCriteria;
import ro.uaic.asli.lab10.persistence.service.ResultSearchService;

import java.util.concurrent.TimeUnit;

/**
 * JMH microbenchmarks for persistence reads (warm-up + measured iterations configured via CLI).
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(0)
@Warmup(iterations = 2, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 3, time = 200, timeUnit = TimeUnit.MILLISECONDS)
public class Lab11PersistenceBenchmark {

    @State(Scope.Benchmark)
    public static class SpringHolder {
        ConfigurableApplicationContext app;
        ResultRepository results;
        ResultSearchService search;

        @Setup(Level.Trial)
        public void setup() {
            System.setProperty("spring.profiles.active", "benchmark");
            app = new SpringApplicationBuilder(Lab10PersistenceSpringBoot.class)
                    .web(WebApplicationType.NONE)
                    .run();
            results = app.getBean(ResultRepository.class);
            search = app.getBean(ResultSearchService.class);
        }

        @TearDown(Level.Trial)
        public void tearDown() {
            if (app != null) {
                app.close();
            }
        }
    }

    @Benchmark
    public void searchDynamicBaseline(SpringHolder s, Blackhole bh) {
        ResultSearchCriteria c = new ResultSearchCriteria();
        c.setMinScore(0);
        bh.consume(s.search.searchResults(c));
    }

    @Benchmark
    public void repositoryFindAll(SpringHolder s, Blackhole bh) {
        bh.consume(s.results.findAll());
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
}
