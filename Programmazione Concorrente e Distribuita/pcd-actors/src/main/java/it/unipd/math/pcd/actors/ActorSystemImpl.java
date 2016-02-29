package it.unipd.math.pcd.actors;

import java.util.HashMap;

/**
 * Created by alessandrofilira on 31/01/16.
 */
public final class ActorSystemImpl extends AbsActorSystem {

    private static boolean exist=false;
    private static ActorSystemImpl istance=null;

    public ActorSystemImpl(){
        super();
    }

/*
    public synchronized ActorSystem getIstance(){

        if(istance==null){
            istance=new ActorSystemImpl();
        }
        return istance;
    }
*/



    @Override
        protected ActorRef createActorReference(ActorMode mode) {
            if (mode == ActorMode.LOCAL) {
                //costruisce e restituisce un ActorRefImpl
                return new ActorRefImpl(this);
            }
            else {
                throw new IllegalArgumentException("Gestione attori remoti non sviluppata");
            }

        }

}
