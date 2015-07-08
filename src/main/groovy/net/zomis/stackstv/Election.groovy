package net.zomis.stackstv

import groovy.transform.ToString

import java.util.stream.Collectors

class Election {

    private final List<Candidate> candidates = new ArrayList<>()
    private final List<Vote> votes = new ArrayList<>()
    final int availablePositions

    Election(int availablePositions) {
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

    List<Candidate> getCandidates(CandidateState state) {
        candidates.stream()
            .filter({it.state == state})
            .collect(Collectors.toList())
    }

    Candidate[] elect() {
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
        Candidate[] preferences
        double excess

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
    }

    static final Election fromURL(URL url) {
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
            stv.votes << vote
            line = reader.readLine();
        }
        for (int i = 0; i < candidates; i++) {
            String name = reader.readLine()
            stv.candidates.get(i).name = name
        }
        stv
    }

}
