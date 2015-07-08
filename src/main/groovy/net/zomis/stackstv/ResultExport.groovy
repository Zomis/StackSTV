package net.zomis.stackstv

import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import net.zomis.stackstv.Election.ElectionResult

/**
 * A class for exporting a ElectionResult to HTML, JavaScript and flot
 */
class ResultExport {

    static void export(File file, String name, ElectionResult results) {
        TemplateConfiguration config = new TemplateConfiguration()
        config.useDoubleQuotes = true
        MarkupTemplateEngine engine = new MarkupTemplateEngine()
        def template = engine.createTemplate(ResultExport.classLoader.getResource('flot.groovy'))
        Map<String, Object> model = new HashMap<>()
        model.put('title', name)
        model.put('rounds', results.rounds)
        model.put('candidates', results.candidateResults)
        template.make(model).writeTo(new PrintWriter(file))
    }

}
