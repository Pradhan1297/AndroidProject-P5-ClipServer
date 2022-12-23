// MusicPlayerInterface.aidl
package com.example.clipservercommon;

// Declare any non-default types here with import statements

interface MusicPlayerInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void play(int clipNumber);
    void pause();
    void stop();
    void resume();
     void stopClipService();
}