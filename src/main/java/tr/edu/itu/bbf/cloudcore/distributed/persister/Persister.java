package tr.edu.itu.bbf.cloudcore.distributed.persister;

import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Component;
import tr.edu.itu.bbf.cloudcore.distributed.entity.Events;
import tr.edu.itu.bbf.cloudcore.distributed.entity.States;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class Persister implements StateMachinePersist<States, Events, UUID> {

    static Map<UUID, States> cache = new HashMap<>(16);

    @Override
    public void write(StateMachineContext<States, Events> context, UUID uuid) throws Exception {
        System.out.println("--- PERSIST via WRITE METHOD ---");
        cache.put(uuid, context.getState());
    }

    @Override
    public StateMachineContext<States, Events> read(UUID uuid) throws Exception {
        System.out.println("--- RESTORE via READ METHOD ---");
        return cache.containsKey(uuid) ?
                new DefaultStateMachineContext<>(cache.get(uuid), null, null, null, null, null) :
                new DefaultStateMachineContext<>(States.UNPAID, null, null, null, null, null);
    }

}