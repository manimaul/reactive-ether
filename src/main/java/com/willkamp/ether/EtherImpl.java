package com.willkamp.ether;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Func1;

import java.util.*;
import java.util.concurrent.TimeUnit;

class EtherImpl {

    private static final int DEFAULT_RETAIN_TIME_SECONDS = 10;
    HashMap<String, Resource> _itemStore = new HashMap<>();
    HashMap<String, List<Subscriber<? super Resource>>> _subscriberHashMap = new HashMap<>();

    EtherImpl() {
    }

    private synchronized void removeItem(String key) {
        _itemStore.remove(key);
        _subscriberHashMap.remove(key);
    }

    synchronized void flush() {
        _itemStore.clear();
        _subscriberHashMap.clear();
    }

    private synchronized Resource addItem(String key, Object item, RetainPolicy policy) {
        Resource<Object> resource = new Resource<>(key, item, policy);
        boolean storeItem = true;
        List<Subscriber<? super Resource>> subscribers = _subscriberHashMap.get(key);
        if (subscribers != null) {
            Iterator<Subscriber<? super Resource>> iterator = subscribers.iterator();
            while (iterator.hasNext()) {
                Subscriber<? super Resource> subscriber = iterator.next();
                if (!subscriber.isUnsubscribed()) {
                    if (policy == RetainPolicy.ONCE_OBSERVED) {
                        storeItem = false;
                    }
                    subscriber.onNext(resource);
                    subscriber.onCompleted();
                }
                iterator.remove();
            }
            _subscriberHashMap.remove(key);
        }
        if (storeItem) {
            _itemStore.put(key, resource);
        }

        return resource;
    }

    private synchronized Resource getItem(String key) {
        Resource resource = _itemStore.get(key);
        if (resource != null && resource._retainPolicy == RetainPolicy.ONCE_OBSERVED) {
            removeItem(key);
        }
        return resource;
    }

    private synchronized void addSubscriber(String key, Subscriber<? super Resource> subscriber) {
        List<Subscriber<? super  Resource>> subscriberList = _subscriberHashMap.get(key);
        if (subscriberList == null) {
            subscriberList = new ArrayList<>();
            _subscriberHashMap.put(key, subscriberList);
        }
        subscriberList.add(subscriber);
    }

    private void scheduleRemoval(final Resource resource, TimeUnit timeUnit, long time) {
        Observable.timer(time, timeUnit).subscribe(new Observer<Long>() {
            public void onCompleted() {
                synchronized (EtherImpl.this) {
                    Resource store = _itemStore.get(resource._key);
                    if (store == resource) {
                        removeItem(store._key);
                    }
                }
            }

            public void onError(Throwable e) {
            }

            public void onNext(Long ignored) {
            }
        });
    }

    //endregion

    String holdForTime(Object item, TimeUnit timeUnit, long time) {
        String key = createUniqueKey();
        holdForTime(key, item, timeUnit, time);
        return key;
    }

    void holdForTime(final String key, Object item, TimeUnit timeUnit, long time) {
        scheduleRemoval(addItem(key, item, RetainPolicy.TIMED), timeUnit, time);
    }

    String hold(Object item) {
        String key = createUniqueKey();
        hold(key, item);
        return key;
    }

    void hold(String key, Object item) {
        holdForTime(key, item, TimeUnit.SECONDS, DEFAULT_RETAIN_TIME_SECONDS);
    }

    String holdUntilObserved(Object item) {
        String key = createUniqueKey();
        holdUntilObserved(key, item);
        return key;
    }

    void holdUntilObserved(String key, Object item) {
        addItem(key, item, RetainPolicy.ONCE_OBSERVED);
    }

    <T> Observable<T> observeResourceWithKey(final String key) {
        return Observable.create(new Observable.OnSubscribe<Resource>() {
            @Override
            public void call(Subscriber<? super Resource> subscriber) {
                Resource resource = getItem(key);
                if (resource != null) {
                    subscriber.onNext(resource);
                    subscriber.onCompleted();
                } else {
                    addSubscriber(key, subscriber);
                }
            }
        }).map(new Func1<Resource, T>() {
            @Override
            public T call(Resource resource) {
                //noinspection unchecked
                return (T) resource._resource;
            }
        });
    }

    <T> T getResourceWithKey(String key) {
        //noinspection unchecked
        return (T) getItem(key)._resource;
    }

    String createUniqueKey() {
        return UUID.randomUUID().toString();
    }
}
