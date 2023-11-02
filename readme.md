# I. Introduction

This document is meant as a quick starting guide for using the protocol-proxy software present in this
git repository. This software aims to provide an easy, text-based way for programmable-logic-controllers
to interact with more complicated protocols. This is done by abstracting away all the painful parts, leaving
a protocol which is easy to implement and debug. Which in turn will also save storage on the logic controllers,
since protocol-specific code can be completely left to the proxy.

# II. How to read this document

In the communication examples invisible characters will be written down
using a human-readable sequence of characters, see the table below which
sequences mean which character.

| Sequence | Character       | ASCII (HEX) |
|----------|-----------------|-------------|
| \<CR\>   | Carriage return | 0x0D        |
| \<LF\>   | Line feed       | 0x0A        |
| \<SP\>   | Space           | 0x20        |

In communication examples placeholders are used to indicate variable parts
of the communication, these placeholders can be replaced with application-specific
values. Each placeholder has an associated type, that indicates the type of value
that is expected. A placeholder is written in the format `[NAME, DATATYPE]`.
If a placeholder such as `[...]` occurs, then it means that the pattern will be
continued.

In the communication examples `>>` Indicates client-to-server and `<<`
indicates server-to-client communication. The `<<` 
or `>>` and the trailing whitespace is purely for readability, 
this should be omitted in the actual communication.

# 1. ModBus TCP

Disclaimer: The author of this documentation lacks any practical experience
with the ModBus protocol. All of this documentation is written using his findings
during the development of the proxy. All the protocol related things are correct,
and modbus can be used fully, however take some examples with a grain of salt 
(semantically, not syntactically).

All communications which involve arbitrarily-sized responses have extra information
added to them which is not present in the actual ModBus protocol. This extra
information is the number of bytes which will be written in the response, for example
the number of bytes which will be written to respond to a read registers request.
This is needed because some clients/masters might use 16/32-bit registers. The quantity
reflects the number of registers, hence it is up to the slave/ master to decide what
the quantity represents, either words (32-bit) or half-words (16-bit). Depending on that
the number of bytes in the response need to be either multiplied by two or four.

To simplify client implementations, concepts like packing coils into arrays of bytes 
are abstracted away by the proxy, instead, the slave/ master will receive a line of
ASCII ones or zero's separated by spaces.

## 1.1. ModBus TCP (Slave)

### 1.1.1. Initialisation of slave

To initialize a modbus slave a connection with the proxy has to be established,
after which the client should write the following lines. These lines tell the 
proxy that we want to become a modbus slave, and that we want to listen on the
a specific hostname and port.

```text
>> MODBUS_SLAVE<CR><LF>
>> [Hostname, IPv4]<SP>[Port, Short]<CR><LF>
```

An example would be the following.

```text
>> MODBUS_SLAVE
>> 127.0.0.1 5000
```

The IPv4 address usually will be `127.0.0.1` unless the machine on which the 
proxy is running has multiple networking interfaces.

### 1.1.2. Single coil write

When the slave receives a single coil write request, it sends the following
information to the client connected to the proxy.

```text
<< WRITE_SINGLE_COIL<CR><LF>
<< [Address, Int]<SP>[Value, 1 | 0]<CR><LF>
```

The client can then decide what to do with this write request, and is expected
to send the following information back once the request has been processed.
This should echo the request if it was processed successfully.

```text
>> [Address, Int]<SP>[Value, 1 | 0]<CR><LF>
```

The following example shows a request being successfully handled.

```text
<< WRITE_SINGLE_COIL
<< 69 1
>> 69 1
```

On the contrary, a request which failed could look like this.
This could indicate that the coil is read-only.

```text
<< WRITE_SINGLE_COIL
<< 69 1
>> 69 0
```

### 1.1.3. Multiple coil write

When the slave receives a write multiple coils request it will forward it
to the client connected to the proxy in the following format.

```text
<< WRITE_MULTIPLE_COILS<CR><LF>
<< [Address, Int]<CR><LF>
<< [First bit, 1 | 0]<SP>[Second bit, 1 | 0]<SP>[...]<CR><LF>
```

The client connected to the proxy should respond with a line in the following
format. Here the `Quantity` placeholder might seem weird, however this is a
thing in the ModBus protocol, the coil write request also has a quantity, however
this one is not explicitly sent to the client, because it can be derived by
counting the number of sent bits.

```text
>> [Address, Int]<SP>[Quantity, Int]<CR><LF>
```

A successful communication might look like this.

```text
<< WRITE_MULTIPLE_COILS
<< 69
<< 1 0 0 1 0 0
>> 69 6
```

And a unsuccessful communication might look like this.

