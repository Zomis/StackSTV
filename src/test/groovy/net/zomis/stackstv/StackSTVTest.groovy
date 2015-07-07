package net.zomis.stackstv

import org.junit.Test

class StackSTVTest {

    @Test
    void test() {
        StackSTV vote = StackSTV.fromURL(getClass().classLoader.getResource('votes.dat'))
        println vote.getAvailablePositions()
        println vote.getCandidates()
        println vote.quota
    }

}