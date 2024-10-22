package org.example.events;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventBus {
    private final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final ConcurrentHashMap<Class<? extends Event>, CopyOnWriteArrayList<Consumer<?
            extends Event>>> listeners = new ConcurrentHashMap<>();

    private final BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>();
    private volatile boolean running = true;
    private final Thread publisherThread;

    private EventBus() {
        publisherThread = new Thread(() -> {
            while (running || !eventQueue.isEmpty()) {
                try {
                    Event event = eventQueue.take();
                    emit(event);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.log(Level.SEVERE, "Dispatcher interrupted", e);
                }
            }

            logger.log(Level.INFO, "Dispatcher stopped");
        });

        publisherThread.setName("Publisher-Thread");
        publisherThread.start();
    }

    private static class Holder {
        private static final EventBus INSTANCE = new EventBus();
    }

    public static EventBus getInstance() {
        return EventBus.Holder.INSTANCE;
    }

    public <T extends Event> void subscribe (Class<T> eventType, Consumer<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    @SuppressWarnings("unchecked")
    public void publish (Event event) {
        if (running) {
            try {
                eventQueue.put(event);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.log(Level.SEVERE, "Dispatcher interrupted", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void emit (Event event) {
        Class<? extends Event> eventType = event.getClass();
        CopyOnWriteArrayList<Consumer<? extends Event>> pubList = listeners.get(eventType);

        if (pubList != null) {
            for (Consumer<? extends Event> listener : pubList) {
                ((Consumer<Event>) listener).accept(event);
            }
        }
    }

    public void shutdown() {
        running = false;
        publisherThread.interrupt();
    }
}
