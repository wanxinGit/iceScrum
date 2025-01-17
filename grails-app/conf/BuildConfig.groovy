/*
 * Copyright (c) 2015 Kagilum SAS
 *
 * This file is part of iceScrum.
 *
 * iceScrum is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * iceScrum is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with iceScrum.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Authors:
 *
 * Vincent Barrier (vbarrier@kagilum.com)
 * Nicolas Noullet (nnoullet@kagilum.com)
 */

import grails.util.GrailsNameUtils
import grails.util.Environment

grails.servlet.version = "3.0"
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.7
grails.project.source.level = 1.7
grails.project.war.file = "target/${appName}.war"
grails.project.dependency.resolver = "maven"
grails.project.war.osgi.headers = false
grails.tomcat.nio = true

def jvmArgs = ['-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005',
               '-Dicescrum.clean=true',
               '-Dfile.encoding=UTF-8',
               '-Duser.timezone=UTC',
               '-Djavax.net.ssl.trustStore=/Library/Java/Home/lib/security/cacerts']

grails.project.fork = [
        test: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, daemon:true],
        run: [maxMemory: 1024, minMemory: 512, debug: false, maxPerm: 256, forkReserve:false, jvmArgs: jvmArgs],
        war: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false]
]

if (Environment.current != Environment.PRODUCTION) {
    println "use inline plugin in env: ${Environment.current}"
    grails.plugin.location.'icescrum-core' = '../plugins/icescrum-core'
}

grails.war.resources = { stagingDir ->
    copy(todir: "${stagingDir}/WEB-INF/classes/grails-app/i18n") {
        fileset(dir: "grails-app/i18n") {
            include(name: "report*")
        }
    }
}

grails.project.dependency.resolution = {
    inherits("global") {
        excludes "xml-apis", "maven-publisher", "itext"
    }
    log "warn"
    repositories {
        grailsPlugins()
        grailsCentral()
        grailsHome()
        mavenCentral()
        mavenRepo "http://repository.codehaus.org"
        mavenRepo "http://repo.icescrum.org/artifactory/plugins-release/"
        mavenRepo "http://repo.icescrum.org/artifactory/plugins-snapshot/"
    }
    dependencies {
        runtime 'mysql:mysql-connector-java:5.1.36'
        build 'com.lowagie:itext:2.1.7'
    }
    plugins {
        compile ':standalone:1.3'
        compile ':cache-headers:1.1.7'
        compile ':asset-pipeline:2.5.0'
        compile ':less-asset-pipeline:2.1.0' // Cannot upgrade because less4j used in 2.1.1 and 2.2.0 cannot compile code.less (TODC)
        compile ':browser-detection:2.5.0'
        // runtime ':database-migration:1.4.0' TODO enable new migration
        runtime ':hibernate4:4.3.10'
        build   ':tomcat:7.0.55.3'
        compile 'org.icescrum:entry-points:1.0'
        if (Environment.current == Environment.PRODUCTION) {
            compile 'org.icescrum:icescrum-core:1.7-SNAPSHOT'
        }
    }
}

def iceScrumPluginsDir = System.getProperty("icescrum.plugins.dir") ?: false
println "Compile and use icescrum plugins : ${iceScrumPluginsDir ? true : false}"

if (iceScrumPluginsDir) {
    "${iceScrumPluginsDir}".split(";").each {
        File dir = new File(it.toString())
        println "Scanning plugin dir : ${dir.canonicalPath}"

        if (dir.exists()) {
            File descriptor = dir.listFiles(new FilenameFilter() {
                public boolean accept(File file, String s) {
                    return s.endsWith("GrailsPlugin.groovy");
                }
            })[0] ?: null;

            if (descriptor) {
                String name = GrailsNameUtils.getPluginName(descriptor.getName());
                println "found plugin : ${name}"
                grails.plugin.location."${name}" = "${it}"
            }
        } else {
            println "no plugin found in dir"
        }

    }
}