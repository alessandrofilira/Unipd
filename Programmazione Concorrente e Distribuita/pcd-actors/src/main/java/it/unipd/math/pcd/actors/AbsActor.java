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


import java.util.concurrent.LinkedBlockingQueue;
import it.unipd.math.pcd.actors.exceptions.NoSuchActorException;

/**
 * Defines common properties of all actors.
 *
 * @author Riccardo Cardin
 * @version 1.0
 * @since 1.0
 */
public abstract class AbsActor<T extends Message> implements Actor<T> {

    /**
     * Self-reference of the actor
     */
    protected ActorRef<T> self;

    /**
     * Sender of the current message
     */
    protected ActorRef<T> sender;


    /**
    * variabile per capire se attore è stato fermato
    */
    protected volatile boolean stopped = false;

    /**
    * mail box personale di ogni attore
    */
    protected final LinkedBlockingQueue<MessageImpl<T>> mailbox;

    {
        mailbox = new LinkedBlockingQueue<>();
    }


    /**
     * Sets the self-referece.
     *
     * @param self The reference to itself
     * @return The actor.
     */
    protected final Actor<T> setSelf(ActorRef<T> self) {
        this.self = self;
        return this;
    }

    /**
     * setta la variabile per capire se attore già stopped
     */
    private void setStopped() throws NoSuchActorException {
        if(stopped){
            throw new NoSuchActorException("Attore già fermato");
        }
        else{
            stopped=true;
        }
    }

    /**
     * costruttore che avvia i thread per l'attore
     */
    public AbsActor() {
        Thread helper = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    /**
                     * ciclo generale finchè attore vivo
                     */
                    while (!stopped) {
                        /**
                         * sincorinizzo ma mailbox per accesso
                         */
                        synchronized (mailbox) {
                            while (mailbox.isEmpty()) {
                                mailbox.wait();
                                /**
                                 * se mailbox vuota non serve far girare il thread, non ha nulla da fare
                                 */
                            }
                        }
                        /**
                         * se esco da ciclo while -> ci sono messaggi, li gestisco
                         */
                        synchronized (this) {
                            MessageImpl message = readMail();
                            sender = message.getActor();
                            receive((T) message.getMessage());
                        }

                    } // stopped

                } //try
                catch (InterruptedException e) {
                    /**
                     * thread interrotto
                     */
                } finally {
                    /**
                     * Devo chiudere "risorse" aperte
                     */
                    synchronized (this) {
                        /**
                         * thread interrotto ma finisce di leggere i messaggi
                         */
                        ReadAllMail();
                        /**
                         * scorsa tutta la maibox devo fermare thread
                         */
                        ActorShotDown();
                    }
                }
            }

            /**
             * Metodo synchronized per leggere la MailBox
             */
                private synchronized void ReadAllMail() {
                    while (!mailbox.isEmpty()) {
                        /**
                         * leggo tutti i messaggi e li rimuovo dalla coda
                         */
                        MessageImpl message = readMail();
                        /**
                         * imposto il sender
                         */
                        sender = message.getActor();
                        /**
                         * mando messaggio ad attore
                         */
                        receive((T) message.getMessage());
                    }
                }

                /**
                 * fermo l'attore e avviso che è stato fermato
                 */
                private void ActorShotDown(){
                    synchronized (AbsActor.this){
                        try{
                            setStopped();
                            notifyAll();
                        }
                        catch (NoSuchActorException e){

                        }
                    }
                }
            }
        );

        helper.start();
        /**
         * Per comodità si poteva portare fuori tutto e fare
         * una classe NON ANONIMA per la gestione del thread
         */
    }




    /**
     * metodo per ActorSystem per fermare attore
     */
    public void stopActor() throws NoSuchActorException{
        setStopped();
    }



    /* Gestione della mailbox */

    public void addMessage(T mex, ActorRef<? extends Message> sender){
        MessageImpl message= new MessageImpl(mex,sender);
        mailbox.add(message);
    }

    public MessageImpl<? extends Message> readMail(){
        return mailbox.remove();
    }

    public boolean isEmpty(){
        return mailbox.isEmpty();
    }

    /**
     * Inserisco i messaggi nella mailbox, con lock per evitare collisioni
     */
    protected final void InsertMessage(T mess, ActorRef<T> sender) {
        synchronized (mailbox) {
            if (!stopped) {
                addMessage(mess, sender);
                mailbox.notifyAll();
            }
        }
    }


}