```text
<< WRITE_MULTIPLE_COILS
<< 69
<< 1 0 0 1 0 0
>> 69 4
```

This could indicate that not all coils could be written to.


### 1.1.4. Read coils

When the proxy receives a read coils request it forwards the request
to the client connected to the proxy in the following format.

```text
<< READ_COILS<CR><LF>
<< [Address, Int]<SP>[Quantity, Int]<CR><LF>
```

The client is then expected to respond in the following format (which does not include the
address and quantity for some reason, seems like an inconsistency in the ModBus protocol).

```text
>> [First bit, 1 | 0]<SP>[Second bit, 1 | 0]<SP>[...]<CR><LF>
```

An example of a successful communication would be as follows.

```text
<< READ_COILS
<< 69, 4
>> 1 0 1 1
```

Based on the response format in the ModBus protocol, the assumption made is that there cannot be a failure response,
this is probably due to the fact that it does not include any writing.

### 1.1.5. Mask write register

When the proxy receives a mask write register command, it will forward it to the client connected to the proxy
in the following format.

```text
<< MASK_WRITE_REGISTER<CR><LF>
<< [Address, Int]<SP>[OrMask, Int]<SP>[AndMask, Int]<CR><LF>
```

The client is then expected to respond to this request in the following format.

```text
>> [Address, Int]<SP>[OrMask, Int]<SP>[AndMask, Int]<CR><LF>
```

Which is basically an echo of the request in the case of success, see the example bellow
which represents a successful communication for a mask write register request.

```text
<< MASK_WRITE_REGISTER
<< 69 1 7
>> 69 1 7
```

If the request could not be handled successfully, the client response could look like the
following example.

```text
<< MASK_WRITE_REGISTER
<< 69 1 7
>> 69 1 0
```

Which could indicate that the and mask could not completely be used (I assume).

### 1.1.6. Read discrete inputs

When the proxy-server receives a request for reading discrete inputs, it will be forwarded to the
proxy-client using the following format. 

```text
<< READ_DISCRETE_INPUTS<CR><LF>
<< [Address, Int]<SP>[Quantity, Int><CR><LF>
```

To which the client is expected to respond using the following format.

```text
>> [First bit, 1 | 0]<SP>[Second bit, 1 | 0]<SP>[...]<CR><LF>
```

Due to the fact that this is _exactly_ the same as the request for reading coils, examples
will be omitted, the reader should look into subsection 1.1.4 for examples.

### 1.1.7. Read holding registers

When the proxy-server receives a request for reading a holding register, it will be forwarded to
the proxy-client in the following format.

```text
<< READ_HOLDING_REGISTERS<CR><LF>
<< [Address, Int]<SP>[Quantity, Int><CR><LF>
```

To which the client is expected to respond in the following format.

```text
>> [NumberOfBytes, Int]<CR><LF>
>> [Bytes, Byte[] of length NumberOfBytes]
```

In the regular ModBus protocol the number of bytes does not have to be specific, here it has been
to make  the proxy more flexible. Due to the fact that some ModBus implementations use 32-bit registers
instead of 16-bit registers, the decision was made to let the proxy-client decide the size of the response
based on the `Quantity` placeholder. So that the proxy can be used across different implementations,
using different register sizes.

An example of a successful communication would look like (assuming a register size of 16-bits).

```text
<< READ_HOLDING_REGISTERS
<< 69, 23
>> 46
>> [Byte[] with length of 46]
```

Since it's a read operation, there should not be any failure response, hence an example of a failure
is omitted.

### 1.1.8. Read input registers

The read input registers request/ response has exactly the same format as the read holding registers response,
with the only difference being the type of request, which changes from `READ_HOLDING_REGISTERS` to
`READ_INPUT_REGISTERS`. Due to the similarity of the format, the reader should go to section 1.1.7
to see the specific format of this request.

### 1.1.9. Read write multiple registers

When the proxy-server receives a request to read/ write multiple registers, it is forwarded
to the proxy-client using the following format.

```text
<< WRITE_MULTIPLE_REGISTERS<CR><LF>
<< [ReadAddress, Int]<SP>[ReadQuantity, Int]<SP>[WriteAddress, Int]<SP>[WriteQuantity, Int]<SP>[NoBytesForWriting, Int]<CR><LF>
<< [WriteBytes, Byte[] of size NoBytesForWriting]
```

After the proxy-client has processed this request, it is expected to respond using the following format as specified
for the response in subsection 1.1.7, hence the reader should reference that subsection to find out how to respond
to this request. Here the bytes will be the read bytes.

### 1.1.10. Write multiple registers

When the proxy-server receives a request to write multiple registers, it is forwarded to the
proxy-client using the following format.

