package net.zomis.stackstv

import groovy.transform.ToString

import java.util.stream.Collectors

class StackSTV {

    private final List<Candidate> candidates = new ArrayList<>()
    private final List<Vote> votes = new ArrayList<>()
    final int availablePositions

    StackSTV(int availablePositions) {
        this.availablePositions = availablePositions
    }

    void addCandidate(String name) {
        this.candidates.add(new Candidate(name: name))
    }

    List<String> getCandidates() {
        new ArrayList<>(candidates)
    }

    double getQuota() {
        double excess = votes.stream().mapToDouble({it.excess}).sum()
        (votes.size() - excess) / (availablePositions + 1)
    }

    Candidate[] elect() {
        int electedCount = 0
        while (electedCount < availablePositions) {
            votes.each {
                it.distribute(candidates)
            }
            List<Candidate> elected = candidates.stream()
                .filter({it.state == CandidateState.HOPEFUL})
                .filter({candidate -> candidate.votes >= quota})
                .collect(Collectors.toList())
            elected.each {
                electedCount++
                it.state = CandidateState.ELECTED
                it.weighting *= quota / it.votes
                println "$it got elected!"
            }
            if (elected.isEmpty()) {
                Candidate loser = candidates.stream()
                    .filter({it.state == CandidateState.HOPEFUL})
                    .min(Comparator.comparingDouble({it.votes})).get()
                println "$loser is out of the race"
                loser.state = CandidateState.EXCLUDED
            }
            break
        }
        candidates
    }

    static enum CandidateState {
        HOPEFUL, EXCLUDED, ELECTED
    }

    @ToString(includeNames = true, includePackage = false)
    static class Candidate {
        String name
        double weighting = 1
        double votes
        CandidateState state = CandidateState.HOPEFUL
    }

    @ToString
    static class Vote {
        int numVotes
        int[] candidates
        double [] distribution
        double excess

        static Vote fromLine(String line) {
            String[] data = line.split()
            Vote vote = new Vote()
            vote.numVotes = data[0] as int
            int candidateVotes = data.length - 2
            vote.candidates = new int[candidateVotes]
            for (int i = 0; i < vote.candidates.length; i++) {
                vote.candidates[i] = data[i + 1] as int
            }
            println vote.candidates
            vote
        }

        void distribute(List<Candidate> candidateScores) {
            int votingCandidate = candidates[0]
            candidateScores.get(votingCandidate).votes += numVotes
        }
    }

    static final StackSTV fromURL(URL url) {
        BufferedReader reader = url.newReader()
        String[] head = reader.readLine().split()
        int candidates = head[0] as int
        println candidates
        StackSTV stv = new StackSTV(head[1] as int)

        String line = reader.readLine();
        while (line != '0') {
            Vote vote = Vote.fromLine(line)
            stv.votes << vote
            line = reader.readLine();
        }
        for (int i = 0; i < candidates; i++) {
            String name = reader.readLine()
            println name
            stv.addCandidate(name)
        }
        stv
    }

}
