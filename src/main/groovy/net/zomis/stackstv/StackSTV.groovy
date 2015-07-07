package net.zomis.stackstv

import groovy.transform.PackageScope
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

    double getQuota() {
        double excess = votes.stream().mapToDouble({it.excess}).sum()
        (votes.size() - excess) / (availablePositions + 1)
    }

    List<Vote> getFinalVotes() {
        new ArrayList<Vote>(votes)
    }

    Candidate[] elect() {
        votes*.initPreferences(candidates)
        int electedCount = 0
        int round = 0
        while (electedCount < availablePositions) {
            double roundQuota = quota
//            println "Round $round quota is $roundQuota"
            round++
            candidates*.votes = 0
            votes*.distribute()
            List<Candidate> elected = candidates.stream()
                .filter({candidate -> candidate.votes > roundQuota})
                .collect(Collectors.toList())
            elected.each {
                if (it.state != CandidateState.ELECTED) {
                    electedCount++
                }
                it.state = CandidateState.ELECTED
                it.weighting *= roundQuota / it.votes
//                println "$it got more than the quota!"
            }
            if (elected.isEmpty()) {
                Candidate loser = candidates.stream()
                    .filter({it.state == CandidateState.HOPEFUL})
                    .min(Comparator.comparingDouble({it.votes})).get()
//                println "$loser is out of the race"
                loser.state = CandidateState.EXCLUDED
                loser.weighting = 0
            }
//            println "Round Result: $candidates"
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
        Candidate[] preferences
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
            vote
        }

        void distribute() {
            float remaining = numVotes
//            println "Distributing votes for $this"
            preferences.eachWithIndex { Candidate entry, int i ->
                if (entry) {
                    float myScore = remaining * entry.weighting
                    entry.votes += myScore
                    remaining -= myScore
//                    println "$this gives $myScore to ${entry.name}, remaining is now $remaining"
                }
            }
            this.excess = remaining
        }

        @PackageScope void initPreferences(List<Candidate> nominees) {
            this.preferences = new Candidate[candidates.length]
            candidates.eachWithIndex { int entry, int i ->
                if (entry > 0) {
                    preferences[i] = nominees.get(entry - 1)
                }
            }
        }
    }

    static final StackSTV fromURL(URL url) {
        BufferedReader reader = url.newReader()
        String[] head = reader.readLine().split()
        int candidates = head[0] as int
        StackSTV stv = new StackSTV(head[1] as int)

        String line = reader.readLine();
        while (line != '0') {
            Vote vote = Vote.fromLine(line)
            stv.votes << vote
            line = reader.readLine();
        }
        for (int i = 0; i < candidates; i++) {
            String name = reader.readLine()
            stv.addCandidate(name)
        }
        stv
    }

}
