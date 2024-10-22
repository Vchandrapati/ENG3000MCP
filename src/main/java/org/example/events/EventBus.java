package org.example.events;

import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventBus {
    private final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final ConcurrentHashMap<Class<? extends Event>, CopyOnWriteArrayList<Consumer<?
            extends Event>>> listeners = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private EventBus() {
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
        if (!executor.isShutdown()) {
            executor.submit(() -> emit(event));
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
        executor.shutdown();
    }
}
