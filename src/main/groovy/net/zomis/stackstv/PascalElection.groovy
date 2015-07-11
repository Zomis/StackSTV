package net.zomis.stackstv

/**
 * Algorithm originally written in Pascal at http://www.dia.govt.nz/diawebsite.NSF/Files/meekm/%24file/meekm.pdf
 */
class PascalElection implements ElectionStrategy {

    @Override
    Election.ElectionResult elect(Election election) {
        List<Round> rounds = []
        int numElected = 0
        int roundId = 0
        while (numElected < election.availablePositions) {
            Round round = new Round(roundId++, election.maxChoices)
            rounds << round
//                {Count votes and elect candidates, transferring
//                    surpluses until no more can be done or all
//                    seats are filled}
            while (true) {
                List<Election.Candidate> elected = electSomeone(election, numElected)
                numElected += elected.size()
                if (elected.isEmpty() || (numElected >= election.availablePositions)) {
                    // do-while doesn't work in Groovy, so using this method instead
                    break;
                }
            }

            if (numElected < election.availablePositions) {
                Optional<Election.Candidate> loser = election.candidates.stream()
                        .filter({it.state == Election.CandidateState.HOPEFUL})
                        .min(Comparator.comparingDouble({it.votes}))
                if (loser.isPresent()) {
                    loser.get().state = Election.CandidateState.EXCLUDED
                    loser.get().weighting = 0
                } else {
                    println "Warning: No loser present. $numElected / ${election.availablePositions}. Candidates ${election.candidates}"
                }
            }
            round.candidates = election.candidates.collect {it.copy()}
//            if (candidates.size() - excludedCount == availablePositions) {
//                candidates.each {
//                    if (it.state == CandidateState.HOPEFUL) {
//                        it.state = CandidateState.ELECTED;
//                    }
//                }
//            }
        }
        new Election.ElectionResult(candidateResults: election.candidates, rounds: rounds)
    }

    List<Election.Candidate> electSomeone(Election election, int numElected) {
        boolean converged = false
        double quota = 0
        double excess = 0
        boolean ended = false
        double droop = 1.0 / (election.availablePositions + 1)
        double votesCount = election.votes.stream().mapToInt({it.numVotes}).sum()
        int count = 0

        while (!converged) {
            excess = 0
            election.candidates*.votes = 0

            election.votes.each {
                double value = it.numVotes
                int candIndex = 0
                ended = false
                while (candIndex < it.preferences.length) {
                    Election.Candidate candidate = it.preferences[candIndex]
                    if (candidate) {
                        if (!ended && candidate.weighting > 0.0) {
                            ended = candidate.state == Election.CandidateState.HOPEFUL
                            if (ended) {
                                candidate.votes += value
                                value = 0.0
                            } else {
                                candidate.votes += value * candidate.weighting
                                value = value * (1.0 - candidate.weighting)
                            }
                        }
                    }
                    ++candIndex
                }
                excess = excess + value
            }

            quota = (votesCount - excess) * droop

            if (quota < 0.0001) {
                quota = 0.0001
            }
            converged = true

            election.candidates.each { Election.Candidate candidate ->
                if (candidate.state == Election.CandidateState.ELECTED) {
                    double temp = (double) quota / candidate.votes
                    if (temp > 1.00001 || temp < 0.99999) {
                        converged = false
                    }
                    temp = candidate.weighting * temp
                    candidate.weighting = temp

                    if (temp > 1.0) {
                        candidate.weighting = 1
                    }
                }
            }

//            iteration++
        }

        election.candidates.each {
            if (it.state == Election.CandidateState.HOPEFUL && it.votes >= quota) {
                it.state = Election.CandidateState.ALMOST
                count++
            }
        }

        while (numElected + count > election.availablePositions) {
            election.candidates.each {
                if (it.state == Election.CandidateState.HOPEFUL) {
                    it.state = Election.CandidateState.EXCLUDED
                }
            }
            count--
        }

        List<Election.Candidate> newlyElected = []
        election.candidates.each {
            if (it.state == Election.CandidateState.ALMOST) {
                it.state = Election.CandidateState.NEWLY_ELECTED
                newlyElected << it
            }
        }

        election.candidates.each {
            if (it.state == Election.CandidateState.NEWLY_ELECTED) {
                if (numElected < election.availablePositions) {
                    it.weighting = quota / it.votes
                }
                it.state = Election.CandidateState.ELECTED
            }
        }
        return newlyElected
    }


}
