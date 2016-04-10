package com.willkamp.ether;

import rx.Observable;

import java.util.concurrent.TimeUnit;

/**
 * Public API for storing resources in and fetching from the "Ether"
 */
@SuppressWarnings("WeakerAccess")
public class Ether {

    static final EtherImpl __etherImpl = new EtherImpl();

    private Ether() {
    }

    /**
     * Store an object that can be observed for a default (10 seconds) length of time.
     *
     * @param resource the item to store.
     * @return the key to later fetch the item.
     */
    public static String hold(Object resource) {
        return __etherImpl.hold(resource);
    }

    /**
     * Store an object that can be observed for a default (10 seconds) length of time.
     *
     * @param key      the key to to store the item.
     * @param resource the item to store.
     */
    public static void hold(String key, Object resource) {
        __etherImpl.hold(key, resource);
    }

    /**
     * Store an object that can be observed for a specified amount of time.
     *
     * @param resource the item to store.
     * @param seconds  the amount of time in seconds.
     * @return the key to later fetch the item.
     */
    public static String holdFor(Object resource, int seconds) {
        return __etherImpl.holdForTime(resource, TimeUnit.SECONDS, seconds);
    }

    /**
     * Store an object that can be observed for a specified amount of time.
     *
     * @param key      the key to to store the item.
     * @param resource the item to store.
     * @param seconds  the amount of time in seconds.
     */
    public static void holdFor(String key, Object resource, int seconds) {
        __etherImpl.holdForTime(key, resource, TimeUnit.SECONDS, seconds);
    }

    /**
     * Store an object that can be observed until it is observed at least once.
     * Note that the item can only be fetched once as it will be removed once fetched.
     *
     * @param resource the item to store.
     * @return the key to later fetch the item.
     */
    public static String holdUntilObserved(Object resource) {
        return __etherImpl.holdUntilObserved(resource);
    }

    /**
     * Store an object that can be observed until it is observed at least once.
     * Note that the item can only be fetched once as it will be removed once fetched.
     *
     * @param key      the key to store the item.
     * @param resource the item to store.
     */
    public static void holdUntilObserved(String key, Object resource) {
        __etherImpl.holdUntilObserved(key, resource);
    }

    /**
     * Observe a held item with a particular key. A single signal is guaranteed for any currently or future held
     * resource with the specified key.
     *
     * @param key the item's key.
     * @param <T> the item's type.
     * @return an observable that emits an
     */
    public static <T> Observable<T> getResourceObservable(String key) {
        return __etherImpl.observeResourceWithKey(key);
    }

    /**
     * Get a n item with a particular key.
     * @param key the item's key.
     * @param <T> the item's type.
     * @return the item or null if it does not exist.
     */
    public static <T> T getResourceWithKey(String key) {
        return __etherImpl.getResourceWithKey(key);
    }
}
