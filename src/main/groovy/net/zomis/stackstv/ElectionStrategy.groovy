package net.zomis.stackstv

interface ElectionStrategy {

    Election.ElectionResult elect(Election election)

}