```text
<< WRITE_MULTIPLE_REGISTERS<CR><LF>
<< [Address, Int]<SP>[Quantity, Int]<SP>[NoBytes, Int]<CR><LF>
<< [Values, Byte[] of size NoBytes]
```

Again, the `NoBytes` placeholder is added to ensure that the proxy can be used with systems
that have different register sizes, as explained in subsection 1.1.7.

When the client has processed the request, it is expected to respond in the following format.

```text
>> [Address, Int]<SP>[Quantity, Int]<CR><LF>
```

Examples are left out because previous examples should have provided enough insights on how
the protocol can be used in practice, based on the formats given.

### 1.1.11. Write single register

When the proxy-server receives a request to write to a single register, it is forwarded to the
proxy-client using the following format.

```text
<< WRITE_SINGLE_REGISTER<CR><LF>
<< [Address, Int]<SP>[Value, Int]<CR><LF>
```

To which the client is expected to respond in the following format.

```text
>> [Address, Int]<SP>[Value, Int]<CR><LF>
```

Where the `Address` placeholder should echo the previously given address, and the `Value` placeholder
should contain the new value, or the old value when the register is read-only/ could not be written to.

## 1.2. ModBus TCP (Master)

### 1.2.1. Initialisation of master

To initialize the ModBus TCP master the client is expected to send the following lines after
initializing the connection to the proxy. Here the `Address` placeholder must contain the IPv4
address of the ModBus TCP Slave the Master should connect to. The `Port` indicates the port on
the Slave on which the Modbus TCP Slave is listening. After sending this, the proxy will initiate
the connection and wait for commands from the proxy client.

```text
>> MODBUS_MASTER<CR><LF>
>> [Address, IPv4]<SP>[Port, Short]<CR><LF>
```

The commands that the ModBus TCP Master can send to the Slave through the proxy follow the same
formatting as of those in the documentation for the ModBus TCP Slave, expect the roles being reversed.
Hence, no further explanation about the master will take place.

# 2. MQTT

## 2.1. MQTT (Client)

### 2.1.1. Creating a client

To create a new MQTT-Client using the Proxy, the proxy client is expected to send the following lines
to the proxy. These lines tell the proxy which MQTT-Broker to connect to, which client information/ credentials
should be used when connecting, and which topics should be subscribed to. The `Username` and `Password` placeholders
can be omitted. When these are not present the client will not perform authentication.

```text
>> MQTT_CLIENT<CR><LF>
>> [Address, IPv4]<SP>[Port, Short]<SP>[ClientID, String]<SP>[Username, String]<SP>[Password, String]<CR><LF>
>> [Topic1, String]<CR><LF>[Topic2, String]<CR><LF>[...]<CR><LF><CR><LF>
```

After sending this, the client will connect to the broker and forward all the messages from the topics specified
during the initialization, the client can also publish to topics. Both are documented bellow. An example of an
initialization in the above specified format, follows.

```text
>> MQTT_CLIENT
>> 192.168.2.68 8341 MotionEngine BrokerUsername BrokerPassword
>> /sensors/accelerometer/raw
>> /sensors/accelerometer/derived-orientation
>> /sensors/velocity/raw
>> /motors/%/update
>>
```

### 2.1.2. Receiving messages

After the initialization is done, the proxy-client will start receiving messages from all subscribed to topics,
each of these messages will be forwarded by first sending the topic, followed by the message header, as seen below.

```text
<< [Topic, String]<CR><LF>
<< [QOS, Int]<SP>[BodySize, Int]<CR><LF>
```

The `BodySize` placeholder indicates the number of bytes which will follow after the message header. Since MQTT messages
aren't necessarily plain-text, the body of the messages will be sent as a BLOB, of which the size is in the
`BodySize` placeholder. So the proxy-client is expected to read exactly the number of bytes specified in the `BodySize`
placeholder, and to start regular reading of lines again after. See the example below (which shows the headers of two
messages and the body of one, showing how they're put immediately after each other, without newlines).

```text
<< /sensors/accelerometer/derived-orientation
<< 0 42
<< [Byte[] of with length 42]/sensors/accelerometer/raw
<< 0 20
```

So again, the message topic and header are specified on lines, but the body of the message is just a chunk of possibly
binary data, that should be read as bytes, and not text. Proxy-clients should not assume that these simply will be
pieces of text, because that's not the case. The format used here is similar to the one used in the SMTP Chunking Extension,
which possibly provides more clarity. Doing it this way prevents clients from having to decode crappy and inefficient formats such
as Base16, Base64 etc.

### 2.1.3. Sending messages

Sending messages is the same as receiving messages, however the roles are reversed. So in order to send a message,
just use the explanation above, and reverse the server/ client roles.