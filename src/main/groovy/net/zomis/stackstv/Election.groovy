package net.zomis.stackstv

import groovy.transform.ToString

import java.util.stream.Collectors

class Election {

    final List<Candidate> candidates = new ArrayList<>()
    final List<Vote> votes = new ArrayList<>()
    int availablePositions
    int maxChoices

    private Election(int availablePositions) {
        this.availablePositions = availablePositions
    }

    void addVote(Vote vote) {
        this.votes << vote
        this.maxChoices = Math.max(maxChoices, vote.preferences.length)
    }

    void addCandidate(String name) {
        this.candidates.add(new Candidate(name: name))
    }

    double calculateQuota(double excess) {
        (votes.size() - excess) / (availablePositions + 1)
    }

    static class ElectionResult {
        List<Round> rounds
        List<Candidate> candidateResults

        List<Candidate> getCandidates(CandidateState state) {
            candidateResults.stream()
                .filter({it.state == state})
                .collect(Collectors.toList())
        }
    }

    ElectionResult elect(ElectionStrategy strategy) {
        strategy.elect(this)
    }

    static enum CandidateState {
        HOPEFUL, EXCLUDED, ALMOST, NEWLY_ELECTED, ELECTED
    }

    @ToString(includeNames = true, includePackage = false)
    static class Candidate {
        String name
        double weighting = 1
        double votes
        CandidateState state = CandidateState.HOPEFUL

        Candidate copy() {
            new Candidate(name: name, weighting: weighting, votes: votes, state: state)
        }
    }

    @ToString
    static class Vote {
        int numVotes
        Candidate[] preferences

        static Vote fromLine(String line, Election election) {
            String[] data = line.split()
            Vote vote = new Vote()
            vote.numVotes = data[0] as int
            int candidateVotes = data.length - 2
            vote.preferences = new Candidate[candidateVotes]
            for (int i = 0; i < vote.preferences.length; i++) {
                int candidate = data[i + 1] as int
                if (candidate > 0) {
                    vote.preferences[i] = election.candidates.get(candidate - 1)
                }
            }
            vote
        }

        void distribute(Round round) {
            double remaining = numVotes
            int choiceIndex = 0
            preferences.eachWithIndex { Candidate entry, int i ->
                if (entry) {
                    double myScore = remaining * entry.weighting
                    entry.votes += myScore
                    remaining -= myScore
                    round.usedVotes[choiceIndex++] += myScore
                }
            }
            round.excess += remaining
        }
    }

    static final ElectionResult fromURL(URL url, ElectionStrategy strategy) {
        BufferedReader reader = url.newReader()
        String[] head = reader.readLine().split()
        int candidates = head[0] as int
        Election stv = new Election(head[1] as int)
        for (int i = 0; i < candidates; i++) {
            stv.addCandidate("Candidate $i")
        }

        String line = reader.readLine();
        while (line != '0') {
            Vote vote = Vote.fromLine(line, stv)
            stv.addVote(vote)
            line = reader.readLine();
        }
        for (int i = 0; i < candidates; i++) {
            String name = reader.readLine()
            stv.candidates.get(i).name = name
        }
        stv.elect(strategy)
    }

}
