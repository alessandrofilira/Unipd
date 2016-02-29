/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2015 Riccardo Cardin
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * <p/>
 * Please, insert description here.
 *
 * @author Riccardo Cardin
 * @version 1.0
 * @since 1.0
 */

/**
 * Please, insert description here.
 *
 * @author Riccardo Cardin
 * @version 1.0
 * @since 1.0
 */
package it.unipd.math.pcd.actors;

import it.unipd.math.pcd.actors.exceptions.NoSuchActorException;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.reflections.Reflections;

/**
 * A map-based implementation of the actor system.
 *
 * @author Riccardo Cardin
 * @version 1.0
 * @since 1.0
 */
public abstract class AbsActorSystem implements ActorSystem {

    /**
     * Associates every Actor created with an identifier.
     */
    private static Map<ActorRef<?>, Actor<?>> actors=new ConcurrentHashMap<>();

    //private static AbsActorSystem istance;
    //private static final String BASE_PACKAGE = "it.unipd.math.pcd.actors";
    /**
     * Variabile bool per capire se ActSys già instanziato
     */
    private static boolean exist=false;


    protected AbsActorSystem(){
        if(!exist)
            exist=true;
    }

    /*
    Tentativo di creazione Singleton con Reflection per capire la classe concreta di AbsActorSystem
    public synchronized static AbsActorSystem getIstance() {
        if(istance==null) {
            Reflections reflections = new Reflections(BASE_PACKAGE);
            Set<Class<? extends AbsActorSystem>> subTypes = reflections.getSubTypesOf(AbsActorSystem.class);
            Class<? extends AbsActorSystem> systemClass = subTypes.iterator().next();
            try {
                istance= systemClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return istance;
    }
    */


    @Override
    public ActorRef<? extends Message> actorOf(Class<? extends Actor> actor, ActorMode mode) {

        // ActorRef instance
        ActorRef<?> reference;
        try {
            // Create the reference to the actor
            reference = this.createActorReference(mode);
            // Create the new instance of the actor
            Actor actorInstance = ((AbsActor) actor.newInstance()).setSelf(reference);
            // Associate the reference to the actor
            actors.put(reference, actorInstance);

        } catch (InstantiationException | IllegalAccessException e) {
            throw new NoSuchActorException(e);
        }
        return reference;
    }


    @Override
    public void stop() {
        synchronized (this) {
            Set acts = actors.keySet();
            Iterator i = acts.iterator();
            //scorro tutta la mappa di attori e avvio la procedura di stop
            while (i.hasNext()) {
                //chiamo per ogni attore il metodo stop per singolo
                stop((ActorRef<?>) i.next());
            }
        }
    }

    @Override
    public void stop(ActorRef<?> actor) {
        /**
         * cast a AbsActor
         */
        AbsActor act= (AbsActor) getActor(actor);
        if(act!=null) {
            if (!act.stopped) {
                // fermo l'attore
                act.stopActor();
                // rimuovo attore da mappa aspettando che legga mailbox

                removeActor(getActorbyRef(act));
            } else {
                throw new NoSuchActorException("Attore già fermato");
            }
        }
        else{
            throw new NoSuchActorException("Attore non esistente");
        }
    }

    /**
     * Metodo per rimuovere una coppia ActorRef-Actor dalla mappa (dopo aver avuto conferma che la mailBox è stata svuotata
     */
    public synchronized void removeActor(ActorRef a) {
        AbsActor act= (AbsActor) getActor(a);
        while (!act.stopped) {
                try {
                    act.wait();
                } catch (InterruptedException ie) {
                    //gestisco eccezione
                }
            }
            /**
             * Rimuovo attore dalla mappa
             */
            actors.remove(a);
    }

    /**
     * Metodo per avere un Actor by ActorRef
     */
    public Actor getActor(ActorRef actRef) throws NoSuchActorException{
        Actor act = actors.get(actRef);
        if(act == null) {
            throw new NoSuchActorException();
        }
        else {
            return act;
        }
    }

    //torna ActorRef dal Actor
    public synchronized ActorRef<?> getActorbyRef(AbsActor act){
        ActorRef actorRef = null;
        Iterator<Map.Entry<ActorRef<?>, Actor<?>>> iter = actors.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<ActorRef<?>,Actor<?>> entry = iter.next();
            if (entry.getValue().equals(act)) {
                actorRef = entry.getKey();
            }
        }
        return actorRef;

    }



    @Override
    public ActorRef<? extends Message> actorOf(Class<? extends Actor> actor) {
        return this.actorOf(actor, ActorMode.LOCAL);
    }

    protected abstract ActorRef createActorReference(ActorMode mode);


    public int getNumElements(){
        return actors.size();
    }
}