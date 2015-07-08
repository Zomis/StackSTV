package net.zomis.stackstv

import groovy.json.JsonBuilder
import groovy.text.markup.BaseTemplate
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

abstract class MyTemplate extends BaseTemplate {
    private final Election.ElectionResult result
    public MyTemplate(
            final MarkupTemplateEngine templateEngine,
            final Map model,
            final Map<String, String> modelTypes,
            final TemplateConfiguration configuration) {
        super(templateEngine, model, modelTypes, configuration)
        result = model.get('result')
        assert result : 'No result provided'
    }

    String getCandidateJSON() {
        def json = new JsonBuilder()
        def res = result
        json {
            res.candidateResults.eachWithIndex { Election.Candidate candidate, int i ->
                "candidate-$i" {
                    label candidate.name
                    def array = new double[res.rounds.size()][2]
                    res.rounds.each { Round round ->
                        array[round.round][0] = round.round
                        array[round.round][1] = round.candidates.get(i).votes
                    }
                    data array
                }
            }
        }
        json.toPrettyString()
    }

}