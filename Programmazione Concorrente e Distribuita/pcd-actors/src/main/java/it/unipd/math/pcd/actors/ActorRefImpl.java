package it.unipd.math.pcd.actors;

import it.unipd.math.pcd.actors.exceptions.NoSuchActorException;

/**
 * Created by alessandrofilira on 31/01/16.
 */
public class ActorRefImpl<T extends Message> implements ActorRef<T>{

    /**
     * mantengo un riferimento a ActorSystem per ottenere l'attore
     */
    protected final ActorSystemImpl actorSystem;


    public ActorRefImpl( ActorSystemImpl actSys ) {
        actorSystem = actSys;
    }

    @Override
    public int compareTo(ActorRef act) {
        return hashCode() == act.hashCode() ? 0 : -1;
    }

    @Override
    public void send(Message message, ActorRef to) throws NoSuchActorException{
        AbsActor act= (AbsActor) actorSystem.getActor(to);
        if(act != null  && !act.stopped){
            act.InsertMessage(message,this);
        }
        else{
            throw new NoSuchActorException("Non si possono aggiungere messaggi in mailbox");
        }
    }


}

