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
>> MODBUS_SLAVE<CR><LF>
>> 127.0.0.1<SP>5000<CR><LF>
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

## ModBus TCP (Master)

### Initialisation of master

```text

```