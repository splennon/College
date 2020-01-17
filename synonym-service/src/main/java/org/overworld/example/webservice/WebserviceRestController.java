package org.overworld.example.webservice;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.overworld.example.webservice.engine.BSDSum;
import org.overworld.example.webservice.engine.MD5Sum;
import org.overworld.example.webservice.engine.SeekTask;
import org.overworld.example.webservice.engine.TaskTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016
 */

@RestController
public class WebserviceRestController {

    /**
     * A bean factory to assist in application configuration
     */
    private @Autowired AutowireCapableBeanFactory beanFactory;

    /**
     * The executor service that will run the SeekTasks
     */
    private ExecutorService service;

    /**
     * A counter to allocate ids to tasks as they are created
     */
    private final AtomicInteger taskCounter = new AtomicInteger(1);

    /**
     * A map to hold the TaskTags against the task id that was communicated to
     * the client on creation
     */
    private final Map<Integer, TaskTag> tasksMap = Collections
        .synchronizedMap(new HashMap<Integer, TaskTag>());

    /**
     * The number of threads in the executor service
     */
    @Value("${service.threadCount}")
    private String threadCount;

    @RequestMapping(value = "/bsd/{digest}", method = RequestMethod.POST)
    public ResponseEntity<String> createBsd(@RequestBody final String body,
        @PathVariable final String digest) {

        final AtomicInteger progress = new AtomicInteger(0);

        final Integer taskId = this.taskCounter.getAndIncrement();

        final SeekTask st = new SeekTask(body, digest, new BSDSum(), progress);

        this.beanFactory.autowireBean(st);

        final Future<String> future = this.service.submit(st);

        this.tasksMap.put(taskId, new TaskTag(taskId, future, progress));

        return new ResponseEntity<>(taskId.toString(), HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "/md5/{digest}", method = RequestMethod.POST)
    public ResponseEntity<String> createMd5(@RequestBody final String body,
        @PathVariable final String digest) {

        final AtomicInteger progress = new AtomicInteger(0);

        final Integer taskId = this.taskCounter.getAndIncrement();

        final SeekTask st = new SeekTask(body, digest, new MD5Sum(), progress);

        this.beanFactory.autowireBean(st);

        final Future<String> future = this.service.submit(st);

        this.tasksMap.put(taskId, new TaskTag(taskId, future, progress));

        return new ResponseEntity<>(taskId.toString(), HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<String> delete(@PathVariable(value = "id") final int id) {

        final TaskTag tag = this.tasksMap.remove(id);

        if (tag == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        tag.getFuture().cancel(true);

        return new ResponseEntity<String>(HttpStatus.OK);
    }

    @PostConstruct
    private void init() {

        this.service = Executors
            .newFixedThreadPool(Integer.parseInt(this.threadCount));
    }

    @RequestMapping(value = "/{id}/progress", method = RequestMethod.GET)
    public ResponseEntity<Integer> progress(
        @PathVariable(value = "id") final int id) {

        final TaskTag tag = this.tasksMap.get(id);

        if (tag == null) {

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {

            return new ResponseEntity<>(tag.getProgress(), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<String> retrieve(@PathVariable(value = "id") final int id) {

        final TaskTag tag = this.tasksMap.get(id);

        if (tag == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (tag.getFuture().isDone()) {

            try {

                final ResponseEntity<String> result = new ResponseEntity<String>(
                    tag.getFuture().get(), HttpStatus.OK);
                this.tasksMap.remove(id);
                return result;
            } catch (ExecutionException | InterruptedException e) {

                return new ResponseEntity<String>(
                    HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {

            final HttpHeaders headers = new HttpHeaders();
            headers.add("Task-Progress", Integer.toString(tag.getProgress()));
            return new ResponseEntity<String>(headers, HttpStatus.ACCEPTED);
        }
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public ResponseEntity<String> status() {

        final List<String> results = this.tasksMap.entrySet().stream()
            .map(es -> Integer.toString(es.getKey()) + " => "
                + es.getValue().toString())
            .collect(Collectors.toList());

        return new ResponseEntity<String>(String.join("\n", results),
            HttpStatus.OK);
    }
}
