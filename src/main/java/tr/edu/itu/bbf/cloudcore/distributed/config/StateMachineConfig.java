package tr.edu.itu.bbf.cloudcore.distributed.config;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.ensemble.StateMachineEnsemble;
import org.springframework.statemachine.zookeeper.ZookeeperStateMachineEnsemble;
import tr.edu.itu.bbf.cloudcore.distributed.entity.Events;
import tr.edu.itu.bbf.cloudcore.distributed.entity.States;
import tr.edu.itu.bbf.cloudcore.distributed.checkpoint.___Checkpoint;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObject;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObjectHandler;

import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

@Configuration
@EnableStateMachine(name = "DistributedStateMachine")
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<States, Events> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private StateMachineEnsemble<States, Events> stateMachineEnsemble;

    @Autowired
    private CheckpointDbObjectHandler dbObjectHandler;


    /** Default Constructor **/
    public StateMachineConfig(){ }

    @Bean
    public StateMachineEnsemble<States, Events> stateMachineEnsemble() throws Exception {
        stateMachineEnsemble =  new ZookeeperStateMachineEnsemble<States, Events>(curatorClient(), "/zkPath");
        return stateMachineEnsemble;
        //return new ZookeeperStateMachineEnsemble<States, Events>(curatorClient(), "/zkPath");
    }


    @Bean
    public CuratorFramework curatorClient() throws Exception {
        /* Sınıfa ait özellikler olabilir mi? Düşünülmeli.
        * https://programmer.ink/think/an-overview-of-zookeeper.html
        */
        String zkConnectionString = "zookeeper:2181";
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .defaultData(new byte[0])
                .retryPolicy(retryPolicy)
                .connectString(zkConnectionString)
                .build();
        client.start();
        CuratorFrameworkState state = client.getState();
        System.out.println("curatorClient state after initialization ----> " + state.name());
        return client;
    }

    @Override
    public void configure(StateMachineStateConfigurer<States, Events> states)
            throws Exception {
        states.withStates()
                .initial(States.UNPAID, initializationAction())
                .stateEntry(States.WAITING_FOR_RECEIVE,entryActionForWaiting())
                .stateExit(States.WAITING_FOR_RECEIVE, exitActionForWaiting())
                .stateEntry(States.DONE, entryActionForDone())
                .stateExit(States.DONE, exitActionForDone());
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<States, Events> config) throws Exception {
        config
                .withDistributed()
                .ensemble(stateMachineEnsemble());
    }


    @Override
    public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
            throws Exception {
        /** Defines "EXTERNAL" type of transitions **/
        transitions
                .withExternal()
                .source(States.UNPAID).target(States.WAITING_FOR_RECEIVE)
                .event(Events.PAY)
                .action(transitionAction())
                .and()
                .withExternal()
                .source(States.WAITING_FOR_RECEIVE).target(States.DONE)
                .event(Events.RECEIVE)
                .action(transitionAction())
                .and()
                .withExternal()
                .source(States.DONE).target(States.UNPAID)
                .event(Events.STARTFROMSCRATCH)
                .action(transitionAction());
    }

    @Bean
    public Action<States, Events> entryActionForWaiting() {
        return new Action<States, Events>() {

            @Override
            public void execute(StateContext<States, Events> context) {
                System.out.println("-----WAITING_FOR_RECEIVE_STATE.ENTER() for statemachine-----> " + context.getStateMachine().getUuid());
                Integer localVar = context.getExtendedState().get("localVarForWaiting", Integer.class);
                System.out.println("-----WAITING_FOR_RECEIVE_STATE.ENTER().PRINT_LOCAL_VAR()-----> " + localVar);
                System.out.println("-----WAITING_FOR_RECEIVE_STATE.ENTER().CHANGE_LOCAL_VAR()-----");
                localVar = localVar + 2;
                System.out.println("-----WAITING_FOR_RECEIVE_STATE.ENTER().PERSIST_LOCAL_VAR()-----");
                context.getExtendedState().getVariables().put("localVarForWaiting", localVar);
            }
        };
    }

    @Bean
    public Action<States, Events> exitActionForWaiting() {
        return new Action<States, Events>() {

            @Override
            public void execute(StateContext<States, Events> context) {
                System.out.println("-----WAITING_FOR_RECEIVE_STATE.EXIT() for statemachine----->" + context.getStateMachine().getUuid() );
                Integer localVar = context.getExtendedState().get("localVarForWaiting", Integer.class);
                System.out.println("-----WAITING_FOR_RECEIVE_STATE.EXIT().PRINT_LOCAL_VAR()-----> " + localVar);
            }
        };
    }

    @Bean
    public Action<States, Events> entryActionForDone() {
        return new Action<States, Events>() {

            @Override
            public void execute(StateContext<States, Events> context) {
                System.out.println("-----DONE_STATE.ENTER() for state machine----->" + context.getStateMachine().getUuid());
                Integer localVar = context.getExtendedState().get("localVarForDone", Integer.class);
                System.out.println("-----DONE_STATE.ENTER().PRINT_LOCAL_VAR()-----> " + localVar);
                System.out.println("-----DONE_STATE.ENTER().CHANGE_LOCAL_VAR()-----");
                localVar = localVar + 5;
                System.out.println("-----DONE_STATE.ENTER().PERSIST_LOCAL_VAR()-----");
                context.getExtendedState().getVariables().put("localVarForDone", localVar);
            }
        };
    }

    @Bean
    public Action<States, Events> exitActionForDone() {
        return new Action<States, Events>() {

            @Override
            public void execute(StateContext<States, Events> context) {
                System.out.println("-----DONE_STATE.EXIT() for state machine-----> " + context.getStateMachine().getUuid());
                Integer localVar = context.getExtendedState().get("localVarForDone", Integer.class);
                System.out.println("-----DONE_STATE.EXIT().PRINT_LOCAL_VAR()-----> " + localVar);
            }
        };
    }

    @Bean
    public Action<States, Events> initializationAction() {
        return new Action<States, Events>() {
            @Override
            public void execute(StateContext<States, Events> context) {
                System.out.println("-----TRANSITION ACTION FOR INITIALIZATION for state machine-----> " + context.getStateMachine().getUuid());
                /** Define extended state variable as common variable used inside transition actions **/
                context.getExtendedState().getVariables().put("common", 0);
                /** Define extended state variable as private/local variable used inside state actions **/
                context.getExtendedState().getVariables().put("localVarForWaiting",10);
                context.getExtendedState().getVariables().put("localVarForDone",50);
            }
        };
    }

    @Bean
    public Action<States, Events> transitionAction() {
        return new Action<States, Events>() {
            @Override
            public void execute(StateContext<States, Events> context) {
                System.out.println("-----TRANSITION ACTION BETWEEN STATES for statemachine-----> " + context.getStateMachine().getUuid());
                /* Get timeSleep from StateContext */
                Object sleep = context.getMessageHeaders().get("timeSleep");
                long longSleep = ((Number) sleep).longValue();
                /* Get processed event from StateContext */
                Object O_event = context.getMessageHeaders().get("processedEvent");
                String processedEvent = O_event.toString();
                /* Get UUID from StateContext and then print */
                Object O_UUID = context.getMessageHeaders().get("machineId");
                UUID uuid = UUID.fromString(O_UUID.toString());
                System.out.printf("Event %s is processed by SMOC ---> %s\n",processedEvent,uuid.toString());
                System.out.println("My UUID --> " + context.getStateMachine().getUuid());
                /*
                try {
                    Object O_statemachine = context.getMessageHeaders().get("stateMachine");
                    StateMachine<States, Events> sm = StateMachine.class.cast(O_statemachine);
                    System.out.println("UUID inside message header --> " + sm.getUuid());
                }catch (Exception e){
                    System.out.println("Exception...");
                    System.out.println("CAUSE: " + e.getCause());
                    System.out.println("MESSAGE: " + e.getMessage());
                    System.out.println("CLASS: " + e.getClass());
                }
                */
                Map<Object, Object> variables = context.getExtendedState().getVariables();
                Integer commonVar = context.getExtendedState().get("common", Integer.class);

                if (commonVar == 0) {
                    logger.info("Switch common variable from 0 to 1");
                    variables.put("common", 1);
                    sleepForAWhile(longSleep);
                } else if (commonVar == 1) {
                    logger.info("Switch common variable from 1 to 2");
                    variables.put("common", 2);
                    sleepForAWhile(longSleep);
                } else if (commonVar == 2) {
                    logger.info("Switch common variable from 2 to 0");
                    variables.put("common", 0);
                    sleepForAWhile(longSleep);
                }

                try {
                    PerformCheckpoint(context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public void sleepForAWhile(Long sleepTime){
        try {
            TimeUnit.MILLISECONDS.sleep(sleepTime);
        } catch (InterruptedException ex) {
            System.out.println("Exception during sleepForAWhile --> " + ex.toString());
        }

    }

    public void PerformCheckpoint(@NotNull StateContext<States, Events> context) throws Exception {
        System.out.println("----- PERFORM CKPT -----");
        Map<Object, Object> variables = context.getExtendedState().getVariables();
        Map<String, ___Checkpoint> checkpoints = (Map<String, ___Checkpoint>) context.getExtendedState().getVariables().get("CKPT");
        /* Get state machine UUID from StateContext */
        Object O_UUID = context.getMessageHeaders().get("machineId");
        UUID uuid = UUID.fromString(O_UUID.toString());
        /* Get source and target states from StateContext */
        Object O_source = context.getMessageHeaders().get("source");
        String sourceState = O_source.toString();
        Object O_target = context.getMessageHeaders().get("target");
        String targetState = O_target.toString();
        /* Get processed event from StateContext */
        Object O_event = context.getMessageHeaders().get("processedEvent");
        String processedEvent = O_event.toString();
        /* Create a new CKPT db object */
        //CheckpointDbObject dbObject = new CheckpointDbObject(getTimeStamp(), uuid, sourceState,processedEvent, targetState);
        //dbObjectHandler.insertCheckpoint(dbObject);
    }

    public String getTimeStamp(){
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1; // Note: zero based!
        int day = now.get(Calendar.DAY_OF_MONTH);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int second = now.get(Calendar.SECOND);
        int ms = now.get(Calendar.MILLISECOND);

        String ts = year + "." + month + "." +  day + "_" + hour + "." + minute + "." + second + "." + ms;
        return ts;
    }


}
