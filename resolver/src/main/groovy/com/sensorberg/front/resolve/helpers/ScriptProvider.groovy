package com.sensorberg.front.resolve.helpers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct

@Service
class ScriptProvider {

    def static fileNames = [
        layoutLogReportedBackHistory  : "layoutLogReportedBackHistory"
    ]


    @Autowired
    FileReaderService fileReader

    Map<String, String> scripts = [:];

    @PostConstruct
    def init(){
        scripts.put(fileNames.layoutLogReportedBackHistory, fileReader.contentAsString("scripts/layoutLogReportedBackHistory.groovy"))
    }

    /**
     *
     * @param name must be one in @{fileNames}
     * @return the script
     */
    def String getScript(String name){
        scripts.get(name);
    }

    /**
     * method that can be used to test the scripts.
     * @param name
     * @param variables
     * @param whatToReturn
     * @return
     */
    static def runScript(String name, Map<String, Object> variables, Set<String> whatToReturn){
        Binding binding = new Binding(variables);

        String path = ScriptProvider.class.getClassLoader().getResource("scripts").getPath();
        GroovyScriptEngine gse = new GroovyScriptEngine(path);
        gse.run("${name}.groovy", binding);

        def returnValues = [:]
        whatToReturn.each { key -> returnValues.put(key, binding.getVariable(key))}

        println(returnValues);
        return returnValues
    }

}
