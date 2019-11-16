package tr.edu.itu.bbf.cloudcore.distributed.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.stereotype.Service;
import tr.edu.itu.bbf.cloudcore.distributed.entity.Events;
import tr.edu.itu.bbf.cloudcore.distributed.entity.States;

import java.util.UUID;

@Service
public class ChckpointPersistenceService {

    @Autowired
    public StateMachinePersister<States, Events, UUID> persister;

}
