# Toguru (トグル)

The toggle guru (Japanese for Toggle).

[![Build Status](https://travis-ci.org/AutoScout24/toguru.svg?branch=master)](https://travis-ci.org/AutoScout24/toguru)


## Simplified akka persistence setup

We decided to use a simplified Akka persistence setup in which we rely on the
data storage for high availability and failover; by this, we avoid having to set
up an Akka cluster. At the same time, this means that in order to prevent
concurrent and conflicting persistent actor states between multiple servers, we
have to bring up a persistent actor in each request, replay its history, and
stop the actor after the request has been processed. The request processing flow
that is entailed by this decision is detailed in the following section.

## Processing flow for mutating http requests

![Toguru Request Flow](https://cloud.githubusercontent.com/assets/6724788/18165628/99d02770-7046-11e6-854f-57fef3071016.png)


Http requests that modify persistent data (i.e. POST and PUT) are processed in
the following way:

1. *request:* a controller action is invoked with a request to process.
2. *create:* the persistent actor that is needed to process the request is
created - as usual, indirectly through the Akka actor system.
3. *recover:* On actor initialization, Akka persistence replays all events from
the database to the actor, and the actor recovers its internal state based on
the replayed events.
4. *ask:* the controller asks the actor (asynchronously) to execute a command,
and the actor validates that the command can be executed.
5. *persist:* After successful validation, the actor persists derived events to
the journal. A custom event adapter (Tagging in the picture) tags the events for
later persistent queries, and a custom ProtoBuf serializer serializes the events.
6. *recover:* Akka invokes the receiveRecover method to replay the successfully
persisted events to the actor.
7. *update:* The actor updates its internal state based on the newly persisted
events.
8. *reply:* A reply is sent to the controller that describes the result of the
command.
9. *stop:* The controller stops the actor, since it has completed its task.
10. *result:* A http result is send to the client based on the reply from the
persistent actor.

The processing of GET requests is similar; since it does not alter the database
and the actor state, the steps 6. (recover) and 7. (update) can be omitted in
the request flow.

## License

See the [LICENSE](LICENSE.md) file for license rights and limitations (MIT).
