package net.zomis.stackstv

import groovy.transform.ToString

import net.zomis.meta.IteratorCategory


class Election {

    final List<Candidate> candidates = new ArrayList<>()
    final List<Vote> votes = new ArrayList<>()
    int availablePositions
    int maxChoices

    private Election(int availablePositions) {
        this.availablePositions = availablePositions
    }

    Election leftShift(Vote vote) {
        this.votes << vote
        this.maxChoices = Math.max(maxChoices, vote.preferences.size())
        
        return this
    }

    Election leftShift(Candidate candidate) {
        this.candidates << candidate

        return this
    }

    double calculateQuota(double excess) {
        (votes.size() - excess) / (availablePositions + 1)
    }

    ElectionResult elect(ElectionStrategy strategy) {
        strategy.elect(this)
    }

    static enum CandidateState {
        HOPEFUL, EXCLUDED, ALMOST, NEWLY_ELECTED, ELECTED
    }

       
    static final ElectionResult fromURL(URL url, ElectionStrategy strategy) {
        def reader = url.newReader()

        reader.withReader {
            def (candidates, positions) = reader
                .readLine()
                .split()
                .collect { it as int }

            def stv = new Election(positions)

            candidates.times {
                /* Use a temporary name at first. 
                 * Real names are at the end of the file
                 */
                stv << new Candidate(name: "Candidate $it")
            }

            use(IteratorCategory) {
                reader.iterator().while { line -> line != '0' }.call { line ->
                    stv << Vote.fromLine(line, stv)
                }.upto(candidates) { line, i -> 
                    stv.candidates.get(i).name = line 
                }
            }

            return stv
        }.elect(strategy)
    }

}

class ElectionResult {
    List<Round> rounds
    List<Candidate> candidateResults

    List<Candidate> getCandidates(Election.CandidateState state) {
        candidateResults.findAll {it.state == state}
    }
}

@ToString
class Vote {
    int numVotes
    List<Candidate> preferences

    static Vote fromLine(String line, Election election) {
        def data = line.split().collect { it as int }
        def candidates = data[1..-2]

        new Vote(
            numVotes: data.head(),
            preferences: candidates.collect { election.candidates[it - 1] }        )
    }

    void distribute(Round round) {
        double remaining = numVotes

        preferences.collect {candidate ->
            def score = remaining * candidate.weighting
            candidate.votes += score
            remaining -= score

            return score
        }.eachWithIndex {score, i ->
            round.usedVotes[i] += score
        }

        round.excess += remaining
    }
}

@ToString(includeNames = true, includePackage = false)
class Candidate implements Cloneable {
    String name
    double weighting = 1
    double votes
    Election.CandidateState state = Election.CandidateState.HOPEFUL

    Object clone() { super.clone() }
}
