package com.sensorberg.front.resolve.resources.index.domain

/**
 * synchronization log item entry
 */
class SynchronizationLogItem {

    String environment
    long tillVersionId
    Date synchronizationDate
    boolean status
    String statusDetails
    long changedItems
}
