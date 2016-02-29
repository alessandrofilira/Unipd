package it.unipd.math.pcd.actors;

/**
 * Created by alessandrofilira on 01/02/16.
 */
public class MessageImpl<T extends Message>{

    /**
     * Variabile con il messaggio, implementato da client
     */
    private final T message;

    /**
     * Variabile per attore sender
     */
    private final ActorRef<T> act;


    public MessageImpl(T m, ActorRef a) {
        message = m;
        act = a;
    }

    public T getMessage(){
        return message;
    }
    public ActorRef<? extends Message> getActor(){
        return act;
    }
}
