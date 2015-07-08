package net.zomis.stackstv

import org.junit.Test

class ElectionTest {

    @Test
    void test() {
        String fileName = 'stackoverflow-com-2015-election-results.blt'
        Election vote = Election.fromURL(getClass().classLoader.getResource(fileName))
        def result = vote.elect()

        def elected = result.getCandidates(Election.CandidateState.ELECTED)
        assert elected.size() == 3
        assert elected.stream().map({it.name}).toArray() == ['"meagar"', '"Martijn Pieters"', '"Jeremy Banks"']

        result.rounds.each {
            println it
        }
    }

    @Test
    void testExport() {
        String fileName = 'mini.blt'
        Election vote = Election.fromURL(getClass().classLoader.getResource(fileName))
        def result = vote.elect()

        def elected = result.getCandidates(Election.CandidateState.ELECTED)
        assert elected.size() == 3
        assert elected.stream().map({it.name}).toArray() == ['"meagar"', '"Martijn Pieters"', '"Jeremy Banks"']

        result.rounds.each {
            println it
        }
        ResultExport.export(new File(new File('output'), fileName + '.html'), fileName, result)
    }

}