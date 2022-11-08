# Learning Guardrail

An example project while I learn how [Guardrail](https://github.com/guardrail-dev/guardrail) works.

Builds a Akka-Http server that can implements a "todo backend", see https://www.todobackend.com

Latest version is usually running here: https://todo-backend-guardrail.herokuapp.com

## Operating

Environtment Variables

| Name           | Description                                                              | default value | example value |
| --             | --                                                                       | --            | --            |
| `DOMAIN`       | the domain the server is publicly available on                           | localhost     | yahoo.com     |
| `PORT`         | the port to bind to                                                      | 8080          | 8080          |
| `ENABLE_HTTPS` | flag that public URLs use Https. set to "on" or anyting else to disable. | off           | on            |
