# Conversations
[![Build Status](https://travis-ci.org/Kjens93/conversations-java.svg?branch=master)](https://travis-ci.org/Kjens93/conversations-java)
[![Coverage Status](https://coveralls.io/repos/github/Kjens93/conversations-java/badge.svg?branch=master)](https://coveralls.io/github/Kjens93/conversations-java?branch=master)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kjens93.conversations/conversations.svg?maxAge=60)](https://maven-badges.herokuapp.com/maven-central/io.github.kjens93.conversations/conversations)

Awesome functional interfaces for Java.

## Installation
```xml
<dependency>
    <groupId>io.github.kjens93.conversations</groupId>
    <artifactId>conversations</artifactId>
    <version>LATEST</version>
</dependency>
```

## Usage
```java
class Example {
    
    public void main() {
        
        Conversations.registerResponder(Message.class, (actions, initialMessage) -> {
            actions.send(new Message(), initialMessage.getRemoteEndpoint());
            actions.receiveOne()
                   .ofType(Message.class)
                   .fromSender(initialMessage.getRemoteEndpoint())
                   .await(1, TimeUnit.SECONDS);
            actions.send(new Message(), initialMessage.getRemoteEndpoint());
            actions.receiveViaTCP(ArrayList.class, initialMessage.getRemoteEndpoint());
        });
        
        Conversations.newConversation((actions) -> {
            Endpoint remote = new Endpoint("127.0.0.1", 12345);
            actions.reliableSend(new Message(), remote, Message.class);
            actions.openTCPConnection(remote)
                   .andThen((conn) -> {
                        conn.writeUTF("Hello world!")
                            .writeUTF("This is totally wicked!")
                            .flush();
                   }).await();
            actions.sendViaTCP(new ArrayList<String>(), remote).await();
        }).await();
        
    }
    
}
```