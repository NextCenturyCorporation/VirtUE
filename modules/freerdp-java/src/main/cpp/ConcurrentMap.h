/*
 * ConcurrentMap.h
 *
 *  Created on: Apr 9, 2018
 *      Author: clong
 */

#ifndef SRC_MAIN_CPP_CONCURRENTMAP_H_
#define SRC_MAIN_CPP_CONCURRENTMAP_H_

#include <map>
#include <mutex>

/**
 * A partial implementation of the std::map interface that is thread safe.
 */
template <typename KEY, typename VAL>
class ConcurrentMap {
public:
    VAL& operator[]( const KEY& key) {
        std::lock_guard<std::mutex> lock(mapLock);
        return map[key];
    }
    typename std::map< KEY, VAL >::size_type erase( const KEY& key ) {
        std::lock_guard<std::mutex> lock(mapLock);
        return map.erase(key);
    }
    std::map< KEY, VAL > clone() const {
        std::lock_guard<std::mutex> lock(mapLock);
        std::map< KEY, VAL > copy(map);
        return copy;
    }
private:
    std::mutex mapLock;
    std::map< KEY, VAL > map;
};


#endif /* SRC_MAIN_CPP_CONCURRENTMAP_H_ */
