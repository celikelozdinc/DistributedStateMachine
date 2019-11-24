package tr.edu.itu.bbf.cloudcore.distributed.persist;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointRepository;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObject;

import java.util.List;
import java.util.Optional;


@Component
public class CheckpointDbObjectHandler {

    //@Autowired
    //private CheckpointRepository checkpointRepository;

    // INSERT
    public CheckpointDbObject insertCheckpoint(@NotNull CheckpointDbObject checkpointDbObject){
        CheckpointRepository checkpointRepository = new CheckpointRepository() {
            @Override
            public <S extends CheckpointDbObject> List<S> saveAll(Iterable<S> entities) {
                return null;
            }

            @Override
            public List<CheckpointDbObject> findAll() {
                return null;
            }

            @Override
            public List<CheckpointDbObject> findAll(Sort sort) {
                return null;
            }

            @Override
            public <S extends CheckpointDbObject> S insert(S entity) {
                return null;
            }

            @Override
            public <S extends CheckpointDbObject> List<S> insert(Iterable<S> entities) {
                return null;
            }

            @Override
            public <S extends CheckpointDbObject> List<S> findAll(Example<S> example) {
                return null;
            }

            @Override
            public <S extends CheckpointDbObject> List<S> findAll(Example<S> example, Sort sort) {
                return null;
            }

            @Override
            public Page<CheckpointDbObject> findAll(Pageable pageable) {
                return null;
            }

            @Override
            public <S extends CheckpointDbObject> S save(S entity) {
                return null;
            }

            @Override
            public Optional<CheckpointDbObject> findById(Integer integer) {
                return Optional.empty();
            }

            @Override
            public boolean existsById(Integer integer) {
                return false;
            }

            @Override
            public Iterable<CheckpointDbObject> findAllById(Iterable<Integer> integers) {
                return null;
            }

            @Override
            public long count() {
                return 0;
            }

            @Override
            public void deleteById(Integer integer) {

            }

            @Override
            public void delete(CheckpointDbObject entity) {

            }

            @Override
            public void deleteAll(Iterable<? extends CheckpointDbObject> entities) {

            }

            @Override
            public void deleteAll() {

            }

            @Override
            public <S extends CheckpointDbObject> Optional<S> findOne(Example<S> example) {
                return Optional.empty();
            }

            @Override
            public <S extends CheckpointDbObject> Page<S> findAll(Example<S> example, Pageable pageable) {
                return null;
            }

            @Override
            public <S extends CheckpointDbObject> long count(Example<S> example) {
                return 0;
            }

            @Override
            public <S extends CheckpointDbObject> boolean exists(Example<S> example) {
                return false;
            }
        };
        System.out.println("INSERT CHECKPOINT");
        System.out.println("Timestamp inside db object ->" + checkpointDbObject.timestamp.toString());
        if (checkpointRepository == null) {System.out.println("Checkpoint repository is null.");}
        try {
            return checkpointRepository.insert(checkpointDbObject);
        } catch(Exception ex) {
            System.out.println("Can not insert :(");
            System.out.println("Exception...");
            ex.printStackTrace();
            return checkpointDbObject;
        }

    }

    // UPDATE
    public void updateCheckpoint(CheckpointDbObject checkpointDbObject) {
        System.out.println("UPDATE");
        //return checkpointRepository.save(checkpointDbObject);
    }

}
