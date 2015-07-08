package net.zomis.stackstv

import org.junit.Test

class ElectionTest {

    Election.ElectionResult runElection(String fileName) {
        Election vote = Election.fromURL(getClass().classLoader.getResource(fileName))
        vote.elect()
    }

    @Test
    void test() {
        String fileName = 'stackoverflow-com-2015-election-results.blt'
        def result = runElection(fileName)

        def elected = result.getCandidates(Election.CandidateState.ELECTED)
        assert elected.size() == 3
        assert elected.stream().map({it.name}).toArray() == ['"meagar"', '"Martijn Pieters"', '"Jeremy Banks"']
        ResultExport.export(new File(new File('output'), fileName + '.html'), fileName, result)
    }

    @Test
    void testExport() {
        String fileName = 'mini.blt'
        def result = runElection(fileName)
        ResultExport.export(new File(new File('output'), fileName + '.html'), fileName, result)
    }

}