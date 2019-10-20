package tr.edu.itu.bbf.cloudcore.distributed.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.ensemble.StateMachineEnsemble;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.zookeeper.ZookeeperStateMachineEnsemble;
import tr.edu.itu.bbf.cloudcore.distributed.entity.Events;
import tr.edu.itu.bbf.cloudcore.distributed.entity.States;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Configuration
@EnableStateMachine(name = "DistributedStateMachine")
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<States, Events> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /*
    @Autowired
    private StateMachineEnsemble<States, Events> stateMachineEnsemble;
     */

    /** Default Constructor **/
    public StateMachineConfig(){ }

    @Override
    public void configure(StateMachineConfigurationConfigurer<States, Events> config) throws Exception {
        config
                .withDistributed()
                .ensemble(stateMachineEnsemble());
    }

    @Bean
    public StateMachineEnsemble<States, Events> stateMachineEnsemble() throws Exception {
        return new ZookeeperStateMachineEnsemble<States, Events>(curatorClient(), "/zkPath");
    }

    @Bean
    public CuratorFramework curatorClient() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.builder().defaultData(new byte[0])
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .connectString("zookeeper:2181").build();
        client.start();
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
                /* Will be used as CKPT */
                Map<String, Integer> ckpt = new HashMap<String, Integer>();
                ckpt.put("common",0);
                ckpt.put("localVarForWaiting",10);
                ckpt.put("localVarForDone",50);
                context.getExtendedState().getVariables().put("CKPT", ckpt);
                /* DUMMY VAR*/
                context.getExtendedState().getVariables().put("DUMMY", 1000);
            }
        };
    }

    @Bean
    public Action<States, Events> transitionAction() {
        return new Action<States, Events>() {
            @Override
            public void execute(StateContext<States, Events> context) {
                System.out.println("-----TRANSITION ACTION BETWEEN STATES for statemachine-----> " + context.getStateMachine().getUuid());

                Object sleep = context.getMessageHeaders().get("timeSleep");
                long longSleep = ((Number) sleep).longValue();

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

                Integer dummy = (Integer) context.getExtendedState().getVariables().get("DUMMY");
                dummy = dummy + 10;
                context.getExtendedState().getVariables().put("DUMMY", dummy);
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



}
