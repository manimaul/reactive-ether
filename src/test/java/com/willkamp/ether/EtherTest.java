package com.willkamp.ether;

import org.junit.Before;
import org.junit.Test;
import rx.Observable;
import rx.Observer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class EtherTest {

    @Before
    public void setUp() throws Exception {
        Ether.__etherImpl.flush();
    }

    @Test
    public void hold() throws Exception {
        // given an object to hold
        TestResource resource = new TestResource("my thing");

        // when we store the object in the ether (for the default time 10seconds)
        String key = Ether.hold(resource);

        // then before the time has expired the resource can be observed
        waitForTime(9);
        Observable<TestResource> observable = Ether.getResourceObservable(key);
        TestResource observed = observable.timeout(1, TimeUnit.SECONDS).toBlocking().first();
        assertNotNull(observed);

        // and the observed resource is the same as the one stored
        assertSame(resource, observed);

        // when the default time has expired
        waitForTime(2);

        // then the object can no longer be observed / is no longer stored
        EtherImpl impl = Ether.__etherImpl;
        assertTrue(impl._itemStore.isEmpty());

        // then the subscription completed
        assertTrue(impl._subscriberHashMap.isEmpty());
    }

    @Test
    public void hold_withKey() throws Exception {
        // given a key to hold an object
        String key = "some_key";

        // and an object to hold
        TestResource resource = new TestResource("some resource");

        // when we store the object in the ether (for the default time 10seconds)
        Ether.hold(key, resource);

        // then before the time has expired the resource can be observed
        waitForTime(9);

        // given multiple observers
        for (int i = 0; i < 100; i++) {
            // then before the time has expired the resource can be observed
            Observable<TestResource> observable = Ether.getResourceObservable(key);
            TestResource observed = observable.timeout(1, TimeUnit.SECONDS).toBlocking().first();
            assertNotNull(observed);

            // and the observed resource is the same as the one stored
            assertSame(resource, observed);
        }

        // when the default time has expired
        waitForTime(2);

        // then the object can no longer be observed / is no longer stored
        EtherImpl impl = Ether.__etherImpl;
        assertTrue(impl._itemStore.isEmpty());

        // then the subscription completed
        assertTrue(impl._subscriberHashMap.isEmpty());
    }

    @Test
    public void hold_withSameKey_OverlappingTimeFrame() throws Exception {
        // given a key to hold an object
        String key = "some_key";

        // and an object to hold
        TestResource resource = new TestResource("some resource");
        TestResource resource2 = new TestResource("some resource");

        // given we store the object in the ether
        Ether.hold(key, resource);

        // given some elapsed time before the first hold expires
        waitForTime(5);

        // given we store another object in the ether using the same key
        Ether.hold(key, resource2);

        // given the time has expired on the first hold
        waitForTime(6);

        // then before the time has expired the second resource can be observed
        Observable<TestResource> observable = Ether.getResourceObservable(key);
        TestResource observed = observable.timeout(1, TimeUnit.SECONDS).toBlocking().first();
        assertNotNull(observed);

        // and the observed resource is the same as the one stored
        assertSame(resource2, observed);

        // when the default time has expired
        waitForTime(5);

        // then the object can no longer be observed / is no longer stored
        EtherImpl impl = Ether.__etherImpl;
        assertTrue(impl._itemStore.isEmpty());

        // then the subscription completed
        assertTrue(impl._subscriberHashMap.isEmpty());
    }

    @Test
    public void holdForTime_ObserveFirst() throws Exception {
        // given some key
        String key = "some_key";

        // when an item is observed
        Observable<TestResource> observable = Ether.getResourceObservable(key);
        SignalResult signalResult = whenAnItemIsObserved(observable, 1);

        // given an object to hold
        TestResource resource = new TestResource("my thing");

        // when we store the object in the ether for some time
        Ether.holdFor(key, resource, 5);

        // given 3 seconds passes
        waitForTime(3);

        // then the item will be retrieved
        assertNotNull(signalResult._resource);
        assertNull(signalResult._error);
        assertSame(signalResult._resource, resource);

        // given some amount of time that is cumulatively over the hold time
        waitForTime(3);

        // when an item is observed
        observable = Ether.getResourceObservable(key);
        SignalResult signalResult2 = whenAnItemIsObserved(observable, 1);

        // then the item will be NOT be received
        assertNull(signalResult2._resource);
        assertNull(signalResult2._error);
    }

    @Test
    public void hold_withSameKey() throws Exception {
        // given a key to hold an object
        String key = "some_key";

        // and an object to hold
        TestResource resource = new TestResource("some resource");

        // given we store an object in the ether
        Ether.hold(key, resource);

        // when an item is observed
        Observable<TestResource> observable = Ether.getResourceObservable(key);
        SignalResult signalResult = whenAnItemIsObserved(observable, 1);

        // then the item will be retrieved
        assertNotNull(signalResult._resource);
        assertNull(signalResult._error);
        assertSame(signalResult._resource, resource);

        // given we store another object in the ether with the same key
        TestResource resource2 = new TestResource("some other resource");
        Ether.hold(key, resource2);

        // when an item is observed
        observable = Ether.getResourceObservable(key);
        SignalResult signalResult2 = whenAnItemIsObserved(observable, 1);

        // then the second item will be received
        assertNotNull(signalResult2._resource);
        assertNull(signalResult2._error);
        assertSame(signalResult2._resource, resource2);
    }

    @Test
    public void holdForTime() throws Exception {
        // given an object to hold
        TestResource resource = new TestResource("my thing");

        // when we store the object in the ether for some time
        String key = Ether.holdFor(resource, 5);

        // given 3 seconds passes
        waitForTime(3);

        // when an item is observed
        Observable<TestResource> observable = Ether.getResourceObservable(key);
        SignalResult signalResult = whenAnItemIsObserved(observable, 1);

        // then the item will be retrieved
        assertNotNull(signalResult._resource);
        assertNull(signalResult._error);
        assertSame(signalResult._resource, resource);

        // given some amount of time that is cumulatively over the hold time
        waitForTime(3);

        // when an item is observed
        observable = Ether.getResourceObservable(key);
        SignalResult signalResult2 = whenAnItemIsObserved(observable, 1);

        // then the item will be NOT be received
        assertNull(signalResult2._resource);
        assertNull(signalResult2._error);
    }

    @Test
    public void holdForTime_withKey() throws Exception {
        // given some key
        String key = "some_key";

        // given an object to hold
        TestResource resource = new TestResource("my thing");

        // when we store the object in the ether for 5 seconds
        Ether.holdFor(key, resource, 5);

        // given 3 seconds passes
        waitForTime(3);

        // when an item is observed
        Observable<TestResource> observable = Ether.getResourceObservable(key);
        SignalResult signalResult = whenAnItemIsObserved(observable, 1);

        // then the item will be retrieved
        assertNotNull(signalResult._resource);
        assertNull(signalResult._error);
        assertSame(signalResult._resource, resource);

        // given some amount of time that is cumulatively over the hold time
        waitForTime(3);

        // when an item is observed
        observable = Ether.getResourceObservable(key);
        SignalResult signalResult2 = whenAnItemIsObserved(observable, 1);

        // then the item will be NOT be received
        assertNull(signalResult2._resource);
        assertNull(signalResult2._error);
    }

    @Test
    public void holdUntilObserved() throws Exception {
        // given an object to hold
        TestResource resource = new TestResource("my thing");

        // when we store the object in the ether for some time
        String key = Ether.holdUntilObserved(resource);

        // when an item is observed
        Observable<TestResource> observable = Ether.getResourceObservable(key);
        SignalResult signalResult = whenAnItemIsObserved(observable, 1);

        // then the item will be retrieved
        assertNotNull(signalResult._resource);
        assertNull(signalResult._error);
        assertSame(signalResult._resource, resource);

        // then the item cannot be retrieved a second time
        EtherImpl impl = Ether.__etherImpl;
        assertTrue(impl._itemStore.isEmpty());

        // then the subscription completed
        assertTrue(signalResult._completed);
        assertTrue(impl._subscriberHashMap.isEmpty());
    }

    @Test
    public void holdUntilObserved_withKey() throws Exception {
        // given some key
        String key = "some_key";

        // given an object to hold
        TestResource resource = new TestResource("my thing");

        // when we store the object in the ether for some time
        Ether.holdUntilObserved(key, resource);

        // when an item is observed
        Observable<TestResource> observable = Ether.getResourceObservable(key);
        SignalResult signalResult = whenAnItemIsObserved(observable, 1);

        // then the item will be retrieved
        assertNotNull(signalResult._resource);
        assertNull(signalResult._error);
        assertSame(signalResult._resource, resource);

        // then the item cannot be retrieved a second time
        EtherImpl impl = Ether.__etherImpl;
        assertTrue(impl._itemStore.isEmpty());

        // then the subscription completed
        assertTrue(signalResult._completed);
        assertTrue(impl._subscriberHashMap.isEmpty());
    }

    @Test
    public void holdUntilObserved_withKey_ObserveFirst() throws Exception {
        // given some key
        String key = "some_key";

        // when an item is observed
        Observable<TestResource> observable = Ether.getResourceObservable(key);
        SignalResult signalResult = whenAnItemIsObserved(observable, 1);

        // given an object to hold
        TestResource resource = new TestResource("my thing");

        // when we store the object in the ether for some time
        Ether.holdUntilObserved(key, resource);

        // then the item will be retrieved
        assertNotNull(signalResult._resource);
        assertNull(signalResult._error);
        assertSame(signalResult._resource, resource);

        // then the item cannot be retrieved a second time
        EtherImpl impl = Ether.__etherImpl;
        assertTrue(impl._itemStore.isEmpty());

        // then the subscription completed
        assertTrue(signalResult._completed);
        assertTrue(impl._subscriberHashMap.isEmpty());
    }

    private SignalResult whenAnItemIsObserved(Observable<TestResource> observable, int timeoutSeconds) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final SignalResult signalResult = new SignalResult();
        observable.subscribe(new Observer<TestResource>() {
            @Override
            public void onCompleted() {
                latch.countDown();
                signalResult._completed = true;
            }

            @Override
            public void onError(Throwable e) {
                signalResult._error = e;
                latch.countDown();
            }

            @Override
            public void onNext(TestResource testResource) {
                signalResult._resource = testResource;
                latch.countDown();
            }
        });
        latch.await(timeoutSeconds, TimeUnit.SECONDS);
        return signalResult;
    }

    private void waitForTime(int seconds) {
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(seconds));
        } catch (InterruptedException e) {
            e.printStackTrace();
            assertTrue("wait failed", false);
        }
    }

    private static class SignalResult {
        TestResource _resource;
        boolean _completed = false;
        Throwable _error;
    }

}