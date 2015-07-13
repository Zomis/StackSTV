package net.zomis.stackstv

import groovy.transform.ToString

@ToString
class Round {

    int round
    List<Election.Candidate> candidates = new ArrayList<>()
    double quota
    double[] usedVotes
    double excess

    Round(int round, int maxChoices) {
        this.round = round
        this.usedVotes = new double[maxChoices]
    }

}
