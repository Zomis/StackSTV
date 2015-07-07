package net.zomis.stackstv

import org.junit.Test

class StackSTVTest {

    @Test
    void test() {
        String fileName = 'stackoverflow-com-2015-election-results.blt'
        StackSTV vote = StackSTV.fromURL(getClass().classLoader.getResource(fileName))
        def result = vote.elect()
        result.each { println it }
    }

}