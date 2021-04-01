# MITREid Connect
---

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.mitre/openid-connect-parent/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.mitre/openid-connect-parent) [![Travis CI](https://travis-ci.org/mitreid-connect/OpenID-Connect-Java-Spring-Server.svg?branch=master)](https://travis-ci.org/mitreid-connect/OpenID-Connect-Java-Spring-Server)  [![Codecov](https://codecov.io/github/mitreid-connect/OpenID-Connect-Java-Spring-Server/coverage.svg?branch=master)](https://codecov.io/github/mitreid-connect/OpenID-Connect-Java-Spring-Server)

This project contains a certified OpenID Connect reference implementation in Java on the Spring platform, including a functioning [server library](openid-connect-server), [deployable server package](openid-connect-server-webapp), [client (RP) library](openid-connect-client), and general [utility libraries](openid-connect-common). The server can be used as an OpenID Connect Identity Provider as well as a general-purpose OAuth 2.0 Authorization Server.

[![OpenID Certified](https://cloud.githubusercontent.com/assets/1454075/7611268/4d19de32-f97b-11e4-895b-31b2455a7ca6.png)](https://openid.net/certification/)

More information about the project can be found:

* [The project homepage on GitHub (with related projects)](https://github.com/mitreid-connect/)
* [Full documentation](https://github.com/mitreid-connect/OpenID-Connect-Java-Spring-Server/wiki)
* [Documentation for the Maven project and Java API](http://mitreid-connect.github.com/)
* [Issue tracker (for bug reports and support requests)](https://github.com/mitreid-connect/OpenID-Connect-Java-Spring-Server/issues)
* The mailing list for the project can be found at `mitreid-connect@mit.edu`, with [archives available online](https://mailman.mit.edu/mailman/listinfo/mitreid-connect).


The authors and key contributors of the project include: 

* [Justin Richer](https://github.com/jricher/)
* [Amanda Anganes](https://github.com/aanganes/)
* [Michael Jett](https://github.com/jumbojett/)
* [Michael Walsh](https://github.com/nemonik/)
* [Steve Moore](https://github.com/srmoore)
* [Mike Derryberry](https://github.com/mtderryberry)
* [William Kim](https://github.com/wikkim)
* [Mark Janssen](https://github.com/praseodym)


Copyright &copy;2017, [MIT Internet Trust Consortium](http://www.trust.mit.edu/). Licensed under the Apache 2.0 license, for details see `LICENSE.txt`. 

## Release Process

Here at Gresham, we use this component for a base for the auth server, our developing branch is 1.3.x and any feature branches should be made off of that branch.

A release build can be invoked by running .circleci/run_release_workflow.sh shell script. It uses CircleCI API to trigger the release workflow and it requires a CIRCLE_TOKEN environment variable with a personal CircleCI API token to be set. Once triggered, the build will bump appropriate versions to release and then proceed to bump them to next snapshot.