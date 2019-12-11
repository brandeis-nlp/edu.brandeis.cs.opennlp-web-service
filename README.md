# Brandeis LAPPS Grid wrapper for Apache OpenNLP
This project is to build Apache OpenNLP wrappers based on LAPPS Grid I/O specification,
namely using LAPPS Interchange Format and LAPPS Vocabulary.
Currently a wrapper as LAPPS web service is available. 
By default (mvn package), the `webservice` module is built into a war artifact.
This version uses OpenNLP ${opennlp.version} internally.


## LICENSE

This project and the original [Apache OpenNLP](https://opennlp.apache.org/) are both developed under the [apache 2 license](LICENSE) and source code for the wrapper is hereby available. 


## For Developers

### parent POM and base artifact

The parent POM used in the project ([`edu.brandeis.lapps.parent-pom`](https://github.com/brandeis-llc/lapps-parent-pom)) and the base java artifact ([`edu.brandeis.lapps.app-scaffolding`](https://github.com/brandeis-llc/lapps-app-scaffolding)) are not available on the maven central. Their source code are available at [`brandeis-llc`](https://github.com/brandeis-llc) github organization, and the maven artifacts are available via [Brandeis LLC nexus repository](http://morbius.cs-i.brandeis.edu:8081/).

### releasing with maven-release-plugin

Don't forget to use `release` profile to include all submodules during the release preparation as well as performing release.

```
mvn release:preprare release:perform -Prelease
```


