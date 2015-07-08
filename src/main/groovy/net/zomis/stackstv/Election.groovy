package net.zomis.stackstv

import groovy.transform.ToString

import java.util.stream.Collectors

class Election {

    private final List<Candidate> candidates = new ArrayList<>()
    private final List<Vote> votes = new ArrayList<>()
    int availablePositions
    private int maxChoices

    Election(int availablePositions) {
        this.availablePositions = availablePositions
    }

    void addVote(Vote vote) {
        this.votes << vote
        this.maxChoices = Math.max(maxChoices, vote.preferences.length)
    }

    void addCandidate(String name) {
        this.candidates.add(new Candidate(name: name))
    }

    double getQuota() {
        double excess = votes.stream().mapToDouble({it.excess}).sum()
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

    ElectionResult elect() {
        List<Round> rounds = new ArrayList<>()

        int electedCount = 0
        int roundsCount = 0
        while (electedCount < availablePositions) {
            Round round = new Round(roundsCount, maxChoices)
            rounds << round
            double roundQuota = quota
            roundsCount++
            round.quota = roundQuota
            candidates*.votes = 0
            votes*.distribute(round)
            List<Candidate> elected = candidates.stream()
                .filter({candidate -> candidate.votes > roundQuota})
                .collect(Collectors.toList())
            elected.each {
                if (it.state != CandidateState.ELECTED) {
                    electedCount++
                }
                it.state = CandidateState.ELECTED
                it.weighting *= roundQuota / it.votes
            }
            if (elected.isEmpty()) {
                Candidate loser = candidates.stream()
                    .filter({it.state == CandidateState.HOPEFUL})
                    .min(Comparator.comparingDouble({it.votes})).get()
                loser.state = CandidateState.EXCLUDED
                loser.weighting = 0
            }
            round.candidates = candidates.collect {it.copy()}
        }
        new ElectionResult(rounds: rounds, candidateResults: candidates)
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

        Candidate copy() {
            new Candidate(name: name, weighting: weighting, votes: votes, state: state)
        }
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

        void distribute(Round round) {
            double remaining = numVotes
//            println "Distributing votes for $this"
            int choiceIndex = 0
            preferences.eachWithIndex { Candidate entry, int i ->
                if (entry) {
                    double myScore = remaining * entry.weighting
                    entry.votes += myScore
                    remaining -= myScore
                    round.usedVotes[choiceIndex++] += myScore
//                    println "$this gives $myScore to ${entry.name}, remaining is now $remaining"
                }
            }
            this.excess = remaining
            round.excess += remaining
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
        int maxChoices = 0
        while (line != '0') {
            Vote vote = Vote.fromLine(line, stv)
            stv.addVote(vote)
            line = reader.readLine();
        }
        for (int i = 0; i < candidates; i++) {
            String name = reader.readLine()
            stv.candidates.get(i).name = name
        }
        stv
    }

}
