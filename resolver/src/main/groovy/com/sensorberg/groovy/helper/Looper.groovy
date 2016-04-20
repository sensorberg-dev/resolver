package com.sensorberg.groovy.helper

/**
 * Created by falkorichter on 18/04/16. found at http://stackoverflow.com/questions/20921546/elegant-way-for-do-while-in-groovy
 */
class Looper {
    private Closure code

    static Looper loop( Closure code ) {
        new Looper(code:code)
    }

    void until( Closure test ) {
        code()
        while (!test()) {
            code()
        }
    }
}
