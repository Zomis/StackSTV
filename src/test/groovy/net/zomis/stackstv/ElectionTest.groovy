package net.zomis.stackstv

import net.zomis.stackstv.export.ResultExport
import org.junit.Test

class ElectionTest {

    private static final ElectionStrategy STRATEGY = new SimonElection()

    ElectionResult runElection(String fileName) {
        ElectionResult result = Election.fromURL(getClass().classLoader.getResource(fileName), STRATEGY)
        result
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

    @Test
    void testCR() {
        String fileName = 'codereview-stackexchange-com-2015-election-results.blt'
        def result = runElection(fileName)

        def elected = result.getCandidates(Election.CandidateState.ELECTED)
        assert elected.size() == 4
        ResultExport.export(new File(new File('output'), fileName + '.html'), fileName, result)
//        assert elected.stream().map({it.name}).toArray() == ['"meagar"', '"Martijn Pieters"', '"Jeremy Banks"']
    }

}
