
# Learn Guardrail

Hello, this is a sample project made with Twilio Guardrail. Read along to understand whats going on, or just compile the project and play with the source code.
What to know
To have a fully-working project using guardrail you need at least 5 parts. In this repo, they are all in individual files, but if you wanted to be compact you could combine a few of them.


1. A swagger spec file to describe your server. server.yaml
2. A Controller to implement your business logic and connect to the generated Handler file. ApiHandler.scala
3. A Bootstrap file to start up akka-http and connect your controller. App.scala
4. A Build file to compile your project and create a fat jar. maven.xml
5. Some unit tests to make sure your server is correct. Scalatest.scala


# Swagger Spec file

Guardrail uses a standard Swagger/OpenAPI spec for building either Servers or Clients. 
[Our example](https://github.com/nickfun/learning-guardrail/blob/master/server.yaml) builds a Server. The goal of our server is to be a “Todo Backend” since that has full functions over a very small surface area.


The [online swagger](https://editor.swagger.io/) editor can be used to examine and manipulate the spec file.

# Build File

Once you have a spec file that describes your server, its time to bring in Guardrail. 
Our [build file](https://github.com/nickfun/learning-guardrail/blob/master/pom.xml) 
declares Guardrail as a dependency when we use it as a Maven plugin. Note that we still also declare Akka, AkkaHttp, and Circe as dependencies too. You are in control of the version of these in your project. Using the option generate-app-server is how we declare to build a Server instead of a Client. You can generate multiple servers and multiple clients in one maven build, our example only needs the one.


Note that the maven-shade-plugin plugin is used to build the fat jar, and has an option to preserve the reference.conf config file. This is preserving the default conf file for AkkaHttp. This is also where the Main Class is defined for our fat jar


# Controller Logic
With a swagger file and a build file we can actually invoke Guardrail to run and generate code for us. Guardrail will generate a Handler interface that we can code against. 
In [our controller](https://github.com/nickfun/learning-guardrail/blob/master/src/main/scala/gs/nick/ApiHandler.scala) 
file we create a class that extends TodosHandler. 


This is where the payoff of Guardrail comes into view. For every operationId we defined in our swagger file we now have a method to define. This method can only return responses that were defined in the swagger file for those routes. This means that should our business logic ever require us to return a new response code we must first define it in the swagger file and then implement it in the controller. There can never be a time that our Controller exposes more than what the swagger file defines.


# Bootstraping the Akka-Http server
With a TodoController defined, we need an AkkaHttp server to run it. In 
[our bootstrap](https://github.com/nickfun/learning-guardrail/blob/master/src/main/scala/gs/nick/App.scala) 
file we create the minimum needed for AkkaHttp to run. Notice that while Guardrail auto-generates the routes, it is up to us to pass them to the server. This lets us hook into how the are used and if needed add any custom logic that isn’t appropriate for a public swagger spec. Admin or Debug routes could be added. In our repo, we add some logic to enable CORS pre-flight requests to pass.


# Unit Tests
The Guardrail system gives us power to construct correct HTTP Servers, but it is still up to the developer to ensure valid business logic. Guardrail keeps total compatibility with standard AkkaHttp testing systems. 
[Our unit tests](https://github.com/nickfun/learning-guardrail/blob/master/src/test/scala/samples/scalatest.scala)
use the ScalaRouteTest to easily send and test HTTP requests to our controller. 

Review of Advantages:

* Guaranteed to keep the server implementation and swagger file in sync.
* No lock-in to anything outside the normal Akka ecosystem
* Have hooks to control how the auto-generated hooks are used
