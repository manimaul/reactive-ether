package com.willkamp.ether;

/**
 * Internal resource data store.
 */
class Resource<T> {
    final String _key;
    final T _resource;
    final RetainPolicy _retainPolicy;

    Resource(String key, T resource, RetainPolicy retainPolicy) {
        _key = key;
        _resource = resource;
        _retainPolicy = retainPolicy;
    }
}
